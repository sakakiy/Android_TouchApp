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
    private BoundBall     ball;

    private int           width;
    private int           height;

    final private int     NUM_MAX = 10;
    private int           num     = 0;
    private float[]       x       = new float[NUM_MAX];
    private float[]       y       = new float[NUM_MAX];

    public SimpleView(Context context) {
        super(context);

        thread = null;
        paint = new Paint();

        for (int i = 0; i < NUM_MAX; i++) {
            x[i] = 0;
            y[i] = 0;
        }
        ball = new BoundBall((float) Math.random() * 600,
                (float) Math.random() * 1000, 30);
        ball.setVec(10f - (float) Math.random() * 20,
                10f - (float) Math.random() * 20);

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

            ball.run();
            collide();

            Canvas canvas = holder.lockCanvas();
            if (canvas == null)
                continue;

            canvas.drawColor(Color.BLACK);

            paint.setColor(Color.argb(255, 255, 255, 255));
            paint.setStyle(Paint.Style.STROKE);

            for (int i = 0; i < num; i++) {
                canvas.drawCircle(x[i], y[i], 200, paint);
            }

            paint.setColor(Color.argb(255, 150, 150, 255));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(ball.getX(), ball.getY(), ball.getRadius(), paint);

            holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new Thread(this);

        width = getWidth();
        height = getHeight();
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
        num = event.getPointerCount();
        for (int i = 0; i < num; i++) {
            x[i] = event.getX(i);
            y[i] = event.getY(i);
        }

        if (ball.isInside(event.getX(), event.getY())) {
            ball.setCoodinate(event.getX(), event.getY());
        }

        // Androidの仕様がちょっとバグい。true を返さず super.onTouchEvent(event) を返すと
        // false になっていて成立しなくなるらしい。
        return true;
    }

    private void collide() {
        if (ball.getX() - ball.getRadius() < 0) {
            ball.setX(0 + ball.getRadius());
            ball.inverseVx();
        } else if (width < ball.getX() + ball.getRadius()) {
            ball.setX(width - ball.getRadius());
            ball.inverseVx();
        }

        if (ball.getY() - ball.getRadius() < 0) {
            ball.setY(0 + ball.getRadius());
            ball.inverseVy();
        } else if (height < ball.getY() + ball.getRadius()) {
            ball.setY(height - ball.getRadius());
            ball.inverseVy();
        }

    }
}
