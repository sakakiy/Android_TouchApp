package com.example.touchapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SimpleView extends SurfaceView implements SurfaceHolder.Callback,
        Runnable, SensorEventListener {

    private Paint         paint;
    private Thread        thread;
    private SurfaceHolder holder;
    private BoundBall     ball;

    // �C���Z���T�A�Ɠx�Z���T
    private SensorManager sensorMng;
    private Sensor        pressureSensor;
    private float         pressureValue;
    private Sensor        lightSensor;
    private float         lightValue;

    // ��ʃT�C�Y
    private int           width;
    private int           height;

    // �}���`�^�b�`�_�̕\��
    final private int     NUM_MAX         = 10;
    private int           num             = 0;
    private float[]       x               = new float[NUM_MAX];
    private float[]       y               = new float[NUM_MAX];

    // �Z���T�̃T�[�r�X����l���󂯎��
    private SensorService sensorService;
    private final int     GRAPH_VALUE_NUM = SensorService.DATA_NUM;
    private float         graphX, graphY, graphMargin, graphWidth;
    private float         sensorValues[]  = new float[GRAPH_VALUE_NUM];
    private int           sensorIndex;

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

        sensorMng = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);

        // �Z���T�̃O���t�֘A�̃t�B�[���h�̏�����
        for (int i = 0; i < GRAPH_VALUE_NUM; i++) {
            sensorValues[i] = 0;
        }
        graphMargin = 3;
        graphX = 100;
        graphY = 800;
        width = 1080;
        graphWidth = ((width - graphX * 2) + graphMargin) / GRAPH_VALUE_NUM
                - graphMargin;
        sensorIndex = 0;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new Thread(this);

        width = getWidth();
        height = getHeight();

        // �C���Z���T�[
        pressureSensor = sensorMng.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sensorMng.registerListener(this, pressureSensor,
                SensorManager.SENSOR_DELAY_UI);
        lightSensor = sensorMng.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorMng.registerListener(this, lightSensor,
                SensorManager.SENSOR_DELAY_UI);

        // �C���Z���T�[�T�[�r�X����l�̔z������炤
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
        sensorMng.unregisterListener(this);
    }

    // �Z���T�[�f�[�^���X�V����
    private void refreshSensorData() {
        if (sensorService != null) {
            float tmp[] = sensorService.getValues();
            for (int i = 0; i < GRAPH_VALUE_NUM; i++) {
                sensorValues[i] = tmp[i];
            }
            sensorIndex = sensorService.getIndex();
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

            // �w�i��h��Ԃ�
            canvas.drawColor(Color.BLACK);

            float fontSize = 50f;
            float marginX = 20;
            float marginY = 10;
            paint.setTextSize(fontSize);
            canvas.drawText("Pressure : " + Float.toString(pressureValue),
                    marginX, 50 + (marginY + fontSize) * 0, paint);
            canvas.drawText("Light    : " + Float.toString(lightValue),
                    marginX, 50 + (marginY + fontSize) * 1, paint);

            paint.setColor(Color.argb(255, 255, 255, 255));
            paint.setStyle(Paint.Style.STROKE);

            for (int i = 0; i < num; i++) {
                canvas.drawCircle(x[i], y[i], 200, paint);
            }

            paint.setColor(Color.argb(255, 150, 150, 255));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(ball.getX(), ball.getY(), ball.getRadius(), paint);

            // �C���Z���T�[�̃f�[�^���O���t��
            for (int i = 0; i < GRAPH_VALUE_NUM; i++) {
                if (i == sensorIndex) {
                    paint.setColor(Color.argb(255, 255, 100, 100));
                } else {
                    paint.setColor(Color.argb(255, 100, 255, 100));
                }
                canvas.drawRect(graphX + i * (graphWidth + graphMargin),
                        graphY, graphX + i * (graphWidth + graphMargin)
                                + graphWidth, graphY + 3
                                * (sensorValues[i] - 950), paint);
            }

            holder.unlockCanvasAndPost(canvas);
        }
    }

    public void setSensorService(SensorService ss) {
        this.sensorService = ss;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // �Z���T�[�̃f�[�^���X�V����
        refreshSensorData();

        num = event.getPointerCount();
        for (int i = 0; i < num; i++) {
            x[i] = event.getX(i);
            y[i] = event.getY(i);
        }

        if (ball.isInside(event.getX(), event.getY())) {
            ball.setCoodinate(event.getX(), event.getY());
        }

        // Android�̎d�l��������ƃo�O���Btrue ��Ԃ��� super.onTouchEvent(event) ��Ԃ���
        // false �ɂȂ��Ă��Đ������Ȃ��Ȃ�炵���B
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            pressureValue = event.values[0];
        } else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            lightValue = event.values[0];
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
