package com.ei.zezoo;

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
import android.os.Environment;
import android.os.RemoteException;
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
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.shawnlin.numberpicker.NumberPicker;


import net.alhazmy13.mediapicker.Video.VideoPicker;
import net.alhazmy13.mediapicker.Video.VideoTags;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import de.blinkt.openvpn.OpenVpnApi;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.VpnStatus;

public class AddLessonActivity extends AppCompatActivity {
    Resources res;
    Toolbar toolbar;
    ImageView imgType, imgSubject;
    MaterialSpinner spinType, spinStudy, spinSubject, spinQuality;
    EditText title;
    Button choose;
    NumberProgressBar progress;
    LinearLayout linearQuality;
    TextView txtProgress, txtVS, txtVS1, txtTime;
    ProgressBar loading;
    FloatingActionButton submit;
    NumberPicker lessonN;
    private boolean isVideoCompressed = false;
    String fileName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lesson);
        res = getResources();
        preference = new SharedPreference(this);
        server = preference.getServer();
        connection = new CheckInternetConnection();
        toolbar = (Toolbar) findViewById(R.id.mainToolbar);
        toolbar.setTitle("أضافة درس جديد");
        toolbar.setTitleTextColor(res.getColor(R.color.white));
        toolbar.setBackgroundColor(res.getColor(R.color.orange));
        changeToolbarFont(toolbar, this);
        setSupportActionBar(toolbar);
        imgType = (ImageView) findViewById(R.id.imageType);
        imgSubject = (ImageView) findViewById(R.id.imageSubject);
        spinType = (MaterialSpinner) findViewById(R.id.spinnerType);
        spinStudy = (MaterialSpinner) findViewById(R.id.spinnerStudy);
        spinSubject = (MaterialSpinner) findViewById(R.id.spinnerSubject);
        spinQuality = (MaterialSpinner) findViewById(R.id.spinnerQuality);
        lessonN = (NumberPicker) findViewById(R.id.number_picker);
        title = (EditText) findViewById(R.id.Title);
        choose = (Button) findViewById(R.id.btn_file);
        progress = (NumberProgressBar) findViewById(R.id.progress);
        linearQuality = (LinearLayout) findViewById(R.id.linearQuality);
        txtProgress = (TextView) findViewById(R.id.txtProgress);
        txtVS = (TextView) findViewById(R.id.videoSize);
        txtVS1 = (TextView) findViewById(R.id.videoSize1);
        txtTime = (TextView) findViewById(R.id.time);
        loading = (ProgressBar) findViewById(R.id.loading);
        submit = (FloatingActionButton) findViewById(R.id.floatingActionSubmit);
        String Type = "", Study = "", Subject = "", Lesson = "";
        submit.hide(true);
        final Animation animFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        imgType.startAnimation(animFadeOut);
        ArrayList<String> type = new ArrayList<>();
        ArrayList<String> study = new ArrayList<>();
        ArrayList<String> subject = new ArrayList<>();
        ArrayList<String> quality = new ArrayList<>();
        quality.add("دقة ضعيفة جداً");
        quality.add("دقة ضعيفة");
        quality.add("دقة متوسطة");
        quality.add("دقة عالية");
        quality.add("دقة عالية جداً");
        spinQuality.setItems(quality);
        spinQuality.setSelectedIndex(2);
        for (int i = 100; i < 105; i++) {
            type.add(ElecUtils.getType(i));
        }
        imgType.setImageResource(ElecUtils.getResourceFromType(100));
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
                final Animation animFadeOut = AnimationUtils.loadAnimation(AddLessonActivity.this, R.anim.fade_in);
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
                final Animation animFadeOut = AnimationUtils.loadAnimation(AddLessonActivity.this, R.anim.fade_in);
                imgSubject.startAnimation(animFadeOut);
            }
        });
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choose.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                fileName = String.valueOf(spinType.getSelectedIndex() + 100) + String.valueOf(spinStudy.getSelectedIndex() + 100) + String.valueOf(spinSubject.getSelectedIndex() + 100) + String.valueOf(lessonN.getValue() + 100) + ".mp4";

                //   d.dismiss();
                new VideoPicker.Builder(AddLessonActivity.this)
                        .mode(VideoPicker.Mode.CAMERA_AND_GALLERY)
                        .directory(Environment.getExternalStorageDirectory() + "/ElectronicInstitute/media/Videos/")
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

    }

    ArrayList<String> videoPaths;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Toast.makeText(this, "يمكنك إرسال الملف دون ضغطه", Toast.LENGTH_LONG).show();
                videoPaths = data.getStringArrayListExtra(VideoPicker.EXTRA_VIDEO_PATH);

                File from = new File(videoPaths.get(0));
                File to = new File(Environment.getExternalStorageDirectory() + "/ElectronicInstitute/media/Videos/" + fileName);
                if (from.exists())
                    from.renameTo(to);
                videoPaths.set(0, to.toString());
                File file = new File(videoPaths.get(0));
                loading.setVisibility(View.GONE);
                choose.setVisibility(View.VISIBLE);
                linearQuality.setVisibility(View.VISIBLE);
                txtVS.setVisibility(View.VISIBLE);
                txtVS.setText(txtVS.getText() + String.valueOf((file.length() / 1024) / 1024) + "Mb");
                choose.setText("ضغط الملف");
                choose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        zipVideo();
                    }
                });
                submit.show(true);
            } else {
                loading.setVisibility(View.GONE);
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

    Long startTime;

    private void zipVideo() {
        choose.setText("إيقاف الضغط");
        txtProgress.setVisibility(View.VISIBLE);
        txtTime.setVisibility(View.VISIBLE);
        txtProgress.setText("جاري ضغط الفيديو..(0%)");
        progress.setVisibility(View.VISIBLE);
        progress.setMax(100);
        submit.hide(true);
        VideoQuality q = VideoQuality.MEDIUM;
        switch (spinQuality.getSelectedIndex()){
            case 0:
                q = VideoQuality.VERY_LOW;
                break;
            case 1:
                q = VideoQuality.LOW;
                break;
            case 2:
                q = VideoQuality.MEDIUM;
                break;
            case 3:
                q = VideoQuality.HIGH;
                break;
            case 4:
                q = VideoQuality.VERY_HIGH;
                break;
        }
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ElectronicInstitute" + File.separator + "media" + File.separator + "CompressedVideos" + File.separator);
        if(!f.exists())
            f.mkdirs();
        VideoCompressor.start(
                this, // => This is required if srcUri is provided. If not, pass null.
                null, // => Source can be provided as content uri, it requires context.
                videoPaths.get(0), // => This could be null if srcUri and context are provided.
                Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ElectronicInstitute" + File.separator + "media" + File.separator + "CompressedVideos" + File.separator + fileName,
                null, /*String, or null*/
                new CompressionListener() {
                    @Override
                    public void onStart() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                startTime = System.currentTimeMillis();
                                Toast.makeText(AddLessonActivity.this, "بدء الضغط", Toast.LENGTH_LONG).show();
                            }
                        });

                    }

                    @Override
                    public void onSuccess() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                choose.setVisibility(View.GONE);
                                txtVS1.setVisibility(View.VISIBLE);
                                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ElectronicInstitute" + File.separator + "media" + File.separator + "CompressedVideos" + File.separator + fileName);
                                txtVS1.setText(txtVS1.getText() + String.valueOf((file.length() / 1024) / 1024) + "Mb");
                                submit.show(true);
                                txtProgress.setText("تم ضغط الفيديو");
                                progress.setProgress(100);
                                txtTime.setVisibility(View.GONE);
                                isVideoCompressed = true;
                            }
                        });

                    }

                    @Override
                    public void onFailure(String failureMessage) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                txtTime.setVisibility(View.GONE);
                                txtProgress.setText("حدث خطأ! يرجى إعادة المحاولة");
                                choose.setText("ضغط الملف");
                                choose.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        zipVideo();
                                    }
                                });
                                //Toast.makeText(AddLessonActivity.this, "حدث خطأ أثناء ضغط الملف يرجى إعادة المحاولة", Toast.LENGTH_LONG).show();
                                Toast.makeText(AddLessonActivity.this, failureMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onProgress(float v) {
                        // Update UI with progress value
                        runOnUiThread(new Runnable() {
                            public void run() {

                                Long elapsedTime = System.currentTimeMillis() - startTime;
                                double allTimeForDownloading = (elapsedTime * 100.0 / v);
                                double remainingTime = allTimeForDownloading - elapsedTime;
                                Long seconds = ((long) remainingTime);
                                if (seconds > 60000) {
                                    int minute = (int) (seconds / 1000) / 60;
                                    int secondsP = (int) ((seconds / 1000) % 60);
                                    txtTime.setText("الوقت المتبقي : " + String.valueOf(minute) + " دقيقة و" + String.valueOf(secondsP) + " ثانية");
                                } else if (seconds == 60000) {
                                    txtTime.setText("الوقت المتبقي : دقيقة");
                                } else {
                                    txtTime.setText("الوقت المتبقي : " + String.valueOf(seconds / 1000) + " ثانية");
                                }
                                progress.setProgress((int) v);
                                txtProgress.setText("جاري ضغط الفيديو..(%" + String.valueOf(v).substring(0,5) + ")");
                            }
                        });
                    }

                    @Override
                    public void onCancelled() {
                        // On Cancelled
                    }
                }, new Configuration(q,
                        24, /*frameRate: int, or null*/
                        false,
                        null /*videoBitrate: int, or null*/)
        );
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VideoCompressor.cancel();
                fileName = "";
                submit.show(true);
                isVideoCompressed = false;
                txtProgress.setText("تم إيقاف ضغط الفيديو");
                choose.setVisibility(View.GONE);
            }
        });
        /** if(spinQuality.getSelectedIndex() == 0) {
         VideoCompress.compressVideoLow(videoPaths.get(0), Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "misdir" + File.separator + "test.mp4", new VideoCompress.CompressListener() {
        @Override public void onStart() {
        choose.setVisibility(View.GONE);
        linearProgress.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        progress.setMax(100);
        Toast.makeText(AddLessonActivity.this, "start", Toast.LENGTH_LONG).show();
        }

        @Override public void onSuccess() {
        Toast.makeText(AddLessonActivity.this, "finish", Toast.LENGTH_LONG).show();
        }

        @Override public void onFail() {
        Toast.makeText(AddLessonActivity.this, "fail", Toast.LENGTH_LONG).show();
        }

        @Override public void onProgress(float percent) {
        progress.setProgress((int) percent);
        }
        });
         }else if(spinQuality.getSelectedIndex() == 1){


         VideoCompress.compressVideoMedium(videoPaths.get(0), Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "misdir" + File.separator + "test.mp4", new VideoCompress.CompressListener() {
        @Override public void onStart() {
        choose.setVisibility(View.GONE);
        linearProgress.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        progress.setMax(100);
        Toast.makeText(AddLessonActivity.this, "start", Toast.LENGTH_LONG).show();
        }

        @Override public void onSuccess() {
        Toast.makeText(AddLessonActivity.this, "finish", Toast.LENGTH_LONG).show();
        }

        @Override public void onFail() {
        Toast.makeText(AddLessonActivity.this, "fail", Toast.LENGTH_LONG).show();
        }

        @Override public void onProgress(float percent) {
        progress.setProgress((int) percent);
        }
        });
         }else{

         VideoCompress.compressVideoHigh(videoPaths.get(0), Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "misdir" + File.separator + "test.mp4", new VideoCompress.CompressListener() {
        @Override public void onStart() {
        choose.setVisibility(View.GONE);
        linearProgress.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        progress.setMax(100);
        Toast.makeText(AddLessonActivity.this, "start", Toast.LENGTH_LONG).show();
        }

        @Override public void onSuccess() {
        Toast.makeText(AddLessonActivity.this, "finish", Toast.LENGTH_LONG).show();
        }

        @Override public void onFail() {
        Toast.makeText(AddLessonActivity.this, "fail", Toast.LENGTH_LONG).show();
        }

        @Override public void onProgress(float percent) {
        progress.setProgress((int) percent);
        }
        });
         }**/
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
    public void onPause() {

        super.onPause();
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