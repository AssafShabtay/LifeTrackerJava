package com.example.myapplication;

import android.graphics.Color;
import androidx.core.graphics.ColorUtils;


import com.example.myapplication.Pie;
import com.example.myapplication.PieType;
import com.example.myapplication.R;

import com.example.myapplication.TimelineItem;
import com.example.myapplication.database.ActivityDao;
import com.example.myapplication.database.MovementActivity;
import com.example.myapplication.database.StillLocation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PieChartHelpers {

    private static final double TOTAL_MINUTES = 1440.0;
    private static final double FULL_CIRCLE_DEG = 360.0;
    private static final double MIN_ANGLE_DEG = 25.0;

    public static Date[] getDayRange(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date != null ? date : new Date());

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date start = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date end = cal.getTime();

        return new Date[]{start, end};
    }

    public static int totalDurationMinutes(List<TimelineItem> timeline, Date dayStart, Date dayEnd) {
        int total = 0;
        for (TimelineItem item : timeline) {
            total += durationMinutes(item.getStartTimeDate(), item.getEndTimeDate(), dayStart, dayEnd);
        }
        return total;
    }

    public static List<TimelineItem> getTimelineForRange(ActivityDao dao, Date startOfDay, Date endOfDay) {
        List<TimelineItem> timeline = new ArrayList<>();

        List<StillLocation> stills = dao.getStillForRange(startOfDay, endOfDay);
        for (StillLocation still : stills) {
            timeline.add(new TimelineItem.Still(still));
        }

        List<MovementActivity> movements = dao.getMovementForRange(startOfDay, endOfDay);
        for (MovementActivity movement : movements) {
            timeline.add(new TimelineItem.Movement(movement));
        }

        // Sort by start time
        Collections.sort(timeline, (o1, o2) -> {
            Date t1 = o1.getStartTimeDate();
            Date t2 = o2.getStartTimeDate();
            if (t1 == null) return -1;
            if (t2 == null) return 1;
            return t1.compareTo(t2);
        });

        if (totalDurationMinutes(timeline, startOfDay, endOfDay) < 1440) {
            Date lastEndTime = startOfDay;
            for (int i = timeline.size() - 1; i >= 0; i--) {
                TimelineItem item = timeline.get(i);
                if (item instanceof TimelineItem.Still || item instanceof TimelineItem.Movement) {
                    lastEndTime = item.getEndTimeDate();
                    break;
                }
            }

            if (lastEndTime == null) lastEndTime = startOfDay;
            timeline.add(new TimelineItem.Remaining(lastEndTime, endOfDay));
        }

        return timeline;
    }

    public static int durationMinutes(Date start, Date end, Date startOfDay, Date endOfDay) {
        if (start == null) return 0;
        Date actualEnd = (end != null) ? end : new Date();

        if (startOfDay != null && endOfDay != null) {
            if (start.getTime() < startOfDay.getTime() || start.getTime() > endOfDay.getTime()) {
                return (int) (Math.max(0, actualEnd.getTime() - startOfDay.getTime()) / 1000 / 60);
            }
            if (actualEnd.getTime() < startOfDay.getTime() || actualEnd.getTime() > endOfDay.getTime()) {
                return (int) (Math.max(0, endOfDay.getTime() - start.getTime()) / 1000 / 60);
            }
        }
        return (int) (Math.max(0, actualEnd.getTime() - start.getTime()) / 1000 / 60);
    }

    public static List<Pie> pieDataFromTimeline(List<TimelineItem> timeline, Date selectedDate) {
        List<Pie> rawPies = new ArrayList<>();

        for (TimelineItem item : timeline) {
            int duration = durationMinutes(item.getStartTimeDate(), item.getEndTimeDate(), null, null);

            int baseColor;
            if (item instanceof TimelineItem.Still) baseColor = Color.GRAY;
            else if (item instanceof TimelineItem.Movement) baseColor = Color.parseColor("#4CAF50");
            else baseColor = Color.parseColor("#E0E0E0");

            Integer icon = null;
            if (item instanceof TimelineItem.Still) {
                icon = android.R.drawable.ic_menu_myplaces; // Use your actual drawable resources
            } else if (item instanceof TimelineItem.Movement) {
                String type = ((TimelineItem.Movement) item).getItem().getActivityType();
                if ("Driving".equals(type)) icon = android.R.drawable.ic_menu_directions;
                // Add your other icons here mapping to R.drawable...
            }

            Double lat = null, lng = null, endLat = null, endLng = null;
            PieType pieType = PieType.Remaining;

            if (item instanceof TimelineItem.Still) {
                TimelineItem.Still still = (TimelineItem.Still) item;
                lat = still.getItem().getLat();
                lng = still.getItem().getLng();
                pieType = PieType.Still;
            } else if (item instanceof TimelineItem.Movement) {
                TimelineItem.Movement move = (TimelineItem.Movement) item;
                lat = move.getItem().getStartLat();
                lng = move.getItem().getStartLng();
                endLat = move.getItem().getEndLat();
                endLng = move.getItem().getEndLng();
                pieType = PieType.Movement;
            }

            String durationText = (item instanceof TimelineItem.Remaining) ? null : minutesToTimeStamp(duration);
            boolean clickable = !(item instanceof TimelineItem.Remaining);

            // Replaces color.copy(alpha = 0.85f)
            int selectedColor = ColorUtils.setAlphaComponent(baseColor, (int)(255 * 0.85f));

            rawPies.add(new Pie(
                    "...", duration, baseColor, lat, lng, endLat, endLng,
                    durationText, icon, pieType, selectedColor, clickable
            ));
        }

        return normalizePieByAngle(rawPies);
    }

    private static String minutesToTimeStamp(int minutes) {
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;

        if (hours == 0) return remainingMinutes + "m";
        if (remainingMinutes == 0) return hours + "h";
        return hours + "h " + remainingMinutes + "m";
    }

    private static List<Pie> normalizePieByAngle(List<Pie> raw) {
        // Translation of your normalization logic
        List<Double> rawAngles = new ArrayList<>();
        double sumRawAngles = 0;
        for (Pie pie : raw) {
            double angle = (pie.getData() / TOTAL_MINUTES) * FULL_CIRCLE_DEG;
            rawAngles.add(angle);
            sumRawAngles += angle;
        }

        double minTotal = raw.size() * MIN_ANGLE_DEG;

        if (minTotal > FULL_CIRCLE_DEG) {
            double scale = FULL_CIRCLE_DEG / sumRawAngles;
            for (int i = 0; i < raw.size(); i++) {
                raw.get(i).setData((int) (rawAngles.get(i) * scale));
            }
            return raw;
        }

        List<Double> clamped = new ArrayList<>();
        double clampedSum = 0;
        for (Double angle : rawAngles) {
            double c = Math.max(angle, MIN_ANGLE_DEG);
            clamped.add(c);
            clampedSum += c;
        }

        if (clampedSum <= FULL_CIRCLE_DEG) {
            for (int i = 0; i < raw.size(); i++) {
                raw.get(i).setData(clamped.get(i).intValue());
            }
            return raw;
        }

        double excess = clampedSum - FULL_CIRCLE_DEG;
        List<Double> adjustable = new ArrayList<>();
        double adjustableTotal = 0;
        for (Double c : clamped) {
            double a = c - MIN_ANGLE_DEG;
            adjustable.add(a);
            adjustableTotal += a;
        }

        adjustableTotal = Math.max(adjustableTotal, 1e-9);

        for (int i = 0; i < raw.size(); i++) {
            double finalAngle = clamped.get(i) - excess * (adjustable.get(i) / adjustableTotal);
            raw.get(i).setData((int) finalAngle);
        }

        return raw;
    }
}