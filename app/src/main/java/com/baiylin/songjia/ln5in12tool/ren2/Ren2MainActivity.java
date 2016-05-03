package com.baiylin.songjia.ln5in12tool.ren2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.baiylin.songjia.ln5in12tool.R;
import com.baiylin.songjia.ln5in12tool.adapter.GroupAdapter;
import com.baiylin.songjia.ln5in12tool.bean.GroupBean;
import com.baiylin.songjia.ln5in12tool.bean.Ln5In12Bean;
import com.baiylin.songjia.ln5in12tool.db.DbHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Ren2MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    DbHelper dbHelper;

    private DataReceiver dataReceiver;

    List<GroupBean> groupList = new ArrayList<>();

    GroupAdapter adapter;

    int style = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ren2_main);
        Intent intent = getIntent();
        style = intent.getIntExtra("style",2);
        initData(style);
        adapter = new GroupAdapter(Ren2MainActivity.this,R.layout.activity_ren2_item,groupList);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        dataReceiver = new DataReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.baiylin.dataReceiver");
        registerReceiver(dataReceiver, intentFilter);//注册Broadcast Receiver
    }

    private void initData(int style){
        getDataListFromSqlite(style);
    }

    private void getDataListFromSqlite(int style){
        groupList.clear();
        this.getShowList(style);  //创建列表list
        dbHelper = new DbHelper(this, "Ln5In12Analysis.db", null, 1);
        //指定库
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query("BASE",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            int z = 0;
            do{
                int no1 = cursor.getInt(cursor.getColumnIndex("NO1"));
                int no2 = cursor.getInt(cursor.getColumnIndex("NO2"));
                int no3 = cursor.getInt(cursor.getColumnIndex("NO3"));
                int no4 = cursor.getInt(cursor.getColumnIndex("NO4"));
                int no5 = cursor.getInt(cursor.getColumnIndex("NO5"));
                int[] arr = {no1,no2,no3,no4,no5};
                //获取任二所有组合
                onlyOneAnalysis(arr,z,style);
                z++;
            }while(cursor.moveToNext());
        }else{

        }
        cursor.close();
    }
    //单个汇总出现次数方法
    public void onlyOneAnalysis(int[] temp,int z,int style){
        List<String> lottoryGroupList = getGroupByIntArr(temp,style);
        this.calc(groupList,lottoryGroupList,z);
    }

    private List<String> getGroupByIntArr(int[] arr,int style){
        List<String> groupList = null;
        Arrays.sort(arr);  //对数组排序这样获取有规则的组合
        if(arr.length > 0){
            groupList = new ArrayList<>();
            for(int i = 0;i < arr.length-1;i++){
                for(int j = i+1;j < arr.length;j++) {
                    if (style >= 3){
                        for(int z = j+1;z < arr.length;z++){
                            if(style >= 4) {
                                for (int p = z + 1; p < arr.length; p++) {
                                    if(style >= 5) {
                                        for (int q = p + 1; q < arr.length; q++) {
                                            groupList.add(arr[i] + ":" + arr[j] + ":" + arr[z] + ":" + arr[p] + ":" + arr[q]);
                                        }
                                    }else{
                                        groupList.add(arr[i] + ":" + arr[j] + ":" + arr[z] + ":" + arr[p]);
                                    }
                                }
                            }else{
                                groupList.add(arr[i] + ":" + arr[j] + ":" + arr[z]);
                            }
                        }
                    } else {
                        groupList.add(arr[i] + ":" + arr[j]);
                    }
                }
            }
        }
        return groupList;
    }

    private void getShowList(int style){
        int[] arr = {1,2,3,4,5,6,7,8,9,10,11,12};
        List<String> allGroupList = this.getGroupByIntArr(arr,style);
        for(String group : allGroupList){
            GroupBean groupBean = new GroupBean();
            groupBean.setGroup(group);
            groupList.add(groupBean);
        }
    }
    //统计组合出现次数
    private void calc(List<GroupBean> showList, List<String> groupList, int z){
        for(GroupBean groupBean : showList) {
            for (String group : groupList) {
                if (groupBean.getGroup().equals(group)){
                    if(z < 80){
                        groupBean.setLastThreeDayCount(groupBean.getLastThreeDayCount()+1);
                    }else if(z < 160){
                        groupBean.setLastTwoDayCount(groupBean.getLastTwoDayCount()+1);
                    }else if(z < 240){
                        groupBean.setYestedayCount(groupBean.getYestedayCount()+1);
                    }else{
                        groupBean.setTodayCount(groupBean.getTodayCount()+1);
                    }
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        System.out.println(adapterView.getId());
        TextView text = (TextView) view.findViewById(R.id.group);
        System.out.println(text.getText());
        System.out.println(groupList.get(i).getGroup());
        System.out.println(view.getId());
        System.out.println(i+"");
        System.out.println(l+"");
    }

    private class DataReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            //更新listview
            /*
             *
             * 如果传递的是List<Object>,可以把list强转成Serializable类型,而且object类型也必须实现了Serializable接口
               Intent.putExtras(key, (Serializable)list)
               接收
               (List<YourObject>)getIntent().getSerializable(key)
             */
            Ln5In12Bean ln5In12Bean = (Ln5In12Bean)intent.getSerializableExtra("Ln5In12Bean");
            int[] paramArr = {ln5In12Bean.getNo1(),ln5In12Bean.getNo2(),ln5In12Bean.getNo3(),ln5In12Bean.getNo4(),ln5In12Bean.getNo5()};
            onlyOneAnalysis(paramArr,1000,style);
            adapter.notifyDataSetChanged();  //重新适配代理
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(dataReceiver);
    }
}
