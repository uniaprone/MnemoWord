package com.kite.mnemoai.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.utils.Transformer;
import com.google.android.material.color.MaterialColors;

public class BgCombineChart extends CombinedChart {
    private Paint bgPaint;

    public BgCombineChart(Context context) {
        super(context);
        bgBarChartInit();
    }

    public BgCombineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        bgBarChartInit();
    }

    public BgCombineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        bgBarChartInit();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawColumnBackground(canvas);
        super.onDraw(canvas);
    }
    private void bgBarChartInit(){
        bgPaint = new Paint();
        bgPaint.setColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurfaceContainerLowest));
    }

    private void drawColumnBackground(Canvas canvas){
        int save = canvas.save();

        canvas.clipRect(
                mViewPortHandler.contentLeft(),
                mViewPortHandler.contentTop(),
                mViewPortHandler.contentRight(),
                mViewPortHandler.contentBottom()
        );

        Transformer transformer = getTransformer(YAxis.AxisDependency.LEFT);
        float[] pts = new float[2];

        XAxis xAxis = getXAxis();
        int count = (int) xAxis.getAxisMaximum();
        for(int i = 0; i < count; i++){
            if(i % 2 == 0) continue;
            float rightX = i + 1;

            pts[0] = (float) i;
            pts[1] = 0;
            transformer.pointValuesToPixel(pts);
            float leftPixel = pts[0];

            pts[0] = rightX;
            transformer.pointValuesToPixel(pts);
            float rightPixel = pts[0];

            canvas.drawRect(leftPixel, mViewPortHandler.contentTop(), rightPixel, mViewPortHandler.contentBottom(), bgPaint);
        }

        canvas.restoreToCount(save);
    }
}
