package com.ei.zezoo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.ei.zezoo.model.Server;
import com.github.clans.fab.FloatingActionButton;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.jaredrummler.materialspinner.MaterialSpinner;


import net.alhazmy13.mediapicker.Video.VideoPicker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.blinkt.openvpn.OpenVpnApi;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.VpnStatus;
import us.raudi.pushraven.Message;
import us.raudi.pushraven.Notification;
import us.raudi.pushraven.Pushraven;

public class EducationActivity extends AppCompatActivity {
    BarChart mChart;
    Resources res;
    Toolbar toolbar;
    TextView total;
    Button viewStudents, viewTeachers, viewSubjects, viewQues, viewBills, signStudent, signTeacher, sendAd, addLesson, addTut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_education);
        res = getResources();

        preference = new SharedPreference(this);
        server = preference.getServer();
        connection = new CheckInternetConnection();
        toolbar = (Toolbar) findViewById(R.id.mainToolbar);
        toolbar.setTitle("دورات التربية السورية");
        toolbar.setTitleTextColor(res.getColor(R.color.white));
        toolbar.setBackgroundColor(res.getColor(R.color.orange));
        changeToolbarFont(toolbar, this);
        setSupportActionBar(toolbar);
        mChart = (BarChart) findViewById(R.id.chart);
        total = (TextView) findViewById(R.id.total);
        viewStudents = (Button) findViewById(R.id.viewStudents);
        viewTeachers = (Button) findViewById(R.id.viewTeachers);
        viewSubjects = (Button) findViewById(R.id.viewSubjects);
        viewQues = (Button) findViewById(R.id.viewQues);
        viewBills = (Button) findViewById(R.id.viewBills);
        signStudent = (Button) findViewById(R.id.signStudent);
        signTeacher = (Button) findViewById(R.id.signTeacher);
        addLesson = (Button) findViewById(R.id.addLesson);
        addTut = (Button) findViewById(R.id.addTut);
        sendAd = (Button) findViewById(R.id.sendAd);
        total.setText("عدد الطلاب الإجمالي : " + String.valueOf(preference.getType(100).getStudentsN().size()));
        createChart();
        viewStudents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStartAnotherActivity = true;
                Intent intent = new Intent(EducationActivity.this, StudentsActivity.class);
                intent.putExtra("type",100);
                startActivity(intent);
            }
        });
        viewTeachers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStartAnotherActivity = true;

                Intent intent = new Intent(EducationActivity.this, TeachersActivity.class);
                intent.putExtra("type",100);
                startActivity(intent);
            }
        });
        viewSubjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        viewQues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        viewBills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        signStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStartAnotherActivity = true;
                Intent intent = new Intent(EducationActivity.this, SignActivity.class);
                intent.putExtra("type",100);
                startActivity(intent);
            }
        });
        signTeacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        addLesson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStartAnotherActivity = true;
