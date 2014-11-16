package com.example.touchapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SimpleView extends SurfaceView implements SurfaceHolder.Callback, Runnable{

    private Paint  paint;
    private Thread thread;
    private SurfaceHolder holder;

    private float  x, y, rv;
    private float  centerX, centerY;

    public SimpleView(Context context) {
        super(context);

        thread = null;
        paint = new Paint();

        x = y = 0;
        centerX = centerY = 0;
        rv = 0;
        holder = getHolder();
        holder.addCallback(this);
        
    }

    @Override
    public void run() {
        while (thread != null) {
            Canvas canvas = holder.lockCanvas();
            canvas.drawColor(Color.BLACK);

            rv += 0.05;
            x = (float) (centerX + 100 * Math.cos(rv));
            y = (float) (centerY + 100 * Math.sin(rv));

            paint.setColor(Color.argb(255, 255, 255, 200));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(x, y, 30, paint);
            paint.setColor(Color.argb(255, 255, 255, 255));
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(centerX, centerY, 100, paint);

            holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new Thread(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        if (thread != null) {
            thread.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        centerX = event.getX();
        centerY = event.getY();

        // Androidの仕様がちょっとバグい。true を返さず super.onTouchEvent(event) を返すと
        // false になっていて成立しなくなるらしい。
        return true;
    }

}
