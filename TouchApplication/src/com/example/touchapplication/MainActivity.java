package com.example.touchapplication;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SimpleView(this));
    }
    
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
     
        super.onDestroy();
    }
    
    
}
