package com.ei.zezoo.model;

import java.util.ArrayList;

public class Institute {
    private int Total,LastNotifId;
    private String M1,M2,M3,Time,Date;
    private ArrayList<String> Days;
    private ArrayList<Integer> Nums;

    public int getTotal() {
        return Total;
    }

    public void setTotal(int total) {
        Total = total;
    }

    public String getM1() {
        return M1;
    }

    public void setM1(String m1) {
        M1 = m1;
    }

    public String getM2() {
        return M2;
    }

    public void setM2(String m2) {
        M2 = m2;
    }

    public String getM3() {
        return M3;
    }

    public void setM3(String m3) {
        M3 = m3;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public Institute(int total, int lastNotifId, String m1, String m2, String m3, String time, String date, ArrayList<String> days, ArrayList<Integer> nums) {
        Total = total;
        LastNotifId = lastNotifId;
        M1 = m1;
        M2 = m2;
        M3 = m3;
        Time = time;
        Date = date;
        Days = days;
        Nums = nums;
    }

    public ArrayList<String> getDays() {
        return Days;
    }

    public void setDays(ArrayList<String> days) {
        Days = days;
    }

    public ArrayList<Integer> getNums() {
        return Nums;
    }

    public void setNums(ArrayList<Integer> nums) {
        Nums = nums;
    }

    public int getLastNotifId() {
        return LastNotifId;
    }

    public void setLastNotifId(int lastNotifId) {
        LastNotifId = lastNotifId;
    }
}
