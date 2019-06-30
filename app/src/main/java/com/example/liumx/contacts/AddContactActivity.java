package com.example.liumx.contacts;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Euqorab on 2019/6/21.
 */

public class AddContactActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ContentResolver resolver;
    private ContactDb db;
    private EditText editName;
    private EditText editPhone;
    private EditText editEmail;
    private EditText editOrganization;
    private EditText editAddress;
    private TextView birthday;          // 用DatePicker做成popupwindow
    //private EditText editNote;
    private DialogHandler dialogHandler;
    private int year = 0, month = 0, day = 0;
    private boolean myCard = false;
    private String CONTACT = "1", MY_CARD = "2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        Intent intent = getIntent();
        if (intent.getStringExtra("flag") != null && intent.getStringExtra("flag").equals(MY_CARD))
            actionBar.setTitle("编辑名片");
        else
            actionBar.setTitle("新联系人");

        Uri uri = Uri.parse("content://com.android.contacts/contacts");
        resolver = this.getContentResolver();
        db = new ContactDb(this);
        dialogHandler = new DialogHandler(this);
        java.util.Calendar c = java.util.Calendar.getInstance();
        year = c.get(java.util.Calendar.YEAR);
        month = c.get(java.util.Calendar.MONTH);
        day = c.get(java.util.Calendar.DAY_OF_MONTH);

        // 初始化控件
        editName = (EditText)findViewById(R.id.edit_name);
        editAddress = (EditText)findViewById(R.id.edit_addr);
        editEmail = (EditText)findViewById(R.id.edit_email);
        editPhone = (EditText)findViewById(R.id.edit_phone);
        editOrganization = (EditText)findViewById(R.id.edit_organization);
        birthday = (TextView) findViewById(R.id.text_date);
        birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                View root = getLayoutInflater().inflate(R.layout.dialog_date, null);
                dialogHandler.showDatePickerWindow(root, false, 0, new DatePicker.OnDateChangedListener() {
                            @Override
                            public void onDateChanged(DatePicker view, int nYear, int monthOfYear, int dayOfMonth) {
                                year = nYear;
                                month = monthOfYear;
                                day = dayOfMonth;
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                birthday.setText(year+"年"+(month+1)+"月"+day+"日");
                                dialogHandler.getDialog().dismiss();
                            }
                        });
            }
        });
        String rname = intent.getStringExtra("name");
        String rphone = intent.getStringExtra("phone");
        String raddress = intent.getStringExtra("address");
        String rorganization = intent.getStringExtra("organization");
        String remail = intent.getStringExtra("email");
        String rbirthday = intent.getStringExtra("birthday");

        System.out.println(rname);

        editName.setText(rname);
        editEmail.setText(remail);
        editPhone.setText(rphone);
        editAddress.setText(raddress);
        editOrganization.setText(rorganization);
        birthday.setText(rbirthday);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_confirm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            case R.id.confirm:
                String name = editName.getText().toString();
                String address = editAddress.getText().toString();
                String phone = editPhone.getText().toString();
                String email = editEmail.getText().toString();
                String organization = editOrganization.getText().toString();
                String birth = birthday.getText().toString();
                Intent intent1 = getIntent();
                String flag = intent1.getStringExtra("flag");
                if (name.equals("") && phone.equals("")) {
                    Toast.makeText(this, "请输入姓名或电话号码", Toast.LENGTH_SHORT).show();
                }
                else if(flag != null && flag.equals(CONTACT)) {
                    Log.e("=========", "update contact");
                    String id = intent1.getStringExtra("id");
                    try {
                        updateContact(id,name,phone,email,organization,address,birth);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Intent intent = getIntent();
                    setResult(445, intent);
                    finish();
                }
                else if (flag != null && flag.equals(MY_CARD)) {
                    Log.e("=========", "update card");
                    updateMyCard(name,phone,email,organization,address,birth);
                    Intent intent = getIntent();
                    setResult(445, intent);
                    finish();
                }
                else {
                    Log.e("=========", "add");
                    addContact(name,phone,email,organization,address,birth);
                    Intent intent = getIntent();
                    setResult(445, intent);
                    finish();
                }
                break;
        }
        return true;
    }

    public void addContact(String name,String phone,String email,String organization,String address,String birth){
        Log.i("+++++++++++++", "add");
        // 插入 raw_contacts 表，并获取 _id 属性
        Uri uri1 = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentValues values = new ContentValues();
        Uri rawContactUri = resolver.insert(ContactsContract.RawContacts.CONTENT_URI, values);
        //Uri rawContactUri = ContactsContract.RawContacts.CONTENT_URI;
        long rawContactId = ContentUris.parseId(rawContactUri);
        //插入 data 表
        Uri uri2 = Uri.parse("content://com.android.contacts/data");
        //add Name
        values.put("raw_contact_id", rawContactId);
        values.put(ContactsContract.Contacts.Data.MIMETYPE,"vnd.android.cursor.item/name");
        values.put("data2", name);
        values.put("data1", name);
        resolver.insert(uri2, values);
        values.clear();
        //add Phone
        values.put("raw_contact_id", rawContactId);
        values.put(ContactsContract.Contacts.Data.MIMETYPE,"vnd.android.cursor.item/phone_v2");
        values.put("data2", "2");
        values.put("data1", phone);
        resolver.insert(uri2, values);
        values.clear();
        //add email
        if(!email.isEmpty())
        {
            values.put("raw_contact_id", rawContactId);
            values.put(ContactsContract.Contacts.Data.MIMETYPE,"vnd.android.cursor.item/email_v2");
            values.put("data2", "2");
            values.put("data1", email);
            resolver.insert(uri2, values);
            values.clear();
        }
        //add organization
        if(!organization.isEmpty())
        {
            values.put("raw_contact_id", rawContactId);
            values.put(ContactsContract.Contacts.Data.MIMETYPE, "vnd.android.cursor.item/organization");
            values.put("data2", "2");
            values.put("data1", organization);
            resolver.insert(uri2, values);
            values.clear();
        }
        //add address
        if(!address.isEmpty())
        {
            values.put("raw_contact_id", rawContactId);
            values.put(ContactsContract.Contacts.Data.MIMETYPE, "vnd.android.cursor.item/postal-address_v2");
            values.put("data2", "2");
            values.put("data1", address);
            resolver.insert(uri2, values);
            values.clear();
        }

        values.put("_id", rawContactId);
        values.put("birthday", birth);
        db.insert("contact", values);
    }

    public void updateContact(String id, String name, String phone, String email, String organization, String address, String birth) throws Exception {
        Uri uri = Uri.parse("content://com.android.contacts/data");
        // 表 data
        ContentValues values = new ContentValues();
        String[] proj = {phone, name, email, address, organization};
        String where = "mimetype=? and raw_contact_id=?";
        String[] mimes = {
                "vnd.android.cursor.item/phone_v2",
                "vnd.android.cursor.item/name",
                "vnd.android.cursor.item/email_v2",
                "vnd.android.cursor.item/postal-address_v2",
                "vnd.android.cursor.item/organization",
        };
        Cursor c1 = resolver.query(uri, new String[]{"raw_contact_id"}, null, null, null);
        while (c1.moveToNext()) {
            Log.i("raw_contact_id", String.valueOf(c1.getString(0)));
        }
        for (int i = 0; i < mimes.length; i++) {
            values.put("data1", proj[i]);
            if (proj[i].equals(name)) {
                values.put("data2", proj[i]);
            }
            Log.e("=======id", id);
            Cursor cursor = resolver.query(uri, null, "raw_contact_id=?", new String[]{id}, null);
            Log.i("size", String.valueOf(cursor.getCount()));
            if (cursor.getCount() > 0) {
                resolver.update(uri, values, where, new String[]{mimes[i], id});
            }
            else if (!proj[i].equals("")) {
                values.put("raw_contact_id", id);
                values.put("mimetype", mimes[i]);
                resolver.insert(uri, values);
            }
            values.clear();
        }
        //修改生日
        values.put("birthday", birth);
        Cursor cursor = db.query("contact", null, "_id=?", new String[]{id}, null);
        if (cursor.moveToNext()) {
            db.update("contact", values, "_id=?", new String[]{id});
        }
        else {
            values.put("_id", String.valueOf(id));
            db.insert("contact", values);
        }
    }

    public void updateMyCard(String name, String phone, String email, String organization, String address, String birthday) {
        Cursor cursor = db.query("my_card", null, null, null, null);
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("phone", phone);
        cv.put("email", email);
        cv.put("organization", organization);
        cv.put("address", address);
        cv.put("birthday", birthday);
        if (cursor.moveToNext()) {
            db.update("my_card", cv, null, null);
        }
        else {
            db.insert("my_card", cv);
        }
    }
}
