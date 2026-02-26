package com.example.myapplication.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.List;

public class MiniPieChartView extends View {
    private Paint stillPaint;
    private Paint movementPaint;
    private Paint remainingPaint;
    private RectF rectF;

    // We'll pass standard floats instead of complex TimelineItems to keep the View decoupled
    private List<Float> durations;
    private List<Integer> types; // 0 = Still, 1 = Movement, 2 = Remaining

    public MiniPieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        stillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stillPaint.setColor(Color.GRAY);

        movementPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        movementPaint.setColor(Color.parseColor("#4CAF50")); // Green

        remainingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        remainingPaint.setColor(Color.parseColor("#E0E0E0")); // Light Gray

        rectF = new RectF();
    }

    public void setData(List<Float> durations, List<Integer> types) {
        this.durations = durations;
        this.types = types;
        invalidate(); // Tells Android to redraw the view
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Define the bounds for the pie chart based on the view size
        rectF.set(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (durations == null || durations.isEmpty()) return;

        float totalDuration = 0;
        for (float d : durations) totalDuration += d;

        if (totalDuration == 0) return;

        float startAngle = -90f; // Start at the top

        for (int i = 0; i < durations.size(); i++) {
            float sweepAngle = (durations.get(i) / totalDuration) * 360f;
            Paint currentPaint;

            int type = types.get(i);
            if (type == 0) currentPaint = stillPaint;
            else if (type == 1) currentPaint = movementPaint;
            else currentPaint = remainingPaint;

            canvas.drawArc(rectF, startAngle, sweepAngle, true, currentPaint);
            startAngle += sweepAngle;
        }
    }
}