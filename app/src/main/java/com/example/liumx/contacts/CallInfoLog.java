package com.example.liumx.contacts;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.telecom.Call;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Euqorab on 2019/6/23.
 */

public class CallInfoLog {
    private ContentResolver resolver;
    private String[] project = {CallLog.Calls.CACHED_NAME// 通话记录的联系人
            , CallLog.Calls.NUMBER// 通话记录的电话号码
            , CallLog.Calls.DATE// 通话记录的日期
            , CallLog.Calls.DURATION// 通话时长
            , CallLog.Calls.TYPE};// 通话类型}
    private String where = CallLog.Calls.NUMBER + "=?";

    private Uri uri = CallLog.Calls.CONTENT_URI;

    public CallInfoLog(ContentResolver resolver) {
        this.resolver = resolver;
    }

    public ArrayList<ArrayList<Map<String, String>>> getTotalCallLog() {
        ArrayList<ArrayList<Map<String, String>>> callLog = new ArrayList<>();
        ArrayList<Map<String, String>> list = new ArrayList<>();

        String orderBy = CallLog.Calls.NUMBER + " desc," + CallLog.Calls.DATE + " desc";
        Cursor cursor = resolver.query(uri, project, null, null, orderBy);
        String tmp = "";
        Log.e("count", String.valueOf(cursor.getCount()));
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                long dateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(dateLong));
                String time = new SimpleDateFormat("HH:mm").format(new Date(dateLong));
                int dur = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));
                int tp = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                String type = "";
                String dayCurrent = new SimpleDateFormat("dd").format(new Date());
                String dayRecord = new SimpleDateFormat("dd").format(new Date(dateLong));
                String typeStr = "";
                switch (tp) {
                    case CallLog.Calls.INCOMING_TYPE:
                        //"打入"
                        typeStr = dur < 60 ? dur + "秒" : (dur / 60) + "分钟";
                        type = "0";
                        break;
                    case CallLog.Calls.OUTGOING_TYPE:
                        //"打出"
                        if (dur == 0) {
                            typeStr = "未接通";
                            type = "2";
                        }
                        else {
                            typeStr = dur < 60 ? dur + "秒" : (dur / 60) + "分钟";
                            type = "1";
                        }
                        break;
                    case CallLog.Calls.MISSED_TYPE:
                        //"未接"
                        typeStr = "未接通";
                        type = "2";
                        break;
                    case CallLog.Calls.REJECTED_TYPE:
                        typeStr = "拒接";
                        type = "3";
                        break;
                    default:
                        typeStr = "未接通";
                        type = "4";
                        break;
                }
                String dayStr;
                if ((Integer.parseInt(dayCurrent)) == (Integer.parseInt(dayRecord))) {
                    dayStr = "今天";
                } else if ((Integer.parseInt(dayCurrent) - 1) == (Integer.parseInt(dayRecord))) {
                    dayStr = "昨天";
                } else {
                    dayStr = new SimpleDateFormat("MM-dd").format(new Date(dateLong));
                    ;
                }

                Map<String, String> map = new HashMap<String, String>();
                map.put("name", name);
                map.put("phone", number);
                map.put("date", date);//通话日期
                map.put("duration", String.valueOf(dur));//时长
                map.put("type", type);
                map.put("typeStr", typeStr);//类型
                map.put("time", time);//通话时间
                map.put("dayStr", dayStr);

                Log.e("++++++++type", String.valueOf(type));

                if (tmp == "") {
                    tmp = number;
                }
                else if (!tmp.equals(number)) {
                    callLog.add((ArrayList<Map<String, String>>) list.clone());
                    list.clear();
                    tmp = number;
                }
                list.add(map);
            }
            callLog.add(list);
        }
        cursor.close();

        Comparator<ArrayList<Map<String, String>>> comparator =
                new Comparator<ArrayList<Map<String, String>>>() {
            public int compare(ArrayList<Map<String, String>> o1,
                               ArrayList<Map<String, String>> o2) {
                String s1 = o1.get(0).get("date");
                String s2 = o2.get(0).get("date");
                return s2.compareTo(s1);
            }
        };
        Collections.sort(callLog, comparator);
        Log.e("size", String.valueOf(callLog.size()));
        return callLog;

    }

    public ArrayList<Map<String, String>> getCallLog(String number) {
        ArrayList<Map<String, String>> list = new ArrayList<>();

        String[] whereArgs = new String[]{number};
        String orderBy = CallLog.Calls.DATE + " desc";
        Log.e("number", number);
        Cursor cursor = resolver.query(uri, project, where, whereArgs, orderBy);
        Log.i("data", String.valueOf(cursor == null));
        String name = "";
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                long dateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                String datelongStr = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(dateLong));
                String time = new SimpleDateFormat("HH:mm").format(new Date(dateLong));
                int dur = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));
                String duration = "";
                int tp = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                String type = "";
                String dayCurrent = new SimpleDateFormat("dd").format(new Date());
                String dayRecord = new SimpleDateFormat("dd").format(new Date(dateLong));
                String typeStr = "";
                switch (tp) {
                    case CallLog.Calls.INCOMING_TYPE:
                        //"打入"
                        typeStr = dur < 60 ? dur + "秒" : (dur / 60) + "分钟";
                        type = "0";
                        break;
                    case CallLog.Calls.OUTGOING_TYPE:
                        //"打出"
                        if (dur == 0) {
                            typeStr = "未接通";
                            type = "2";
                        }
                        else {
                            typeStr = dur < 60 ? dur + "秒" : (dur / 60) + "分钟";
                            type = "1";
                        }
                        break;
                    case CallLog.Calls.MISSED_TYPE:
                        //"未接"
                        typeStr = "未接通";
                        type = "2";
                        break;
                    case CallLog.Calls.REJECTED_TYPE:
                        typeStr = "拒接";
                        type = "3";
                        break;
                    default:
                        typeStr = "未接通";
                        type = "4";
                        break;
                }
                String dayStr;
                if ((Integer.parseInt(dayCurrent)) == (Integer.parseInt(dayRecord))) {
                    dayStr = "今天";
                } else if ((Integer.parseInt(dayCurrent) - 1) == (Integer.parseInt(dayRecord))) {
                    dayStr = "昨天";
                } else {
                    dayStr = new SimpleDateFormat("MM-dd").format(new Date(dateLong));
                    ;
                }

                Map<String, String> map = new HashMap<String, String>();
                map.put("number", number);
                map.put("datelong", datelongStr);
                Log.e("datelong", datelongStr);
                map.put("date", date);//通话日期
                map.put("duration", duration);//时长
                map.put("type", type);
                map.put("typeStr", typeStr);//类型
                map.put("time", time);//通话时间
                map.put("dayStr", dayStr);//
                list.add(map);
            }
        }
        cursor.close();
        if (list.size() > 0){
            Log.i(name, list.get(0).get("date"));
        }
        return list;
    }
}
