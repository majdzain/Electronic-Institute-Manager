package com.ei.zezoo;

import android.content.Context;
import android.content.SharedPreferences;

import com.ei.zezoo.model.Institute;
import com.ei.zezoo.model.Manager;
import com.ei.zezoo.model.Server;
import com.ei.zezoo.model.Type;

import java.util.ArrayList;
import java.util.HashSet;


public class SharedPreference {

    private static final String APP_PREFS_NAME = "CakeVPNPreference";

    private SharedPreferences mPreference;
    private SharedPreferences.Editor mPrefEditor;
    private Context context;

    private static final String SERVER_COUNTRY = "server_country";
    private static final String SERVER_FLAG = "server_flag";
    private static final String SERVER_OVPN = "server_ovpn";
    private static final String SERVER_OVPN_USER = "server_ovpn_user";
    private static final String SERVER_OVPN_PASSWORD = "server_ovpn_password";
    private static final String MANAGER_USER = "manager_user";
    private static final String MANAGER_USERID = "manager_user_id";
    private static final String MANAGER_PASS = "manager_pass";
    private static final String MANAGER_NAME = "manager_name";
    private static final String MANAGER_IS = "manager_is";
    private static final String INSTITUTE_TOTAL = "institute_total";
    private static final String INSTITUTE_NOTIFID = "institute_notifId";
    private static final String INSTITUTE_M1 = "institute_m1";
    private static final String INSTITUTE_M2 = "institute_m2";
    private static final String INSTITUTE_M3 = "institute_m3";
    private static final String INSTITUTE_TIME = "institute_time";
    private static final String INSTITUTE_DATE = "institute_date";
    private static final String INSTITUTE_DAYS = "institute_days";
    private static final String TYPE_STUDIES = "_type_studies";
    private static final String TYPE_TEACHERS = "_type_teachers";
    private static final String TYPE_STUDENTS = "_type_students";
    private static final String TYPE_SUBJECTS = "_type_subjects";


