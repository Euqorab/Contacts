package com.example.liumx.contacts;

import android.content.Intent;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
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
import android.widget.SimpleAdapter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 64849 on 2019/6/25.
 */

public class StatNotificationActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ContactDb db;
    private ListView listView;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("通话提醒");

        db = new ContactDb(this);
        listView = (ListView) findViewById(R.id.listView);
        show();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        View root;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            case R.id.add:
                Intent intent = new Intent(this, AddNotificationActivity.class);
                String raw_id = String .valueOf(db.getCount("notify_list"));
                intent.putExtra("raw_id", raw_id);
                startActivityForResult(intent, 99);
                break;
        }
        show();
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 99 && resultCode == 919) {
            show();
        }
    }

    public void show() {
        final ArrayList<Map<String, Object>> list = new ArrayList<>();
        Cursor cursor = db.query("notify_list", null, null, null, "date_time asc"
        );

        while (cursor.moveToNext()) {
            String date = cursor.getString(cursor.getColumnIndex("date_time"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String phone = cursor.getString(cursor.getColumnIndex("phone"));
            String  raw_id = cursor.getString(cursor.getColumnIndex("raw_id"));
            String note = null;
            if (name.length() > 0) note = "打电话给" + name + " " + cursor.getString(cursor.getColumnIndex("note"));
            else note = "打电话给" + phone + " " + cursor.getString(cursor.getColumnIndex("note"));

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("date_time",date);
            map.put("note", note);
            map.put("raw_id", raw_id);
            Date dateTime = null;
            Log.i("======Str_time===", date);
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
                dateTime = formatter.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (dateTime.getTime() < System.currentTimeMillis()){
                db.delete("notify_list", "raw_id=?", new String[] {String.valueOf(raw_id)});
            }
            else  list.add(map);
        }
        cursor.close();
        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.contact_info_list_item,
                new String[]{"date_time", "note"}, new int[]{R.id.title, R.id.subtitle});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(StatNotificationActivity.this, AddNotificationActivity.class);
                intent.putExtra("raw_id", String.valueOf(list.get(position).get("raw_id")));
                startActivityForResult(intent, 99);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                PopupMenu popupMenu = new PopupMenu(StatNotificationActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.menu_pop, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        DialogHandler dialogHandler = new DialogHandler(StatNotificationActivity.this);
                        switch (item.getItemId()) {
//                            case R.id.pop_edit:
//                                /* 编辑设置 */
//                                Intent intent = new Intent(StatNotificationActivity.this, AddNotificationActivity.class);
//                                intent.putExtra("raw_id", (int)list.get(position).get("raw_id"));
//                                startActivityForResult(intent, 99);
//                                break;
                            case R.id.pop_del:
                                /* 删除记录 */
                                View root = getLayoutInflater().inflate(R.layout.dialog_bottom, null);
                                dialogHandler.showBottomWindow(root, "是否删除此提醒？", "此提醒事项将从本机删除。是否删除？",
                                        "我已阅读并了解", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                db.delete("notify_list", "raw_id=?", new String[]{String.valueOf(list.get(position).get("raw_id"))});
//                                                Log.i("test", String.valueOf(db.query("notify_list", null, "raw_id=?", new String[]{String.valueOf(list.get(position).get("raw_id"))}, null).getCount()));
                                                show();
                                            }
                                        });
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
                return true;
            }
        });
    }
}
