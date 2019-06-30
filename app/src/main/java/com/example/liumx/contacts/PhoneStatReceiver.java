package com.example.liumx.contacts;

import com.android.internal.telephony.ITelephony;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Euqorab on 2019/6/25.
 */

public class PhoneStatReceiver extends BroadcastReceiver {
    String TAG = "tag";
    TelephonyManager telMgr;

    @Override
    public void onReceive(Context context, Intent intent) {
        telMgr = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        switch (telMgr.getCallState()) {
            case TelephonyManager.CALL_STATE_RINGING:
                String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                Log.i(TAG, "number:" + number);
                if (doNotDisturbModeOn(context) && number != null && !inWhiteList(context, number)) {
                    SharedPreferences phonenumSP = context.getSharedPreferences("in_phone_num", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = phonenumSP.edit();
                    editor.putString(number,number);
                    editor.commit();
                    endCall();
                }
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                break;
        }

    }
    /**
     * 挂断电话
     */
    private void endCall()
    {
        Class<TelephonyManager> c = TelephonyManager.class;
        try
        {
            Method getITelephonyMethod = c.getDeclaredMethod("getITelephony", (Class[]) null);
            getITelephonyMethod.setAccessible(true);
            ITelephony iTelephony;
            Log.e(TAG, "End call.");
            iTelephony = (ITelephony) getITelephonyMethod.invoke(telMgr, (Object[]) null);
            iTelephony.endCall();
        }
        catch (Exception e)
        {
            Log.e(TAG, "Fail to answer ring call.", e);
        }
    }

    private boolean inWhiteList(Context context, String number) {
        ContactDb db = new ContactDb(context);
        Cursor cursor = db.query("white_list", null, "phone=?", new String[]{number}, null);
        return (cursor.getCount() > 0);
    }

    private boolean doNotDisturbModeOn(Context context) {
        ContactDb db = new ContactDb(context);
        Cursor cursor = db.query("pref", null, "setting_item IN (?,?,?,?)",
                new String[]{"do_not_disturb", "set_time", "start_time", "end_time"}, null);
        if (cursor.getCount() > 0) {
            boolean doNotDisturb = false, setTime = false;
            String startTime = "22:00", endTime = "07:00";

            while (cursor.moveToNext()) {
                if (cursor.getString(0).equals("do_not_disturb"))
                    doNotDisturb = cursor.getString(1).equals("true");
                if (cursor.getString(0).equals("set_time"))
                    setTime = cursor.getString(1).equals("true");
                if (cursor.getString(0).equals("start_time"))
                    startTime = cursor.getString(1);
                if (cursor.getString(0).equals("end_time"))
                    endTime = cursor.getString(1);
            }

            if (!doNotDisturb) {
                return false;
            }

            if (doNotDisturb && !setTime) {
                return true;
            }
            else {
                Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);
                int minuteOfDay = hour * 60 + minute;// 从0:00分开是到目前为止的分钟数

                int start = Integer.valueOf(startTime.substring(0, 2)) * 60 +
                        Integer.valueOf(startTime.substring(3, 5));
                int end = Integer.valueOf(endTime.substring(0, 2)) * 60 +
                        Integer.valueOf(endTime.substring(3, 5));

                if (startTime.compareTo(endTime) < 0 &&
                        minuteOfDay >= start && minuteOfDay <= end) {
                    return true;
                }
                else if (startTime.compareTo(endTime) >= 0 &&
                        (minuteOfDay < start || minuteOfDay > end)){
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        return false;
    }

    private ArrayList<String> getPhoneNum(Context context) {
        ArrayList<String> list = new ArrayList<String>();
        ContactDb db = new ContactDb(context);
        String where = ContactsContract.Data._ID + "=?";
        String[] whereArgs;
        Cursor cursor = db.query("white_list", null, null, null, null);
        while (cursor.moveToNext()) {
            list.add(cursor.getString(cursor.getColumnIndex("raw_id")));
        }
        cursor.close();
        whereArgs = list.toArray(new String[list.size()]);
        list.clear();

        ContentResolver resolver = context.getContentResolver();
        cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, where, whereArgs, null);
        while (cursor.moveToNext())
        {
            // 取得联系人ID
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor phone = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
            // 取得电话号码(可能存在多个号码)
            while (phone.moveToNext())
            {
                String strPhoneNumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                list.add(strPhoneNumber);
                Log.v("tag","strPhoneNumber:"+strPhoneNumber);
            }

            phone.close();
        }
        cursor.close();
        return list;
    }
}
