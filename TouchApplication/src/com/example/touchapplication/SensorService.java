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

    private final String  LOGSTR = "SensorSevice";

    private SensorManager pressureMng;
    private Sensor        pressureSensor;
    private boolean       isSensing;

    private long          lastSensingTime;
    private final long    intervalSensingTime = 5 * 1000;

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, LOGSTR + " onCreate", Toast.LENGTH_SHORT).show();

        // ‹Cˆ³Œv
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
    // TODO ‚¢‚ÂŒÄ‚Î‚ê‚é‚Ì‚©?
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
            lastSensingTime = currentTime;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}
