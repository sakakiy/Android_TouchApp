package com.example.touchapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SimpleView extends SurfaceView implements SurfaceHolder.Callback,
        Runnable {

    private Paint         paint;
    private Thread        thread;
    private SurfaceHolder holder;

    final private int     NUM_MAX = 10;
    private int           num     = 0;
    private float[]       x       = new float[NUM_MAX];
    private float[]       y       = new float[NUM_MAX];
    private float         rv;
    private float         centerX, centerY;

    public SimpleView(Context context) {
        super(context);

        thread = null;
        paint = new Paint();

        for (int i = 0; i < NUM_MAX; i++) {
            x[i] = 0;
            y[i] = 0;
        }
        centerX = centerY = 0;
        rv = 0;
        holder = getHolder();
        holder.addCallback(this);

    }

    @Override
    public void run() {
        while (thread != null) {
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Canvas canvas = holder.lockCanvas();
            if (canvas == null)
                continue;

            canvas.drawColor(Color.BLACK);

            rv += 0.05;
            // x = (float) (centerX + 100 * Math.cos(rv));
            // y = (float) (centerY + 100 * Math.sin(rv));

            paint.setColor(Color.argb(255, 255, 255, 200));
            paint.setStyle(Paint.Style.FILL);

            paint.setColor(Color.argb(255, 255, 255, 255));
            paint.setStyle(Paint.Style.STROKE);
            // canvas.drawCircle(centerX, centerY, 100, paint);            
            for (int i = 0; i < num; i++) {
                canvas.drawCircle(x[i], y[i], 200, paint); 
            }

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
        Log.v("surface destroyed", "surface destroyed");
        thread = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        centerX = event.getX();
        centerY = event.getY();
        num = event.getPointerCount();
        for(int i=0; i<num; i++){
            x[i] = event.getX(i);
            y[i] = event.getY(i);
        }

        // Androidの仕様がちょっとバグい。true を返さず super.onTouchEvent(event) を返すと
        // false になっていて成立しなくなるらしい。
        return true;
    }
}
