package com.awnai.assistant;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

public class HologramFaceView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private String mode = "idle";
    private long startTime = System.currentTimeMillis();

    public HologramFaceView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setMode(String newMode) {
        mode = newMode;
        invalidate();
    }

    private void stroke(Paint p, int color, float width) {
        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(width);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float scale = Math.min(getWidth(), getHeight()) / 300f;
        float t = (System.currentTimeMillis() - startTime) / 1000f;

        canvas.save();
        canvas.translate(cx, cy);
        canvas.scale(scale, scale);

        int cyan = Color.rgb(70, 225, 255);
        int blue = Color.rgb(25, 135, 255);
        int dim = Color.argb(90, 40, 190, 255);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(dim);

        float pulse = 1f + (float)Math.sin(t * 2f) * 0.025f;
        canvas.drawCircle(0, 0, 128 * pulse, paint);
        canvas.drawCircle(0, 0, 116, paint);
        canvas.drawCircle(0, 0, 139, paint);

        for (int i = 0; i < 24; i++) {
            float angle = (float)(i * Math.PI / 12 + t * 0.25);
            float x1 = (float)Math.cos(angle) * 132;
            float y1 = (float)Math.sin(angle) * 132;
            float x2 = (float)Math.cos(angle) * 140;
            float y2 = (float)Math.sin(angle) * 140;
            canvas.drawLine(x1, y1, x2, y2, paint);
        }

        float bob = (float)Math.sin(t * 1.7f) * 2.5f;
        canvas.translate(0, bob);

        Path face = new Path();
        face.moveTo(-68, -78);
        face.quadTo(-83, -105, -48, -111);
        face.quadTo(0, -126, 48, -111);
        face.quadTo(83, -105, 68, -78);
        face.quadTo(85, -15, 58, 65);
        face.quadTo(42, 105, 0, 112);
        face.quadTo(-42, 105, -58, 65);
        face.quadTo(-85, -15, -68, -78);
        face.close();

        stroke(paint, cyan, 2.2f);
        canvas.drawPath(face, paint);

        stroke(paint, dim, 1f);
        canvas.drawArc(new RectF(-74, -92, 74, 105), 195, 150, false, paint);

        float blink = Math.abs((float)Math.sin(t * 0.8f));
        float eyeHeight = 8f * (0.15f + blink * 0.85f);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(cyan);
        canvas.drawOval(new RectF(-48, -37 - eyeHeight / 2, -16, -37 + eyeHeight / 2), paint);
        canvas.drawOval(new RectF(16, -37 - eyeHeight / 2, 48, -37 + eyeHeight / 2), paint);

        stroke(paint, blue, 2f);
        Path nose = new Path();
        nose.moveTo(0, -25);
        nose.lineTo(-5, 12);
        nose.quadTo(0, 19, 8, 12);
        canvas.drawPath(nose, paint);

        float mouth = mode.equals("talking")
                ? 4 + Math.abs((float)Math.sin(t * 12f)) * 15
                : mode.equals("listening") ? 5 : 3;

        stroke(paint, cyan, 2.5f);
        RectF mouthRect = new RectF(-23, 39, 23, 39 + mouth);
        canvas.drawArc(mouthRect, 10, 160, false, paint);

        stroke(paint, dim, 1.2f);
        canvas.drawLine(-55, -68, -35, -78, paint);
        canvas.drawLine(55, -68, 35, -78, paint);
        canvas.drawLine(-47, 65, -28, 78, paint);
        canvas.drawLine(47, 65, 28, 78, paint);

        for (int i = 0; i < 7; i++) {
            float y = -90 + i * 28;
            stroke(paint, Color.argb(45, 70, 220, 255), 1f);
            canvas.drawLine(-65, y, 65, y, paint);
        }

        canvas.restore();

        postInvalidateDelayed(50);
    }
}
