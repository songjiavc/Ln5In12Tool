package com.baiylin.songjia.ln5in12tool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.baiylin.songjia.ln5in12tool.R;
import com.baiylin.songjia.ln5in12tool.bean.GroupBean;

import java.util.List;

/**
 * Created by songjia on 16-4-21.
 */
public class GroupAdapter extends ArrayAdapter<GroupBean> {

    private int resourceId;

    public GroupAdapter(Context context, int textViewResourceId,List<GroupBean> objects) {
        super(context, textViewResourceId,objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        GroupBean group = getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,null);
            viewHolder = new ViewHolder();
            viewHolder.group = (TextView) view.findViewById(R.id.group);
            viewHolder.today = (TextView) view.findViewById(R.id.today);
            viewHolder.yesterday = (TextView) view.findViewById(R.id.yesterday);
            viewHolder.lastTwoDay = (TextView) view.findViewById(R.id.lastTwoDay);
            viewHolder.lastThreeDay = (TextView) view.findViewById(R.id.lastThreeDay);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }
        viewHolder.group.setText(group.getGroup());
        viewHolder.today.setText(Integer.toString(group.getTodayCount()));
        viewHolder.yesterday.setText(Integer.toString(group.getYestedayCount()));
        viewHolder.lastTwoDay.setText(Integer.toString(group.getLastTwoDayCount()));
        viewHolder.lastThreeDay.setText(Integer.toString(group.getLastThreeDayCount()));
        return view;
    }

    class ViewHolder{

        TextView group;

        TextView today;

        TextView yesterday;

        TextView lastTwoDay;

        TextView lastThreeDay;
    }

}
