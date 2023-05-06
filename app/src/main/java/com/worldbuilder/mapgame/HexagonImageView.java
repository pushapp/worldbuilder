package com.worldbuilder.mapgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import androidx.appcompat.widget.AppCompatImageView;

public class HexagonImageView extends AppCompatImageView {
    private Path hexagonPath;
    private Paint hexagonPaint;

    public HexagonImageView(Context context) {
        super(context);
        init();
    }

    private void init() {
        hexagonPath = new Path();
        hexagonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hexagonPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the hexagon background
        hexagonPath.reset();
        PointF[] hexagonPoints = calculateHexagonPoints(getWidth(), getHeight());
        hexagonPath.moveTo(hexagonPoints[0].x, hexagonPoints[0].y);
        for (int i = 1; i < hexagonPoints.length; i++) {
            hexagonPath.lineTo(hexagonPoints[i].x, hexagonPoints[i].y);
        }
        hexagonPath.close();
        canvas.drawPath(hexagonPath, hexagonPaint);

        // Draw the image
        canvas.clipPath(hexagonPath);
        super.onDraw(canvas);
    }

    public void setHexagonColor(int color) {
        hexagonPaint.setColor(color);
        invalidate();
    }


    private PointF[] calculateHexagonPoints(int width, int height) {
        PointF[] points = new PointF[6];
        float centerX = width / 2f;
        float centerY = height / 2f;
        float radius = Math.min(centerX, centerY);

        for (int i = 0; i < 6; i++) {
            float angleDegrees = 60 * i;
            float angleRadians = (float) Math.toRadians(angleDegrees);
            float x = centerX + radius * (float) Math.cos(angleRadians);
            float y = centerY + radius * (float) Math.sin(angleRadians);
            points[i] = new PointF(x, y);
        }

        return points;
    }
}