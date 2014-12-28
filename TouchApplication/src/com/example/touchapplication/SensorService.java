package com.example.touchapplication;

import java.util.Calendar;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

public class SensorService extends Service implements SensorEventListener {

    private final String        LOGSTR              = "SensorService";    // ログで出すときのタグ名
    public static final int     DATA_NUM            = 100;                // 保持するデータ数

    // 気圧センサマネージャ
    private SensorManager       pressureMng;
    private Sensor              pressureSensor;
    private boolean             isSensing;

    // 計測インターバル関連
    private long                lastSensingTime;
    private final long          intervalSensingTime = 30 * 60 * 1000;     // MilliSecond

    // 保持する計測データを格納する配列
    private float               sensorValues[]      = new float[DATA_NUM];
    private int                 sensorIndex         = 0;
    private String              currentDate         = "";
    private String              currentTime         = "";

    // 通知
    private NotificationManager notiMng;
    private Notification        note;
    private final int           NOTE_ID             = 123;

    // Activity と共有するための SharedPreferences
    private SharedPreferences   sharedPref;

    public static final String  PREF_NAME           = "SENSOR_SERVICE";

    public static final String  SENSOR_INDEX        = "SENSOR_INDEX";
    public static final String  SENSOR_VALUE        = "SENSOR_VALUE_";
    public static final String  SENSOR_DATE         = "SENSOR_DATE_";
    public static final String  SENSOR_TIME         = "SENSOR_TIME_";
    public static final String  SENSOR_LAST_TIME    = "SENSOR_LAST_TIME";

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

        // test
        startSensor();

        // SharedPreferences からインデックスを取得する
        sharedPref = getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        sensorIndex = sharedPref.getInt(SENSOR_INDEX, 0);
        lastSensingTime = sharedPref.getLong(SENSOR_LAST_TIME, 0);
        
        // Battery Reaciever
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOGSTR, "SensorService onDestroy");
        Toast.makeText(this, LOGSTR + " onDestroy", Toast.LENGTH_SHORT).show();
        pressureMng.unregisterListener(this);
        
        unregisterReceiver(broadcastReceiver);

        // test
        stopSensor();
    }
    
    public static BroadcastReceiver broadcastReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){
                int batteryLevel = intent.getIntExtra("level", 0);
                Log.v("BATTERY", "BATTERY LEVEL : " + Integer.toString(batteryLevel));
                Toast.makeText(context, "BATTERY : " + Integer.toString(batteryLevel), Toast.LENGTH_SHORT).show();
            }
           
            
        }
        
    };

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
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis > lastSensingTime + intervalSensingTime) {
            Log.v(LOGSTR,
                    "onSensorChanged : Value "
                            + Float.toString(event.values[0]));

            // 自分の中でデータを保持する（インデックスは処理の最後で増やす）
            sensorValues[sensorIndex] = event.values[0];

            // 通知時間取得
            currentDate = DateFormat.format("yyyy/MM/dd",
                    Calendar.getInstance()).toString();
            this.currentTime = DateFormat.format("HH:mm:ss",
                    Calendar.getInstance()).toString();

            // データを通知
            String notiStr = Float.toString(sensorValues[sensorIndex]);

            notiMng.cancel(NOTE_ID);

            // アプリへ飛べるような Notification を作成
            Intent launchIntent = new Intent(getApplicationContext(),
                    MainActivity.class);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    getApplicationContext(), 0, launchIntent, 0);
            note = new Notification.Builder(getApplicationContext())
                    .setContentTitle("Pressure Service")
                    .setContentText(
                            currentDate + " " + currentTime + " ** " + notiStr)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_launcher).build();

            notiMng.notify(NOTE_ID, note);

            Toast.makeText(this, "[Sensor]" + notiStr, Toast.LENGTH_SHORT)
                    .show();

            // センサデータを保存
            saveSensorData();

            // 次に備えインデックス増やす
            sensorIndex = (sensorIndex + 1) % DATA_NUM;

            // インターバルのため、現在の時間を記録
            lastSensingTime = currentTimeMillis;
        }
    }

    // センサデータを保存
    private void saveSensorData() {
        // 値を SharedPreference に保持し続ける
        sharedPref = getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt(SENSOR_INDEX, sensorIndex);
        editor.putFloat(SENSOR_VALUE + Integer.toString(sensorIndex),
                sensorValues[sensorIndex]);
        editor.putString(SENSOR_DATE + Integer.toString(sensorIndex),
                currentDate);
        editor.putString(SENSOR_TIME + Integer.toString(sensorIndex),
                currentTime);

        editor.putLong(SENSOR_LAST_TIME, lastSensingTime);
        editor.commit();
    }

    // センサデータを格納した配列を返す
    public float[] getValues() {
        return sensorValues;
    }

    public int getIndex() {
        return sensorIndex;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}
