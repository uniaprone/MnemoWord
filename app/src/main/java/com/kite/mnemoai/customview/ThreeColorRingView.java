package com.kite.mnemoai.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.kite.mnemoai.R;

public class ThreeColorRingView extends View {

    // 三种状态的颜色（默认值）
    private int toLearnColor;
    private int reviewingColor;
    private int masteredColor;
    private int trackColor;

    private int toLearnCount;
    private int reviewingCount;
    private int masteredCount;
    private int totalCount;

    private Paint segmentPaint;
    private Paint trackPaint;
    private RectF rectF;

    // 环的粗细（dp）
    private float strokeWidth = 8f;

    public ThreeColorRingView(Context context) {
        this(context, null);
    }

    public ThreeColorRingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThreeColorRingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaints(context, attrs);
    }

    private void initPaints(Context context, AttributeSet attrs) {
        // 读取自定义属性（如果有）
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ThreeColorRingView);
            try{
                toLearnColor = ta.getColor(R.styleable.ThreeColorRingView_ringToLearnColor, 0xFFE53935); // 红色
                reviewingColor = ta.getColor(R.styleable.ThreeColorRingView_ringReviewingColor, 0xFFFB8C00); // 橙色
                masteredColor = ta.getColor(R.styleable.ThreeColorRingView_ringMasteredColor, 0xFF43A047); // 绿色
                trackColor = ta.getColor(R.styleable.ThreeColorRingView_ringTrackColor, 0xFFE0E0E0); // 浅灰背景
                strokeWidth = ta.getDimension(R.styleable.ThreeColorRingView_ringStrokeWidth, dpToPx(8f));
            }finally {
                ta.recycle();
            }
        } else {
            // 默认颜色
            toLearnColor = 0xFFE53935;
            reviewingColor = 0xFFFB8C00;
            masteredColor = 0xFF43A047;
            trackColor = 0xFFE0E0E0;
        }

        segmentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        segmentPaint.setStyle(Paint.Style.STROKE);
        segmentPaint.setStrokeWidth(strokeWidth);
        segmentPaint.setStrokeCap(Paint.Cap.ROUND);

        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidth);
        trackPaint.setColor(trackColor);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);

        rectF = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float halfStroke = strokeWidth / 2f;
        rectF.set(
                halfStroke,
                halfStroke,
                w - halfStroke,
                h - halfStroke
        );
    }

    /**
     * 更新数据，传入三个数量
     * @param toLearn 待学习数量
     * @param reviewing 复习中数量
     * @param mastered 已掌握数量
     */
    public void setProgress(int toLearn, int reviewing, int mastered) {
        this.toLearnCount = Math.max(0, toLearn);
        this.reviewingCount = Math.max(0, reviewing);
        this.masteredCount = Math.max(0, mastered);
        this.totalCount = this.toLearnCount + this.reviewingCount + this.masteredCount;
        invalidate(); // 触发重绘
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (totalCount <= 0) {
            // 没有数据时，画一个完整的灰色圆环
            canvas.drawArc(rectF, 0, 360, false, trackPaint);
            return;
        }

        // 计算三个扇区的角度（360度）
        float toLearnSweep = (toLearnCount / (float) totalCount) * 360;
        float reviewingSweep = (reviewingCount / (float) totalCount) * 360;
        float masteredSweep = (masteredCount / (float) totalCount) * 360;

        float startAngle = -90f; // 从12点钟方向开始

        // 1. 画待学习（红色）
        if (toLearnSweep > 0) {
            segmentPaint.setColor(toLearnColor);
            canvas.drawArc(rectF, startAngle, toLearnSweep, false, segmentPaint);
            startAngle += toLearnSweep;
        }

        // 2. 画复习中（橙色）
        if (reviewingSweep > 0) {
            segmentPaint.setColor(reviewingColor);
            canvas.drawArc(rectF, startAngle, reviewingSweep, false, segmentPaint);
            startAngle += reviewingSweep;
        }

        // 3. 画已掌握（绿色）
        if (masteredSweep > 0) {
            segmentPaint.setColor(masteredColor);
            canvas.drawArc(rectF, startAngle, masteredSweep, false, segmentPaint);
            // 剩余的角度自动留空（显示背景色）
        }

        // 画剩余背景（浅灰色）
        if (startAngle < 270f) { // 360 - 90 = 270
            float remaining = 360 - (toLearnSweep + reviewingSweep + masteredSweep);
            if (remaining > 0) {
                trackPaint.setColor(trackColor);
                canvas.drawArc(rectF, startAngle, remaining, false, trackPaint);
            }
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
