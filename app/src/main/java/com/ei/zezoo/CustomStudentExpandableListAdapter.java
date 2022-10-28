package com.ei.zezoo;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnDismissedListener;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomStudentExpandableListAdapter extends AnimatedExpandableListAdapter {
    private DatabaseReference ref;
    private Context context;
    private List<String> listGroupTitle; // header titles
    private HashMap<String, List<UserListChildItem>> listChildData;
    private static final int[] EMPTY_STATE_SET = {};
    private static final int[] GROUP_EXPANDED_STATE_SET = {android.R.attr.state_expanded};
    private static final int[][] GROUP_STATE_SETS = {EMPTY_STATE_SET, // 0
            GROUP_EXPANDED_STATE_SET // 1
    };
    private static final int STATE_IDLE = 0;
    private static final int STATE_EXPANDING = 1;
    private static final int STATE_COLLAPSING = 2;
    private int lastPosition = -1;
    private int lastGroupPosition = -1;
    PowerMenu powerMenu;
    UserListChildItem current;
    UserSQLDatabaseHandler dbu;
    UserListChildItem u = new UserListChildItem();
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
    public CustomStudentExpandableListAdapter(Context context, List<String> listGroupTitle, HashMap<String, List<UserListChildItem>> listChildData) {
        this.context = context;
        this.listGroupTitle = listGroupTitle;
        this.listChildData = listChildData;
        dbu = new UserSQLDatabaseHandler(context);
        ref = FirebaseDatabase.getInstance().getReference();

    }

    private void createBillDialog() {
    }


    @Override
    public Object getChild(int groupPosition, int childPosition) {
        // return this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).get(expandedListPosition);
        return this.listChildData.get(this.listGroupTitle.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        UserListChildItem memData = (UserListChildItem) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.student_child_item, null);
        }
        CardView card = (CardView) convertView.findViewById(R.id.studentCard);
        TextView email = (TextView) convertView.findViewById(R.id.emailTxt);
        TextView name = (TextView) convertView.findViewById(R.id.nameTxt);
        TextView subjects = (TextView) convertView.findViewById(R.id.subjectTxt);
        CircleImageView profile = (CircleImageView) convertView.findViewById(R.id.profileImage);
       ImageView ban = (ImageView) convertView.findViewById(R.id.imgBan);
        ImageView status = (ImageView) convertView.findViewById(R.id.imgStatus);
        ProgressBar loading = (ProgressBar) convertView.findViewById(R.id.loading);
        email.setText(memData.getUser());
        name.setText(memData.getName());
        if(memData.isBanned())
            ban.setVisibility(View.VISIBLE);
        else
            ban.setVisibility(View.GONE);
        if(isLoginable(memData.getStatus()) == 0){
            status.setImageResource(R.drawable.status_0);
        }else if(isLoginable(memData.getStatus()) == 1){
            status.setImageResource(R.drawable.status_1);
        }else if(isLoginable(memData.getStatus()) == 2){
            status.setImageResource(R.drawable.status_2);
        }
        String studies = "";
        for(int i =0;i<memData.getStudies().size();i++){
            if( i == 0){
                studies = ElecUtils.getSubject(memData.getType(),memData. getStudy(),memData.getStudies().get(i));
            }else{
                studies = studies +"|"+ ElecUtils.getSubject(memData.getType(),memData. getStudy(),memData.getStudies().get(i));
            }
        }
        subjects.setText(studies);
        if(memData.getProfile() == null){
            StorageReference refe = FirebaseStorage.getInstance().getReference().child("Profile Images/" + memData.getUserId() + ".jpg");
            try {
                final File localFile = File.createTempFile("Images", "jpg");
                refe.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap my_image = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        memData.setProfile(my_image);
                        profile.setImageBitmap(memData.getProfile());
                        dbu.updateUser(memData);
                        loading.setVisibility(View.GONE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loading.setVisibility(View.GONE);
                        Toast.makeText(context, "حدث خطأ!", Toast.LENGTH_LONG).show();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                loading.setVisibility(View.GONE);
                Toast.makeText(context, "حدث خطأ!", Toast.LENGTH_LONG).show();
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }else{
            loading.setVisibility(View.GONE);
            profile.setImageBitmap(memData.getProfile());
        }

       card.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
                Intent i = new Intent((StudentsActivity) context,StudentActivity.class);
                i.putExtra("KEY",memData.getColumn());
                context.startActivity(i);
           }
       });
       card.setOnLongClickListener(new View.OnLongClickListener() {
           @Override
           public boolean onLongClick(View view) {
               current = memData;
               u = memData;
               createMenu(view);
               return true;
           }
       });
        return convertView;
    }

    private void createMenu(View view) {
        powerMenu = new PowerMenu.Builder(context)
                .addItem(new PowerMenuItem("عرض المعلومات وتعديلها", R.drawable.pop_view, false))
                .addItem(new PowerMenuItem("عرض اسم المستخدم وكلمة السر", R.drawable.pop_view_pass, false))
                .addItem(new PowerMenuItem("عرض مدة التسجيل", R.drawable.pop_time, false))
                .addItem(new PowerMenuItem("كشف حساب وإضافة دفعات", R.drawable.pop_cash, false))
                .addItem(new PowerMenuItem("اتصال...", R.drawable.pop_call, false))
                .addItem(new PowerMenuItem((u.isBanned() ? "فك حظر الطالب" : "حظر الطالب"), R.drawable.pop_block, false)) // add an item.
                .addItem(new PowerMenuItem("حذف التسجيل", R.drawable.pop_delete, false)) // aad an item list.
                .setAnimation(MenuAnimation.SHOWUP_BOTTOM_RIGHT) // Animation start point (TOP | LEFT).
                .setMenuRadius(10f) // sets the corner radius.
                .setMenuShadow(10f) // sets the shadow.
                .setTextColor(ContextCompat.getColor(context, R.color.md_grey_800))
                .setTextGravity(Gravity.CENTER).setIconSize(30).setTextSize(14)
                .setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
                .setSelectedTextColor(Color.WHITE)
                .setMenuColor(Color.WHITE).setSize(400,400)
                .setSelectedMenuColorResource(R.color.orange)
                .setOnMenuItemClickListener(new OnMenuItemClickListener<PowerMenuItem>() {
                    @Override
                    public void onItemClick(int position, PowerMenuItem item) {
                        switch (position) {
                            case 0:
                                Intent i = new Intent((StudentsActivity)context,StudentActivity.class);
                                i.putExtra("KEY",u.getColumn());
                                context.startActivity(i);
                                break;
                            case 1:
                                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("حساب الطالب" + u.getName());
                                String s[] = {"اسم المستخدم : " + u.getUser(), "كلمة السر : " + u.getPassword()};
                                builder.setItems(s, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("معلومات الطالب " + u.getName(), "اسم المستخدم : " + u.getUser() + "\n" + "كلمة السر : " + u.getPassword());
                                        clipboard.setPrimaryClip(clip);
                                        Toast.makeText(context, "تم النسخ إلى الحافظة", Toast.LENGTH_LONG).show();
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
                                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("معلومات الطالب " + u.getName(), "اسم المستخدم : " + u.getUser() + "\n" + "كلمة السر : " + u.getPassword());
                                        clipboard.setPrimaryClip(clip);
                                        Toast.makeText(context, "تم النسخ إلى الحافظة", Toast.LENGTH_LONG).show();
                                    }
                                });
                                builder.show();
                                break;
                            case 2:
                                android.app.AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                                builder1.setTitle("حساب الطالب" + u.getName());
                                builder1.setMessage("مسجل من تاريخ : " + u.getDate() + "|" + u.getTime());
                                // builder.setMessage(R.string.cancel_connection_query);
                                builder1.setPositiveButton("موافق", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                builder1.show();
                                break;
                            case 3:
                                createBillDialog();
                                break;
                            case 4:
                                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                                callIntent.setData(Uri.parse("tel:" + Long.valueOf(u.getPhone())));//change the number
                                context.startActivity(callIntent);
                                break;
                            case 5:
                                if (u.isBanned()) {
                                    ref.child("Users").child(u.getUserId()).child("isBanned").setValue("0", new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                            if (error == null) {
                                                u.setBanned(false);
                                                dbu.updateUser(u);
                                                Toast.makeText(context, "تم فك الحظر عن الطالب", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(context, "حدث خطأ! يرجى إعادة المحاولة", Toast.LENGTH_LONG).show();
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
                                                Toast.makeText(context, "تم حظر الطالب", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(context, "حدث خطأ! يرجى إعادة المحاولة", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                                break;
                            case 6:
                                ref.child("Users").child(u.getUserId()).removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                        if (error == null) {
                                            Toast.makeText(context, "تم حذف حساب الطالب", Toast.LENGTH_LONG).show();
                                            dbu.deleteUser(u);
                                            Intent i = new Intent(context, StudentsActivity.class);
                                            i.putExtra("type", u.getType());
                                            context.startActivity(i);
                                        } else {
                                            Toast.makeText(context, "حدث خطأ! يرجى إعادة المحاولة", Toast.LENGTH_LONG).show();
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

    @Override
    public int getRealChildrenCount(int groupPosition) {
        return this.listChildData.get(this.listGroupTitle.get(groupPosition)).size();
    }


    @Override
    public Object getGroup(int groupPosition) {
        return this.listGroupTitle.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listGroupTitle.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.student_group_item, null);
        }
        TextView listTitleTextView = (TextView) convertView.findViewById(R.id.listStudentItemTitle);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle+"("+String.valueOf(getChildrenCount(groupPosition))+")");
        /**Image view which you put in row_group_list.xml
         View ind = convertView.findViewById(R.id.groupStudentIndicator);
         if (ind != null) {
         ImageView indicator = (ImageView) ind;
         indicator.setVisibility(View.VISIBLE);
         int stateSetIndex = (isExpanded ? 1 : 0);
         Drawable drawable = indicator.getDrawable();
         drawable.setState(GROUP_STATE_SETS[stateSetIndex]);
         }**/
        return convertView;
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}
