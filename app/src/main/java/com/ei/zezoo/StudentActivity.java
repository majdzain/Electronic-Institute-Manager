package com.ei.zezoo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;

import de.hdodenhof.circleimageview.CircleImageView;

public class StudentActivity extends AppCompatActivity {
    UserListChildItem u;
    UserSQLDatabaseHandler dbu;
    Resources res;
    private DatabaseReference ref;
    TextView txtName, txtUser, txtStatus, date, comments, ques, cash, study, history;
    ImageView imgStatus, imgBan, imgMenu;
    CircleImageView profile;
    LinearLayout linearDate, linearCash;
    ScrollView scrollView;
    EditText name, father, mother, place, id, from, phone, telephone, email;
    FloatingActionButton save;

    boolean isSubmitHidden = false;
    PowerMenu powerMenu;

    public static int isLoginable(String str) {
        boolean is = true;
        if (str == null) {
            is = false;
        }
        int length = str.length();
        if (length == 0) {
            is = false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                is = false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                is = false;
            }
        }
        if (is) {
            return Integer.valueOf(str);
        } else
            return 2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        dbu = new UserSQLDatabaseHandler(this);
        Bundle b = getIntent().getExtras();
        u = dbu.getUser(b.getInt("KEY"));
        res = getResources();
        txtName = (TextView) findViewById(R.id.Name);
        txtUser = (TextView) findViewById(R.id.User);
        date = (TextView) findViewById(R.id.txtDate);
        study = (TextView) findViewById(R.id.txtStudy);
        comments = (TextView) findViewById(R.id.txtComments);
        cash = (TextView) findViewById(R.id.txtCash);
        ques = (TextView) findViewById(R.id.txtQues);
        history = (TextView) findViewById(R.id.txtHistory);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        imgStatus = (ImageView) findViewById(R.id.imgStatus);
        imgBan = (ImageView) findViewById(R.id.imgBan);
        imgMenu = (ImageView) findViewById(R.id.imgOverflow);
        profile = (CircleImageView) findViewById(R.id.profileImage);
        linearDate = (LinearLayout) findViewById(R.id.btnDate);
        linearCash = (LinearLayout) findViewById(R.id.btnCash);
        name = (EditText) findViewById(R.id.txtName);
        father = (EditText) findViewById(R.id.txtFather);
        mother = (EditText) findViewById(R.id.txtMother);
        place = (EditText) findViewById(R.id.txtPlace);
        id = (EditText) findViewById(R.id.txtId);
        from = (EditText) findViewById(R.id.txtFrom);
        phone = (EditText) findViewById(R.id.txtPhone);
        telephone = (EditText) findViewById(R.id.txtTelephone);
        email = (EditText) findViewById(R.id.txtEmail);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        save = (FloatingActionButton) findViewById(R.id.floatingActionSubmit);
        txtName.setText(u.getName());
        txtUser.setText(u.getUser());
        history.setText(history.getText() + u.getDate() + "|" + u.getTime());
        study.setText(ElecUtils.getStudy(u.getType(), u.getStudy()));
        ref = FirebaseDatabase.getInstance().getReference();
        if (isLoginable(u.getStatus()) == 0) {
            txtStatus.setText("حساب جديد");
            imgStatus.setImageResource(R.drawable.status_0);
        } else if (isLoginable(u.getStatus()) == 1) {
            txtStatus.setText("متصل الآن");
            imgStatus.setImageResource(R.drawable.status_1);
        } else if (isLoginable(u.getStatus()) == 2) {
            txtStatus.setText("آخر ظهور : " + u.getStatus());
            imgStatus.setImageResource(R.drawable.status_2);
        }
        if (u.isBanned())
            imgBan.setVisibility(View.VISIBLE);
        date.setText(u.getBirthday());
        comments.setText(String.valueOf(u.getNumComments()));
        // cash.setText();
        ques.setText("0");
        profile.setImageBitmap(u.getProfile());
        name.setText(u.getName());
        father.setText(u.getFather());
        mother.setText(u.getMother());
        place.setText(u.getPlace());
        id.setText(u.getId());
        from.setText(u.getFrom());
        phone.setText(u.getPhone());
        telephone.setText(u.getTelephone());
        email.setText(u.getEmail());
        imgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                powerMenu = new PowerMenu.Builder(StudentActivity.this)
                        .addItem(new PowerMenuItem("عرض اسم المستخدم وكلمة السر", R.drawable.pop_view_pass, false))
                        .addItem(new PowerMenuItem("كشف حساب وإضافة دفعات", R.drawable.pop_cash, false))
                        .addItem(new PowerMenuItem("اتصال...", R.drawable.pop_call, false))
                        .addItem(new PowerMenuItem((u.isBanned() ? "فك حظر الطالب" : "حظر الطالب"), R.drawable.pop_block, false)) // add an item.
                        .addItem(new PowerMenuItem("حذف التسجيل", R.drawable.pop_delete, false)) // aad an item list.
                        .setAnimation(MenuAnimation.SHOWUP_TOP_LEFT) // Animation start point (TOP | LEFT).
                        .setMenuRadius(10f) // sets the corner radius.
                        .setMenuShadow(10f) // sets the shadow.
                        .setTextColor(ContextCompat.getColor(StudentActivity.this, R.color.md_grey_800))
                        .setTextGravity(Gravity.CENTER).setIconSize(30).setTextSize(14)
                        .setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
                        .setSelectedTextColor(Color.WHITE)
                        .setMenuColor(Color.WHITE).setSize(400, 400)
                        .setSelectedMenuColorResource(R.color.orange)
                        .setOnMenuItemClickListener(new OnMenuItemClickListener<PowerMenuItem>() {
                            @Override
                            public void onItemClick(int position, PowerMenuItem item) {
                                switch (position) {
                                    case 0:
                                        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(StudentActivity.this);
                                        builder.setTitle("حساب الطالب" + u.getName());
                                        String s[] = {"اسم المستخدم : " + u.getUser(), "كلمة السر : " + u.getPassword()};
                                        builder.setItems(s, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                                ClipData clip = ClipData.newPlainText("معلومات الطالب " + u.getName(), "اسم المستخدم : " + u.getUser() + "\n" + "كلمة السر : " + u.getPassword());
                                                clipboard.setPrimaryClip(clip);
                                                Toast.makeText(StudentActivity.this, "تم النسخ إلى الحافظة", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                        // builder.setMessage(R.string.cancel_connection_query);
                                        builder.setPositiveButton("موافق", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        });
                                        builder.setPositiveButton("نسخ", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                                ClipData clip = ClipData.newPlainText("معلومات الطالب " + u.getName(), "اسم المستخدم : " + u.getUser() + "\n" + "كلمة السر : " + u.getPassword());
                                                clipboard.setPrimaryClip(clip);
                                                Toast.makeText(StudentActivity.this, "تم النسخ إلى الحافظة", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                        builder.show();
                                        break;
                                    case 1:
                                        createBillDialog();
                                        break;
                                    case 2:
                                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                                        callIntent.setData(Uri.parse("tel:" + Long.valueOf(u.getPhone())));//change the number
                                        startActivity(callIntent);
                                        break;
                                    case 3:
                                        if (u.isBanned()) {
                                            ref.child("Users").child(u.getUserId()).child("isBanned").setValue("0", new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                    if (error == null) {
                                                        u.setBanned(false);
                                                        dbu.updateUser(u);
                                                        Toast.makeText(StudentActivity.this, "تم فك الحظر عن الطالب", Toast.LENGTH_LONG).show();
                                                    } else {
                                                        Toast.makeText(StudentActivity.this, "حدث خطأ! يرجى إعادة المحاولة", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        } else {
                                            ref.child("Users").child(u.getUserId()).child("isBanned").setValue("1", new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                    if (error == null) {
                                                        u.setBanned(true);
                                                        dbu.updateUser(u);
                                                        Toast.makeText(StudentActivity.this, "تم حظر الطالب", Toast.LENGTH_LONG).show();
                                                    } else {
                                                        Toast.makeText(StudentActivity.this, "حدث خطأ! يرجى إعادة المحاولة", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }
                                        break;
                                    case 4:
                                        ref.child("Users").child(u.getUserId()).removeValue(new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                if (error == null) {
                                                    Toast.makeText(StudentActivity.this, "تم حذف حساب الطالب", Toast.LENGTH_LONG).show();
                                                    dbu.deleteUser(u);
                                                    Intent i = new Intent(StudentActivity.this, StudentsActivity.class);
                                                    i.putExtra("type", u.getType());
                                                    startActivity(i);
                                                } else {
                                                    Toast.makeText(StudentActivity.this, "حدث خطأ! يرجى إعادة المحاولة", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                        break;
                                }
                            }
                        })
                        .build();
                powerMenu.showAsDropDown(view);
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        linearDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        linearCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY(); // For ScrollView
                if (scrollY == 0) {
                    save.show(true);
                    isSubmitHidden = false;
                } else if (!isSubmitHidden) {
                    save.hide(true);
                    isSubmitHidden = true;
                }
            }
        });

    }

    private void createBillDialog() {
    }
}