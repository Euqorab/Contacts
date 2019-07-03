package com.example.liumx.contacts;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Euqorab on 2019/6/29.
 */

public class MyCard extends AppCompatActivity {
    private Toolbar toolbar;
    private ContactDb db;
    private TextView title;
    private TextView subtitle;
    private TextView cardPhone;
    private TextView cardEmail;
    private ImageView cardQr;
    private RelativeLayout card;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_share_qrcode);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle("我的名片");

        db = new ContactDb(this);

        title = (TextView) findViewById(R.id.cardName);
        subtitle = (TextView) findViewById(R.id.cardOrg);
        cardPhone = (TextView) findViewById(R.id.cardPhone);
        cardEmail = (TextView) findViewById(R.id.cardEmail);
        cardQr = (ImageView) findViewById(R.id.img_qrcode);
        card = (RelativeLayout) findViewById(R.id.relayout);

        initWidget();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        else {
            Intent intent = new Intent(this, AddContactActivity.class);
            intent.putExtra("flag", "2");
            Cursor cursor = db.query("my_card", null, null, null, null);
            if (cursor.moveToNext()) {
                intent.putExtra("name", cursor.getString(0));
                intent.putExtra("phone", cursor.getString(1));
                intent.putExtra("email", cursor.getString(2));
                intent.putExtra("organization", cursor.getString(3));
                intent.putExtra("address", cursor.getString(4));
                intent.putExtra("birthday", cursor.getString(5));
            }
            startActivityForResult(intent, 112);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(requestCode + "", resultCode + "");
        if (requestCode == 112 && resultCode == 445) {
            initWidget();
        }
    }

    private void initWidget() {
        Cursor cursor = db.query("my_card", new String[]{"name", "phone", "email", "organization", "address", "birthday"},
                null, null, null);
        if (cursor.moveToNext()) {
            String name, phone, email, org, address, birthday;
            name = cursor.getString(0);
            phone = cursor.getString(1);
            email = cursor.getString(2);
            org = cursor.getString(3);
            address = cursor.getString(4);
            birthday = cursor.getString(5);

            if (!cursor.getString(0).equals("")) {
                title.setText(name);
                title.setVisibility(View.VISIBLE);
                subtitle.setText(org);
                subtitle.setTextSize(16);
            }
            else {
                title.setVisibility(View.GONE);
                subtitle.setTextSize(24);
                subtitle.setText(name);
            }
//            cardPhone.setEnabled(phone.equals(""));
            Log.i(phone, email);
            cardPhone.setText(phone);
//            cardEmail.setEnabled(email.equals(""));
            cardEmail.setText(email);

            String count = "name=" + name + "\nphone=" + phone + "\nemail=" + email
                    + "\norganization=" + org + "\naddress=" + address
                    + "\nbirthday=" + birthday;
            Bitmap bitmap = generateBitmap(count, 600, 600);
            cardQr.setImageBitmap(bitmap);

            card.setVisibility(View.VISIBLE);
        }

        else {
            Log.e("============", "===============");
            card.setVisibility(View.GONE);
        }
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

}
