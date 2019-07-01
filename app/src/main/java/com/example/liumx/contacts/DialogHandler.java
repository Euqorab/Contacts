package com.example.liumx.contacts;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by Euqorab on 2019/5/21.
 */

public class DialogHandler extends AppCompatActivity {
    private Context mContext;
    private Dialog dialog;

    public DialogHandler(Context context) {
        mContext = context;
    }

    public Dialog getDialog() {
        return dialog;
    }

    public void showBottomWindow(View root, String title, String subtitle,
                                 String checkText, View.OnClickListener confirmAction) {
        dialog = new Dialog(mContext, R.style.BottomDialogStyle);

        TextView titleText = (TextView) root.findViewById(R.id.title);
        TextView subtitleText = (TextView) root.findViewById(R.id.subtitle);
        final CheckBox checkBox = (CheckBox) root.findViewById(R.id.check_box);
        TextView cancel = (TextView) root.findViewById(R.id.cancel_action);
        final TextView confirm = (TextView) root.findViewById(R.id.confirm_action);

        final int colorEnable = root.getResources().getColor(R.color.colorAccent);
        final int colorDisable = root.getResources().getColor(R.color.colorAccentLight);

        titleText.setText(title);
        if (subtitle != "") {
            subtitleText.setText(subtitle);
        }

        if (checkText.equals("")) {
            checkBox.setVisibility(View.GONE);
            confirm.setEnabled(true);
            confirm.setTextColor(colorEnable);
        }
        else {
            checkBox.setText(checkText);
            confirm.setTextColor(colorDisable);
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkBox.isChecked()) {
                        confirm.setEnabled(true);
                        confirm.setTextColor(colorEnable);
                    }
                    else {
                        confirm.setEnabled(false);
                        confirm.setTextColor(colorDisable);
                    }
                }
            });
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        confirm.setOnClickListener(confirmAction);

        dialog.setContentView(root);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (getScreenWidth(mContext));
        lp.y = 0; //设置Dialog距离底部的距离
        dialogWindow.setAttributes(lp); //将属性设置给窗体
        dialog.show();//显示对话框
    }

    public void showListWindow(View root, ArrayList<Map<String, Object>> list,
                               AdapterView.OnItemClickListener onItemClickListener) {
        dialog = new Dialog(mContext, R.style.BottomDialogStyle);

        SimpleAdapter adapter = new SimpleAdapter(mContext, list, R.layout.dialog_list_item,
                new String[]{"text"}, new int[]{R.id.item_text});
        ListView listView = (ListView) root.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener);

        dialog.setContentView(root);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (getScreenWidth(mContext));
        lp.y = 0; //设置Dialog距离底部的距离
        dialogWindow.setAttributes(lp); //将属性设置给窗体
        dialog.show();//显示对话框
    }

    public void showDatePickerWindow(View root, boolean setMinDate, long minDate, DatePicker.OnDateChangedListener onDateChange,
                                     View.OnClickListener confirmAction) {
        dialog = new Dialog(mContext, R.style.BottomDialogStyle);

        TextView cancel = (TextView) root.findViewById(R.id.cancel_action);
        final TextView confirm = (TextView) root.findViewById(R.id.confirm_action);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        confirm.setOnClickListener(confirmAction);
        confirm.setEnabled(true);

        DatePicker datePicker = (DatePicker) root.findViewById(R.id.date_picker);
        if (setMinDate) {
            datePicker.setMinDate(minDate);
        }
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        // 初始化DatePicker组件，初始化时指定监听器
        datePicker.init(year, month, day, onDateChange);

        dialog.setContentView(root);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (getScreenWidth(mContext));
        lp.y = 0; //设置Dialog距离底部的距离
        dialogWindow.setAttributes(lp); //将属性设置给窗体
        dialog.show();//显示对话框
    }

    public void showTimePickerWindow(View root, int hour, int minute,
                                     TimePicker.OnTimeChangedListener onTimeChange,
                                     View.OnClickListener confirmAction) {
        dialog = new Dialog(mContext, R.style.BottomDialogStyle);

        TextView cancel = (TextView) root.findViewById(R.id.cancel_action);
        final TextView confirm = (TextView) root.findViewById(R.id.confirm_action);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        confirm.setOnClickListener(confirmAction);
        confirm.setEnabled(true);

        TimePicker timePicker = (TimePicker) root.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);
        Calendar c = Calendar.getInstance();
        if (hour == -1) {
            hour = c.get(Calendar.HOUR);
        }
        if (minute == -1) {
            minute = c.get(Calendar.MINUTE);
        }
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minute);
        timePicker.setOnTimeChangedListener(onTimeChange);

        dialog.setContentView(root);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (getScreenWidth(mContext));
        lp.y = 0; //设置Dialog距离底部的距离
        dialogWindow.setAttributes(lp); //将属性设置给窗体
        dialog.show();//显示对话框
    }

    protected static int getScreenWidth(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getWidth();
    }
}
