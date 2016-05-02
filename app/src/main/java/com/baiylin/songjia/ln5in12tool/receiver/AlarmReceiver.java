package com.baiylin.songjia.ln5in12tool.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.baiylin.songjia.ln5in12tool.service.GetLottoryData;

/**
 * Created by songjia on 16-5-1.
 */
public class AlarmReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent){
        Intent i = new Intent(context,GetLottoryData.class);
        context.startService(i);
    }
}
