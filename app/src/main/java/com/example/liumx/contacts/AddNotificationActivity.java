package com.example.liumx.contacts;

import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.os.Handler;

import java.text.ParseException;
import java.util.Date;

import static java.lang.Math.pow;

/**
 * Created by 64849 on 2019/6/24.
 */

public class AddNotificationActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ContentResolver resolver;
    private EditText editNameNofity;
    private EditText editPhoneNotify;
    private EditText editNoteNotify;
    private TextView timeToNotify;
    private NotificationManager notificationManager;
    private Notification notification;
    private static final int NOTIFICATION_1 = 10;
    private DialogHandler dialogHandler;
    private int year = 0, month = 0, day = 0;
    private int hour = 0, minute = 0;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        toolbar = (Toolbar) findViewById(R.id.toolbar_notification);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("新建通知事项");

        dialogHandler = new DialogHandler(this);

        // 初始化控件
        editNameNofity = (EditText) findViewById(R.id.edit_name_notify);
        editPhoneNotify = (EditText) findViewById(R.id.edit_phone_notify);
        editNoteNotify = (EditText) findViewById(R.id.edit_note_notify);
        timeToNotify = (TextView) findViewById(R.id.text_time_notify);

        String dbId = getIntent().getStringExtra("raw_id");
        if (dbId != "-1") {
            ContactDb db = new ContactDb(this);
            Cursor cursor = db.query("notify_list", null, "raw_id=?", new String[] {String.valueOf(dbId)}, null);
            if (cursor.moveToNext()) {
                editNameNofity.setText(cursor.getString(cursor.getColumnIndex("name")));
                editPhoneNotify.setText(cursor.getString(cursor.getColumnIndex("phone")));
                editNoteNotify.setText(cursor.getString(cursor.getColumnIndex("note")));
                timeToNotify.setText(cursor.getString(cursor.getColumnIndex("date_time")));
            }
            else {
                if (getIntent().getStringExtra("name") != null)
                    editNameNofity.setText(getIntent().getStringExtra("name"));
                editPhoneNotify.setText(getIntent().getStringExtra("phone"));
//                editNoteNotify.setText(getIntent().getStringExtra("note"));
//                if (!getIntent().getStringExtra("date_time").isEmpty() &&
//                        !getIntent().getStringExtra("date_time").equals(""))
//                    timeToNotify.setText(getIntent().getStringExtra("date_time") + "8时0分");
            }
        }


        timeToNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                year = Calendar.getInstance().get(Calendar.YEAR);
                month = Calendar.getInstance().get( Calendar.MONTH);
                day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                dialogHandler.showDatePickerWindow(
                        getLayoutInflater().inflate(R.layout.dialog_date, null),
                        false, System.currentTimeMillis(),
                        new DatePicker.OnDateChangedListener() {
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
                                hour = Calendar.getInstance().get(Calendar.HOUR);
                                minute = Calendar.getInstance().get(Calendar.MINUTE);
                                dialogHandler.getDialog().dismiss();
                                dialogHandler.showTimePickerWindow(
                                        getLayoutInflater().inflate(R.layout.dialog_time, null),
                                        hour, minute,
                                        new TimePicker.OnTimeChangedListener() {
                                            @Override
                                            public void onTimeChanged(TimePicker view, int hourOfDay, int nMinute) {
                                                hour = hourOfDay;
                                                minute = nMinute;
                                            }
                                        },
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                timeToNotify.setText(year + "年" + (month+1) + "月" + day + "日" +
                                                        hour + "时" + minute + "分");
                                                dialogHandler.getDialog().dismiss();
                                            }
                                        }
                                );
                            }
                        }
                );
            }
        });
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

    public Context getContext() {
        return this;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        Intent intent1;
        switch (item.getItemId()) {
            case R.id.confirm:
                // 转换时间
                String strTime = timeToNotify.getText().toString();
                Date date = null;
                Log.i("date: ", strTime + "====");
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日HH时mm分");
                    date = formatter.parse(strTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (date.getTime() <= System.currentTimeMillis()){
                    Toast.makeText(this, "设置的时间过早，请重新设置。", Toast.LENGTH_SHORT).show();
                    return true;
                }
                String notificaitionId = getIntent().getStringExtra("raw_id");


                Intent intent = new Intent (this, PushNotification.class);
                intent.putExtra("delayTime", (int)(date.getTime() - System.currentTimeMillis()));
                intent.putExtra("contentTitle", "通话提醒");
                intent.putExtra("phone", editPhoneNotify.getText().toString());
                intent.putExtra("subTitle", editNameNofity.getText().toString());
                // Todo
                intent.putExtra("contentText", editNoteNotify.getText().toString());

                intent.putExtra("notificationId", notificaitionId);
                startService(intent);

                ContactDb db = new ContactDb(this);
                ContentValues cv = new ContentValues();
                cv.put("note", editNoteNotify.getText().toString());
                cv.put("name", editNameNofity.getText().toString());
                cv.put("phone", editPhoneNotify.getText().toString());
                cv.put("date_time", strTime);
                cv.put("raw_id", notificaitionId);

                Cursor cursor = db.query("notify_list", null, "raw_id=?", new String[] {String.valueOf(notificaitionId)}, null);
                if (cursor.moveToNext()) db.update("notify_list", cv, "raw_id=?", new String[] {String.valueOf(notificaitionId)});
                else db.insert("notify_list", cv);

                Log.i("test1", String.valueOf(db.query("notify_list", null, null, null, null).getCount()));
                Toast.makeText(this, "提醒已添加！", Toast.LENGTH_SHORT).show();
                intent1 = getIntent();
                setResult(919, intent1);
                finish();
                break;
            case android.R.id.home:
                intent1 = getIntent();
                setResult(919, intent1);
                finish();
                break;
        }
        return true;
    }
}
