package com.example.liumx.contacts;

import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

/**
 * Created by Euqorab on 2019/6/21.
 */

public class ContactRec {
    private int id;
    private String name = "";
    private String phone = "";
    private String email = "";
    private String organization = "";
    private String address = "";
    private String birthday = "";
    private String attribution = "";
    private ArrayList<Map<String, String>> callLog = null;

    ContactRec() {}

    public int getId() {
        return id;
    }

    public String getName() {
        if (name.equals("")) {
            return phone;
        } else {
            return name;
        }
    }

    public String getPhone() {
        return phone;
    }

    public String getFormatPhone() {
        switch (phone.length()) {
            case 11:
                return splitPhone(new int[]{3, 7});
            case 10:
                return splitPhone(new int[]{4, 7});
            case 8:
                return splitPhone(new int[]{4});
            case 7:
                return splitPhone(new int[]{4});
            default:
                return phone;
        }
    }

    protected String splitPhone(int[] split) {
        String result = "";
        int pos = 0;
        for (int i = 0; i < split.length; i++) {
            for (; pos < split[i]; pos++) {
                result += phone.charAt(pos);
            }
            result += " ";
        }
        for (; pos < phone.length(); pos++) {
            result += phone.charAt(pos);
        }
        return result;
    }

    public String getEmail() {
        return email;
    }

    public String getOrganization() {
        return organization;
    }

    public String getAddress() {
        return address;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getAttribution() {

        return attribution;
    }

    public ArrayList<Map<String, String>> getCallLog() {
        return callLog;
    }

    public void setId(int i) {
        id = i;
    }

    public void setName(String s) {
        if (s != null) {
            name = s;
        }
    }

    public void setPhone(String s) {
        phone = "";
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) <= '9' && s.charAt(i) >= '0') {
                phone += s.charAt(i);
            }
        }
    }

    public void setAttribution() {
        if (!phone.equals("")) {
            PhoneModel phoneModel = PhoneUtil.getPhoneModel(phone);
            if (phoneModel != null) {
                String province = phoneModel.getProvinceName();
                String city = phoneModel.getCityName();
                city = city.substring(0, city.length() - 1);
                String Carrier = phoneModel.getCarrier();
                attribution = province + city + " " + Carrier;
                String[] MUNICIPALITY = {"北京", "天津", "上海", "重庆"};
                for (int i = 0; i < 4; i++) {
                    if (city == MUNICIPALITY[i]) {
                        attribution = city + Carrier;
                    }
                }
            } else {
                attribution = "未知";
            }
            System.out.println(attribution);
        }
    }

    public void setEmail(String s) {
        email = s;
    }

    public void setOrganization(String s) {
        organization = s;
    }

    public void setAddress(String s) {
        address = s;
    }

    public void setBirthday(String s) {
        birthday = s;
    }

    public void setCallLog(ArrayList<Map<String, String>> l) {
        callLog = l;
    }

}
