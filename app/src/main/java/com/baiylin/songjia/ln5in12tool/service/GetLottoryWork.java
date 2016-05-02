package com.baiylin.songjia.ln5in12tool.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;

import com.baiylin.songjia.ln5in12tool.R;
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
import java.util.TimerTask;

/**
 * Created by songjia on 16-4-30.
 */
public class GetLottoryWork extends TimerTask {

    DbHelper dbHelper;

    private Context context;

    public GetLottoryWork(Context contextParam){
        context = contextParam;
    }

    @Override
    public void run() {
        //undo 执行数据接口访问
        SharedPreferences sharedPreferences = context.getSharedPreferences("data",0);
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
                Message mess = new Message();
                mess.what = 1;
                handler.sendMessage(mess);
                //插入成功后调用后台每隔十分钟请求一次的获取开奖结果的服务
                //将需要的数据存放到share中
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(connection != null){
                connection.disconnect();
            }
        }

    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if (msg.what == 1) {
                //当数据插入成功
                String currentAcitive = getCurrentActivityName(context);
                if(currentAcitive.equals("Ren2MainActivity")){
                  //  ListView listView = (ListView) this.findViewById(R.id.list_view);
                }
                Log.d("GetLottoryWork","新记录获取成功！");
            }
        }

    };

    private void insertData2Db(List<Ln5In12Bean> dataList){
        SQLiteDatabase db = this.getDbHelper().getWritableDatabase();
        db.delete("BASE",null,null);
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

    private String getCurrentActivityName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);


        // get the info from the currently running task
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);


        ComponentName componentInfo = taskInfo.get(0).topActivity;
        return componentInfo.getClassName();
    }

    private DbHelper getDbHelper(){
        if(dbHelper == null){
            dbHelper = new DbHelper(this.context, "Ln5In12Analysis.db", null, 1);
        }
        return dbHelper;
    }
}