    public SharedPreference(Context context) {
        this.mPreference = context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE);
        this.mPrefEditor = mPreference.edit();
        this.context = context;
    }

    /**
     * Save server details
     * @param server details of ovpn server
     */
    public void saveServer(Server server){
        mPrefEditor.putString(SERVER_COUNTRY, server.getCountry());
        mPrefEditor.putString(SERVER_OVPN, server.getOvpn());
        mPrefEditor.putString(SERVER_OVPN_USER, server.getOvpnUserName());
        mPrefEditor.putString(SERVER_OVPN_PASSWORD, server.getOvpnUserPassword());
        mPrefEditor.commit();
    }

    public void saveInstitute(Institute i){
        mPrefEditor.putString(INSTITUTE_M1, i.getM1());
        mPrefEditor.putString(INSTITUTE_M2, i.getM2());
        mPrefEditor.putString(INSTITUTE_M3, i.getM3());
        mPrefEditor.putString(INSTITUTE_TIME, i.getTime());
        mPrefEditor.putString(INSTITUTE_DATE, i.getDate());
        mPrefEditor.putInt(INSTITUTE_TOTAL, i.getTotal());
        mPrefEditor.putInt(INSTITUTE_NOTIFID, i.getLastNotifId());
        ArrayList<String> a = new ArrayList<>();
        for(int l =0;l<i.getNums().size();l++){
            a.add(i.getDays().get(l) + "/" + String.valueOf(i.getNums().get(l)));
        }
        mPrefEditor.putStringSet(INSTITUTE_DAYS, new HashSet<String>(a));
        mPrefEditor.commit();
    }

    public void saveManager(Manager manager){
        mPrefEditor.putString(MANAGER_USER, manager.getUser());
        mPrefEditor.putString(MANAGER_USERID, manager.getUserId());
        mPrefEditor.putString(MANAGER_PASS, manager.getPass());
        mPrefEditor.putString(MANAGER_NAME, manager.getName());
        mPrefEditor.putBoolean(MANAGER_IS, manager.isSigned());
        mPrefEditor.commit();
    }
    public void saveType(Type type){

        mPrefEditor.putStringSet(String.valueOf(type.getTypeN()) + TYPE_STUDIES, new HashSet<String>(type.getStudiesN()));
        mPrefEditor.putStringSet(String.valueOf(type.getTypeN()) + TYPE_TEACHERS,new HashSet<String>( type.getTeachersN()));
        mPrefEditor.putStringSet(String.valueOf(type.getTypeN()) + TYPE_STUDENTS,new HashSet<String>( type.getStudentsN()));
        mPrefEditor.putStringSet(String.valueOf(type.getTypeN()) + TYPE_SUBJECTS,new HashSet<String>( type.getSubjectsN()));
        mPrefEditor.commit();
    }

    public void saveVpnStarted(boolean b){
        mPrefEditor.putBoolean("isVpnConnected",b);
        mPrefEditor.commit();
    }
    public boolean isVpnStarted(){
        return mPreference.getBoolean("isVpnConnected",false);
    }

    /**
     * Get server data from shared preference
     * @return server model object
     */
    public Server getServer() {

        Server server = new Server(
                mPreference.getString(SERVER_COUNTRY,"Japan"),
                mPreference.getString(SERVER_OVPN,"aaa.ovpn"),
                mPreference.getString(SERVER_OVPN_USER,""),
                mPreference.getString(SERVER_OVPN_PASSWORD,"")
        );

        return server;
    }
    public Manager getManager() {

        Manager manager = new Manager(
                mPreference.getString(MANAGER_USER,"user"),
                mPreference.getString(MANAGER_PASS,"pass"),
                mPreference.getString(MANAGER_NAME,"name"),
                mPreference.getString(MANAGER_USERID,"userId"), mPreference.getBoolean(MANAGER_IS,false)
        );

        return manager;
    }
    public Institute getInstitute() {
        ArrayList<String> a = new ArrayList<>();
        a.addAll(mPreference.getStringSet(INSTITUTE_DAYS,new HashSet<String>()));
        ArrayList<String> ad = new ArrayList<>();
        ArrayList<Integer> an = new ArrayList<>();
        for(int l =0;l<a.size();l++){
            ad.add(a.get(l).substring(0,a.get(l).indexOf("/")));
            an.add(Integer.valueOf(a.get(l).substring(a.get(l).indexOf("/")+1)));
        }
        Institute institute = new Institute(
                mPreference.getInt(INSTITUTE_TOTAL,0),
                mPreference.getInt(INSTITUTE_NOTIFID,100000), mPreference.getString(INSTITUTE_M1,"m1"),
                mPreference.getString(INSTITUTE_M2,"m2"),
                mPreference.getString(INSTITUTE_M3,"m3"),
                mPreference.getString(INSTITUTE_TIME,"time"),
                mPreference.getString(INSTITUTE_DATE,"date"),
                ad, an);

        return institute;
    }
    public Type getType(int n) {
ArrayList<String> a1 = new ArrayList<>();
a1.addAll(mPreference.getStringSet(String.valueOf(n) + TYPE_STUDIES,new HashSet<String>()));
        ArrayList<String> a2 = new ArrayList<>();
        a2.addAll(mPreference.getStringSet(String.valueOf(n) +TYPE_TEACHERS,new HashSet<String>()));
        ArrayList<String> a3 = new ArrayList<>();
        a3.addAll(mPreference.getStringSet(String.valueOf(n) + TYPE_STUDENTS,new HashSet<String>()));
        ArrayList<String> a4 = new ArrayList<>();
        a4.addAll(mPreference.getStringSet(String.valueOf(n) + TYPE_SUBJECTS,new HashSet<String>()));
        Type type = new Type(n,a1,a2,a3,a4);
        return type;
    }
}
