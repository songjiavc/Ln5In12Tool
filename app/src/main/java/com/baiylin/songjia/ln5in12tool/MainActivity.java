package com.baiylin.songjia.ln5in12tool;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baiylin.songjia.ln5in12tool.bean.Ln5In12Bean;
import com.baiylin.songjia.ln5in12tool.bean.ResponseBean;
import com.baiylin.songjia.ln5in12tool.db.DbHelper;
import com.baiylin.songjia.ln5in12tool.receiver.DataReceiver;
import com.baiylin.songjia.ln5in12tool.ren2.Ren2MainActivity;
import com.baiylin.songjia.ln5in12tool.service.GetLottoryData;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private DbHelper dbHelper ;

    private DataReceiver dataReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DbHelper(this, "Ln5In12Analysis.db", null, 1);
        //对按钮进行时间绑定
        Button loadData = (Button) findViewById(R.id.loadData);
        loadData.setOnClickListener(this);
        Button analysisSel2 = (Button) findViewById(R.id.analysisSel2);
        analysisSel2.setOnClickListener(this);
        Button analysisSel3 = (Button) findViewById(R.id.analysisSel3);
        analysisSel3.setOnClickListener(this);
        Button analysisSel4 = (Button) findViewById(R.id.analysisSel4);
        analysisSel4.setOnClickListener(this);
        Button analysisSel5 = (Button) findViewById(R.id.analysisSel5);
        analysisSel5.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        if(view.getId() == R.id.loadData) {
            getUrlMessageFromRemoteService();
            Toast.makeText(MainActivity.this, "data load success", Toast.LENGTH_SHORT).show();
        }else if(view.getId() == R.id.analysisSel2){
            Intent intent = new Intent(MainActivity.this, Ren2MainActivity.class);
            intent.putExtra("style", 2);
            startActivity(intent);
        }else if(view.getId() == R.id.analysisSel3){
            Intent intent = new Intent(MainActivity.this, Ren2MainActivity.class);
            intent.putExtra("style", 3);
            startActivity(intent);
        }else if(view.getId() == R.id.analysisSel4){
            Intent intent = new Intent(MainActivity.this, Ren2MainActivity.class);
            intent.putExtra("style", 4);
            startActivity(intent);
        }else if(view.getId() == R.id.analysisSel5){
            Intent intent = new Intent(MainActivity.this, Ren2MainActivity.class);
            intent.putExtra("style", 5);
            startActivity(intent);
        }else{
            Log.v("MainActivity","未完待续@");
        }
    }

    private void getUrlMessageFromRemoteService(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String urlStr = "http://www.dzzst.cn:1819/webappmgr/lottoryOuter/getLn5In12List.action";
                URL url = null;
                HttpURLConnection connection = null;
                Message mess = new Message();
                try {
                    url = new URL(urlStr);
                    System.out.println(url);
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
                        //插入成功后调用后台每隔十分钟请求一次的获取开奖结果的服务
                        //将需要的数据存放到share中
                        String lastIssueNumber = dataList.get(dataList.size()-1).getIssueNumber();
                        SharedPreferences.Editor editor = getSharedPreferences("data",
                                MODE_PRIVATE).edit();
                        editor.putString("lastIssueNumber",lastIssueNumber);
                        editor.commit();
                        mess.what = 1;
                    }else{
                        //UNDO 数据加载失败！
                        mess.what = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if(connection != null){
                        connection.disconnect();
                    }
                    handler.sendMessage(mess);
                }
            }
        }).start();
    }

    private void insertData2Db(List<Ln5In12Bean> dataList){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
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

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            if (msg.what == 1) {
                Intent intent = new Intent(MainActivity.this,GetLottoryData.class);
                startService(intent);
                Log.d("GetLottoryWork","新记录获取成功！");
            }else{
                Log.d("GetLottoryWork","新记录获取失败！");
            }
        }

    };
}
