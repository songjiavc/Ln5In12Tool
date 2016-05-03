package com.baiylin.songjia.ln5in12tool.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.baiylin.songjia.ln5in12tool.bean.Ln5In12Bean;
import com.baiylin.songjia.ln5in12tool.bean.ResponseBean;
import com.baiylin.songjia.ln5in12tool.db.DbHelper;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by songjia on 16-4-30.
 */
public class GetLottoryData extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    DbHelper dbHelper;

    @Override
    public void onCreate(){
        super.onCreate();
        /*   timer 是有可能不唤醒cpu的
        Timer timer = new Timer();
        timer.schedule(new GetLottoryWork(this.getBaseContext()),10000,30000);
        Log.d(this.getClass().getSimpleName(),"服务创建成功!");
        */
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        //Log.d(this.getClass().getSimpleName(),"服务启动!");
        new Thread(new Runnable(){
            @Override
            public void run(){
                //undo 执行数据接口访问
                SharedPreferences sharedPreferences = getSharedPreferences("data",0);
                String lastIssueNumber = sharedPreferences.getString("lastIssueNumber",null);
                final String urlStr = "http://www.dzzst.cn:1819/webappmgr/lottoryOuter/getLn5In12List.action";
                URL url = null;
                HttpURLConnection connection = null;
                try {
                    String content = "issueNumber="+lastIssueNumber;
                    url = new URL(urlStr+"?"+content);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    String jsonStr = response.toString();
                    Gson gson = new Gson();
                    ResponseBean responseBean = gson.fromJson(jsonStr,ResponseBean.class);
                    if(responseBean.getStatus() == 1){
                        List<Ln5In12Bean> dataList = responseBean.getLn5In12List();
                        insertData2Db(dataList);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("lastIssueNumber",dataList.get(dataList.size()-1).getIssueNumber());
                        editor.commit();
                        //当获取单条开奖结果成功后调用data广播处理开奖结果内容
                        Intent intent = new Intent();//创建Intent对象
                        intent.setAction("com.baiylin.dataReceiver");
                        intent.putExtra("status", 1);
                        intent.putExtra("msg", "success!");
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("Ln5In12Bean", dataList.get(0));
                        intent.putExtras(bundle);
                        sendBroadcast(intent);//发送广播
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if(connection != null){
                        connection.disconnect();
                    }
                }
            }
        } ).start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 5000; // 这是一小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent("ELITOR_CLOCK");
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onDestroy(){
        Log.d(this.getClass().getSimpleName(),"服务销毁!");
        super.onDestroy();
    }
    private void insertData2Db(List<Ln5In12Bean> dataList){
        SQLiteDatabase db = this.getDbHelper().getWritableDatabase();
        for(Ln5In12Bean ln5In12Bean : dataList) {
            ContentValues value = new ContentValues();
            value.put("ISSUE_NUMBER",ln5In12Bean.getIssueNumber());
            value.put("NO1",ln5In12Bean.getNo1());
            value.put("NO2",ln5In12Bean.getNo2());
            value.put("NO3",ln5In12Bean.getNo3());
            value.put("NO4",ln5In12Bean.getNo4());
            value.put("NO5",ln5In12Bean.getNo5());
            db.insert("BASE",null,value);
        }
    }
    private DbHelper getDbHelper(){
        if(dbHelper == null){
            dbHelper = new DbHelper(this, "Ln5In12Analysis.db", null, 1);
        }
        return dbHelper;
    }
}