package com.example.liumx.contacts;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by 64849 on 2019/6/29.
 */

public class MyPopDialog extends Dialog{
    public MyPopDialog(Context context) {
        super(context);
    }

    public MyPopDialog(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private Context context;
        private Bitmap image;
        private String name, phone, org, email;

        public Builder(Context context) {
            this.context = context;
        }

        public Bitmap getImage() {
            return image;
        }

        public String getName() { return name; }

        public String getPhone() { return phone; }

        public String getOrg() { return org; }

        public String getEmail() { return email; }

        public void setInfo(Bitmap image, String name, String phone, String org, String email) {
            this.image = image;
            this.name = name;
            this.phone = phone;
            this.org = org;
            this.email = email;
        }

        public MyPopDialog create() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final MyPopDialog dialog = new MyPopDialog(context,R.style.Dialog);
            View layout = inflater.inflate(R.layout.layout_share_qrcode, null);
            dialog.addContentView(layout, new ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    , android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
            dialog.setContentView(layout);
            ImageView img = (ImageView)layout.findViewById(R.id.img_qrcode);
            TextView cardName = (TextView) layout.findViewById(R.id.cardName);
            TextView cardPhone = (TextView) layout.findViewById(R.id.cardPhone);
            TextView cardOrg = (TextView) layout.findViewById(R.id.cardOrg);
            TextView cardEmail = (TextView) layout.findViewById(R.id.cardEmail);
            layout.setClickable(true);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            cardName.setText(getName());
            cardPhone.setText(getPhone());
            cardOrg.setText(getOrg());
            cardEmail.setText(getEmail());
            img.setImageBitmap(getImage());
            return dialog;
        }
    }
}
