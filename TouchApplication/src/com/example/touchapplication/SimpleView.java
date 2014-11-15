package com.example.touchapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SimpleView extends SurfaceView implements Runnable,
        SurfaceHolder.Callback {

    private Paint         paint;
    private SurfaceHolder holder;
    private Thread        thread;

    private float         x, y, rv;

    public SimpleView(Context context) {
        super(context);

        holder = null;
        thread = null;
        paint = new Paint();

        x = y = 0;
        rv = 0;

        getHolder().addCallback(this);
    }

    @Override
    public void run() {
        while (thread != null) {
            Canvas canvas = holder.lockCanvas();
            canvas.drawColor(Color.BLACK);

            rv+=0.01;
            x = (float) (300 + 100 * Math.cos(rv));
            y = (float)(300 + 100 * Math.sin(rv));

            paint.setColor(Color.argb(255, 255, 255, 200));
            canvas.drawCircle(x, y, 30, paint);

            holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.holder = holder;
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

}
