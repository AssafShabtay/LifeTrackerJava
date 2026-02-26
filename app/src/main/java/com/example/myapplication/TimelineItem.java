package com.example.myapplication;



import com.example.myapplication.database.MovementActivity;
import com.example.myapplication.database.StillLocation;

import java.util.Date;

public abstract class TimelineItem {

    public abstract Date getStartTimeDate();
    public abstract Date getEndTimeDate();

    public static class Still extends TimelineItem {
        private final StillLocation item;
        public Still(StillLocation item) { this.item = item; }
        public StillLocation getItem() { return item; }

        @Override public Date getStartTimeDate() { return item.getStartTimeDate(); }
        @Override public Date getEndTimeDate() { return item.getEndTimeDate(); }
    }

    public static class Movement extends TimelineItem {
        private final MovementActivity item;
        public Movement(MovementActivity item) { this.item = item; }
        public MovementActivity getItem() { return item; }

        @Override public Date getStartTimeDate() { return item.getStartTimeDate(); }
        @Override public Date getEndTimeDate() { return item.getEndTimeDate(); }
    }

    public static class Remaining extends TimelineItem {
        private final Date startTimeDate;
        private final Date endTimeDate;

        public Remaining(Date startTimeDate, Date endTimeDate) {
            this.startTimeDate = startTimeDate;
            this.endTimeDate = endTimeDate;
        }

        @Override public Date getStartTimeDate() { return startTimeDate; }
        @Override public Date getEndTimeDate() { return endTimeDate; }
    }
}