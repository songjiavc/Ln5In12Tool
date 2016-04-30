package com.baiylin.songjia.ln5in12tool.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by songjia on 16-4-19.
 */
public class DbHelper extends SQLiteOpenHelper {
    //创建语句
    public static final String CREATE_BASE_TABLE = "CREATE TABLE BASE(" +
            "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "ISSUE_NUMBER TEXT," +
            "NO1 INTEGER," +
            "NO2 INTEGER," +
            "NO3 INTEGER," +
            "NO4 INTEGER," +
            "NO5 INTEGER)";

    //上下文内容
    private Context mContext;

    public DbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,int version){
        super(context,name,factory,version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_BASE_TABLE);
      //  Toast.makeText(mContext, "表建立成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        //UNDO
        //db.execSQL(CREATE_BASE_1_TABLE);
    }
}
