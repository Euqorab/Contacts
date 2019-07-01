package com.example.liumx.contacts;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
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
 * Created by Euqorab on 2019/6/21.
 */

public class StatCallLogActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ContentResolver resolver;
    private ListView listView;
    private ArrayList<ContactRec> contacts;
    private DialogHandler dialogHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("通话统计");

        Uri uri = Uri.parse("content://com.android.contacts/contacts");
        resolver = this.getContentResolver();
        dialogHandler = new DialogHandler(this);

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
        getMenuInflater().inflate(R.menu.menu_stat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = getIntent();
                setResult(445, intent);
                finish();
                break;

            case R.id.multi_delete:
                intent = new Intent(this, MultiDeleteActivity.class);
                startActivityForResult(intent, 810);
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 810 && (resultCode == 919 || resultCode == 445)) {
            contacts = getContacts();
            showLog();
        }
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
            Log.e("name", contact.getName());
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

            int totalDur = 0, callLogNum = 0;
            for (int j = 0; j < callLog.size(); j++) {
                Log.e("date", callLog.get(j).get("date"));
                Log.e("tgdate", sf.format(targetDate));
                try {
                    Date callDate = sf.parse(callLog.get(j).get("date"));
                    Log.e(contacts.get(i).getName(), callDate + "====" + targetDate);
                    if (callDate.getTime() >= targetDate.getTime() || days == -1) {
                        totalDur += Integer.valueOf(callLog.get(j).get("duration"));
                        callLogNum++;
                    }
                } catch (Exception e) {}
            }

            if (callLogNum > 0) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("icon", "-1");
                String title = contacts.get(i).getName();

                if (callLogNum > 1) {
                    title += "(" + callLogNum + ")";
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

        final MyAdapter adapter = new MyAdapter(this, list, R.layout.contact_info_list_item,
                new String[]{"icon", "title", "subtitle"}, new int[]{R.id.icon, R.id.title, R.id.subtitle});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("click", "===============");
                Intent intent = new Intent(StatCallLogActivity.this, ContactInfoActivity.class);
                Uri uri = Uri.parse("content://com.android.contacts/contacts");
                Cursor cursor = resolver.query(uri,
                        new String[]{Data._ID},
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "=?",
                        new String[]{contacts.get(position).getName()},
                        null);
                if (cursor.moveToNext()) {
                    intent.putExtra("_id", String.valueOf(cursor.getInt(0)));
                    intent.putExtra("unknow", false);
                    intent.putExtra("showLog", true);
                }
                else {
                    intent.putExtra("unknow", true);
                    intent.putExtra("phone", contacts.get(position).getPhone());
                    intent.putExtra("showLog", true);
                }
                startActivityForResult(intent, 810);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                Log.i("LongClick", "===========");
                final PopupMenu popupMenu = new PopupMenu(StatCallLogActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.menu_pop_delete, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        dialogHandler.showBottomWindow(
                                getLayoutInflater().inflate(R.layout.dialog_bottom, null),
                                "是否删除此通话记录？",
                                "此通话记录将从本机删除。是否删除？",
                                "我已阅读并了解",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ArrayList<Map<String, String>> callLog = contacts.get(position).getCallLog();
                                        String phone = contacts.get(position).getPhone();
                                        for (int i = 0; i < callLog.size(); i++) {
                                            Log.e("number", phone);
                                            try {
                                                deleteCallLog(phone);
                                                contacts.remove(position);
                                                showLog();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        dialogHandler.getDialog().dismiss();
                                    }
                                });
                        return true;
                    }
                });
                popupMenu.show();
                return true;
            }
        });
    }

    public void deleteCallLog(String phone)
    {
        Uri uri = CallLog.Calls.CONTENT_URI;
        String where = CallLog.Calls.NUMBER + "=?";
        String[] whereArgs = new String[]{phone};
        resolver.delete(uri, where, whereArgs);
    }
}

