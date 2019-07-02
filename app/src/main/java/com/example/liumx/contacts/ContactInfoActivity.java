package com.example.liumx.contacts;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
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
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Euqorab on 2019/6/21.
 */

public class ContactInfoActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ContactDb db;
    private String _id;
    private ListView listView;
    private TextView title1;
    private TextView title2;
    private TextView detail;
    private TextView log;
    private ContentResolver resolver;
    private ContactRec contact;
    static private boolean DETAIL = true;
    static private boolean LOG = false;
    private boolean page;
    private boolean contactChanged = false;
    private DialogHandler dialogHandler;
    private boolean unknowContact = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_info);

        db = new ContactDb(this);
        dialogHandler = new DialogHandler(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("");

        listView = (ListView) findViewById(R.id.info_list);
        title1 = (TextView) findViewById(R.id.title1);
        title2 = (TextView) findViewById(R.id.title2);
        detail = (TextView) findViewById(R.id.detail);
        log = (TextView) findViewById(R.id.log);
        page = DETAIL;

        resolver = this.getContentResolver();

        _id = getIntent().getStringExtra("_id");
        //Log.e("id======", _id);
        Intent intent = getIntent();
        unknowContact = intent.getBooleanExtra("unknow", false);
        if (unknowContact)
            contact = getContactByExtra(intent);
        else
            contact = getContact();

        //Log.e("======", intent.getBooleanExtra("showLog", false) + "");
        initWidget(intent.getBooleanExtra("showLog", false));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!unknowContact) {
            contact = getContact();
            int menuId = db.query("white_list", null, "phone=?", new String[]{contact.getPhone()}, null)
                    .getCount() > 0 ? R.menu.menu_contact_info2 : R.menu.menu_contact_info;
            getMenuInflater().inflate(menuId, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        View root;
        final Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                if (contactChanged)
                    setResult(445);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                Date date = new Date(System.currentTimeMillis());
                Log.e("time1", simpleDateFormat.format(date));
                finish();
                break;

            case R.id.whitelist:
                String[] whereArgs = new String[]{contact.getPhone()};
                final String title;
                if (db.query("white_list", null, "phone=?", whereArgs, null)
                        .getCount() > 0) {
                    db.delete("white_list", "phone=?", whereArgs);
                    title = "加入白名单";
                }
                else {
                    ContentValues cv = new ContentValues();
                    cv.put("raw_id", _id);
                    cv.put("phone", contact.getPhone());
                    db.insert("white_list", cv);
                    title = "移出白名单";
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SystemClock.sleep(100);
                        item.setTitle(title);
                    }
                }).run();
                break;

            case R.id.edit:
                intent = new Intent(this, AddContactActivity.class);
                System.out.println(contact.getName());
                intent.putExtra("id", _id);
                if (!unknowContact)
                    intent.putExtra("flag", "1");
                else
                    intent.putExtra("unknow", true);
                if (contact.getName().equals(contact.getPhone()))
                    intent.putExtra("name", "");
                else
                    intent.putExtra("name", contact.getName());
                intent.putExtra("phone", contact.getPhone());
                intent.putExtra("email", contact.getEmail());
                intent.putExtra("address", contact.getAddress());
                intent.putExtra("organization", contact.getOrganization());
                intent.putExtra("birthday", contact.getBirthday());
                startActivityForResult(intent, 344);
                break;

            case R.id.notify:
                Intent intent1 = new Intent(ContactInfoActivity.this, AddNotificationActivity.class);
                String raw_id = String.valueOf(db.getCount("notify_list"));
                intent1.putExtra("raw_id", raw_id);
                intent1.putExtra("name", contact.getName());
                intent1.putExtra("phone", contact.getPhone());
                if (contact.getBirthday().equals("") || contact.getBirthday().isEmpty()) {
                    intent1.putExtra("date_time", "");
                    intent1.putExtra("note", "");
                }
                else {
                    intent1.putExtra("date_time", getBirthday(contact.getBirthday()));
                    intent1.putExtra("note", "生日");
                }

                Log.i("test: ", String.valueOf(intent1.getStringExtra("raw_id")));
                startActivity(intent1);
                break;

            case R.id.delete:
                root = getLayoutInflater().inflate(R.layout.dialog_bottom, null);
                intent = new Intent(this, MainActivity.class);
                DialogHandler dlgHandleWord = new DialogHandler(this);
                dlgHandleWord.showBottomWindow(root, "是否删除此联系人？",
                        "此联系人将从本机删除。是否删除？", "我已阅读并了解",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String id = String.valueOf(contact.getId());
                                try {
                                    deleteContact(id);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                setResult(445, intent);
                                finish();
                            }
                        });
                break;

            case R.id.contact_create:
                String name = contact.getName();
                String phone = contact.getPhone();
                String email = contact.getEmail();
                String org = contact.getOrganization();
                String address = contact.getAddress();
                String birthday = contact.getBirthday();
                String count = "name=" + name + "\nphone=" + phone + "\nemail=" + email
                        + "\norganization=" + org + "\naddress=" + address
                        + "\nbirthday=" + birthday;
                Bitmap bitmap = generateBitmap(count, 600, 600);
                MyPopDialog.Builder dialogBuild = new MyPopDialog.Builder(ContactInfoActivity.this);
                dialogBuild.setInfo(bitmap, name, phone, org, email); // 设置名片信息
                MyPopDialog dialog = dialogBuild.create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                break;
        }
        return true;
    }

    // 生成二维码
    private Bitmap generateBitmap(String content, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        try {
            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (encode.get(j, i)) {
                        pixels[i * width + j] = 0x00000000;
                    } else {
                        pixels[i * width + j] = 0xffffffff;
                    }
                }
            }
            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getBirthday(String bornDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
        Calendar cal = Calendar.getInstance();
        int yearNow = cal.get(Calendar.YEAR);
        Log.i("Before: ", bornDate);
        try {
            cal.setTime(formatter.parse(bornDate));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String nowStr = String.valueOf(yearNow) + bornDate.substring(4);
        String nextStr = String.valueOf(yearNow + 1) + bornDate.substring(4);
        Log.i("Error: ", nowStr);

        Date nowDate = null;
        try {
            nowDate = formatter.parse(nowStr);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (nowDate.getTime() <= System.currentTimeMillis()) return nextStr;
        else return nowStr;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 344 && resultCode == 445) {
            contactChanged = true;
            contact = getContact();

            String name = contact.getName() != "" ? contact.getName() : "未备注联系人";
            if (contact.getOrganization() != "") {
                title1.setText(name);
                title1.setVisibility(View.VISIBLE);
                title2.setText(contact.getOrganization());
                title2.setTextSize(16);
                title2.setTypeface(Typeface.DEFAULT);
            }
            else {
                title1.setVisibility(View.GONE);
                title2.setText(name);
                title2.setTextSize(24);
                title2.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            }
            if (page == DETAIL) {
                showDetail();
            }
        }
    }

    protected void initWidget(boolean flag) {
        String name = contact.getName() != "" ? contact.getName() : "未备注联系人";
        if (contact.getOrganization() != "") {
            title1.setText(name);
            title2.setText(contact.getOrganization());
        }
        else {
            title1.setVisibility(View.GONE);
            title2.setText(name);
            title2.setTextSize(24);
            title2.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }

        if (flag)
            showLog();
        else
            showDetail();

        detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("page", "DETAIL");
                if (page == LOG) {
                    showDetail();
                }
            }
        });

        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("page", "LOG");
                if (page == DETAIL) {
                    if (contact.getPhone() != "") {
                        CallInfoLog callInfoLog = new CallInfoLog(resolver);
                        contact.setCallLog(callInfoLog.getCallLog(contact.getPhone()));
                    }
                    showLog();
                }
            }
        });

    }

    public ContactRec getContactByExtra(Intent intent) {
        ContactRec newContact = new ContactRec();
        if (intent.getStringExtra("phone")!= null) {
            String phone = intent.getStringExtra("phone");
            Uri uri = Uri.parse("content://com.android.contacts/data");
            Cursor cursor = resolver.query(uri, new String[]{"raw_contact_id"},
                    "mimetype=? and data1=?", new String[]{"vnd.android.cursor.item/phone_v2", phone}, null);

            if (cursor.moveToNext()) {
                _id = cursor.getString(0);
                return getContact();
            }

            newContact.setPhone(phone);
            newContact.setAttribution();
            CallInfoLog callInfoLog = new CallInfoLog(resolver);
            newContact.setCallLog(callInfoLog.getCallLog(phone));
        }
        return newContact;
    }

    public ContactRec getContact() {
        ContactRec newContact = new ContactRec();
        newContact.setId(Integer.parseInt(_id));
        Uri uri = Uri.parse("content://com.android.contacts/contacts");
        Cursor cursor = resolver.query(uri, new String[]{"name_raw_contact_id"}, "_id=? or name_raw_contact_id=?", new String[]{_id, _id}, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            _id = cursor.getString(0);
            uri = Uri.parse("content://com.android.contacts/data");
            Cursor cursor1 = resolver.query(uri, new String[]{Data.DATA1, Data.DATA2, Data.MIMETYPE}, "raw_contact_id=?", new String[]{_id}, null);
            while (cursor1.moveToNext()) {
                Log.i(cursor1.getString(2), cursor1.getString(0) + "============" + cursor1.getString(1));
                String data = cursor1.getString(cursor1.getColumnIndex("data1"));
                String mimetype = cursor1.getString(cursor1.getColumnIndex("mimetype"));
                if (mimetype.equals("vnd.android.cursor.item/name")) {
                    newContact.setName(data);
                }
                else if (mimetype.equals("vnd.android.cursor.item/phone_v2")) {
                    newContact.setPhone(data);
                    newContact.setAttribution();
                }
                else if (!data.equals("") && mimetype.equals("vnd.android.cursor.item/email_v2")) {
                    newContact.setEmail(data);
                }
                else if (!data.equals("") && mimetype.equals("vnd.android.cursor.item/organization")) {
                    newContact.setOrganization(data);
                }
                else if (!data.equals("") && mimetype.equals("vnd.android.cursor.item/postal-address_v2")) {
                    newContact.setAddress(data);
                }
                else if (!data.equals("") && mimetype.equals("vnd.android.cursor.item/contact_event") &&
                        cursor1.getString(1).equals("3")) {
                    String birth = "";
                    Log.e("birthday========", data + "=====");
                    SimpleDateFormat sf1 = new SimpleDateFormat("yyyy年MM月dd日");
                    SimpleDateFormat sf2 = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        birth = sf1.format(sf2.parse(data));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    newContact.setBirthday(birth);
                }
            }
        }
        cursor.close();

        Log.e("=========", newContact.getPhone());
        if (!newContact.getPhone().equals("")) {
            CallInfoLog callInfoLog = new CallInfoLog(resolver);
            newContact.setCallLog(callInfoLog.getCallLog(newContact.getPhone()));
            Log.i("+++++++++++++", callInfoLog.getCallLog(newContact.getPhone()).size() + "");
        }
        Log.i("name==========", newContact.getName());
        return newContact;
    }

    public void showDetail() {
        page = DETAIL;
        log.setBackgroundColor(Color.alpha(0));
        log.setTypeface(Typeface.DEFAULT);
        log.setTextColor(getResources().getColor(R.color.colorText));
        detail.setBackgroundResource(R.drawable.bg_switch);
        detail.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        detail.setTextColor(getResources().getColor(R.color.green));

        ArrayList<Map<String, Object>> list = new ArrayList<>();
        if (contact.getPhone() != "") {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("title", contact.getFormatPhone());
            map.put("subtitle", contact.getAttribution());         // 归属地
            map.put("icon", R.drawable.ic_phone);
            map.put("showCallIcon", true);
            map.put("showType", false);
            list.add(map);
        }
        if (contact.getEmail() != "") {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("title", contact.getEmail());
            map.put("subtitle", "邮件");
            map.put("icon", R.drawable.ic_email);
            map.put("showCallIcon", false);
            map.put("showType", false);
            list.add(map);
        }
        if (contact.getAddress() != "") {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("title", contact.getAddress());
            map.put("subtitle", "地址");
            map.put("icon", R.drawable.ic_address);
            map.put("showCallIcon", false);
            map.put("showType", false);
            list.add(map);
        }
        Log.e("birth", contact.getBirthday());
        if (!contact.getBirthday().isEmpty()) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("title", contact.getBirthday());
            map.put("subtitle", "生日");
            map.put("icon", R.drawable.ic_birthday);
            map.put("showCallIcon", false);
            map.put("showType", false);
            list.add(map);
        }

        MyAdapter adapter = new MyAdapter(this, list, R.layout.contact_info_list_item,
                new String[]{"icon", "title", "subtitle"}, new int[]{R.id.icon, R.id.title, R.id.subtitle});
        listView.setAdapter(adapter);
    }

    public void showLog() {
        page = LOG;
        detail.setBackgroundColor(Color.alpha(0));
        detail.setTypeface(Typeface.DEFAULT);
        detail.setTextColor(getResources().getColor(R.color.colorText));
        log.setBackgroundResource(R.drawable.bg_switch);
        log.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        log.setTextColor(getResources().getColor(R.color.green));

        ArrayList<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < contact.getCallLog().size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            switch (contact.getCallLog().get(i).get("type")) {
                case "0" :  //打入
                    map.put("icon", R.drawable.ic_call_in);
                    break;
                case "1" :  //打出
                    map.put("icon", R.drawable.ic_call_out);
                    break;
                case "2" :  //未接
                    map.put("icon", R.drawable.ic_call_closed);
                    break;
                case "3" :  //未接
                    map.put("icon", R.drawable.ic_call_closed);
                    break;
                default:
                    map.put("icon", R.drawable.ic_call_closed);
                    break;
            }
            map.put("title", contact.getCallLog().get(i).get("dayStr")
                    + " " + contact.getCallLog().get(i).get("time"));
            Log.e("=======", contact.getCallLog().get(i).get("time"));
            map.put("subtitle", contact.getFormatPhone());
            map.put("showCallIcon", false);
            map.put("showType", true);
            map.put("type", contact.getCallLog().get(i).get("typeStr"));
            Log.e("==============type", contact.getCallLog().get(i).get("typeStr"));
            list.add(map);
        }

        MyAdapter adapter = new MyAdapter(this, list, R.layout.contact_info_list_item,
                new String[]{"icon", "title", "subtitle"}, new int[]{R.id.icon, R.id.title, R.id.subtitle});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contact.getFormatPhone()));
                startActivity(intent);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                PopupMenu popupMenu = new PopupMenu(ContactInfoActivity.this, view);
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
                                        String deleteDate = contact.getCallLog().get(position).get("datelong");
                                        String number = contact.getPhone();
                                        Log.e("datelong", deleteDate);
                                        Log.e("number", number);
                                        try {
                                            deleteCallLog(number, deleteDate);
                                            if (unknowContact)
                                                contact = getContactByExtra(getIntent());
                                            else
                                                contact = getContact();
                                            showLog();
                                        }
                                        catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        contactChanged = true;
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

        if (cursor.getCount() > 0) {
            resolver.delete(uri, CallLog.Calls.DATE + "=?", new String[]{deleteDate});
        }

    }

    public void deleteContact(String id) throws Exception{
        Uri uri = Uri. parse ("content://com.android.contacts/raw_contacts");
        // 表 raw_contacts
        Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Contacts.Data. _ID}, "_id=?", new String[]{id}, null);
        if(cursor.moveToNext()) {
            // 根据 id 删除 data 中的相应数据
            resolver.delete(uri,"_id=?",new String[]{id});
            uri = Uri. parse ("content://com.android.contacts/data");
            resolver.delete(uri, "raw_contact_id=?", new String[]{id});
        }
        cursor = db.query("contact", null, "_id=?", new String[]{id}, null);
        if (cursor.moveToNext()) {
            db.delete("contact", "_id=?", new String[]{String.valueOf(id)});
            //System.out.println("hhh");
        }
    }
}