Intent i = new Intent(EducationActivity.this,AddLessonActivity.class);
startActivity(i);
            }
        });
        addTut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        sendAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAdDialog();
            }
        });

    }

    private void createAdDialog() {
        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.dialog_send_notification);
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ImageView imgTypeN = (ImageView) d.findViewById(R.id.imageTypeN);
        ImageView imgType = (ImageView) d.findViewById(R.id.imageType);
        ImageView imgSubject = (ImageView) d.findViewById(R.id.imageSubject);
        MaterialSpinner spinTypeN = (MaterialSpinner) d.findViewById(R.id.spinnerTypeN);
        MaterialSpinner spinType = (MaterialSpinner) d.findViewById(R.id.spinnerType);
        MaterialSpinner spinStudy = (MaterialSpinner) d.findViewById(R.id.spinnerStudy);
        MaterialSpinner spinSubject = (MaterialSpinner) d.findViewById(R.id.spinnerSubject);
        LinearLayout linearStudy = (LinearLayout) d.findViewById(R.id.Study);
        LinearLayout linearSubject = (LinearLayout) d.findViewById(R.id.Subject);
        EditText title = (EditText) d.findViewById(R.id.Title);
        EditText text = (EditText) d.findViewById(R.id.Text);

        ArrayList<String> typeN = new ArrayList<>();
        ArrayList<String> type = new ArrayList<>();
        ArrayList<String> study = new ArrayList<>();
        ArrayList<String> subject = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            typeN.add(ElecUtils.getNamefromTypeN(i));
        }
        for (int i = 100; i < 105; i++) {
            type.add(ElecUtils.getType(i));
        }
        type.add("جميع الدورات");
        imgTypeN.setImageResource(ElecUtils.getResourceFromTypeN(0));
        imgType.setImageResource(ElecUtils.getResourceFromType(100));
        final Animation animFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        imgTypeN.startAnimation(animFadeOut);
        imgType.startAnimation(animFadeOut);
        spinTypeN.setItems(typeN);
        spinType.setItems(type);
        for (int i = 100; i < 109; i++) {
            study.add(ElecUtils.getStudy(spinType.getSelectedIndex() + 100, i));
        }
        spinStudy.setItems(study);
        for (int i = 100; i < 118; i++) {
            subject.add(ElecUtils.getSubject(spinType.getSelectedIndex() + 100, spinStudy.getSelectedIndex() + 100, i));
        }
        spinSubject.setItems(subject);
        imgSubject.setImageResource(ElecUtils.getResourceFromSubject(spinType.getSelectedIndex() + 100, spinStudy.getSelectedIndex() + 100, 100));
        spinTypeN.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                imgTypeN.setImageResource(ElecUtils.getResourceFromTypeN(position));
                final Animation animFadeOut = AnimationUtils.loadAnimation(EducationActivity.this, R.anim.fade_in);
                imgTypeN.startAnimation(animFadeOut);
                if (position == 0) {
                    linearSubject.setVisibility(View.VISIBLE);
                    linearSubject.startAnimation(animFadeOut);

                    for (int i = 100; i < 118; i++) {
                        subject.add(ElecUtils.getSubject(spinType.getSelectedIndex() + 100, spinStudy.getSelectedIndex() + 100, i));
                    }
                    spinSubject.setItems(subject);
                    imgSubject.setImageResource(ElecUtils.getResourceFromSubject(spinType.getSelectedIndex() + 100, spinStudy.getSelectedIndex() + 100, 100));
                } else {
                    linearSubject.setVisibility(View.GONE);
                }
            }
        });
        spinType.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                imgType.setImageResource(ElecUtils.getResourceFromType(position + 100));
                final Animation animFadeOut = AnimationUtils.loadAnimation(EducationActivity.this, R.anim.fade_in);
                imgType.startAnimation(animFadeOut);
            }
        });
        spinStudy.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {

            }
        });
        spinSubject.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                imgSubject.setImageResource(ElecUtils.getResourceFromSubject(spinType.getSelectedIndex() + 100, spinStudy.getSelectedIndex() + 100, position + 100));
                final Animation animFadeOut = AnimationUtils.loadAnimation(EducationActivity.this, R.anim.fade_in);
                imgSubject.startAnimation(animFadeOut);
            }
        });
        sendAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    Pushraven.setCredential(GoogleCredential.fromStream(new FileInputStream("service_account.json")));
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(EducationActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
                Pushraven.setProjectId("elec-46701");
                Notification not = new Notification()
                        .title("Hello World")
                        .body(String.valueOf(preference.getInstitute().getLastNotifId() + 1)
                                + String.valueOf(spinTypeN.getSelectedIndex())
                                + String.valueOf(spinType.getSelectedIndex() + 100)
                                + String.valueOf(spinStudy.getSelectedIndex() + 100)
                                + ((spinTypeN.getSelectedIndex() == 0) ? String.valueOf(spinSubject.getSelectedIndex() + 100) : "")
                                + text.getText().toString());
                Message raven = new Message()
                        .name(String.valueOf(preference.getInstitute().getLastNotifId() + 1))
                        .notification(not);
                Pushraven.push(raven);
            }
        });
        d.setCanceledOnTouchOutside(true);
        d.show();
    }

    Button choose;
    NumberProgressBar progress;
    LinearLayout linearProgress;
    TextView txtProgress;
    ProgressBar loading;

    boolean isChoosen = false;
    Dialog d;
    void createNewLessonDialog() {
      d  = new Dialog(this);
        d.setContentView(R.layout.dialog_upload_lesson);
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ImageView imgType = (ImageView) d.findViewById(R.id.imageType);
        ImageView imgSubject = (ImageView) d.findViewById(R.id.imageSubject);
        MaterialSpinner spinType = (MaterialSpinner) d.findViewById(R.id.spinnerType);
        MaterialSpinner spinStudy = (MaterialSpinner) d.findViewById(R.id.spinnerStudy);
        MaterialSpinner spinSubject = (MaterialSpinner) d.findViewById(R.id.spinnerSubject);
        EditText title = (EditText) d.findViewById(R.id.Title);
        choose = (Button) d.findViewById(R.id.btn_file);
        progress = (NumberProgressBar) d.findViewById(R.id.progress);
        linearProgress = (LinearLayout) d.findViewById(R.id.linearProgress);
        txtProgress = (TextView) d.findViewById(R.id.txtProgress);
        loading = (ProgressBar) d.findViewById(R.id.loading);
        FloatingActionButton submit = (FloatingActionButton) d.findViewById(R.id.floatingActionSubmit);
        String Type = "", Study = "", Subject = "", Lesson = "";
        ArrayList<String> type = new ArrayList<>();
        ArrayList<String> study = new ArrayList<>();
        ArrayList<String> subject = new ArrayList<>();
        for (int i = 100; i < 105; i++) {
            type.add(ElecUtils.getType(i));
        }
        type.add("جميع الدورات");
        imgType.setImageResource(ElecUtils.getResourceFromTypeN(100));
        final Animation animFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        imgType.startAnimation(animFadeOut);
        spinType.setItems(type);
        for (int i = 100; i < 109; i++) {
            study.add(ElecUtils.getStudy(spinType.getSelectedIndex() + 100, i));
        }
        spinStudy.setItems(study);
        for (int i = 100; i < 118; i++) {
            subject.add(ElecUtils.getSubject(spinType.getSelectedIndex() + 100, spinStudy.getSelectedIndex() + 100, i));
        }
        spinSubject.setItems(subject);
        imgSubject.setImageResource(ElecUtils.getResourceFromSubject(spinType.getSelectedIndex() + 100, spinStudy.getSelectedIndex() + 100, 100));
        spinType.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                imgType.setImageResource(ElecUtils.getResourceFromType(position + 100));
                final Animation animFadeOut = AnimationUtils.loadAnimation(EducationActivity.this, R.anim.fade_in);
                imgType.startAnimation(animFadeOut);
            }
        });
        spinStudy.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {

            }
        });
        spinSubject.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                imgSubject.setImageResource(ElecUtils.getResourceFromSubject(spinType.getSelectedIndex() + 100, spinStudy.getSelectedIndex() + 100, position + 100));
                final Animation animFadeOut = AnimationUtils.loadAnimation(EducationActivity.this, R.anim.fade_in);
                imgSubject.startAnimation(animFadeOut);
            }
        });
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choose.setVisibility(View.GONE);
                linearProgress.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
             //   d.dismiss();
                new VideoPicker.Builder(EducationActivity.this)
                        .mode(VideoPicker.Mode.GALLERY)
                        .directory(VideoPicker.Directory.DEFAULT)
                        //.extension(VideoPicker.Extension.MP4)
                        .enableDebuggingMode(true)
                        .build();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        d.setCanceledOnTouchOutside(false);
       // d.setCancelable(false);
        d.show();
    }

    private void createChart() {
        String[] labels = {"العاشر العلمي", "العاشر الأدبي", "الحادي عشر العلمي", "الحادي عشر الأدبي", "البكالوريا العلمي", "البكالوريا الأدبي"};
        List<String> xAxisValues = new ArrayList<>(Arrays.asList("العاشر العلمي", "العاشر الأدبي", "الحادي عشر العلمي", "الحادي عشر الأدبي", "البكالوريا العلمي", "البكالوريا الأدبي"));
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);
        mChart.getXAxis().setEnabled(true);
        mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getXAxis().setLabelRotationAngle(-10);
        mChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(xAxisValues));
        mChart.getDescription().setEnabled(false);
        mChart.setPinchZoom(false);
        mChart.setScaleEnabled(false);
        mChart.setDrawGridBackground(false);
        mChart.getAxisLeft().setEnabled(true);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getAxisRight().setEnabled(false);
        mChart.getLegend().setEnabled(false);

        float groupSpace = 0.08f;
        float barSpace = 0.03f; // x4 DataSet
        float barWidth = 0.5f; // x4 DataSet
        int groupCount = 4;
        int startYear = 2018;

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        ArrayList<BarEntry> yVals2 = new ArrayList<BarEntry>();
        ArrayList<BarEntry> yVals3 = new ArrayList<BarEntry>();
        ArrayList<BarEntry> yVals4 = new ArrayList<BarEntry>();
        ArrayList<BarEntry> yVals5 = new ArrayList<BarEntry>();
        ArrayList<BarEntry> yVals6 = new ArrayList<BarEntry>();

        yVals1.add(new BarEntry(0, Float.parseFloat("16")));
        yVals2.add(new BarEntry(1, Float.parseFloat("4")));
        yVals3.add(new BarEntry(2, Float.parseFloat("23")));
        yVals4.add(new BarEntry(3, Float.parseFloat("12")));
        yVals5.add(new BarEntry(4, Float.parseFloat("42")));
        yVals6.add(new BarEntry(5, Float.parseFloat("33")));
        BarDataSet set1, set2, set3, set4, set5, set6;

        // create 4 DataSets
        set1 = new BarDataSet(yVals1, "العاشر العلمي");
        set1.setColor(res.getColor(R.color.red));
        set2 = new BarDataSet(yVals2, "العاشر الأدبي");
        set2.setColor(res.getColor(R.color.darker_red));
        set3 = new BarDataSet(yVals3, "الحادي عشر العلمي");
        set3.setColor(res.getColor(R.color.orange));
        set4 = new BarDataSet(yVals4, "الحادي عشر الأدبي");
        set4.setColor(res.getColor(R.color.darker_orange));
        set5 = new BarDataSet(yVals5, "البكالوريا العلمي");
        set5.setColor(res.getColor(R.color.blue));
        set6 = new BarDataSet(yVals6, "البكالوريا الأدبي");
        set6.setColor(res.getColor(R.color.darker_blue));

        BarData data = new BarData(set1, set2, set3, set4, set5, set6);

        mChart.setData(data);
        // specify the width each bar should have
        mChart.getBarData().setBarWidth(barWidth);
        // restrict the x-axis range
        //  mChart.getXAxis().setAxisMinimum(startYear);
        // barData.getGroupWith(...) is a helper that calculates the width each group needs based on the provided parameters
        //  mChart.getXAxis().setAxisMaximum(startYear + mChart.getBarData().getGroupWidth(groupSpace, barSpace) * groupCount);
        //   mChart.groupBars(0, groupSpace, barSpace);
        mChart.animateY(2000);
        mChart.invalidate();
        //  mChart.zoom(1.8f, 0f, 0f, 0f);
    }

    public class LabelFormatter implements IAxisValueFormatter {
        private final String[] mLabels;

        public LabelFormatter(String[] labels) {
            mLabels = labels;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return mLabels[(int) value];
        }
    }


    private OpenVPNThread vpnThread = new OpenVPNThread();
    private SharedPreference preference;
    private CheckInternetConnection connection = new CheckInternetConnection();
    MenuItem menuConnected;
    boolean isStartAnotherActivity = false;
    private OpenVPNService vpnService = new OpenVPNService();
    private Server server;
    private Menu men;
    private boolean isConnectAnimationStarted = false;

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
            Toast.makeText(this, "prepare", Toast.LENGTH_SHORT).show();
            //     Toast.makeText(SplashActivity.this, "Vpn Started", Toast.LENGTH_LONG).show();
            prepareVpn();
        }
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

    @Override
    protected void onDestroy() {
        // ref.child("Users").child(userId).removeEventListener(managerListener);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        //  ref.child("Users").child(userId).removeEventListener(managerListener);
        super.onStop();
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("connectionState"));
        setStatus(vpnService.getStatus());
        VpnStatus.initLogCache(getCacheDir());
        if (!preference.isVpnStarted()) {
            prepareVpn();
        }
        super.onResume();
    }

    public void prepareVpn() {
        if (getInternetStatus()) {
            // Checking permission for network monitor
            Intent intent = VpnService.prepare(this);
            if (intent != null) {
                startActivityForResult(intent, 1);
            } else startVpn();//have already permission

            // Update confection status
            //status("connecting");

        } else {

            // No internet connection available
            Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onPause(){

        super.onPause();
        if(d != null)
            d.dismiss();
    }

    public boolean getInternetStatus() {

        return connection.netCheck(this);
    }

    ArrayList<String> videoPaths;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                d.show();
                Toast.makeText(this, "جاري تجهيز الفيديو", Toast.LENGTH_LONG).show();
                videoPaths =  data.getStringArrayListExtra(VideoPicker.EXTRA_VIDEO_PATH);
                zipVideo();
            } else {
                loading.setVisibility(View.GONE);
                linearProgress.setVisibility(View.GONE);
                choose.setVisibility(View.VISIBLE);
                Toast.makeText(this, "يرجى اختيار فيديو", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                //Permission granted, start the VPN
                startVpn();
            } else
                Toast.makeText(this, "يرجى السماح للبرنامج بالاتصال", Toast.LENGTH_LONG).show();
        }
    }

    private void zipVideo() {

        //Fetch the list of files that needs to be zipped
       /** File file = new File(videoPaths.get(0));
        File[] files = file.listFiles();
        LinkedList<File> fileList = new LinkedList<>();
        fileList.add(file);

        //Perform the zip operation
        WZip wZip = new WZip();
        wZip.zip(fileList,
                new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "misdir" + File.separator + "test.zip"),
                "rar",
                this);**/


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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_login, menu);
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

            case R.id.menu_u_sign:

                return true;
            case R.id.menu_u_about:

                return true;
            case R.id.menu_u_exit:
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
        isStartAnotherActivity = true;
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