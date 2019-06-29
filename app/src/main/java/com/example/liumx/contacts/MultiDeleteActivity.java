package com.example.liumx.contacts;

import android.content.ContentResolver;
import android.content.Intent;
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

import java.util.ArrayList;
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
                    dialogHandler.showBottomWindow(root,
                            "是否删除" + adapter.getCheckedPosition().size() + "项通话记录",
                            "已选择的通话记录将从本机删除。是否删除？",
                            "我已阅读并了解",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ArrayList<Integer> checkedPositon = adapter.getCheckedPosition();
                                    for (int i = 0; i < checkedPositon.size(); i++) {
                                        int pos = checkedPositon.get(i);
                                        Log.e("num", contacts.get(pos).getPhone());
                                        deleteCallLog(contacts.get(pos).getPhone());
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
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < contacts.size(); i++) {
            ArrayList<Map<String, String>> callLog = contacts.get(i).getCallLog();

            if (callLog.size() > 0) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("icon", "-1");
                String title = contacts.get(i).getName();
                if (callLog.size() > 1) {
                    title += "(" + callLog.size() + ")";
                }
                map.put("title", title);
                map.put("subtitle", callLog.get(0).get("dayStr")
                        + " " + callLog.get(0).get("time"));
                map.put("showCallIcon", false);
                map.put("showType", false);
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

    public void deleteCallLog(String phone)
    {
        Uri uri = CallLog.Calls.CONTENT_URI;
        String where = CallLog.Calls.NUMBER + "=?";
        String[] whereArgs = new String[]{phone};
        resolver.delete(uri, where, whereArgs);
    }
}


