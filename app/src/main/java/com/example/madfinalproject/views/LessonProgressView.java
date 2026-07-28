package com.example.madfinalproject.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

public class LessonProgressView extends View {
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF ringBounds = new RectF();
    private int displayedProgress;
    private int targetProgress;
    private int startColor = Color.parseColor("#7C3AED");
    private int endColor = Color.parseColor("#22C55E");
    private ValueAnimator animator;

    public LessonProgressView(Context context) {
        this(context, null);
    }

    public LessonProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LessonProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float density = getResources().getDisplayMetrics().density;

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(3f * density);
        trackPaint.setColor(Color.parseColor("#E5EAF2"));

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStrokeWidth(3f * density);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(10f * getResources().getDisplayMetrics().scaledDensity);
    }

    public void setAccentColors(int start, int end) {
        startColor = start;
        endColor = end;
        invalidate();
    }

    public void setProgress(int progress, boolean animate) {
        targetProgress = Math.max(0, Math.min(100, progress));
        if (animator != null) animator.cancel();
        if (!animate || !isLaidOut()) {
            displayedProgress = targetProgress;
            invalidate();
            return;
        }
        animator = ValueAnimator.ofInt(displayedProgress, targetProgress);
        animator.setDuration(750);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(valueAnimator -> {
            displayedProgress = (int) valueAnimator.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float stroke = progressPaint.getStrokeWidth();
        float inset = stroke / 2f + 2f;
        ringBounds.set(inset, inset, getWidth() - inset, getHeight() - inset);
        canvas.drawArc(ringBounds, 0f, 360f, false, trackPaint);

        if (displayedProgress > 0) {
            float centerX = getWidth() / 2f;
            float centerY = getHeight() / 2f;
            progressPaint.setShader(new SweepGradient(
                    centerX,
                    centerY,
                    new int[]{startColor, endColor, startColor},
                    new float[]{0f, 0.72f, 1f}
            ));
            canvas.drawArc(ringBounds, -90f, displayedProgress * 3.6f, false, progressPaint);
            progressPaint.setShader(null);
        }

        textPaint.setColor(displayedProgress == 0
                ? Color.parseColor("#8792A7")
                : startColor);
        float baseline = getHeight() / 2f
                - (textPaint.ascent() + textPaint.descent()) / 2f;
        canvas.drawText(displayedProgress + "%", getWidth() / 2f, baseline, textPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) animator.cancel();
        super.onDetachedFromWindow();
    }
}
