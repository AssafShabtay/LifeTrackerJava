package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import com.example.myapplication.views.CalendarCell;
import com.example.myapplication.views.MiniPieChartView;

import java.util.ArrayList;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private List<CalendarCell> cells = new ArrayList<>();
    private final OnDateSelectedListener listener;

    // Interface to pass clicks back to the Activity or Fragment
    public interface OnDateSelectedListener {
        void onDateSelected(int day);
    }

    public CalendarAdapter(OnDateSelectedListener listener) {
        this.listener = listener;
    }

    public void submitList(List<CalendarCell> newCells) {
        this.cells = newCells;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_calendar_day_item, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        CalendarCell cell = cells.get(position);
        Context context = holder.itemView.getContext();

        // Handle empty padding days (represented by day == 0)
        if (cell.getDay() == 0) {
            holder.tvDayNumber.setText("");
            holder.miniPieChart.setVisibility(View.INVISIBLE);
            holder.itemView.setOnClickListener(null);
            holder.itemView.setClickable(false);
            return;
        }

        // Setup valid day
        holder.tvDayNumber.setText(String.valueOf(cell.getDay()));
        holder.miniPieChart.setVisibility(View.VISIBLE);

        // Visual States (Colors and Typography)
        if (cell.isFuture()) {
            holder.tvDayNumber.setTextColor(Color.LTGRAY); // Dimmed for future
            holder.tvDayNumber.setTypeface(null, Typeface.NORMAL);
            holder.itemView.setAlpha(0.5f);

            // Disable clicks for future dates
            holder.itemView.setOnClickListener(null);
            holder.itemView.setClickable(false);
        } else {
            holder.itemView.setAlpha(1.0f);

            // Enable clicks for valid past/present dates
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDateSelected(cell.getDay());
                }
            });

            if (cell.isSelected()) {
                // Assuming you have a primary color in colors.xml
                holder.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.purple_500));
                holder.tvDayNumber.setTypeface(null, Typeface.BOLD);

                // You can swap the background of the pie chart container here to show the border
                // holder.miniPieChart.setBackgroundResource(R.drawable.circle_border_selected);
            } else {
                holder.tvDayNumber.setTextColor(Color.BLACK);
                holder.tvDayNumber.setTypeface(null, Typeface.NORMAL);

                // holder.miniPieChart.setBackgroundResource(R.drawable.circle_border_outline);
            }
        }

        // Bind the Mini Pie Chart data
        if (!cell.isFuture() && cell.getDurations() != null && !cell.getDurations().isEmpty()) {
            holder.miniPieChart.setData(cell.getDurations(), cell.getTypes());
        } else {
            // Pass empty lists to clear the chart for days without data
            holder.miniPieChart.setData(new ArrayList<>(), new ArrayList<>());
        }
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    // ViewHolder class
    static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayNumber;
        MiniPieChartView miniPieChart;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            miniPieChart = itemView.findViewById(R.id.miniPieChart);
        }
    }
}