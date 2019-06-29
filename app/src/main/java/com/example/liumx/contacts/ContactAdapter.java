package com.example.liumx.contacts;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Euqorab on 2019/6/22.
 */

public class ContactAdapter extends SimpleAdapter {
    private Context mContext;
    private ArrayList<Map<String, Object>> data;
    private int resource;
    private String[] from;
    private int[] to;
    private char flag;
    private int offset;
    private int[] tagIndex;
    private boolean[] tagSetDone;
    private int startIndex;

    public class ViewHolder{
        TextView textAlpha;
        ImageView imageView;
        TextView textName;
        TextView textSubtitle;
    }

    public ContactAdapter(Context mContext,     ArrayList<Map<String, Object>> data,
                       int resource, String[] from, int[] to) {
        super(mContext, data, resource, from, to);
        this.mContext = mContext;
        this.data = data;
        this.resource = resource;
        this.from = from;
        this.to = to;
        flag = 'a';
        offset = 0;
        tagIndex = new int[27];
        tagSetDone = new boolean[27];
        for (int i = 0; i < 27; i++) {
            tagIndex[i] = -1;
            tagSetDone[i] = false;
        }
    }



    public int getAlphaPosition(String alpha) {
        int index = alpha == "#" ? 26 : alpha.charAt(0) - 'A';
        while (tagIndex[index] < 0 && index < 27) {
            index++;
        }
        return index == 27 ? data.size() : tagIndex[index];
    }

    protected void setTagIndexs(int startIndex) {
        PinyinUtils pinyinUtils = new PinyinUtils();
        char tmp = 'a';
        for (int i = startIndex; i < data.size(); i++) {
            Map<String, Object> map = data.get(i);
            String str = String.valueOf(map.get(from[1]));
            char firstLetter = pinyinUtils.getFirstLetter(str);
            if (firstLetter != tmp) {
                tmp = firstLetter;
                tagIndex[firstLetter >= 'A' ? firstLetter - 'A' : 26] = i;
            }
        }
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(resource, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.textAlpha = (TextView) convertView.findViewById(R.id.text_alpha);
            viewHolder.imageView = (ImageView) convertView.findViewById(to[0]);
            viewHolder.textName = (TextView) convertView.findViewById(to[1]);
            if (to.length > 2)
                viewHolder.textSubtitle = (TextView) convertView.findViewById(to[2]);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        convertView.setTag(viewHolder);

        PinyinUtils pinyinUtils = new PinyinUtils();
        Map<String, Object> map = data.get(position);
        String str = String.valueOf(map.get(from[1]));

        flag = pinyinUtils.getFirstLetter(str);
        int index = flag >= 'A' ? flag - 'A' : 26;
        if (tagIndex[index] == position) {
            viewHolder.textAlpha.setVisibility(View.VISIBLE);
            viewHolder.textAlpha.setText(String.valueOf(flag));
        }
        else {
            viewHolder.textAlpha.setVisibility(View.GONE);
        }

        viewHolder.imageView.setImageResource((int) map.get(from[0]));
        viewHolder.textName.setText(str);
        if (from.length > 2 && data.get(position).get(from[2]) != "") {
            viewHolder.textSubtitle.setVisibility(View.VISIBLE);
            viewHolder.textSubtitle.setText(String.valueOf(data.get(position).get(from[2])));
        }
        return convertView;
    }
}

