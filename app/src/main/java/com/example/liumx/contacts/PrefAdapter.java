package com.example.liumx.contacts;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Euqorab on 2019/6/26.
 */

public class PrefAdapter extends SimpleAdapter {
    private Context mContext;
    private ContactDb db;
    private ArrayList<Map<String, Object>> data;
    private int resource;
    private String[] from;
    private int[] to;
    private boolean modeEnable = false;
    private boolean timeEnable = false;

    public class ViewHolder{
        TextView title;
        TextView subtitle;
        Switch aSwitch;
        TextView tag;
    }

    public PrefAdapter(Context mContext, ArrayList<Map<String, Object>> data,
                              int resource, String[] from, int[] to) {
        super(mContext, data, resource, from, to);
        this.mContext = mContext;
        this.data = data;
        this.resource = resource;
        this.from = from;
        this.to = to;
        db = new ContactDb(mContext);
        Cursor cursor = db.query("pref", new String[]{"do_not_disturb", "set_time"},
                null, null, null);
        if (cursor.moveToNext()) {
            modeEnable = cursor.getInt(0) == 1;
            timeEnable = cursor.getInt(1) == 1;
        }
        Log.i("timeenable", String.valueOf(timeEnable));
    }

    @Override
    public boolean isEnabled(int position) {
        if (!modeEnable) {
            return false;
        }
        else if (timeEnable) {
            return (data.get(position).get("switch") == "");
        }
        return false;
    }

    public void update() {
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        PrefAdapter.ViewHolder viewHolder;
        if (convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(resource, parent, false);
            viewHolder = new PrefAdapter.ViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
            viewHolder.subtitle = (TextView) convertView.findViewById(R.id.subtitle);
            viewHolder.aSwitch = (Switch) convertView.findViewById(R.id.switch1);
            viewHolder.tag = (TextView) convertView.findViewById(R.id.tag);
        }
        else {
            viewHolder = (PrefAdapter.ViewHolder) convertView.getTag();
        }

        convertView.setTag(viewHolder);

        viewHolder.title.setText(String.valueOf(data.get(position).get("title")));

        if (data.get(position).get("subtitle") != "") {
            viewHolder.subtitle.setVisibility(View.VISIBLE);
            viewHolder.subtitle.setText(String.valueOf(data.get(position).get("subtitle")));
        }

        if (data.get(position).get("switch") != "") {
            viewHolder.aSwitch.setVisibility(View.VISIBLE);

            if (data.get(position).get("switch") == "set_time") {
                viewHolder.title.setTextColor(modeEnable ?
                        convertView.getResources().getColor(R.color.colorText) :
                        convertView.getResources().getColor(R.color.gray));
            }

            if (data.get(position).get("switch") == "set_time") {
                if (!modeEnable) {
                    viewHolder.aSwitch.setEnabled(false);
                    viewHolder.aSwitch.setChecked(false);
                }
                else {
                    viewHolder.aSwitch.setEnabled(true);
                }
            }
            else {
                viewHolder.aSwitch.setChecked(modeEnable);
            }

            viewHolder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ContentValues cv = new ContentValues();
                    Log.e("test", String.valueOf(timeEnable));
                    cv.put(String.valueOf(data.get(position).get("switch")), isChecked ? 1 : 0);
                    db.update("pref", cv, "name=?", new String[]{"DoNotDisturb"});
                    if (data.get(position).get("switch") == "do_not_disturb") {
                        modeEnable = !modeEnable;
                    }
                    if (data.get(position).get("switch") == "set_time") {
                        timeEnable = !timeEnable;
                    }
                    Log.e("test", String.valueOf(timeEnable));
                    update();
                }
            });
        }

        if (data.get(position).get("set_time") != "") {
            viewHolder.title.setTextColor(isEnabled(position) ?
                    convertView.getResources().getColor(R.color.colorText) :
                    convertView.getResources().getColor(R.color.gray));
            Cursor cursor = db.query("pref", new String[]{"start_time", "end_time"},
                    null, null, null);
            String timeTag;
            if (cursor.moveToFirst()) {
                if (data.get(position).get("set_time") == "start_time")
                    timeTag = cursor.getString(0);
                else {
                    timeTag = cursor.getString(1).compareTo(cursor.getString(0)) <= 0 ?
                            "次日 " : "";
                    timeTag += cursor.getString(1);
                }
            }
            else {
                timeTag = data.get(position).get("set_time") == "start_time" ?
                        "10:00" : "次日 07:00";
            }
            viewHolder.tag.setVisibility(View.VISIBLE);
            viewHolder.tag.setText(timeTag);
        }
        else if (data.get(position).get("tag") != "") {
            viewHolder.tag.setVisibility(View.VISIBLE);
            viewHolder.tag.setText(String.valueOf(data.get(position).get("tag")));
        }

        return convertView;
    }
}

