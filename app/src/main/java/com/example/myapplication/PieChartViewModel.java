package com.example.myapplication;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import com.example.myapplication.PieChartHelpers;
import com.example.myapplication.TimelineItem;
import com.example.myapplication.database.ActivityDao;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PieChartViewModel extends ViewModel {

    private final ActivityDao dao;
    private final ExecutorService executor = Executors.newFixedThreadPool(4); // Replaces viewModelScope

    // State is held in LiveData for the View to observe
    private final MutableLiveData<Date> selectedDate = new MutableLiveData<>(new Date());
    private final MutableLiveData<List<TimelineItem>> dayTimeline = new MutableLiveData<>();
    private final MutableLiveData<Map<Integer, List<TimelineItem>>> monthData = new MutableLiveData<>(new HashMap<>());

    public PieChartViewModel(ActivityDao dao) {
        this.dao = dao;
        loadDataForDay(selectedDate.getValue());
    }

    // Getters for observing the LiveData
    public LiveData<Date> getSelectedDate() { return selectedDate; }
    public LiveData<List<TimelineItem>> getDayTimeline() { return dayTimeline; }
    public LiveData<Map<Integer, List<TimelineItem>>> getMonthData() { return monthData; }

    public void loadDataForDay(Date date) {
        selectedDate.setValue(date); // Main thread safe

        executor.execute(() -> {
            Date[] bounds = PieChartHelpers.getDayRange(date);
            // Synchronous DAO call safely done on background thread
            List<TimelineItem> timeline = PieChartHelpers.getTimelineForRange(dao, bounds[0], bounds[1]);

            // postValue pushes the result back to the Main thread
            dayTimeline.postValue(timeline);
        });
    }

    public void loadDataForLastMonth(int month, int year) {
        executor.execute(() -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, 1);
            int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            Map<Integer, List<TimelineItem>> currentMonthMap = new HashMap<>();

            for (int day = 1; day <= days; day++) {
                final int currentDay = day;

                // You can submit individual days to the thread pool for parallel loading
                executor.execute(() -> {
                    Calendar dayCal = Calendar.getInstance();
                    dayCal.set(year, month, currentDay);

                    Date[] bounds = getBounds(dayCal.getTime());
                    List<TimelineItem> timeline = PieChartHelpers.getTimelineForRange(dao, bounds[0], bounds[1]);

                    synchronized (currentMonthMap) {
                        currentMonthMap.put(currentDay, timeline);
                        // Post intermediate updates to mimic your incremental Compose behavior
                        monthData.postValue(new HashMap<>(currentMonthMap));
                    }
                });
            }
        });
    }

    private Date[] getBounds(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date start = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        return new Date[]{start, cal.getTime()};
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown(); // Clean up threads when ViewModel dies
    }
}