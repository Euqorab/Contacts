package com.example.liumx.contacts;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Euqorab on 2019/6/23.
 */

public class MyAdapter extends SimpleAdapter {
    private Context mContext;
    private ArrayList<Map<String, Object>> data;
    private int resource;
    private String[] from;
    private int[] to;
    private boolean checkEnable = false;
    private int[] checkData;
    private boolean allSelected = false;
    private int UNSET = -1, UNCHECKED = 0, CHECKED = 1;
    private View.OnClickListener iconOnClickListener;

    public class ViewHolder{
        ImageView icon;
        TextView title;
        TextView subtitle;
        ImageView call;
        TextView type;
        CheckBox checkBox;
    }

    public MyAdapter(Context mContext, ArrayList<Map<String, Object>> data,
                     int resource, String[] from, int[] to) {
        super(mContext, data, resource, from, to);
        this.mContext = mContext;
        this.data = data;
        this.resource = resource;
        this.from = from;
        this.to = to;
        this.checkData = new int[data.size()];
        for (int i = 0; i < checkData.length; i++) {
            checkData[i] = UNSET;
        }
    }

    public void setIconOnClickListener(View.OnClickListener listener) {
        iconOnClickListener = listener;
    }

    public void update() {
        notifyDataSetChanged();
    }

    public void selectAll() {
        for (int i = 0; i < checkData.length; i++) {
            checkData[i] = allSelected ? UNCHECKED : CHECKED;
        }
        allSelected = !allSelected;
        notifyDataSetChanged();
    }

    public void setCheckEnable(boolean checkEnable) {
        this.checkEnable = checkEnable;
        notifyDataSetChanged();
    }

    public void setCheck(int position) {
        if (checkData[position] == UNSET) {
            checkData[position] = CHECKED;
        }
        else {
            checkData[position] = checkData[position] == UNCHECKED ? CHECKED : UNCHECKED;
        }
        notifyDataSetChanged();
    }

    public ArrayList<Integer> getCheckedPosition() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < checkData.length; i++) {
            if (checkData[i] == CHECKED) {
                list.add(i);
            }
        }
        return list;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        MyAdapter.ViewHolder viewHolder;
        if (convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(resource, parent, false);
            viewHolder = new MyAdapter.ViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(to[0]);
            viewHolder.title = (TextView) convertView.findViewById(to[1]);
            viewHolder.subtitle = (TextView) convertView.findViewById(to[2]);
            viewHolder.call = (ImageView) convertView.findViewById(R.id.call);
            viewHolder.type = (TextView) convertView.findViewById(R.id.type);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.check_box);
        }
        else {
            viewHolder = (MyAdapter.ViewHolder) convertView.getTag();
        }

        convertView.setTag(viewHolder);

        if (String.valueOf(data.get(position).get(from[0])).equals("-1"))
            viewHolder.icon.setVisibility(View.GONE);
        else {
            viewHolder.icon.setImageResource((int) data.get(position).get(from[0]));
            viewHolder.icon.setVisibility(View.VISIBLE);
        }
        viewHolder.title.setText(String.valueOf(data.get(position).get(from[1])));
        viewHolder.subtitle.setText(String.valueOf(data.get(position).get(from[2])));

        if ((boolean) data.get(position).get("showCallIcon")) {
            viewHolder.call.setVisibility(View.VISIBLE);
            viewHolder.call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + data.get(position).get(from[1])));
                    mContext.startActivity(intent);
                }
            });
        }
        if ((boolean) data.get(position).get("showType")) {
            viewHolder.type.setVisibility(View.VISIBLE);
            String type = String.valueOf(data.get(position).get("type"));
            viewHolder.type.setText(type);
            if (type.equals("拒接") || type.equals("未接通"))
                viewHolder.title.setTextColor(convertView.getResources().getColor(R.color.colorAccent));
        }
        if (checkEnable) {
            viewHolder.checkBox.setEnabled(true);
            viewHolder.checkBox.setClickable(false);
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            if (checkData[position] != UNSET) {
                viewHolder.checkBox.setChecked(checkData[position] == CHECKED);
            }
            else {
                viewHolder.checkBox.setChecked(false);
            }
        }
        return convertView;
    }
}

