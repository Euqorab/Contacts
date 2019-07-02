package com.example.liumx.contacts;

import android.content.ContentResolver;
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
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Random;

/**
 * Created by Euqorab on 2019/6/26.
 */

public class PrefAdapter extends SimpleAdapter {
    private Context mContext;
    private ContactDb db;
    private ContentResolver resolver;
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
        ImageView divider;
        LinearLayout prefItem;
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
        resolver = mContext.getContentResolver();
        Cursor cursor = db.query("pref", null, "setting_item IN (?,?)",
                new String[]{"do_not_disturb", "set_time"}, null);
        //Log.i("=============", cursor.getCount() + "");
        if (cursor.moveToNext()) {
            Log.i("=========", cursor.getString(1) + "");
            modeEnable = cursor.getString(1).equals("true");
            cursor.moveToNext();
            Log.i("=========", cursor.getString(1) + "");
            timeEnable = cursor.getString(1).equals("true");
        }
        //Log.i("timeenable", String.valueOf(timeEnable));
    }

    @Override
    public boolean isEnabled(int position) {
        if (position == 0) {
            return true;
        }
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
            viewHolder.divider = (ImageView) convertView.findViewById(R.id.item_divider);
            viewHolder.prefItem = (LinearLayout) convertView.findViewById(R.id.pref_item);
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

        if (position == 0) {
            Cursor cursor = db.query("pref", null, "setting_item=?",
                    new String[]{"days_of_call_log"}, null);
            if (cursor.moveToNext()) {
                String[] dayStr = {"全部", "一周内", "一个月内", "半年内", "一年内"};
                int days = Integer.valueOf(cursor.getString(1));
                int[] dayInt = {-1, 7, 30, 180, 365};
                for (int i = 0; i < dayInt.length; i++) {
                    if (dayInt[i] == days)
                        viewHolder.tag.setText(dayStr[i]);
                }
                viewHolder.tag.setVisibility(View.VISIBLE);
            }
        }

        else if (position == 2) {
            viewHolder.aSwitch.setVisibility(View.VISIBLE);
            final Cursor cursor = db.query("pref", null, "setting_item=?",
                    new String[]{"birthday_notification"}, null);
            if (cursor.moveToNext())
                viewHolder.aSwitch.setChecked(cursor.getString(1).equals("true"));

            viewHolder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ContentValues cv = new ContentValues();
                    cv.put("data", isChecked ? "true" : "false");
                    db.update("pref", cv, "setting_item=?",
                            new String[]{String.valueOf(data.get(position).get("switch"))});

                    if (isChecked) {
                        ArrayList<ContactRec> contacts = getContactsWithBirthday();
                        int count = 0;
                        for (int i = 0; i < contacts.size(); i++) {
                            if (!contacts.get(i).getBirthday().equals("")) {
                                count++;
                                // Todo Add Notifications
                                Random ra = new Random();
                                int raw_id = ra.nextInt(9999999) + 1; // 这里要更改一下，不能使用currentTimeMills作为raw_id
                                Log.i("====Write_time====", contacts.get(i).getBirthday());
                                String notify_date = getBirthday2(contacts.get(i).getBirthday()) + "8时0分";
//                                String notify_date = "2019年7月2日22时0分";

                                Date date = null;
                                try {
                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日HH时mm分");
                                    date = formatter.parse(notify_date);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
//                                Log.i("=======date=========:", String.valueOf(date));
                                cv = new ContentValues();
                                cv.put("note", "生日提醒");
                                cv.put("name", contacts.get(i).getName());
                                cv.put("phone", contacts.get(i).getPhone());
                                cv.put("date_time", notify_date);
                                cv.put("raw_id", raw_id); // 待定
                                db.insert("notify_list", cv);
                                Intent intent = new Intent (mContext, PushNotification.class);
                                intent.putExtra("delayTime", (int)(date.getTime() - System.currentTimeMillis()));
                                intent.putExtra("contentTitle", "通话提醒");
                                intent.putExtra("phone", contacts.get(i).getPhone());
                                intent.putExtra("subTitle", contacts.get(i).getName());
                                intent.putExtra("contentText", "生日提醒");
                                intent.putExtra("notificationId", raw_id);
                                mContext.startService(intent);
                            }
                        }
                        Log.e("birthday count++++++", count + "");
                    }
                    else {
                        db.delete("notify_list", "note=?", new String[]{"生日提醒"});
                        // Todo Delete Notifications

                        // delete service
                        Intent intent_finish = new Intent (mContext, PushNotification.class);
                        mContext.stopService(intent_finish);

                        // 数据库查询
                        Cursor cursor = db.query("notify_list", null, null, null, null);
                        while (cursor.moveToNext()) {
                            Intent intent = new Intent (mContext, PushNotification.class);
                            String notify_date = cursor.getString(cursor.getColumnIndex("date_time"));
                            Date date = null;
                            Log.i("=======printtt:", notify_date);
                            try {
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日HH时mm分");
                                date = simpleDateFormat.parse(notify_date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            intent.putExtra("delayTime", (int)(date.getTime() - System.currentTimeMillis()));
                            intent.putExtra("contentTitle", "通话提醒");
                            intent.putExtra("phone", cursor.getString(cursor.getColumnIndex("phone")));
                            intent.putExtra("subTitle", cursor.getString(cursor.getColumnIndex("name")));
                            intent.putExtra("contentText", cursor.getString(cursor.getColumnIndex("note")));
                            intent.putExtra("notificationId", cursor.getInt(cursor.getColumnIndex("raw_id")));
                            mContext.startService(intent);
                        }
                    }
                }
            });
        }

        else if (position == 4 || position == 5) {
            viewHolder.aSwitch.setVisibility(View.VISIBLE);

            if (data.get(position).get("switch") == "set_time") {
                viewHolder.title.setTextColor(modeEnable ?
                        convertView.getResources().getColor(R.color.colorText) :
                        convertView.getResources().getColor(R.color.gray));

                if (!modeEnable) {
                    viewHolder.aSwitch.setEnabled(false);
                    viewHolder.aSwitch.setChecked(false);
                }
                else {
                    viewHolder.aSwitch.setEnabled(true);
                    viewHolder.aSwitch.setChecked(timeEnable);
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
                    cv.put("data", isChecked ? "true" : "false");
                    db.update("pref", cv, "setting_item=?",
                            new String[]{String.valueOf(data.get(position).get("switch"))});
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

        else if (position == 6 || position == 7) {
            viewHolder.title.setTextColor(isEnabled(position) ?
                    convertView.getResources().getColor(R.color.colorText) :
                    convertView.getResources().getColor(R.color.gray));
            Cursor cursor = db.query("pref", null, "setting_item IN (?,?)",
                    new String[]{"start_time", "end_time"}, "setting_item desc");
            String timeTag;
            if (cursor.moveToFirst()) {
                String start_time = cursor.getString(1);
                cursor.moveToNext();
                String end_time = cursor.getString(1);
                if (data.get(position).get("set_time") == "start_time")
                    timeTag = start_time;
                else {
                    timeTag = end_time.compareTo(start_time) <= 0 ?
                            "次日 " : "";
                    timeTag += end_time;
                }
            }
            else {
                timeTag = data.get(position).get("set_time") == "start_time" ?
                        "22:00" : "次日 07:00";
            }
            viewHolder.tag.setVisibility(View.VISIBLE);
            viewHolder.tag.setText(timeTag);
        }
        else {
            viewHolder.divider.setVisibility(View.VISIBLE);
            viewHolder.prefItem.setVisibility(View.GONE);
        }

        return convertView;
    }
    public String getBirthday2(String bornDate) {
        String newDate = "";
        SimpleDateFormat sf1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sf2 = new SimpleDateFormat("yyyy年MM月dd日");
        Calendar cal = Calendar.getInstance();
        int yearNow = cal.get(Calendar.YEAR);
        Log.i("Before: ", bornDate);
        try {
            newDate = sf2.format(sf1.parse(bornDate));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String nowStr = String.valueOf(yearNow) + newDate.substring(4);
        String nextStr = String.valueOf(yearNow + 1) + newDate.substring(4);
        Log.i("Error: ", nowStr);

        Date nowDate = null;
        try {
            nowDate = sf2.parse(nowStr);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (nowDate.getTime() <= System.currentTimeMillis()) return nextStr;
        else return nowStr;
    }


    public ArrayList<ContactRec> getContactsWithBirthday() {
        ArrayList<ContactRec> newContacts = new ArrayList<>();

        Uri uri = Uri.parse("content://com.android.contacts/contacts");
        Cursor cursor = resolver.query(uri, new String[]{"name_raw_contact_id"}, null, null, "name_raw_contact_id");
        uri = Uri.parse("content://com.android.contacts/data");
        Cursor cursor1 = resolver.query(uri, new String[]{"raw_contact_id", "mimetype", "data1", "data2"}, null, null, "raw_contact_id");

        if (cursor1.getCount() > 0)
            cursor1.moveToNext();

        while (cursor.moveToNext()) {
            ContactRec contact = new ContactRec();
            //获得id并且在data中寻找数据
            int id = cursor.getInt(0);
            contact.setId(id);
            //Log.i("id", id + " ");

            if (cursor1.getString(1).equals("vnd.android.cursor.item/name")) {
                contact.setName(cursor1.getString(2));
            }
            if (cursor1.getString(1).equals("vnd.android.cursor.item/phone_v2")) {
                contact.setPhone(cursor1.getString(2));
            }
            if (cursor1.getString(1).equals("vnd.android.cursor.item/contact_event") &&
                    cursor1.getString(3).equals("3")) {
                contact.setBirthday(cursor1.getString(2));
            }

            while (cursor1.moveToNext() && cursor1.getInt(0) == id) {
//                Log.e("======", cursor1.getString(0));
                if (cursor1.getString(1).equals("vnd.android.cursor.item/name")) {
                    contact.setName(cursor1.getString(2));
                }
                if (cursor1.getString(1).equals("vnd.android.cursor.item/phone_v2")) {
                    contact.setPhone(cursor1.getString(2));
                }
                if (cursor1.getString(1).equals("vnd.android.cursor.item/contact_event") &&
                        cursor1.getString(3).equals("3")) {
                    contact.setBirthday(cursor1.getString(2));
                }
            }

            if(!contact.getName().isEmpty() && !contact.getBirthday().equals("")){
                newContacts.add(contact);
            }
        }
        cursor.close();

        // 按字典序或拼音字典序排序
        Comparator<ContactRec> comparator = new Comparator<ContactRec>() {
            public int compare(ContactRec o1, ContactRec o2) {
                PinyinUtils pinyinUtils = new PinyinUtils();
                String s1, s2;
                s1 = pinyinUtils.isChinese(o1.getName()) ?
                        pinyinUtils.getSelling(o1.getName()) : o1.getName();
                s2 = pinyinUtils.isChinese(o2.getName()) ?
                        pinyinUtils.getSelling(o2.getName()) : o2.getName();
                return s1.compareToIgnoreCase(s2);
            }
        };
        Collections.sort(newContacts, comparator);
        return newContacts;
    }
}

