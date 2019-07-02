package com.example.liumx.contacts;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ContactDb db;
    private ContentResolver resolver;
    private ListView listView;
    private Sidebar sidebar;
    private ContactAdapter adapter;
    private SearchView searchView;
    private ArrayList<ContactRec> contacts;
    private ArrayList<ArrayList<Map<String, String>>> callLog;
    private boolean mShowRequestPermission = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init_permission();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this, R.style.Toolbar_TitleText);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle("联系人");

        contacts = new ArrayList<ContactRec>();

        listView = (ListView) findViewById(R.id.contact_group_list);
        sidebar = (Sidebar) findViewById(R.id.sidebar);
        searchView = (SearchView) findViewById(R.id.search_bar);

        db = new ContactDb(this);
        initPref();

        resolver = this.getContentResolver();

        contacts = getContacts();
        showContactList(contacts);
        initWidgets();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.add:
                intent = new Intent(this, AddContactActivity.class);
                intent.putExtra("flag","0");
                startActivityForResult(intent, 566);
                break;
            case R.id.stat:
                intent = new Intent(this, StatCallLogActivity.class);
                startActivityForResult(intent, 566);
                break;
            case R.id.pref:
                intent = new Intent(this, PrefActivity.class);
                startActivity(intent);
                break;
            case R.id.notify:
                intent = new Intent(this, StatNotificationActivity.class);
                startActivity(intent);
                break;

            case R.id.scan:
                new IntentIntegrator(this)
                        .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)// 扫码的类型,可选：一维码，二维码，一/二维码
                        //.setPrompt("请对准二维码")// 设置提示语
                        .setCaptureActivity(QrCodeActivity.class)
                        .setCameraId(0)// 选择摄像头,可使用前置或者后置
                        .setBeepEnabled(true)// 是否开启声音,扫完码之后会"哔"的一声
                        .initiateScan();// 初始化扫码
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(requestCode + "", resultCode + "");
        if (requestCode == 566 && resultCode == 445) {
            Log.e(requestCode + "", resultCode + "");
            contacts = getContacts();
            showContactList(contacts);
            searchView.setQuery("", false);
        }
        else {
            IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (intentResult != null) {
                if (intentResult.getContents() == null) {
                    //扫码失败
                } else {
                    String result = intentResult.getContents();
                    Log.e("result", result);
                    String[] tmp = result.split("[\r\n]");
                    for (int i = 0; i < tmp.length; i++)
                        Log.e("=========", "" + tmp[i].length());
                    String name, phone;
                    name = tmp[0].substring(5);
                    phone = tmp[1].substring(6);
                    Intent intent = new Intent(this, AddContactActivity.class);
                    if (name.equals(phone))
                        intent.putExtra("name", "");
                    else
                        intent.putExtra("name", name);
                    intent.putExtra("phone", phone);
                    intent.putExtra("email", tmp[2].substring(6));
                    intent.putExtra("address", tmp[4].substring(8));
                    intent.putExtra("organization", tmp[3].substring(13));
                    intent.putExtra("birthday", tmp[5].substring(9));
                    startActivityForResult(intent, 566);
                }
            }
        }
    }

    void initPref() {
        if (db.query("pref", null, null, null, null).getCount() == 0) {
            Log.e("test", "insert");
            String[] setting_items = {"days_of_call_log", "birthday_notification",
                    "do_not_disturb", "set_time", "start_time", "end_time"};
            String[] data = {"-1", "false", "false", "false", "22:00", "07:00"};
            for (int i = 0; i < setting_items.length; i++) {
                ContentValues cv = new ContentValues();
                cv.put("setting_item", setting_items[i]);
                cv.put("data", data[i]);
                db.insert("pref", cv);
            }
        }
    }

    void initWidgets() {
        sidebar.setOnTouchingLetterChangedListener(new Sidebar.OnChooseLetterChangedListener() {
            @Override
            public void onChooseLetter(final String s) {
                listView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listView.requestFocusFromTouch();
                        listView.setSelection(listView.getHeaderViewsCount() +
                                adapter.getAlphaPosition(s));
                    }
                }, 10);
            }
            @Override
            public void onNoChooseLetter() {}
        });

        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();
        searchView.findViewById(android.support.v7.appcompat.R.id.search_plate).setBackground(null);
        searchView.findViewById(android.support.v7.appcompat.R.id.submit_area).setBackground(null);
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            // 清空
            @Override
            public boolean onClose() {
                searchView.setQuery("", false);
                searchView.clearFocus();
                contacts = getContacts();
                Log.e("=============", contacts.get(0).getName());
                showContactList(contacts);
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String newText) {
                if (newText.equals(""))
                    showContactList(contacts);
                else{
                    ArrayList<Pair<ContactRec, String>> restrictContacts = getRestrictContacts(newText);
                    showRestrictContactList(restrictContacts);
                }
                searchView.clearFocus();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals(""))
                    showContactList(contacts);
                else{
                    ArrayList<Pair<ContactRec, String>> restrictContacts = getRestrictContacts(newText);
                    showRestrictContactList(restrictContacts);
                }
                return false;
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                searchView.clearFocus();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //searchView.clearFocus();
            }
        });
    }

    public boolean getSdkVersionSix() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private void init_permission() {
        if (getSdkVersionSix()) {
            String[] permissions = new String[]{
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.WRITE_CALL_LOG,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CALL_PHONE};
            List<String> mPermissionList = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }

            if (mPermissionList.isEmpty()) {// 全部允许
                mShowRequestPermission = true;
            } else {//存在未允许的权限
                String[] permissionsArr = mPermissionList.toArray(new String[mPermissionList.size()]);
                ActivityCompat.requestPermissions(this, permissionsArr, 101);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 101:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        //判断是否勾选禁止后不再询问
                        boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i]);
                        if (showRequestPermission) {
                            init_permission();
                            return;
                        } else { // false 被禁止了，不在访问
                            mShowRequestPermission = false;//已经禁止了
                        }
                    }
                }
                break;
        }
    }

    public ArrayList<ContactRec> getContacts() {
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

            if(!contact.getName().isEmpty()){
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

        ContactRec myCard = new ContactRec();
        myCard.setName("我的名片");
        newContacts.add(0, myCard);

        return newContacts;
    }

    private ArrayList<Pair<ContactRec, String>> getRestrictContacts(String restrict) {
        ArrayList<Pair<ContactRec, String>> newContacts = new ArrayList<>();
        Log.e("size", String.valueOf(contacts.size()));
        for (int i = 1; i < contacts.size(); i++) {
            String tmpName = contacts.get(i).getName();
            String pyName = tmpName;
            Log.e("i", tmpName + "+++++++++++++++");

            String[] tmp = {
                    contacts.get(i).getPhone(),
                    contacts.get(i).getEmail(),
                    contacts.get(i).getOrganization(),
            };
            PinyinUtils pinyinUtils = new PinyinUtils();
            if (pinyinUtils.isChinese(tmpName)) {
                pyName = pinyinUtils.getSelling(tmpName);
            }
            Log.i(tmpName, restrict);
            if (pyName.toLowerCase().contains(restrict.toLowerCase())
                    || tmpName.contains(restrict)) {
                newContacts.add(new Pair<>(contacts.get(i), ""));
                continue;
            }
            boolean flag = true;
            for (int j = 0; j < tmp.length; j++) {
                Log.e("j", j + tmp[j]);
                if ((tmp[j].toLowerCase().contains(restrict.toLowerCase()) ||
                        tmp[j].contains(restrict)) && flag) {
                    Log.e("true", "-=-=--=-=-=-=-");
                    newContacts.add(new Pair<>(contacts.get(i), tmp[j]));
                    flag = false;
                }
            }
        }
        return newContacts;
    }

    public void showContactList(final ArrayList<ContactRec> sContacts){
        final ArrayList<Map<String, Object>> list = new ArrayList<>();
        Log.i("size", String.valueOf(sContacts.size()));
        for (int i = 0; i < sContacts.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("head", R.drawable.head_default);
            map.put("name", sContacts.get(i).getName());
            list.add(map);
            //Log.i("contact", contacts.get(i).getName());
        }

        adapter = new ContactAdapter(this, list, R.layout.contact_brief_list_item,
                new String[]{"head", "name"}, new int[]{R.id.image_head, R.id.text_name});
        Log.e("===========", String.valueOf(list.get(0).get("name")));
        adapter.setTagIndexs(1);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                if (position == 0) {
                    intent = new Intent(MainActivity.this, MyCard.class);
                    startActivity(intent);
                }
                else {
                    intent = new Intent(MainActivity.this, ContactInfoActivity.class);
                    intent.putExtra("_id", String.valueOf(sContacts.get(position).getId()));
                    intent.putExtra("unknow", false);
                    startActivityForResult(intent, 566);
                }
            }
        });
    }

    public void showRestrictContactList(final ArrayList<Pair<ContactRec, String>> sContacts) {
        final ArrayList<Map<String, Object>> list = new ArrayList<>();
        Log.i("size", String.valueOf(sContacts.size()));
        for (int i = 0; i < sContacts.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("head", R.drawable.head_default);
            map.put("title", sContacts.get(i).first.getName());
            map.put("subtitle", sContacts.get(i).second);
            list.add(map);
            //Log.i("contact", contacts.get(i).getName());
        }

        adapter = new ContactAdapter(this, list, R.layout.contact_brief_list_item,
                new String[]{"head", "title", "subtitle"}, new int[]{R.id.image_head, R.id.text_name, R.id.text_subtitle});
        adapter.setTagIndexs(0);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ContactInfoActivity.class);
                intent.putExtra("_id", String.valueOf(sContacts.get(position).first.getId()));
                intent.putExtra("unknow", false);
                startActivityForResult(intent, 566);
            }
        });
    }
}
