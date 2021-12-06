package com.example.hospital_management;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.appsearch.StorageInfo;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.PrimitiveIterator;

public class activity_patient_registration extends AppCompatActivity {
    private TextView login_page1;
    private TextInputEditText registrationFullname,registerationIdNumber,registrationPhonenumber,LoginEmail,loginpassword;
    private Button regButton;
    private Uri resultUrl;
    private ImageView profileimage;
    private FirebaseAuth mAuth;
    private DatabaseReference userdatabaseref;
    private ProgressDialog loader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_registration);
        login_page1 = findViewById(R.id.login_page);
        login_page1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(activity_patient_registration.this, Login_main.class);
                startActivity(i);
            }
        });
        registrationFullname=findViewById(R.id.regfulllname);
        registerationIdNumber=findViewById(R.id.regidnumber);
        registrationPhonenumber=findViewById(R.id.phoneno);
        LoginEmail=findViewById(R.id.login_email);
        loginpassword=findViewById(R.id.login_password);
        regButton=findViewById(R.id.regbutton);
        profileimage=findViewById(R.id.profileimage);
        loader=new ProgressDialog(this);
        mAuth=FirebaseAuth.getInstance();
        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(Intent.ACTION_PICK);
                i.setType("image/");
                startActivityForResult(i,1);


            }
        });

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email=LoginEmail.getText().toString().trim();
                final String password=loginpassword.getText().toString().trim();
                final String  regfullname=registrationFullname.getText().toString().trim();
                final String idNumber=registerationIdNumber.getText().toString().trim();
                final String phonenumber=registrationPhonenumber.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    LoginEmail.setError("Email is required!");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    loginpassword.setError("Password is mandatory");
                    return;
                }
                if(TextUtils.isEmpty(regfullname)){
                    registrationFullname.setError("Enter your full name");
                    return;
                }
                if(TextUtils.isEmpty(idNumber)){
                    registerationIdNumber.setError("Please enter the ID number");
                    return;
                }
                if(TextUtils.isEmpty(phonenumber)){
                    registrationPhonenumber.setError("Phone number is required");
                }
                if(resultUrl==null){
                    Toast.makeText(activity_patient_registration.this,"Please set the profile picture",Toast.LENGTH_SHORT).show();
                }
                else {
                        loader.setMessage("Registration is in progress");
                        loader.setCanceledOnTouchOutside(false);
                        loader.show();

                        mAuth.createUserWithEmailAndPassword(email,password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()) {
                            String error=task.getException().toString();
                            Toast.makeText(activity_patient_registration.this,"Error Occured",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String currentUserId=mAuth.getCurrentUser().getUid();
                                userdatabaseref= FirebaseDatabase.getInstance().getReference()
                                        .child("users").child(currentUserId);

                                HashMap userInfo=new HashMap();
                                userInfo.put("id",idNumber);
                                userInfo.put("email",email);
                                userInfo.put("name",registrationFullname);
                                userInfo.put("phonenumber",phonenumber);
                                userInfo.put("type","patient");



                                userdatabaseref.updateChildren(userInfo).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(activity_patient_registration.this,"Details set sucessfully",Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            Toast.makeText(activity_patient_registration.this,task.getException().toString(),Toast.LENGTH_SHORT).show();
                                        }
                                        finish();
                                        loader.dismiss();
                                    }
                                });
                                if(resultUrl!=null){
                                    final StorageReference filepath=
                                            FirebaseStorage.getInstance().getReference().child("profile pictures")
                                            .child(currentUserId);
                                    Bitmap bitmap=null;
                                    try {
                                        bitmap= MediaStore.Images.Media.getBitmap(getApplication().
                                                getContentResolver(),resultUrl);
                                    }catch (IOException e){
                                        e.printStackTrace();
                                    }
                                    ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();

                                    bitmap.compress(Bitmap.CompressFormat.JPEG,20,byteArrayOutputStream);

                                    byte[] data=byteArrayOutputStream.toByteArray();

                                    UploadTask uploadTask=filepath.putBytes(data);

                                    uploadTask.addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            finish();
                                            return;
                                        }
                                    });
                                   uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                       @Override
                                       public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        if(taskSnapshot.getMetadata()!=null){
                                            Task<Uri> result=taskSnapshot.getStorage().getDownloadUrl();
                                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    String imageurl=uri.toString();
                                                    Map newImageMap=new HashMap();
                                                    newImageMap.put("profilepictureurl",imageurl);

                                                    userdatabaseref.updateChildren(newImageMap).addOnCompleteListener(new OnCompleteListener() {
                                                        @Override
                                                        public void onComplete(@NonNull Task task) {
                                                            if(task.isSuccessful()){
                                                                Toast.makeText(activity_patient_registration.this,"Reg Success",Toast.LENGTH_SHORT).show();
                                                            }
                                                            else {
                                                                 Toast.makeText(activity_patient_registration.this,task.getException().toString(),Toast.LENGTH_SHORT).show();
                                                            }

                                                        }
                                                    });

                                                    finish();


                                                }
                                            });
                                        }
                                       }
                                   });
                                   Intent i=new Intent(activity_patient_registration.this,MainActivity.class);
                                   startActivity(i);

                                   finish();
                                   loader.dismiss();

                                }




                            }
                            }
                        });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1 && resultCode== Activity.RESULT_OK && data!=null){
            resultUrl=data.getData();
            profileimage.setImageURI(resultUrl);
        }
    }
}