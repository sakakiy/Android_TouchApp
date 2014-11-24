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

    private final String    LOGSTR              = "SensorSevice";     // ���O�ŏo���Ƃ��̃^�O��
    public static final int DATA_NUM            = 50;                 // �ێ�����f�[�^��

    // �C���Z���T�}�l�[�W��
    private SensorManager   pressureMng;
    private Sensor          pressureSensor;
    private boolean         isSensing;

    // �v���C���^�[�o���֘A
    private long            lastSensingTime;
    private final long      intervalSensingTime = 30 * 60 * 1000;     // MilliSecond

    // �ێ�����v���f�[�^���i�[����z��
    private float           values[]            = new float[DATA_NUM];
    private int             valueIndex          = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, LOGSTR + " onCreate", Toast.LENGTH_SHORT).show();

        // �C���v
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
    // TODO ���Ă΂��̂�?
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

            // �����̒��Ńf�[�^��ێ�����
            valueIndex = (valueIndex + 1) % DATA_NUM;
            values[valueIndex] = event.values[0];

            lastSensingTime = currentTime;
        }
    }

    // �Z���T�f�[�^���i�[�����z���Ԃ�
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
