package com.ei.zezoo;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.VpnService;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.ei.zezoo.model.Institute;
import com.ei.zezoo.model.Manager;
import com.ei.zezoo.model.Server;
import com.ei.zezoo.model.Type;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
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

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    Button sign;
    private FirebaseAuth firebasing;
    private ProgressDialog Loading;
    private DatabaseReference referroot;
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    Button btn_sign;
    private FirebaseAuth mauth;
    private FirebaseUser currentuser;
    private UserSQLDatabaseHandler dbu;
    private Manager manager;
    private Bitmap my_image;
    private DatabaseReference ref;
    private String userId;
    private ProgressDialog Loadingbar;
    boolean isStartAnotherActivity = false;
    private OpenVPNService vpnService = new OpenVPNService();
    private Server server;
    //   LessonSQLDatabaseHandler dbl;
    private Toolbar toolbar;
    private Menu men;
    private boolean isConnectAnimationStarted = false;
    private ValueEventListener managerListener, infoListener, usersListener;
    ArrayList<String> sub1, sub2, sub3, sub4, sub5;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        preference = new SharedPreference(this);
        server = preference.getServer();
        firebasing = FirebaseAuth.getInstance();
        referroot = FirebaseDatabase.getInstance().getReference();
        Loading = new ProgressDialog(LoginActivity.this);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        //  dbl = new LessonSQLDatabaseHandler(this);
        dbu = new UserSQLDatabaseHandler(this);
        // dbl.allLessons();
        populateAutoComplete();
        toolbar = (Toolbar) findViewById(R.id.mainToolbar);
        toolbar.setTitle("المعهد الإلكتروني");
        operations = new ArrayList<>();
        operationsDates = new ArrayList<>();
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setBackgroundColor(getResources().getColor(R.color.orange));
        changeToolbarFont(toolbar, this);
        setSupportActionBar(toolbar);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
                    progressBar.setProgress(10);
                    ref.child("info").addListenerForSingleValueEvent(infoListener);
                    //  ref.child("Managers").child(userId).removeEventListener(this);

                } else {
                    d.cancel();
                    Toast.makeText(LoginActivity.this, "تم حذف حسابك يرجى مراجعة المعهد!", Toast.LENGTH_LONG).show();
                    mauth.signOut();
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
                    progressBar.setProgress(15);
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
                    progressBar.setProgress(30);
                    Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        numUsers++;
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



    private void saveUser(String value, boolean hasNext) {
        ref.child("Users").child(value).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserListChildItem u = new UserListChildItem();
                    //String snow = dataSnapshot.child("isNow").getValue().toString();
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
                    String sban = dataSnapshot.child("isBanned").getValue().toString();
                    String sstatus = dataSnapshot.child("status").getValue().toString();

                    String stype = dataSnapshot.child("type").getValue().toString();
                    String spass = dataSnapshot.child("pass").getValue().toString();

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
                        if (steacher.contains("1"))
                            u.setTeacher(true);
                        else
                            u.setTeacher(false);
                        if(simage.matches("uri")) {
                            Bitmap im = BitmapFactory.decodeResource(getResources(), R.drawable.profile_image);
                            u.setProfile(im);
                        }else
                            u.setProfile(null);
                        u.setStatus(sstatus);
                        u.setColumn(dbu.allUsers().size());
                        dbu.addUser(u);
                        progressBar.setProgress(progressBar.getProgress() + (50/numUsers));
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
                                        progressBar.setProgress(100);
                                        isStartAnotherActivity = true;
                                        Intent in = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(in);
                                        d.cancel();
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


    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        boolean cancel = false;
        View focusView = null;
        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError("كلمة السر قصيرة جدا");
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError("هذه الخانة مطلوبة");
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError("هذا الإيميل غير صالح");
            focusView = mEmailView;
            cancel = true;
        }
        boolean isExist = false;
        for (int i = 0; i < dbu.allUsers().size(); i++) {
            if (dbu.allUsers().get(i).getUser().matches(mEmailView.getText().toString()))
                isExist = true;
        }
        if (isExist) {
            cancel = true;
            mEmailView.setError("الحساب موجود مسبقا");
            focusView = mEmailView;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.

            focusView.requestFocus();
        } else {
            createDownloadProgressDialog();
        //    Loading.setTitle("تسجيل الدخول...");
         //   Loading.setMessage("يرجى الإنتظار...");
       //     Loading.setCanceledOnTouchOutside(false);
         //   Loading.show();
            firebasing.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                progressBar.setProgress(5);
                                dbu = new UserSQLDatabaseHandler(LoginActivity.this);
                                dbu.allUsers();
                                manager = new Manager();
                                mauth = FirebaseAuth.getInstance();
                                currentuser = mauth.getCurrentUser();
                                ref = FirebaseDatabase.getInstance().getReference();
                                if (currentuser != null) {
                                    userId = currentuser.getUid();
                                    ref.child("Managers").child(userId).addListenerForSingleValueEvent(managerListener);
                                }

                            } else {

                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "خطأ " + message, Toast.LENGTH_LONG).show();
                                Loading.dismiss();
                            }
                        }
                    });
        }
    }
    int numUsers = 0;
    Dialog d;
NumberProgressBar progressBar;
    private void createDownloadProgressDialog() {
        d = new Dialog(this);
        d.setContentView(R.layout.dialog_download_database);
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressBar = (NumberProgressBar) d.findViewById(R.id.progress);
        progressBar.setMax(100);
        d.setCanceledOnTouchOutside(false);
        d.setCancelable(false);
        d.show();
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

    private DatabaseReference groupref;
    boolean isLastSubject = false;

    private void RetrieveGroups(UserListChildItem user) {

        //  groupref = FirebaseDatabase.getInstance().getReference().child(ElecUtils.getType(user.getType())).child(ElecUtils.getStudy(user.getType(),user.getStudy()));
        //  groupref.addListenerForSingleValueEvent(groupListener);
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

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
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

    private OpenVPNThread vpnThread = new OpenVPNThread();
    private SharedPreference preference;

    @Override
    protected void onDestroy() {
        // ref.child("Users").child(userId).removeEventListener(managerListener);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        if (!isStartAnotherActivity) {
            vpnThread.stop();
            preference.saveVpnStarted(false);
        }
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

    private CheckInternetConnection connection = new CheckInternetConnection();

    public boolean getInternetStatus() {

        return connection.netCheck(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            //Permission granted, start the VPN
            startVpn();
        } else {
            Toast.makeText(this, "يرجى السماح للبرنامج بالاتصال", Toast.LENGTH_LONG).show();
        }
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

    MenuItem menuConnected;

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
}

