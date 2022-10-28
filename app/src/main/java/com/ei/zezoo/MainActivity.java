package com.ei.zezoo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.VpnService;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ei.zezoo.model.Institute;
import com.ei.zezoo.model.Manager;
import com.ei.zezoo.model.Server;
import com.ei.zezoo.model.Type;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import de.blinkt.openvpn.OpenVpnApi;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.VpnStatus;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;


public class MainActivity extends AppCompatActivity {
    Resources res;
    LineChartView lineChartView;
    LinearLayout btnEdu;
    Toolbar toolbar;
    private FirebaseAuth mauth;
    private DatabaseReference ref;
    private FirebaseUser currentuser;
    private String userId;
    private boolean isStartAnotherActivity = false;
    private Menu men;
    private boolean isConnectAnimationStarted = false;
    SharedPreference preference;
    FrameLayout loadingLayout;
    Institute institute;
    Manager manager;
    Type type0, type1, type2, type3, type4;
    TextView total, studies0, teeachers0, subjects0, students0;
    private ValueEventListener managerListener, infoListener, usersListener;
    ArrayList<String> sub1, sub2, sub3, sub4, sub5;
    UserSQLDatabaseHandler dbu;
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mauth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference();
        currentuser = mauth.getCurrentUser();
        res = getResources();
        setVpnProxy();
        if (currentuser == null) {
            signupAsFirst();
        } else {
            Toast.makeText(this, "مرحبا بك في المعهد الإلكتروني...", Toast.LENGTH_LONG).show();
            setContentView(R.layout.activity_main);
            userId = currentuser.getUid();
            lineChartView = (LineChartView) findViewById(R.id.chart);
            toolbar = (Toolbar) findViewById(R.id.mainToolbar);
            toolbar.setTitle("المعهد الإلكتروني");
            toolbar.setTitleTextColor(res.getColor(R.color.white));
            toolbar.setBackgroundColor(res.getColor(R.color.orange));
            changeToolbarFont(toolbar, this);
            setSupportActionBar(toolbar);
            preference = new SharedPreference(this);
            dbu = new UserSQLDatabaseHandler(this);
            operations = new ArrayList<>();
            operationsDates = new ArrayList<>();
            loadingLayout = (FrameLayout) findViewById(R.id.loadingFrame);
            total = (TextView) findViewById(R.id.totalStudents);
            studies0 = (TextView) findViewById(R.id.eduStudy);
            teeachers0 = (TextView) findViewById(R.id.eduTeacher);
            students0 = (TextView) findViewById(R.id.eduStudent);
            subjects0 = (TextView) findViewById(R.id.eduSubject);
            btnEdu = (LinearLayout) findViewById(R.id.btn_education);
            institute = preference.getInstitute();
            manager = preference.getManager();
            type0 = preference.getType(100);
            type1 = preference.getType(101);
            type2 = preference.getType(102);
            type3 = preference.getType(103);
            type4 = preference.getType(104);
            setTexts();
            createChart();
            loadingLayout.setVisibility(View.GONE);

            btnEdu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isStartAnotherActivity = true;
                    Intent i = new Intent(MainActivity.this, EducationActivity.class);
                    startActivity(i);
                }
            });
            sub1 = new ArrayList<>();
            sub2 = new ArrayList<>();
            sub3 = new ArrayList<>();
            sub4 = new ArrayList<>();
            sub5 = new ArrayList<>();

            managerListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        String semail = dataSnapshot.child("email").getValue().toString();
                        String sname = dataSnapshot.child("name").getValue().toString();
                        String sid = dataSnapshot.child("id").getValue().toString();
                        String spass = dataSnapshot.child("pass").getValue().toString();
                        preference.saveManager(new Manager(semail, spass, sname, sid, true));
                        ref.child("info").addListenerForSingleValueEvent(infoListener);
                        //  ref.child("Managers").child(userId).removeEventListener(this);

                    } else {
                        Toast.makeText(MainActivity.this, "تم حذف حسابك يرجى مراجعة المعهد!", Toast.LENGTH_LONG).show();
                        closeApplication();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            infoListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String tot = dataSnapshot.child("total").getValue().toString();
                        String ni = dataSnapshot.child("lastNotifId").getValue().toString();
                        String m1 = dataSnapshot.child("m1").getValue().toString();
                        String m2 = dataSnapshot.child("m2").getValue().toString();
                        String m3 = dataSnapshot.child("m3").getValue().toString();
                        String time = dataSnapshot.child("time").getValue().toString();
                        String date = dataSnapshot.child("date").getValue().toString();
                        ArrayList<String> days = new ArrayList<>();

                        ArrayList<Integer> nums = new ArrayList<>();
                        ref.child("info").child("days").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Iterator iterator = snapshot.getChildren().iterator();
                                    while (iterator.hasNext()) {
                                        String day = ((DataSnapshot) iterator.next()).getKey();
                                        operationsDates.add(false);
                                        int opIndex = operationsDates.size() - 1;
                                        ref.child("info").child("days").child(day).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot Snapshot) {
                                                if (Snapshot.exists()) {
                                                    String num = (String) Snapshot.child("num").getValue();
                                                    days.add(day);
                                                    nums.add(Integer.valueOf(num));

                                                    operationsDates.set(opIndex, true);
                                                    boolean isLast = true;
                                                    for (int i = 0; i < operationsDates.size(); i++) {
                                                        if (!operationsDates.get(i)) {
                                                            isLast = false;
                                                        }
                                                    }
                                                    if (isLast) {
                                                        //   sortDates(days, nums);
                                                        preference.saveInstitute(new Institute(Integer.valueOf(tot), Integer.valueOf(ni), m1, m2, m3, time, date, days, nums));
                                                        ref.child("Users").addListenerForSingleValueEvent(usersListener);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        // ref.child("info").removeEventListener(this);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            usersListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Iterator iterator = dataSnapshot.getChildren().iterator();
                        while (iterator.hasNext()) {
                            saveUser(((DataSnapshot) iterator.next()).getKey(), iterator.hasNext());
                        }
                        //   ref.child("Users").removeEventListener(this);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
        }
    }

    private void saveUser(String value, boolean hasNext) {
        ref.child("Users").child(value).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserListChildItem u = new UserListChildItem();
                   // String snow = dataSnapshot.child("isNow").getValue().toString();
                    String sbirthday = dataSnapshot.child("birthday").getValue().toString();
                    String sdate = dataSnapshot.child("date").getValue().toString();
                    String stime = dataSnapshot.child("time").getValue().toString();
                    String semail = dataSnapshot.child("email").getValue().toString();
                    String sfather = dataSnapshot.child("father").getValue().toString();
                    String smother = dataSnapshot.child("mother").getValue().toString();
                    String sfrom = dataSnapshot.child("from").getValue().toString();
                    String sid = dataSnapshot.child("id").getValue().toString();
                    String simage = dataSnapshot.child("image").getValue().toString();
                    String sname = dataSnapshot.child("name").getValue().toString();
                    String snum = dataSnapshot.child("num").getValue().toString();
                    String snum_ = dataSnapshot.child("num_").getValue().toString();
                    String snumcomments = dataSnapshot.child("numcomments").getValue().toString();
                    String splace = dataSnapshot.child("place").getValue().toString();
                    String sstudy = dataSnapshot.child("study").getValue().toString();
                    String suid = dataSnapshot.child("uid").getValue().toString();
                    String sbill = dataSnapshot.child("bill").getValue().toString();
                    String steacher = dataSnapshot.child("isTeacher").getValue().toString();

                    String stype = dataSnapshot.child("type").getValue().toString();
                    String spass = dataSnapshot.child("pass").getValue().toString();
                    String sban = dataSnapshot.child("isBanned").getValue().toString();
                    String sstatus = dataSnapshot.child("status").getValue().toString();

                    ArrayList<Integer> studyList = new ArrayList<>();
                    String studies = dataSnapshot.child("studies").getValue().toString();
                    if (!TextUtils.isEmpty(studies)) {
                        for (int i = 0; i < studies.length(); i += (steacher.contains("1") ? 6 : 3)) {
                            studyList.add(Integer.valueOf(studies.substring(i, i + (steacher.contains("1") ? 6 : 3))));
                        }
                    }
                    if (!dbu.isExist(sid)) {
                        u.setStudies(studyList);
                        u.setType(Integer.valueOf(stype));
                        u.setName(sname);
                        u.setFather(sfather);
                        u.setMother(smother);
                        u.setId(sid);
                        u.setFrom(sfrom);
                        u.setPlace(splace);
                        u.setBirthday(sbirthday);
                        u.setPhone(snum);
                        u.setTelephone(snum_);
                        u.setEmail(semail);
                        u.setUser(semail);
                        u.setStudy(Integer.valueOf(sstudy));
                        u.setDate(sdate);
                        u.setTime(stime);
                        u.setBill(sbill);
                        u.setBills(sbill);
                        u.setUserId(suid);
                        u.setCurrent(true);
                        u.setNumComments(Integer.valueOf(snumcomments));
                        u.setPassword(spass);
                        if (sban.contains("1"))
                            u.setBanned(true);
                        else
                            u.setBanned(false);
                        u.setStatus(sstatus);
                        if (steacher.contains("1"))
                            u.setTeacher(true);
                        else
                            u.setTeacher(false);
                        u.setProfile(null);
                        boolean isExist = false;
                        List<UserListChildItem> users = dbu.allUsers();
                        int column = users.size();
                        for(int i = 0;i<users.size();i++){
                            if(users.get(i).getUserId().matches(u.getUserId())){
                                isExist = true;
                                column = users.get(i).getColumn();
                                u.setProfile(users.get(i).getProfile());
                            }
                        }
                        u.setColumn(column);
                        if (isExist)
                            dbu.updateUser(u);
                            else
                        dbu.addUser(u);
                        if (!hasNext) {
                            //      for (int i = 100; i < 105; i++) {
                            saveType(100);
                            //  }

                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    ArrayList<Boolean> operations, operationsDates;

    private void saveType(int i) {
        ref.child(ElecUtils.getType(i)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Iterator iterator = dataSnapshot.getChildren().iterator();
                    ArrayList<String> studies = new ArrayList<>();
                    while (iterator.hasNext()) {
                        String study = ((DataSnapshot) iterator.next()).getKey();
                        studies.add(study);
                        operations.add(false);
                        int opIndex = operations.size() - 1;
                        ref.child(ElecUtils.getType(i)).child(study).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Iterator iterator1 = snapshot.getChildren().iterator();
                                    while (iterator1.hasNext()) {
                                        String subject = ((DataSnapshot) iterator1.next()).getKey();
                                        switch (i) {
                                            case 100:
                                                if (!isSubjectExist(sub1, subject))
                                                    sub1.add(subject);
                                                Type t1 = preference.getType(i);
                                                t1.setSubjectsN(sub1);
                                                preference.saveType(t1);

                                                break;
                                            case 101:
                                                if (!isSubjectExist(sub2, subject))
                                                    sub2.add(subject);
                                                Type t2 = preference.getType(i);
                                                t2.setSubjectsN(sub2);
                                                preference.saveType(t2);
                                                break;
                                            case 102:
                                                if (!isSubjectExist(sub3, subject))
                                                    sub3.add(subject);
                                                Type t3 = preference.getType(i);
                                                t3.setSubjectsN(sub3);
                                                preference.saveType(t3);
                                                break;
                                            case 103:
                                                if (!isSubjectExist(sub4, subject))
                                                    sub4.add(subject);
                                                Type t4 = preference.getType(i);
                                                t4.setSubjectsN(sub4);
                                                preference.saveType(t4);
                                                break;
                                            case 104:
                                                if (!isSubjectExist(sub5, subject))
                                                    sub5.add(subject);
                                                Type t5 = preference.getType(i);
                                                t5.setSubjectsN(sub5);
                                                preference.saveType(t5);
                                                break;
                                        }
                                    }
                                    operations.set(opIndex, true);
                                    boolean isLast = true;
                                    for (int l = 0; l < operations.size(); l++) {
                                        if (!operations.get(l))
                                            isLast = false;
                                    }
                                    if (isLast) {
                                        institute = preference.getInstitute();
                                        manager = preference.getManager();
                                        type0 = preference.getType(100);
                                        type1 = preference.getType(101);
                                        type2 = preference.getType(102);
                                        type3 = preference.getType(103);
                                        type4 = preference.getType(104);
                                        setTexts();
                                        createChart();
                                    }


                                    //   ref.child(ElecUtils.getType(i)).child(study).removeEventListener(this);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    ArrayList<String> teachers = new ArrayList<>();
                    ArrayList<String> students = new ArrayList<>();
                    List<UserListChildItem> users = dbu.allUsers();
                    for(int j = 0;j<users.size();j++){
                        if(users.get(j).getType() == i){
                            if(users.get(j).isTeacher())
                                teachers.add(users.get(j).getUser());
                            else
                                students.add(users.get(j).getUser());
                        }
                    }
                    preference.saveType(new Type(i, studies, teachers, students, new ArrayList<>()));
                    // ref.child(ElecUtils.getType(i)).removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private boolean isSubjectExist(ArrayList<String> subs, String subject) {
        boolean is = false;
        for (int i = 0; i < subs.size(); i++) {
            if (subs.get(i).matches(subject)) {
                is = true;
                break;
            }
        }
        return is;
    }

    ArrayList<String> daysA;
    ArrayList<Integer> numsA;

    private void sortDates(ArrayList<String> d, ArrayList<Integer> n) {
        int maxPos, last = d.size() - 1;
        while (last > 0) {
            maxPos = maxSelect(d, last + 1);
            String tempD = d.get(maxPos);
            int tempN = n.get(maxPos);
            d.set(maxPos, d.get(last));
            d.set(last, tempD);
            n.set(maxPos, n.get(maxPos));
            n.set(last, tempN);
            last--;
        }
        for (int i = 0; i < d.size(); i++) {
            String s = d.get(i).replace(d.get(i).substring(d.get(i).lastIndexOf("-")), "").replaceAll("-", "/");
            d.set(i, s);
        }
        daysA = d;
        numsA = n;
    }

    private int maxSelect(ArrayList<String> d, int n) {
        int maxPos = 0;

        for (int i = 1; i < n; i++) {
            String da = d.get(maxPos);
            int day = Integer.valueOf(da.substring(0, da.indexOf("-")));
            int month = Integer.valueOf(da.substring(da.indexOf("-") + 1, da.lastIndexOf("-")));
            int date = Integer.valueOf(da.substring(da.lastIndexOf("-") + 1));
            String daC = d.get(i);
            int dayC = Integer.valueOf(daC.substring(0, daC.indexOf("-")));
            int monthC = Integer.valueOf(daC.substring(daC.indexOf("-") + 1, daC.lastIndexOf("-")));
            int dateC = Integer.valueOf(daC.substring(daC.lastIndexOf("-") + 1));
            if (dateC == date) {
                if (monthC == month) {
                    if (dayC > day) {
                        maxPos = i;
                    }
                } else if (monthC > month) {
                    maxPos = i;
                }
            } else if (dateC > date) {
                maxPos = i;
            }
        }
        return maxPos;
    }

    private void setTexts() {
        total.setText("عدد الطلاب الإجمالي : " + String.valueOf(institute.getTotal()));
        studies0.setText(String.valueOf(type0.getStudiesN().size()));
        teeachers0.setText(String.valueOf(type0.getTeachersN().size()));
        students0.setText(String.valueOf(type0.getStudentsN().size()));
        subjects0.setText(String.valueOf(type0.getSubjectsN().size()));
    }

    private void createChart() {
        String[] axisData = {"Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sept",
                "Oct", "Nov", "Dec"};
        int[] yAxisData = {50, 20, 15, 30, 20, 60, 15, 40, 45, 10, 90, 18};
        sortDates(institute.getDays(), institute.getNums());
        List<PointValue> mPointValues = new ArrayList<PointValue>();
        List<AxisValue> mAxisXValues = new ArrayList<AxisValue>();
        for (int i = 0; i < daysA.size(); i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(daysA.get(i)));
            mPointValues.add(new PointValue(i, numsA.get(i)));
        }


        Line line = new Line(mPointValues).setColor(getResources().getColor(R.color.orange));
        List lines = new ArrayList();
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);
        Axis axis = new Axis();
        axis.setValues(mAxisXValues);
        Axis yAxis = new Axis();
        axis.setTextSize(13);
        axis.setTextColor(getResources().getColor(R.color.blue));
        yAxis.setTextColor(getResources().getColor(R.color.blue));
        yAxis.setTextSize(14);
        data.setAxisYLeft(yAxis);
        data.setAxisXBottom(axis);
        lineChartView.setLineChartData(data);
        Viewport viewport = new Viewport(lineChartView.getMaximumViewport());
        viewport.top = 100;
        viewport.bottom = 0;
        //lineChartView.setMaximumViewport(viewport);
        //lineChartView.setCurrentViewport(viewport);
        lineChartView.setZoomType(ZoomType.HORIZONTAL);
        lineChartView.setZoomEnabled(true);
    }

    private Server server;
    private CheckInternetConnection connection;

    OpenVPNThread vpnThread = new OpenVPNThread();
    OpenVPNService vpnService = new OpenVPNService();


    private void setVpnProxy() {
        preference = new SharedPreference(this);
        server = preference.getServer();
        Server s = new Server("Japan",
                "aaa.ovpn",
                "",
                ""
        );
        preference.saveServer(s);
        server = preference.getServer();
        connection = new CheckInternetConnection();
        if (!vpnService.isConnected()) {
            preference.saveVpnStarted(false);
        }
        if (!preference.isVpnStarted()) {

            //     Toast.makeText(SplashActivity.this, "Vpn Started", Toast.LENGTH_LONG).show();
            prepareVpn();
        }
    }

    private void prepareVpn() {
        if (getInternetStatus()) {
            // Checking permission for network monitor
            Intent intent = VpnService.prepare(this);
            if (intent != null) {
                startActivityForResult(intent, 1);
            } else startVpn();//have already permissio
        } else {
            // No internet connection available
            showToast("يجب الاتصال بالإنترنت!");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //Permission granted, start the VPN
            startVpn();
        } else {
            showToast("Permission Deny !! ");
        }
    }

    public boolean getInternetStatus() {
        return connection.netCheck(this);
    }

    private void startVpn() {
        try {

            if (vpnService.getStatus().matches("DISCONNECTED") || !vpnService.isConnected()) {
                preference.saveVpnStarted(true);
                // .ovpn file
                InputStream conf = getAssets().open(server.getOvpn());
                InputStreamReader isr = new InputStreamReader(conf);
                BufferedReader br = new BufferedReader(isr);
                String config = "";
                String line;

                while (true) {
                    line = br.readLine();
                    if (line == null) break;
                    config += line + "\n";
                }
                br.readLine();
                OpenVpnApi.startVpn(this, config, server.getCountry(), server.getOvpnUserName(), server.getOvpnUserPassword());

                // Update log
                Toast.makeText(this, "جاري الاتصال...", Toast.LENGTH_SHORT).show();
                //    binding.logTv.setText("جاري الاتصال...");
            }
        } catch (IOException | RemoteException e) {
            e.printStackTrace();
        }
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStop() {
        if (server != null) {
            preference.saveServer(server);
        }

        super.onStop();
    }

    public boolean stopVpn() {
        try {
            preference.saveVpnStarted(false);
            vpnThread.stop();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        if(!isStartAnotherActivity){
            vpnThread.stop();
            preference.saveVpnStarted(false);
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("connectionState"));
        setStatus(vpnService.getStatus());
        VpnStatus.initLogCache(getCacheDir());

        super.onResume();
    }

    private void signupAsFirst() {
isStartAnotherActivity = true;
        Intent LoginIntent = new Intent(MainActivity.this, LoginActivity.class);
        LoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginIntent);
        finish();
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                setStatus(intent.getStringExtra("state"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void setStatus(String connectionState) {
        if (connectionState != null)
            switch (connectionState) {
                case "DISCONNECTED":
                    //      status("connect");
                    vpnService.setDefaultStatus();
                    preference.saveVpnStarted(false);
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    try {
                        setConnectButton(false);
                        if (isConnectAnimationStarted)
                            stopRotateAni();
                    } catch (Exception e) {
                    }
                    Toast.makeText(this, "غير متصل", Toast.LENGTH_SHORT).show();
                    //   binding.logTv.setText("");
                    break;
                case "CONNECTED":
                    // it will use after restart this activity
                    //    status("connected");
                    //  binding.logTv.setText("");
                    Toast.makeText(this, "متصل", Toast.LENGTH_SHORT).show();
                    preference.saveVpnStarted(true);
                    ref.child("Managers").child(userId).addListenerForSingleValueEvent(managerListener);
                    try {
                        stopRotateAni();
                        setConnectButton(true);
                    } catch (Exception e) {
                    }
                    break;
                case "WAIT":
                    try {
                        setConnectButton(false);
                        if (!isConnectAnimationStarted) {
                            startRotateAni(menuConnected);
                        }
                    } catch (Exception e) {

                    }
                    //   binding.logTv.setText("waiting for server connection!!");
                    //   Toast.makeText(this,"waiting for server connection!!",Toast.LENGTH_SHORT).show();
                    break;
                case "AUTH":
                    try {
                        setConnectButton(false);
                        if (!isConnectAnimationStarted) {
                            startRotateAni(menuConnected);
                        }
                    } catch (Exception e) {

                    }
                    //     binding.logTv.setText("server authenticating!!");
                    //    Toast.makeText(this,"server authenticating!!",Toast.LENGTH_SHORT).show();
                    break;
                case "RECONNECTING":
                    try {
                        setConnectButton(false);
                        if (!isConnectAnimationStarted) {
                            startRotateAni(menuConnected);
                        }
                    } catch (Exception e) {

                    }
                    //status("connecting");
                    //       Toast.makeText(this,"RECONNECTING",Toast.LENGTH_SHORT).show();
                    break;
                case "NONETWORK":
                    try {
                        stopRotateAni();
                        setConnectButton(false);
                        preference.saveVpnStarted(false);
                    } catch (Exception e) {

                    }
                    //      binding.logTv.setText("No network connection");
                    //  Toast.makeText(this,"لا يوجد ",Toast.LENGTH_SHORT).show();
                    break;
            }
    }

    MenuItem menuConnected;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user, menu);
        men = menu;
        menuConnected = menu.findItem(R.id.menu_connected);
        preference = new SharedPreference(this);
        if (vpnService.getStatus().matches("CONNECTED")) {
            menuConnected.setIcon(R.drawable.connected);
            menuConnected.setTitle("متصل");
        } else if (vpnService.getStatus().matches("DISCONNECTED") || vpnService.getStatus().matches("NONETWORK")) {
            menuConnected.setIcon(R.drawable.reconnect);
            menuConnected.setTitle("غير متصل");
            isConnectAnimationStarted = false;
        } else {
            startRotateAni(menuConnected);
        }
        return super.onCreateOptionsMenu(menu);

    }

    private void startRotateAni(MenuItem item) {
        menuConnected = item;

        //This uses an ImageView to set the ActionView of the MenuItem so that we can use this ImageView to display the rotation animation.
        ImageView refreshActionView = (ImageView) getLayoutInflater().inflate(R.layout.iv_connect, null);

        refreshActionView.setImageResource(R.drawable.reconnect_);
        menuConnected.setActionView(refreshActionView);

        Animation rotateAni = AnimationUtils.loadAnimation(this,
                R.anim.rotate_connect);
        LinearInterpolator lin = new LinearInterpolator();
        rotateAni.setInterpolator(lin);
        item.setCheckable(false);// Do not accept any click events while scanning
        refreshActionView.startAnimation(rotateAni);
        isConnectAnimationStarted = true;
    }

    private void stopRotateAni() {
        if (menuConnected != null) {
            menuConnected.setCheckable(true);
            View view = menuConnected.getActionView();
            if (view != null) {
                view.clearAnimation();
                menuConnected.setActionView(null);
            }
            isConnectAnimationStarted = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connected:
                if (!isConnectAnimationStarted && !preference.isVpnStarted()) {
                    startRotateAni(item);
                    prepareVpn();
                }
                return true;

            case R.id.menu_logout:

                return true;
            case R.id.menu_about:

                return true;
            case R.id.menu_exit:
                closeApplication();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setConnectButton(boolean isConnected) {
        //str is the string variable you pass from the Activity, it can be null...
        if (isConnected) {
            stopRotateAni();
            menuConnected.setIcon(R.drawable.connected);
            menuConnected.setTitle("متصل");
        } else {
            menuConnected.setIcon(R.drawable.reconnect);
            menuConnected.setTitle("غير متصل");
        }
    }

    public void closeApplication() {
        finish();
        finishAffinity();
        moveTaskToBack(true);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeApplication();
    }

    private String getDate() {
        String date = "";
        Date now = new Date();
        Date alsoNow = Calendar.getInstance().getTime();
        date = new SimpleDateFormat("yyyy/MM/dd").format(now);
        return date;
    }

    private String getTime() {
        String time = "";
        Calendar calander = Calendar.getInstance();
        SimpleDateFormat simpledateformat = new SimpleDateFormat("HH:mm");
        time = simpledateformat.format(calander.getTime());
        return time;
    }

    public static void changeToolbarFont(Toolbar toolbar, Activity context) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);
            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                if (tv.getText().equals(toolbar.getTitle())) {
                    applyFont(tv, context);
                    break;
                }
            }
        }
    }

    public static void applyFont(TextView tv, Activity context) {
        tv.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/font_1.ttf"));
    }
}