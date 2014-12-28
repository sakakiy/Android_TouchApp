package com.example.touchapplication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
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
    private MainActivity  mainActivity;

    private Paint         paint;
    private Thread        thread;
    private SurfaceHolder holder;
    private BoundBall     ball;

    // 画面サイズ
    private int           width;
    private int           height;

    // マルチタッチ点の表示
    final private int     TOUCH_NUM_MAX  = 10;
    private int           num            = 0;
    private float[]       x              = new float[TOUCH_NUM_MAX];
    private float[]       y              = new float[TOUCH_NUM_MAX];

    // センサのサービスから値を受け取る
    private final int     VALUE_MAX      = SensorService.DATA_NUM;
    private float         sensorValues[] = new float[VALUE_MAX];
    private int           sensorIndex;
    private SensorData[]  sensorData     = new SensorData[SensorService.DATA_NUM];

    private float         graphX, graphY, graphMargin, graphWidth;

    // タッチにより動的に値を変える。位置などに自由に使う
    private float[]       touchY         = new float[3];

    public SimpleView(Context context) {
        super(context);

        thread = null;
        paint = new Paint();

        for (int i = 0; i < TOUCH_NUM_MAX; i++) {
            x[i] = 0;
            y[i] = 0;
        }
        ball = new BoundBall((float) Math.random() * 600,
                (float) Math.random() * 1000, 30);
        ball.setVec(10f - (float) Math.random() * 20,
                10f - (float) Math.random() * 20);

        holder = getHolder();
        holder.addCallback(this);

        // センサのグラフ関連のフィールドの初期化
        for (int i = 0; i < VALUE_MAX; i++) {
            sensorValues[i] = 0;
        }
        graphMargin = 3;
        graphX = 100;
        graphY = 1100;
        width = 1080;
        graphWidth = ((width - graphX * 2) + graphMargin) / VALUE_MAX
                - graphMargin;
        sensorIndex = 0;

        // SensorData initialize
        for (int i = 0; i < VALUE_MAX; i++) {
            sensorData[i] = new SensorData();
        }
    }

    public void setMainActivity(MainActivity ma) {
        if (mainActivity == null) {
            mainActivity = ma;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new Thread(this);

        width = getWidth();
        height = getHeight();

        // 気圧センサーサービスから値の配列をもらう
        refreshSensorData();
        Log.v("SimpleView", "surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        if (thread != null) {
            thread.start();
        }
        Log.v("SimpleView", "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v("SimpleView", "surface destroyed");
        thread = null;
    }

    // TODO Activity から取得するように変更
    // センサーデータを更新する
    private void refreshSensorData() {
        if (mainActivity != null) {

            mainActivity.refreshSensorData();
            sensorIndex = mainActivity.getSensorIndex();
            float[] tmpValues = mainActivity.getSensorValues();

            SensorData[] sData = mainActivity.getSensorData();
            for (int i = 0; i < VALUE_MAX; i++) {
                sensorValues[i] = tmpValues[i];

                sensorData[i] = sData[i];
            }

        }
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

            // 背景を塗りつぶす
            canvas.drawColor(Color.BLACK);

            // タッチできる領域を塗りつぶす
            int colorDark = 20;
            paint.setColor(Color.argb(255, 0, 0, colorDark));
            canvas.drawRect(0, 0, 3 * width / 3, height, paint);
            paint.setColor(Color.argb(255, 0, colorDark, 0));
            canvas.drawRect(0, 0, 2 * width / 3, height, paint);
            paint.setColor(Color.argb(255, colorDark, 0, 0));
            canvas.drawRect(0, 0, 1 * width / 3, height, paint);

            // タッチ円描画
            paint.setColor(Color.argb(255, 255, 255, 255));
            paint.setStyle(Paint.Style.STROKE);

            for (int i = 0; i < num; i++) {
                canvas.drawCircle(x[i], y[i], 200, paint);
            }

            // テキスト表示
            float fontSize = 50f;
            float marginX = 20;
            float marginY = 10;

            paint.setTextSize(fontSize);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(Color.argb(255, 150, 255, 255));

            canvas.drawText(sensorData[sensorIndex].getDateTime() + " ATM : "
                    + Float.toString(sensorValues[sensorIndex]), marginX,
                    200 + (marginY + fontSize) * 0, paint);
            int pastSensorIndex = 0;
            pastSensorIndex = (sensorIndex - 1 + VALUE_MAX) % VALUE_MAX;
            canvas.drawText(
                    sensorData[pastSensorIndex].getDateTime() + " ATM : "
                            + Float.toString(sensorValues[pastSensorIndex]),
                    marginX, 200 + (marginY + fontSize) * 1, paint);
            pastSensorIndex = (sensorIndex - 2 + VALUE_MAX) % VALUE_MAX;
            canvas.drawText(
                    sensorData[pastSensorIndex].getDateTime() + " ATM : "
                            + Float.toString(sensorValues[pastSensorIndex]),
                    marginX, 200 + (marginY + fontSize) * 2, paint);
            pastSensorIndex = (sensorIndex - 3 + VALUE_MAX) % VALUE_MAX;
            canvas.drawText(
                    sensorData[pastSensorIndex].getDateTime() + " ATM : "
                            + Float.toString(sensorValues[pastSensorIndex]),
                    marginX, 200 + (marginY + fontSize) * 3, paint);
            pastSensorIndex = (sensorIndex - 4 + VALUE_MAX) % VALUE_MAX;
            canvas.drawText(
                    sensorData[pastSensorIndex].getDateTime() + " ATM : "
                            + Float.toString(sensorValues[pastSensorIndex]),
                    marginX, 200 + (marginY + fontSize) * 4, paint);

            canvas.drawText(Float.toString(touchY[0]), marginX,
                    200 + (marginY + fontSize) * 5, paint);
            canvas.drawText(Float.toString(touchY[1]), marginX,
                    200 + (marginY + fontSize) * 6, paint);
            canvas.drawText(Float.toString(touchY[2]), marginX,
                    200 + (marginY + fontSize) * 7, paint);

            // ボール描画
            paint.setColor(Color.argb(255, 150, 150, 255));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(ball.getX(), ball.getY(), ball.getRadius(), paint);

            // 今日の日付(MM/dd)を取得。怒られるのでロケールを追加する
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd",
                    Locale.JAPANESE);
            String currentDate = sdf.format(new Date());
            // 気圧センサーのデータをグラフ化
            int tmpIndex = 0;
            for (int i = 0; i < VALUE_MAX; i++) {

                // グラフの描画順を変えるための一時変数
                tmpIndex = (i + 1 + sensorIndex) % VALUE_MAX;

                // グラフの色の変更
                if (tmpIndex == sensorIndex) { // センサーのインデックスと同じなら赤く
                    paint.setColor(Color.argb(255, 255, 100, 100));
                } else if (isMatchDate(currentDate,
                        sensorData[tmpIndex].getDate())) { // 今日の日付ならちょっと色を付ける
                    paint.setColor(Color.argb(255, 255, 200, 100));
                } else { // それ以外は緑
                    paint.setColor(Color.argb(255, 100, 255, 100));
                }

                // グラフの描画
                canvas.drawRect(graphX + i * (graphWidth + graphMargin), graphY
                        - 6 * (sensorValues[tmpIndex] - 980), graphX + i
                        * (graphWidth + graphMargin) + graphWidth, graphY,
                        paint);
            }

            holder.unlockCanvasAndPost(canvas);
        }
    }

    // 与えられた日付（MM/dd）が今日の日付と一致するかを調べる
    private boolean isMatchDate(String currentDate, String date) {

        String[] str = date.split("/");
        date = new StringBuilder().append(str[1]).append("/").append(str[2])
                .toString();
        return date.equals(currentDate);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // センサーのデータを更新する
        refreshSensorData();

        num = event.getPointerCount();
        for (int i = 0; i < num; i++) {
            x[i] = event.getX(i);
            y[i] = event.getY(i);
        }

        if (ball.isInside(event.getX(), event.getY())) {
            ball.setCoodinate(event.getX(), event.getY());
        }

        // 自由に使える変数
        touchY[(int) event.getX() / (width / 3)] = (height - event.getY())
                / (float) height;

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
