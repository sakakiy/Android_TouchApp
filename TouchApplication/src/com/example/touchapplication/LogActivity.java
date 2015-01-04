package com.example.touchapplication;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

public class LogActivity extends Activity {

    // View 関連
    ScrollView        scrollView;
    TextView          textView;

    // SharedPreferences
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_log);
        scrollView = new ScrollView(this);
        textView = new TextView(this);
        textView.setText("a");
        textView.setBackgroundColor(Color.BLACK);
        textView.setTextColor(Color.WHITE);

        scrollView.addView(textView);
        scrollView.setBackgroundColor(Color.DKGRAY);
        setContentView(scrollView);

        // 永続データ
        sharedPref = getSharedPreferences(SensorService.PREF_NAME,
                Activity.MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // SharedPreferences から値を読み出し TextView  で表示
        String logStr = "";
        int index = sharedPref.getInt(SensorService.SENSOR_INDEX, 0);
        for (int i = 0; i < SensorService.DATA_NUM; i++) {
            logStr += String.format("[%03d]  ", i);
            logStr += sharedPref.getString(
                    SensorService.SENSOR_DATE + Integer.toString(i), "null");
            logStr += " ";
            logStr += sharedPref.getString(
                    SensorService.SENSOR_TIME + Integer.toString(i), "null");
            logStr += "     ";
            logStr += String.valueOf(sharedPref.getFloat(
                    SensorService.SENSOR_VALUE + Integer.toString(i), 0));
            
            // インデックスが指す位置を表す 
            if(i == index){
                logStr += "  <--------- index ---------\n";
            } else {
                logStr += "\n";
            }
        }
        textView.setText(logStr);
    }
}
