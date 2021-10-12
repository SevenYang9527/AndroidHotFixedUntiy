package com.ming.androidhotfixunitytest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import io.github.noodle1983.Boostrap;

public class MainActivity extends AppCompatActivity {

    public Button CheckButton;
    public Button UnityButton;
    public static TextView LogText;

    private static int logIndex;

    public static MainActivity mainActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity=this;
        CheckButton = (Button) findViewById(R.id.checkbutton);
        UnityButton = (Button) findViewById(R.id.untiybutton);
        LogText = (TextView) findViewById(R.id.allLog);

        HotFixManager.Instance().InitHotFix(this);
        CheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnCheckUpdateEvent();
            }
        });

        UnityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnEnterUnityEvent();
            }
        });
    }

    /**
     * 检测更新
     **/
    private void OnCheckUpdateEvent() {
        HotFixManager.Instance().CheckHotFix(this);
    }
    /**
     * 启动Unity
     **/
    public static void OnEnterUnityEvent() {
        Boostrap.hook();
        Intent intent = new Intent(mainActivity, UnityPlayerActivity.class);
        mainActivity.startActivity(intent);
    }
/**
 * 添加日志
 * **/
    public static String logStr="";
    public static void AddLog(String logText){
        logIndex++;
        System.out.println("LLLLLLLLLLLLogggggggggggggg    "+logText);
        if(logStr.length()>8000)logStr="";
        logStr=logIndex+">>>>>"+logText+"\n"+logStr;
        LogText.setText(logStr);
    }
}