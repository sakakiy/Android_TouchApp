package com.example.touchapplication;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ShareCompat.IntentBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {

    // センサー甩のサービス
    private SensorService  sensorService;
    private Intent         serviceIntent;
    private int            VALUE_MAX      = SensorService.DATA_NUM;
    private float          sensorValues[] = new float[SensorService.DATA_NUM];
    private SensorData     sensorData[]   = new SensorData[SensorService.DATA_NUM];
    private int            sensorIndex;

    // ビュー
    private SimpleView     simpleView;
    private RelativeLayout layout;
    private Button         startButton, stopButton;

    // データの保存
    SharedPreferences      sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(new SimpleView(this));

        // レイアウトを設定する
        layout = new RelativeLayout(this);
        setContentView(layout);
        settingLayout();

        for (int i = 0; i < VALUE_MAX; i++) {
            sensorData[i] = new SensorData();
        }

        // 自作サービス
        serviceIntent = new Intent(this, SensorService.class);

        // 永続データ
        sharedPref = getSharedPreferences(SensorService.PREF_NAME,
                Activity.MODE_PRIVATE);
        // sharedPref = getPreferences(Activity.MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("Activity", "Activity onResume");

        // サービスの明示的な起動（永続する）
        startService(serviceIntent);
        // サービスにバインドする
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        refreshSensorData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v("Activity", "Activity onPause");
        if (!sensorService.isSensing()) {
            stopService(serviceIntent);
            Log.v("Activity", "Stop Service");
        }
        unbindService(serviceConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("Activity", "Activity onDestroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, "ALL LOG");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        super.onOptionsItemSelected(item);
        
        if(item.getItemId() == Menu.FIRST){
            Intent intent = new Intent(getApplicationContext(), LogActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        
        return true;
    }

    private void settingLayout() {
        // SimpleView
        simpleView = new SimpleView(this);

        int MP = ViewGroup.LayoutParams.MATCH_PARENT;
        int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

        layout.addView(simpleView, new RelativeLayout.LayoutParams(MP, WC));

        // StartButton
        startButton = new Button(this);
        startButton.setText("START");
        startButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // sensorService.startSensor();

                // test
                startService(serviceIntent);
            }
        });
        RelativeLayout.LayoutParams paramTop = new RelativeLayout.LayoutParams(
                MP, WC);
        paramTop.addRule(RelativeLayout.ALIGN_TOP);
        layout.addView(startButton, paramTop);

        // StopButton
        stopButton = new Button(this);
        stopButton.setText("STOP");
        stopButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // sensorService.stopSensor();

                // test
                stopService(serviceIntent);
            }
        });
        RelativeLayout.LayoutParams paramBottom = new RelativeLayout.LayoutParams(
                MP, WC);
        paramBottom.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        layout.addView(stopButton, paramBottom);

        // MainActivity を登録
        simpleView.setMainActivity(this);
    }

    public float[] getSensorValues() {
        return sensorValues;
    }

    public SensorData[] getSensorData() {
        return sensorData;
    }

    public int getSensorIndex() {
        return sensorIndex;
    }

    public void refreshSensorData() {

        // センサデータ復旧
        for (int i = 0; i < VALUE_MAX; i++) {
            // データが無かったら 10 を返す
            sensorValues[i] = sharedPref.getFloat(SensorService.SENSOR_VALUE
                    + Integer.toString(i), 10);

            // SensorData クラスを使ってみる
            sensorData[i].setSensorValue(sharedPref.getFloat(
                    SensorService.SENSOR_VALUE + Integer.toString(i), 10));
            sensorData[i].setDate(sharedPref.getString(
                    SensorService.SENSOR_DATE + Integer.toString(i), "null"));
            sensorData[i].setTime(sharedPref.getString(
                    SensorService.SENSOR_TIME + Integer.toString(i), "null"));
        }
        sensorIndex = sharedPref.getInt(SensorService.SENSOR_INDEX, 0);
        simpleView.refreshDrawableState();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

                                                    @Override
                                                    public void onServiceConnected(
                                                            ComponentName name,
                                                            IBinder service) {
                                                        Log.v("Activity",
                                                                "onServiceConnected");
                                                        sensorService = ((SensorService.ServiceBinder) service)
                                                                .getService();
                                                    }

                                                    @Override
                                                    public void onServiceDisconnected(
                                                            ComponentName name) {
                                                        Log.v("Activity",
                                                                "onServiceDisconnected");
                                                        sensorService = null;
                                                    }
                                                };

}
