package com.example.touchapplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {

    // センサー甩のサービス
    private SensorService  sensorService;
    private Intent         serviceIntent;

    // ビュー
    private SimpleView     simpleView;
    private RelativeLayout layout;
    private Button         startButton, stopButton;

    // 通知
    // NotificationManager notiMng;
    // Notification note;
    private final int      NOTE_ID = 120;

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

        // 自作サービス
        serviceIntent = new Intent(this, SensorService.class);

        // 永続データ
        sharedPref = getPreferences(Activity.MODE_PRIVATE);
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

                // Notification start
                // notiMng.notify(NOTE_ID, note);
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

                // Notification cancel
                // notiMng.cancel(NOTE_ID);
            }
        });
        RelativeLayout.LayoutParams paramBottom = new RelativeLayout.LayoutParams(
                MP, WC);
        paramBottom.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        layout.addView(stopButton, paramBottom);
    }

    public void setSensorData(float[] v, int index) {
        simpleView.setSensorData(v, index);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("Activity", "Activity onResume");

        // サービスの明示的な起動（永続する）
        startService(serviceIntent);
        // サービスにバインドする
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        // 永続データ（データがなかったら111を返す）
        simpleView.setTestValue(sharedPref.getLong("TEST_VALUE", 111));
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

        // 永続データ
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("TEST_VALUE", simpleView.getTestValue());
        editor.commit();
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

                                                        simpleView
                                                                .setSensorService(sensorService);
                                                        sensorService
                                                                .setActivity(MainActivity.this);
                                                    }

                                                    @Override
                                                    public void onServiceDisconnected(
                                                            ComponentName name) {
                                                        Log.v("Activity",
                                                                "onServiceDisconnected");
                                                        sensorService = null;
                                                    }
                                                };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("Activity", "Activity onDestroy");
    }

}
