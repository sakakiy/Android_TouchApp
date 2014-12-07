package com.example.touchapplication;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
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

    private final String        LOGSTR              = "SensorService";    // ログで出すときのタグ名
    public static final int     DATA_NUM            = 50;                 // 保持するデータ数

    // 気圧センサマネージャ
    private SensorManager       pressureMng;
    private Sensor              pressureSensor;
    private boolean             isSensing;

    // 計測インターバル関連
    private long                lastSensingTime;
    private final long          intervalSensingTime = 15 * 60 * 1000;     // MilliSecond

    // 保持する計測データを格納する配列
    private float               values[]            = new float[DATA_NUM];
    private int                 valueIndex          = 0;

    // 通知
    private NotificationManager notiMng;
    private Notification        note;
    private final int           NOTE_ID             = 123;

    // アクティビティ
    private MainActivity        mainActivity        = null;

    private int                 counter             = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(LOGSTR, "SensorService onCreate");
        Toast.makeText(this, LOGSTR + " onCreate", Toast.LENGTH_SHORT).show();

        // 気圧計
        pressureMng = (SensorManager) getSystemService(SENSOR_SERVICE);
        pressureSensor = pressureMng.getDefaultSensor(Sensor.TYPE_PRESSURE);
        isSensing = false;

        // 通知
        notiMng = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification Setting
        note = new Notification.Builder(getApplicationContext())
                .setContentTitle("Notification from Service")
                .setContentText("--").setSmallIcon(R.drawable.ic_launcher)
                .build();

        counter = 0;

        // test
        startSensor();

    }

    public void setActivity(MainActivity act) {
        mainActivity = act;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOGSTR, "SensorService onDestroy");
        Toast.makeText(this, LOGSTR + " onDestroy", Toast.LENGTH_SHORT).show();
        pressureMng.unregisterListener(this);

        // test
        stopSensor();
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
        Log.v(LOGSTR, "onBind");
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

            // 通知時間取得
            String time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                    .format(new Date());

            // データを通知
            counter++;
            String notiStr = Integer.toString(counter) + " : "
                    + Float.toString(values[valueIndex]);

            notiMng.cancel(NOTE_ID);

            // アプリへ飛べるような Notification を作成
            Intent launchIntent = new Intent(getApplicationContext(),
                    MainActivity.class);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    getApplicationContext(), 0, launchIntent, 0);
            note = new Notification.Builder(getApplicationContext())
                    .setContentTitle("Pressure Service")
                    .setContentText(time + " ** " + notiStr)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_launcher).build();

            notiMng.notify(NOTE_ID, note);

            Toast.makeText(this, "[Sensor]" + notiStr, Toast.LENGTH_SHORT)
                    .show();

            // MainActivity にセンサデータを反映
            if (mainActivity != null) {
                float[] v = new float[counter];
                for (int i = 0; i < counter; i++) {
                    v[i] = values[(valueIndex + i) % DATA_NUM];
                }
                mainActivity.setSensorData(v, counter);
                counter = 0;
            }

            // 現在の時間を記録
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
