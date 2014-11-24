package com.example.touchapplication;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class SensorService extends Service implements SensorEventListener {

    private final String    LOGSTR              = "SensorSevice";     // ログで出すときのタグ名
    public static final int DATA_NUM            = 50;                 // 保持するデータ数

    // 気圧センサマネージャ
    private SensorManager   pressureMng;
    private Sensor          pressureSensor;
    private boolean         isSensing;

    // 計測インターバル関連
    private long            lastSensingTime;
    private final long      intervalSensingTime = 30 * 60 * 1000;     // MilliSecond

    // 保持する計測データを格納する配列
    private float           values[]            = new float[DATA_NUM];
    private int             valueIndex          = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, LOGSTR + " onCreate", Toast.LENGTH_SHORT).show();

        // 気圧計
        pressureMng = (SensorManager) getSystemService(SENSOR_SERVICE);
        pressureSensor = pressureMng.getDefaultSensor(Sensor.TYPE_PRESSURE);
        isSensing = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, LOGSTR + " onDestroy", Toast.LENGTH_SHORT).show();
        pressureMng.unregisterListener(this);
    }

    public void startSensor() {
        pressureMng.registerListener(this, pressureSensor,
                SensorManager.SENSOR_DELAY_UI);
        Log.v(LOGSTR, "SensorService START");
        Toast.makeText(this, "Start Sensing.", Toast.LENGTH_SHORT).show();

        isSensing = true;
    }

    public void stopSensor() {
        pressureMng.unregisterListener(this);
        Log.v(LOGSTR, "SensorService STOP");
        Toast.makeText(this, "Stop Sensing.", Toast.LENGTH_SHORT).show();

        isSensing = false;
    }

    public boolean isSensing() {
        return isSensing;
    }

    public class ServiceBinder extends Binder {
        SensorService getService() {
            return SensorService.this;
        }
    }

    private final ServiceBinder binder = new ServiceBinder();

    @Override
    // TODO いつ呼ばれるのか?
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long currentTime = System.currentTimeMillis();
        if (currentTime > lastSensingTime + intervalSensingTime) {
            Log.v(LOGSTR,
                    "onSensorChanged : Value "
                            + Float.toString(event.values[0]));

            // 自分の中でデータを保持する
            valueIndex = (valueIndex + 1) % DATA_NUM;
            values[valueIndex] = event.values[0];

            lastSensingTime = currentTime;
        }
    }

    // センサデータを格納した配列を返す
    public float[] getValues() {
        return values;
    }

    public int getIndex() {
        return valueIndex;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}
