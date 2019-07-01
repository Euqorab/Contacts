package com.example.liumx.contacts;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TimePicker;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Euqorab on 2019/5/28.
 */

public class PrefActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ListView listView;
    private ArrayList<Map<String, Object>> list;
    private int viewPosition = 0;
    private ContactDb db;
    private PrefAdapter adapter;
    private DialogHandler dialogHandler;
    private int hour = 10, min = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle("设置");

        db = new ContactDb(this);
        dialogHandler = new DialogHandler(this);

        listView = (ListView) findViewById(R.id.listView);
        list = new ArrayList<>();
        list.add(buildItem("通话统计展示时段", "", "", "", "全部"));
        list.add(buildItem("", "", "", "", ""));
        list.add(buildItem("开启免打扰", "拦截非白名单内的来电", "do_not_disturb", "", ""));
        list.add(buildItem("设定拦截时间", "", "set_time", "", ""));
        list.add(buildItem("开始时间", "", "", "start_time", "10:00"));
        list.add(buildItem("结束时间", "", "", "end_time", "次日 07:00"));
        adapter = new PrefAdapter(this, list, R.layout.pref_list_item,
                null, null);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                viewPosition = position;
                if (position == 0) {
                    View root = getLayoutInflater().inflate(R.layout.dialog_list, null);
                    ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                    final String[] listItems = {"全部", "一周内", "一个月内", "半年内", "一年内"};
                    for (int i = 0; i < listItems.length; i++) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("text", listItems[i]);
                        list.add(map);
                    }
                    dialogHandler.showListWindow(root, list, new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            int[] days = {-1, 7, 30, 180, 365};
                            ContentValues cv = new ContentValues();
                            cv.put("data", days[position]);
                            db.update("pref", cv, "setting_item=?", new String[]{"days_of_call_log"});
                            adapter.update();
                            dialogHandler.getDialog().dismiss();
                        }
                    });
                }
                else {
                    View root = getLayoutInflater().inflate(R.layout.dialog_time, null);
                    Log.e("root", String.valueOf(root == null));
                    dialogHandler.showTimePickerWindow(root, hour, min, new TimePicker.OnTimeChangedListener() {
                                @Override
                                public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                                    Cursor cursor = db.query("pref", null, "setting_item IN (?,?)",
                                            new String[]{"start_time", "end_time"}, "setting_item desc");
                                    if (cursor.moveToNext()) {
                                        Log.i("============+++++", "===");
                                        String start_time, end_time;
                                        start_time = cursor.getString(1);
                                        cursor.moveToNext();
                                        end_time = cursor.getString(1);
                                        if (list.get(viewPosition).get("set_time") == "start_time") {
                                            hour = Integer.valueOf(start_time.substring(0, 2));
                                            min = Integer.valueOf(start_time.substring(3, 5));
                                        } else {
                                            hour = Integer.valueOf(end_time.substring(0, 2));
                                            min = Integer.valueOf(end_time.substring(3, 5));
                                        }
                                    }
                                    hour = hourOfDay;
                                    min = minute;
                                }
                            },
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ContentValues cv = new ContentValues();
                                    String setting_item = String.valueOf(list.get(viewPosition).get("set_time"));
                                    cv.put("data", dataLong(hour) + ":" + dataLong(min));
                                    db.update("pref", cv, "setting_item=?", new String[]{setting_item});
                                    adapter.update();
                                    dialogHandler.getDialog().dismiss();
                                }
                            });
                }
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    public String dataLong(int num) {
        return num < 10 ? "0" + num : String.valueOf(num);
    }

    public Map<String, Object> buildItem(String title, String subtitle, String aSwitch,
                                         String timeTag, String tag) {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("subtitle", subtitle);
        map.put("switch", aSwitch);
        map.put("set_time", timeTag);
        map.put("tag", tag);
        return map;
    }
}
