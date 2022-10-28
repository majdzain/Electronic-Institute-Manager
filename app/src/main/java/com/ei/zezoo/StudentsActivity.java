package com.ei.zezoo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.VpnService;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ei.zezoo.model.Server;
import com.ei.zezoo.model.Type;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.blinkt.openvpn.OpenVpnApi;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.VpnStatus;

public class StudentsActivity extends AppCompatActivity {
    ArrayList<String> listGroupTitles;
    HashMap<String, List<UserListChildItem>> listChildData;
    CustomStudentExpandableListAdapter expandableListAdapter;
    Resources res;
    AnimatedExpandableListView expListView;
    UserSQLDatabaseHandler dbu;
    PowerMenu powerMenu;
    UserListChildItem currentUser;
    private int lastExpandedPosition = -1;
    SharedPreference preference;
    private boolean isConnectAnimationStarted = false;
    boolean isStartAnotherActivity = false;
    Toolbar toolbar;
    Type Type;
    int type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students);
        res = getResources();
        preference = new SharedPreference(this);
        type = getIntent().getExtras().getInt("type",100);
        Type = preference.getType(type);
        server = preference.getServer();
        connection = new CheckInternetConnection();
        toolbar = (Toolbar) findViewById(R.id.mainToolbar);
        toolbar.setTitle("الطلاب");
        toolbar.setTitleTextColor(res.getColor(R.color.white));
        toolbar.setBackgroundColor(res.getColor(R.color.orange));
        changeToolbarFont(toolbar, this);
        setSupportActionBar(toolbar);
        dbu = new UserSQLDatabaseHandler(this);
        expListView = (AnimatedExpandableListView) findViewById(R.id.expList);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        expListView.setIndicatorBounds(width - GetPixelFromDips(360), width - GetPixelFromDips(320));
      //  String classes[] = {"الصف السابع","الصف الثامن","الصف التاسع","الصف العاشر علمي","الصف العاشر أدبي","الصف الحادي عشر علمي","الصف الحادي عشر أدبي","بكالوريا علمي","بكالوريا أدبي"};

        powerMenu = new PowerMenu.Builder(this)
                .addItem(new PowerMenuItem("عرض المعلومات وتعديلها", R.drawable.pop_view, false))
                .addItem(new PowerMenuItem("عرض كلمة السر", R.drawable.pop_view_pass, false))
                .addItem(new PowerMenuItem("عرض مدة التسجيل", R.drawable.pop_time, false))
                .addItem(new PowerMenuItem("كشف حساب", R.drawable.pop_cash, false))
                .addItem(new PowerMenuItem("اتصال...", R.drawable.pop_call, false))
                .addItem(new PowerMenuItem("حظر الطالب", R.drawable.pop_block, false)) // add an item.
                .addItem(new PowerMenuItem("حذف التسجيل", R.drawable.pop_delete, false)) // aad an item list.
                .setAnimation(MenuAnimation.SHOWUP_TOP_LEFT) // Animation start point (TOP | LEFT).
                .setMenuRadius(10f) // sets the corner radius.
                .setMenuShadow(10f) // sets the shadow.
                .setTextColor(ContextCompat.getColor(this, R.color.md_grey_800))
                .setTextGravity(Gravity.CENTER)
                .setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
                .setSelectedTextColor(Color.WHITE)
                .setMenuColor(Color.WHITE)
                .setSelectedMenuColorResource(R.color.orange)
                .setOnMenuItemClickListener(new OnMenuItemClickListener<PowerMenuItem>() {
                    @Override
                    public void onItemClick(int position, PowerMenuItem item) {

                    }
                })
                .build();
        createExpList(Type.getStudiesN(),dbu.allUsers());
    }

    private void createExpList(List<String> folders, List<UserListChildItem> users) {
        final Animation animFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        expListView.startAnimation(animFadeOut);
        expListView.setVisibility(View.VISIBLE);
        // Get the expandable list
        String[] itemsFolders = new String[folders.size()];
        for (int i = 0; i < folders.size(); i++) {
            itemsFolders[i] = folders.get(i);
        }
        // Setting up list
        listGroupTitles = new ArrayList<String>(Arrays.asList(itemsFolders));
        listChildData = new HashMap<String, List<UserListChildItem>>();
        // Adding district names and number of poplation as children
        for (int i1 = 0; i1 < listGroupTitles.size(); i1++) {
            String folder = itemsFolders[i1];
            List<UserListChildItem> pDistricts= new ArrayList<UserListChildItem>();
            for (int i = 0; i < users.size(); i++) {
                if(users.get(i).getType() == type && !users.get(i).isTeacher() && ElecUtils.getStudy(type,users.get(i).getStudy()).matches(folder)){
                    pDistricts.add(users.get(i));
                }
            }
            listChildData.put(folder, pDistricts);
        }
        expandableListAdapter = new CustomStudentExpandableListAdapter(this, listGroupTitles, listChildData);
        // Setting list adapter
        expListView.setAdapter(expandableListAdapter);

        expListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ExpandableListView listView = (ExpandableListView) parent;
                long pos = listView.getExpandableListPosition(position);

                // get type and correct positions
                int itemType = ExpandableListView.getPackedPositionType(pos);
                int groupPos = ExpandableListView.getPackedPositionGroup(pos);
                int childPos = ExpandableListView.getPackedPositionChild(pos);

                // if child is long-clicked
                if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    Toast.makeText(StudentsActivity.this,"aaa",Toast.LENGTH_LONG).show();
                    currentUser= (UserListChildItem) expandableListAdapter.getChild(groupPos, childPos);
                    powerMenu.showAsDropDown(view);
                } else if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    //createPopupGroupItemMenu(view, groupPos);
                }
                return true;
            }
        });
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (expListView.isGroupExpanded(groupPosition)) {
                    expListView.collapseGroupWithAnimation(groupPosition);
                } else {
                    expListView.expandGroupWithAnimation(groupPosition);
                }
                return true;
            }
        });
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int i) {
                if (lastExpandedPosition != -1
                        && i != lastExpandedPosition) {
                    expListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = i;
            }
        });
    }

    MenuItem menuConnected;
    Menu men;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_students_teachers, menu);
        men = menu;
        menuConnected = menu.findItem(R.id.menu_connected);
        preference = new SharedPreference(this);
        if(vpnService.getStatus().matches("CONNECTED")) {
            menuConnected.setIcon(R.drawable.connected);
            menuConnected.setTitle("متصل");
        }else if(vpnService.getStatus().matches("DISCONNECTED") || vpnService.getStatus().matches("NONETWORK") ) {
            menuConnected.setIcon(R.drawable.reconnect);
            menuConnected.setTitle("غير متصل");
            isConnectAnimationStarted = false;
        }else{
            startRotateAni(menuConnected);
        }
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connected:
                if(!isConnectAnimationStarted && !preference.isVpnStarted()){
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


    public void setConnectButton(boolean isConnected) {
        //str is the string variable you pass from the Activity, it can be null...
        if(isConnected) {
            stopRotateAni();
            menuConnected.setIcon(R.drawable.connected);
            menuConnected.setTitle("متصل");
        }else {
            menuConnected.setIcon(R.drawable.reconnect);
            menuConnected.setTitle("غير متصل");
        }
    }

    public int GetPixelFromDips(float pixels) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
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
        super.onDestroy();
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("connectionState"));
        setStatus(vpnService.getStatus());
        VpnStatus.initLogCache(getCacheDir());
        super.onResume();
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