package com.example.liumx.contacts;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Euqorab on 2019/6/27.
 */

public class MultiDeleteActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private Toolbar toolbar;
    private ContentResolver resolver;
    private ListView listView;
    private MyAdapter adapter;
    private ArrayList<ContactRec> contacts;
    private DialogHandler dialogHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("未选择");

        dialogHandler = new DialogHandler(this);

        Uri uri = Uri.parse("content://com.android.contacts/contacts");
        resolver = this.getContentResolver();

        listView = (ListView) findViewById(R.id.listView);
        contacts = getContacts();
        showLog();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_multi_delete, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = getIntent();
                setResult(919, intent);
                finish();
                break;

            case R.id.select_all:
                adapter.selectAll();
                break;

            case R.id.delete:
                if (adapter.getCheckedPosition().size() > 0) {
                    View root = getLayoutInflater().inflate(R.layout.dialog_bottom, null);
                    ContactDb db = new ContactDb(this);
                    Cursor cursor = db.query("pref", null, "setting_item=?",
                            new String[]{"days_of_call_log"}, null);
                    int[] days = {-1, 7, 30, 180, 365};
                    final String[] dayStr = {"全部", "一周内", "一个月内", "半年内", "一年内"};
                    int pos = 0;
                    if (cursor.moveToNext())
                        for (; days[pos] != Integer.valueOf(cursor.getString(1)); pos++);

                    dialogHandler.showBottomWindow(root,
                            "是否删除" + adapter.getCheckedPosition().size() + "项通话记录",
                            dayStr[pos] + "已选择的通话记录将从本机删除。是否删除？",
                            "我已阅读并了解",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ArrayList<Integer> checkedPositon = adapter.getCheckedPosition();
                                    for (int i = 0; i < checkedPositon.size(); i++) {
                                        int pos = checkedPositon.get(i);
                                        Log.e("num", contacts.get(pos).getPhone());
                                        ArrayList<Map<String, String>> callLog = contacts.get(pos).getCallLog();
                                        Log.i("size====", callLog.size() + "");
                                        for (int j = 0; j < callLog.size(); j++) {
                                            Log.i("+++++++++", callLog.get(j).get("date"));
                                            deleteCallLog(contacts.get(pos).getPhone(), callLog.get(j).get("datelong"));
                                        }
                                    }
                                    contacts = getContacts();
                                    showLog();
                                    dialogHandler.getDialog().dismiss();
                                }
                            });
                }
                break;
        }
        return true;
    }

    public ArrayList<ContactRec> getContacts() {
        CallInfoLog callInfoLog = new CallInfoLog(resolver);
        ArrayList<ArrayList<Map<String, String>>> callLog = callInfoLog.getTotalCallLog();
        ArrayList<ContactRec> contacts = new ArrayList<>();
        for (int i = 0; i < callLog.size(); i++) {
            Log.i("test", i + "");
            ContactRec contact = new ContactRec();
            contact.setName(callLog.get(i).get(0).get("name"));
            contact.setPhone(callLog.get(i).get(0).get("phone"));
            contact.setCallLog(callLog.get(i));
            contacts.add(contact);
        }
        return contacts;
    }

    public void showLog() {
        final ArrayList<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < contacts.size(); i++) {
            ArrayList<Map<String, String>> callLog = contacts.get(i).getCallLog();
            ContactDb db = new ContactDb(this);
            Cursor cursor = db.query("pref", null, "setting_item=?", new String[]{"days_of_call_log"}, null);
            int days = callLog.size();
            if (cursor.moveToNext())
                days = Integer.valueOf(cursor.getString(1));

            Date targetDate = Calendar.getInstance().getTime();
            targetDate = new Date(targetDate.getTime() - (long) days * 24 * 60 * 60 * 1000);
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");

            int totalDur = 0;
            for (int j = 0; j < callLog.size(); j++) {
                Log.e("date", callLog.get(j).get("date"));
                Log.e("tgdate", sf.format(targetDate));
                try {
                    Date callDate = sf.parse(callLog.get(j).get("date"));
                    Log.e(contacts.get(i).getName(), callDate + "====" + targetDate);
                    if (callDate.getTime() >= targetDate.getTime() || days == -1) {
                        totalDur += Integer.valueOf(callLog.get(j).get("duration"));
                    }
                    else {
                        callLog.remove(j);
                        j--;
                    }
                } catch (Exception e) {}
            }

            if (callLog.size() > 0) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("icon", "-1");
                String title = contacts.get(i).getName();

                if (callLog.size() > 1) {
                    title += "(" + callLog.size() + ")";
                }
                map.put("title", title);
                map.put("subtitle", "最近通话：" + callLog.get(0).get("dayStr")
                        + " " + callLog.get(0).get("time"));
                map.put("showCallIcon", false);
                map.put("showType", true);

                String typeStr;
                if (totalDur == 0)
                    typeStr = "未接通";
                else
                    typeStr = "共" + (totalDur < 60 ? totalDur + "秒" : (totalDur / 60) + "分钟");

                map.put("type", typeStr);
                list.add(map);
            }
        }

        adapter = new MyAdapter(this, list, R.layout.contact_info_list_item,
                new String[]{"icon", "title", "subtitle"}, new int[]{R.id.icon, R.id.title, R.id.subtitle});
        adapter.setCheckEnable(true);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setCheck(position);
                if (adapter.getCheckedPosition().size() > 0) {
                    actionBar.setTitle("已选择" + adapter.getCheckedPosition().size() + "项");
                }
                else {
                    actionBar.setTitle("未选择");
                }
                Log.i("click", "true");
            }
        });
    }
//
//    public void deleteCallLog(String phone)
//    {
//        Uri uri = CallLog.Calls.CONTENT_URI;
//        String where = CallLog.Calls.NUMBER + "=?";
//        String[] whereArgs = new String[]{phone};
//        resolver.delete(uri, where, whereArgs);
//    }
//
    public void deleteCallLog(String number, String deleteDate)
    {
        Uri uri = CallLog.Calls.CONTENT_URI;
        String[] project = {CallLog.Calls.CACHED_NAME// 通话记录的联系人
                , CallLog.Calls.NUMBER// 通话记录的电话号码
                , CallLog.Calls.DATE// 通话记录的日期
                , CallLog.Calls.DURATION// 通话时长
                , CallLog.Calls.TYPE};// 通话类型}
        String where = CallLog.Calls.NUMBER + "=?";
        String[] whereArgs = new String[]{number};
        String orderBy = CallLog.Calls.DATE + " desc";
        Cursor cursor = resolver.query(uri, project, where, whereArgs, orderBy);

        Log.e("============", deleteDate);
        if (cursor.getCount() > 0) {
            resolver.delete(uri, CallLog.Calls.DATE + "=?", new String[]{deleteDate});
        }
    }

}


