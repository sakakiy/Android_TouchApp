package com.example.touchapplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {

    // 自作サービス
    private SensorService  sensorService;
    private Intent         serviceIntent;

    // ビュー
    private SimpleView     simpleView;
    private RelativeLayout layout;
    private Button         startButton, stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(new SimpleView(this));

        // レイアウトを設定する
        layout = new RelativeLayout(this);
        setContentView(layout);
        simpleView = new SimpleView(this);

        int MP = ViewGroup.LayoutParams.MATCH_PARENT;
        int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

        layout.addView(simpleView, new RelativeLayout.LayoutParams(MP, WC));
        startButton = new Button(this);
        startButton.setText("START");
        startButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                sensorService.startSensor();
            }
        });
        stopButton = new Button(this);
        stopButton.setText("STOP");
        stopButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                sensorService.stopSensor();
            }
        });
        RelativeLayout.LayoutParams paramTop = new RelativeLayout.LayoutParams(
                MP, WC);
        paramTop.addRule(RelativeLayout.ALIGN_TOP);
        layout.addView(startButton, paramTop);
        RelativeLayout.LayoutParams paramBottom = new RelativeLayout.LayoutParams(
                MP, WC);
        paramBottom.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        layout.addView(stopButton, paramBottom);

        // 自作サービス
        serviceIntent = new Intent(this, SensorService.class);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // サービスの明示的な起動（永続する）
        startService(serviceIntent);
        Log.v("Activity", "Activity onResume");
        // サービスにバインドする
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!sensorService.isSensing()) {
            stopService(serviceIntent);
        }
        unbindService(serviceConnection);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

                                                    @Override
                                                    public void onServiceConnected(
                                                            ComponentName name,
                                                            IBinder service) {
                                                        sensorService = ((SensorService.ServiceBinder) service)
                                                                .getService();
                                                    }

                                                    @Override
                                                    public void onServiceDisconnected(
                                                            ComponentName name) {
                                                        sensorService = null;
                                                    }
                                                };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
