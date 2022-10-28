package com.ei.zezoo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.ei.zezoo.model.Server;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.tsongkha.spinnerdatepicker.DatePicker;
import com.tsongkha.spinnerdatepicker.DatePickerDialog;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import android.os.RemoteException;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import de.blinkt.openvpn.OpenVpnApi;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.hdodenhof.circleimageview.CircleImageView;

public class SignActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    EditText edit_name, edit_address, edit_email, edit_from, edit_father, edit_mother, edit_id, edit_user, edit_pass;
    PrefixEditText edit_num, edit_num_;
    PinEntryEditText edit_coupon;
    Spinner spin_study;
    LinearLayout btn_birthday;
    TextView txt_birthday;
    CheckBox check_100, check_101, check_102, check_103, check_104, check_105, check_106, check_107, check_108, check_109, check_110, check_111, check_112, check_113, check_114, check_115, check_116, check_117;
   ScrollView scrollView;
    private ProgressDialog Loadingbar;
    private CircleImageView image;
    private FirebaseAuth mauth;
    private DatabaseReference ref;
    private FirebaseUser currentuser;
    private String userId;
    private StorageReference userprofileimagereference;
    private static final int gallerypic = 1;
    private UserSQLDatabaseHandler dbu;
    private UserListChildItem u;
    private Bitmap my_image;
    private ValueEventListener userListener;
    boolean isSubmitHidden = false;
    Dialog d;
    NumberProgressBar progressBar;
    MyTextView title;

    private void createDownloadProgressDialog() {
        d = new Dialog(this);
        d.setContentView(R.layout.dialog_download_database);
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressBar = (NumberProgressBar) d.findViewById(R.id.progress);
        title = (MyTextView) d.findViewById(R.id.title);
        title.setText("حفظ طالب جديد");
        progressBar.setMax(100);
        d.setCanceledOnTouchOutside(false);
        d.setCancelable(false);
        d.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        mauth = FirebaseAuth.getInstance();
        Loadingbar = new ProgressDialog(SignActivity.this);
        preference = new SharedPreference(this);
        server = preference.getServer();
        image = (CircleImageView) findViewById(R.id.set_profile_image);
        connection = new CheckInternetConnection();
        //RetrieveUserInfo();
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStartAnotherActivity = true;
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(SignActivity.this);
                /** Intent intent = new Intent();
                 intent.setType("image/*");
                 intent.setAction(Intent.ACTION_GET_CONTENT);
                 startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);**/
            }
        });
        androidx.appcompat.app.ActionBar actionbar = getSupportActionBar();
        TextView textview = new TextView(getApplicationContext());
        RelativeLayout.LayoutParams layoutparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        textview.setLayoutParams(layoutparams);
        textview.setText("دورات مناهج التربية السورية");
        textview.setTextColor(Color.WHITE);
        textview.setGravity(Gravity.RIGHT);
        textview.setTextSize(20);
        actionbar.setDisplayOptions(androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setCustomView(textview);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionSubmitSign);
        scrollView = (ScrollView) findViewById(R.id.scrollView) ;
        edit_name = (EditText) findViewById(R.id.edit_name);
        edit_num = (PrefixEditText) findViewById(R.id.edit_num_ph);
        edit_num_ = (PrefixEditText) findViewById(R.id.edit_num_ph_);
        edit_address = (EditText) findViewById(R.id.edit_place);
        edit_father = (EditText) findViewById(R.id.edit_father);
        edit_mother = (EditText) findViewById(R.id.edit_mother);
        edit_from = (EditText) findViewById(R.id.edit_from);
        edit_id = (EditText) findViewById(R.id.edit_id);
        edit_email = (EditText) findViewById(R.id.edit_email);
        edit_user = (EditText) findViewById(R.id.edit_user);
        edit_coupon = (PinEntryEditText) findViewById(R.id.edit_coupon);
        edit_pass = (EditText) findViewById(R.id.edit_pass);
        spin_study = (Spinner) findViewById(R.id.spin_lang);
        check_100 = (CheckBox) findViewById(R.id.check_100);
        check_101 = (CheckBox) findViewById(R.id.check_101);
        check_102 = (CheckBox) findViewById(R.id.check_102);
        check_103 = (CheckBox) findViewById(R.id.check_103);
        check_104 = (CheckBox) findViewById(R.id.check_104);
        check_105 = (CheckBox) findViewById(R.id.check_105);
        check_106 = (CheckBox) findViewById(R.id.check_106);
        check_107 = (CheckBox) findViewById(R.id.check_107);
        check_108 = (CheckBox) findViewById(R.id.check_108);
        check_109 = (CheckBox) findViewById(R.id.check_109);
        check_110 = (CheckBox) findViewById(R.id.check_110);
        check_111 = (CheckBox) findViewById(R.id.check_111);
        check_112 = (CheckBox) findViewById(R.id.check_112);
        check_113 = (CheckBox) findViewById(R.id.check_113);
        check_114 = (CheckBox) findViewById(R.id.check_114);
        check_115 = (CheckBox) findViewById(R.id.check_115);
        check_116 = (CheckBox) findViewById(R.id.check_116);
        check_117 = (CheckBox) findViewById(R.id.check_117);
        btn_birthday = (LinearLayout) findViewById(R.id.birthday);
        txt_birthday = (TextView) findViewById(R.id.txt_birthday);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY(); // For ScrollView
                if(scrollY == 0) {
                    fab.show(true);
                    isSubmitHidden = false;
                }else if(!isSubmitHidden){
                    fab.hide(true);
                    isSubmitHidden = true;
                }
            }
        });
        btn_birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SpinnerDatePickerDialogBuilder()
                        .context(SignActivity.this)
                        .callback(SignActivity.this)
                        .spinnerTheme(R.style.NumberPickerStyle)
                        .showTitle(true)
                        .showDaySpinner(true)
                        .defaultDate(2017, 0, 1)
                        .maxDate(2020, 0, 1)
                        .minDate(2000, 0, 1)
                        .build()
                        .show();
            }
        });
        String study[] = {"سابع", "ثامن", "تاسع", "عاشر علمي", "عاشر أدبي", "حادي عشر علمي", "حادي عشر أدبي", "بكالوريا علمي", "بكالوريا أدبي"};
        ArrayAdapter<CharSequence> spinnerCurrencyArrayAdapter = new ArrayAdapter<CharSequence>(SignActivity.this, R.layout.spinner_item, study);
        spinnerCurrencyArrayAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        spin_study.setAdapter(spinnerCurrencyArrayAdapter);
        spin_study.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0 || i == 1 || i == 2) {
                    check_100.setVisibility(View.GONE);
                    check_101.setVisibility(View.VISIBLE);
                    check_102.setVisibility(View.VISIBLE);
                    check_103.setVisibility(View.GONE);
                    check_104.setVisibility(View.VISIBLE);
                    check_105.setVisibility(View.GONE);
                    check_106.setVisibility(View.GONE);
                    check_107.setVisibility(View.VISIBLE);
                    check_111.setVisibility(View.VISIBLE);
                    check_112.setVisibility(View.GONE);
                    check_113.setVisibility(View.GONE);
                    check_114.setVisibility(View.GONE);
                    check_115.setVisibility(View.GONE);
                    check_117.setVisibility(View.GONE);
                } else if (i == 3 || i == 4) {
                    check_100.setVisibility(View.GONE);
                    check_101.setVisibility(View.GONE);
                    check_102.setVisibility(View.GONE);
                    check_103.setVisibility(View.VISIBLE);
                    check_104.setVisibility(View.GONE);
                    check_105.setVisibility(View.VISIBLE);
                    check_106.setVisibility(View.VISIBLE);
                    check_107.setVisibility(View.VISIBLE);
                    check_111.setVisibility(View.GONE);
                    check_112.setVisibility(View.VISIBLE);
                    check_113.setVisibility(View.VISIBLE);
                    check_114.setVisibility(View.VISIBLE);
                    check_115.setVisibility(View.VISIBLE);
                    check_117.setVisibility(View.VISIBLE);
                } else if (i == 5 || i == 7) {
                    check_100.setVisibility(View.GONE);
                    check_101.setVisibility(View.GONE);
                    check_102.setVisibility(View.GONE);
                    check_103.setVisibility(View.VISIBLE);
                    check_104.setVisibility(View.GONE);
                    check_105.setVisibility(View.VISIBLE);
                    check_106.setVisibility(View.VISIBLE);
                    check_107.setVisibility(View.VISIBLE);
                    check_111.setVisibility(View.GONE);
                    check_112.setVisibility(View.GONE);
                    check_113.setVisibility(View.GONE);
                    check_114.setVisibility(View.VISIBLE);
                    check_115.setVisibility(View.VISIBLE);
                    check_117.setVisibility(View.VISIBLE);
                } else if (i == 6 || i == 8) {
                    check_100.setVisibility(View.GONE);
                    check_101.setVisibility(View.GONE);
                    check_102.setVisibility(View.GONE);
                    check_103.setVisibility(View.GONE);
                    check_104.setVisibility(View.GONE);
                    check_105.setVisibility(View.GONE);
                    check_106.setVisibility(View.GONE);
                    check_107.setVisibility(View.GONE);
                    check_111.setVisibility(View.GONE);
                    check_112.setVisibility(View.VISIBLE);
                    check_113.setVisibility(View.VISIBLE);
                    check_114.setVisibility(View.VISIBLE);
                    check_115.setVisibility(View.VISIBLE);
                    check_117.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             //   Snackbar.make(view, "انتظر من فضلك", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                boolean isOk = true;
                if (TextUtils.isEmpty(edit_name.getText().toString())) {
                    edit_name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_edit_error, 0, 0, 0);
                    isOk = false;
                } else if (TextUtils.isEmpty(edit_num.getText().toString())) {
                    edit_num.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_edit_error, 0, 0, 0);
                    isOk = false;
                } else if (TextUtils.isEmpty(edit_address.getText().toString())) {
                    edit_address.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_edit_error, 0, 0, 0);
                    isOk = false;
                } else if (TextUtils.isEmpty(edit_user.getText().toString())) {
                    edit_user.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_edit_error, 0, 0, 0);
                    isOk = false;
                } else if (TextUtils.isEmpty(edit_pass.getText().toString())) {
                    edit_pass.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_edit_error, 0, 0, 0);
                    isOk = false;
                }
                if (isOk) {
                    String email = "";
                    if (!TextUtils.isEmpty(edit_user.getText().toString())) {
                        email = edit_user.getText().toString();
                    }
                    String password = "majdzain2001";
                    createDownloadProgressDialog();

                    mauth.createUserWithEmailAndPassword(email, edit_pass.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        progressBar.setProgress(10);

                                        ref = FirebaseDatabase.getInstance().getReference();
                                        currentuser = mauth.getCurrentUser();
                                        userId = currentuser.getUid();
                                        userprofileimagereference = FirebaseStorage.getInstance().getReference().child("Profile Images");

                                        Toast.makeText(SignActivity.this, userId, Toast.LENGTH_LONG).show();
                                        String name = edit_name.getText().toString();
                                        String father = edit_father.getText().toString();
                                        String mother = edit_mother.getText().toString();
                                        String from = edit_from.getText().toString();
                                        String id = edit_id.getText().toString();
                                        String place = edit_address.getText().toString();

                                        String birthday = txt_birthday.getText().toString();
                                        String num = edit_num.getText().toString();
                                        String num_ = edit_num_.getText().toString();
                                        String email = edit_email.getText().toString();
                                        String bill = (TextUtils.isEmpty(edit_coupon.getText().toString()) ? "نسخة تجريبية" : edit_coupon.getText().toString());
                                        String user = edit_user.getText().toString();
                                        String pass = edit_pass.getText().toString();
                                        String study = "1650";
                                        for (int i = 0; i < 9; i++) {
                                            if (spin_study.getSelectedItemPosition() == i)
                                                study = String.valueOf(100 + i);
                                        }
                                        String type = "100";
                                        String studies = "";
                                        if (check_100.isChecked())
                                            studies += "100";
                                        if (check_101.isChecked())
                                            studies += "101";
                                        if (check_102.isChecked())
                                            studies += "102";
                                        if (check_103.isChecked())
                                            studies += "103";
                                        if (check_104.isChecked())
                                            studies += "104";
                                        if (check_105.isChecked())
                                            studies += "105";
                                        if (check_106.isChecked())
                                            studies += "106";
                                        if (check_107.isChecked())
                                            studies += "107";
                                        if (check_108.isChecked())
                                            studies += "108";
                                        if (check_109.isChecked())
                                            studies += "109";
                                        if (check_110.isChecked())
                                            studies += "110";
                                        if (check_111.isChecked())
                                            studies += "111";
                                        if (check_112.isChecked())
                                            studies += "112";
                                        if (check_113.isChecked())
                                            studies += "113";
                                        if (check_114.isChecked())
                                            studies += "114";
                                        if (check_115.isChecked())
                                            studies += "115";
                                        if (check_116.isChecked())
                                            studies += "116";
                                        if (check_117.isChecked())
                                            studies += "117";

                                        final HashMap<String, String> h = new HashMap<>();
                                        h.put("subjects", studies);
                                        h.put("study", study);
                                        h.put("studies", studies);
                                        h.put("type", type);
                                        h.put("email", email);
                                        h.put("num_", num_);
                                        h.put("num", num);
                                        h.put("numcomments", "0");
                                        h.put("birthday", birthday);
                                        h.put("place", place);
                                        h.put("id", id);
                                        h.put("from", from);
                                        h.put("mother", mother);
                                        h.put("father", father);
                                        h.put("name", name);
                                        h.put("uid", userId);
                                        h.put("bill", bill);
                                        h.put("date", getDate());
                                        h.put("time", getTime());
                                        h.put("isTeacher", "0");
                                        h.put("pass", pass);
                                        h.put("status", "0");
                                        h.put("isBanned", "0");

                                        progressBar.setProgress(25);

                                        if (isImageSelected) {
                                            h.put("image", userId);
                                            StorageReference file = userprofileimagereference.child(userId + ".jpg");
                                            file.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        progressBar.setProgress(60);
                                                        ref.child("Users").child(userId).setValue(h)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            progressBar.setProgress(100);
                                                                            Toast.makeText(SignActivity.this, "تم إنشاء الحساب بنجاح", Toast.LENGTH_LONG).show();
                                                                            d.cancel();
                                                                        } else {
                                                                            String error = task.getException().toString();
                                                                            Toast.makeText(SignActivity.this, "Error " + error, Toast.LENGTH_LONG).show();
                                                                        }
                                                                    }
                                                                });
                                                    } else {
                                                        String message = task.getException().toString();
                                                        Toast.makeText(SignActivity.this, "Error : " + message, Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        } else {
                                            //ref.child("Users").child(userId).child("image").setValue("uri");
                                            h.put("image","uri");
                                            progressBar.setProgress(60);
                                            ref.child("Users").child(userId).setValue(h)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            if (task.isSuccessful()) {
                                                                progressBar.setProgress(100);
                                                                d.cancel();
                                                                Toast.makeText(SignActivity.this, "تم إنشاء الحساب بنجاح", Toast.LENGTH_LONG).show();
                                                            } else {
                                                                String error = task.getException().toString();
                                                                Toast.makeText(SignActivity.this, "Error " + error, Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    } else {
                                        String message = task.getException().toString();
                                        Toast.makeText(SignActivity.this, "خطأ" + message, Toast.LENGTH_LONG).show();
                                        Loadingbar.dismiss();
                                    }
                                }

                            });
                }
            }

        });
    }

    boolean isImageSelected = false;
    Uri imageUri = null;


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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3) {
            if (resultCode == RESULT_OK) {

                //Permission granted, start the VPN
                startVpn();
            } else {
                Toast.makeText(this, "يرجى السماح للبرنامج بالاتصال", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                isImageSelected = true;
                imageUri = result.getUri();
                Picasso.get().load(imageUri).into(image);
                isStartAnotherActivity = false;
                /**Loadingbar.setTitle("Set Profile Image");
                 Loadingbar.setMessage("Please Wait,your profile image is updating...");
                 Loadingbar.setCanceledOnTouchOutside(false);
                 Loadingbar.show();

                 Uri resulturi=result.getUri();

                 StorageReference file=userprofileimagereference.child(userId+".jpg");
                 file.putFile(resulturi).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful())
                {
                Toast.makeText(SignActivity.this,"Profile Image uploaded Successfully",Toast.LENGTH_LONG).show();
                final String downloadurl=task.getResult().getMetadata().getReference().getDownloadUrl().toString();
                ref.child("Users").child(userId).child("image").setValue(downloadurl)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                Toast.makeText(SignActivity.this,"Image saved in database,successfully...",Toast.LENGTH_LONG).show();
                Loadingbar.dismiss();
                }
                else
                {
                String message=task.getException().toString();
                Toast.makeText(SignActivity.this,"Error : "+message,Toast.LENGTH_LONG).show();
                Loadingbar.dismiss();
                }
                }
                });
                }
                else
                {
                String message=task.getException().toString();
                Toast.makeText(SignActivity.this,"Error : "+message,Toast.LENGTH_LONG).show();
                Loadingbar.dismiss();
                }
                }
                });**/
            }
        }
    }

    /**
     * private void RetrieveUserInfo() {
     * ref.child("Users").child(userId).addValueEventListener(new ValueEventListener() {
     *
     * @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
     * if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))) {
     * String retrieveusername = dataSnapshot.child("name").getValue().toString();
     * String retrievestatus = dataSnapshot.child("status").getValue().toString();
     * String retrieveimage = dataSnapshot.child("image").getValue().toString();
     * name.setText(retrieveusername);
     * status.setText(retrievestatus);
     * Picasso.get().load(retrieveimage).into(image);
     * <p>
     * <p>
     * } else if (dataSnapshot.exists() && (dataSnapshot.hasChild("name"))) {
     * String retrieveusername = dataSnapshot.child("name").getValue().toString();
     * String retrievestatus = dataSnapshot.child("status").getValue().toString();
     * name.setText(retrieveusername);
     * status.setText(retrievestatus);
     * } else {
     * //                    name.setVisibility(View.VISIBLE);
     * Toast.makeText(NameUserActivity.this, "Please set and update your profile picture!!!", Toast.LENGTH_LONG).show();
     * }
     * }
     * @Override public void onCancelled(@NonNull DatabaseError databaseError) {
     * <p>
     * }
     * });
     * }
     **/
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

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MM yyyy", Locale.US);
        Calendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        txt_birthday.setText(simpleDateFormat.format(calendar.getTime()));
    }

    private OpenVPNThread vpnThread = new OpenVPNThread();
    private SharedPreference preference;
    boolean isStartAnotherActivity = false;
    private OpenVPNService vpnService = new OpenVPNService();
    private Server server;

    @Override
    public void onStop() {
        if (!isStartAnotherActivity) {
            vpnThread.stop();
            preference.saveVpnStarted(false);
        }
        super.onStop();
    }

    private CheckInternetConnection connection;

    public boolean getInternetStatus() {

        return connection.netCheck(this);
    }

    public void prepareVpn() {
        if (getInternetStatus()) {
            // Checking permission for network monitor
            Intent intent = VpnService.prepare(this);
            if (intent != null) {
                startActivityForResult(intent, 3);
            } else startVpn();//have already permission

            // Update confection status
            //status("connecting");

        } else {

            // No internet connection available
            Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();
                //    binding.logTv.setText("Connecting...");
            }
        } catch (IOException | RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        if (!preference.isVpnStarted()) {
            prepareVpn();
        }
        super.onResume();
    }


}
