package com.berightback;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * FIX ME: Across all the classes
 *  1. Move constants to String.xml
 *  2. Method level refactoring skipped. There may be some memory leaks.
 * 
*/
public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(Main2Activity.this,MainActivity.class));
                Main2Activity.this.finish();
            }
        },1500);
    }
}
