package com.mark.dhookutils;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.mark.dhookutils.utils.HookActivityUtils;

import java.lang.reflect.InvocationTargetException;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try{
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.mark.dhookutils","com.mark.dhookutils.AimActivity"));
            startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        try {
            HookActivityUtils.hookAMN();
            HookActivityUtils.hookActivityThread();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
