package com.example.madfinalproject.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ScoreRingView extends View {

    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private RectF rectF;
    private int score = 85; // Aap isay baad mein change kar sakte hain

    public ScoreRingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Background ring (Grey)
        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(12f);
        backgroundPaint.setColor(Color.parseColor("#334155")); // Dark grey

        // Progress ring (Green)
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStrokeWidth(12f);
        progressPaint.setColor(Color.parseColor("#10B981")); // Emerald Green

        // Text in the center
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        rectF = new RectF();
    }

    public void setScore(int score) {
        this.score = score;
        invalidate(); // Redraw the view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int padding = 16;
        rectF.set(padding, padding, getWidth() - padding, getHeight() - padding);

        // Draw background ring (full circle)
        canvas.drawArc(rectF, 0, 360, false, backgroundPaint);

        // Draw progress ring based on score
        float angle = (score * 360f) / 100f;
        canvas.drawArc(rectF, -90, angle, false, progressPaint);

        // Draw text in the middle
        float xPos = (getWidth() / 2f);
        float yPos = (int) ((getHeight() / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f));
        canvas.drawText(String.valueOf(score), xPos, yPos, textPaint);
    }

    public void setScoreColor(int scoreColor) {
    }
}