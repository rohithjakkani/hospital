package com.example.hospital_management;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class activity_doctor_registration extends AppCompatActivity {
    private TextView regpageQuestion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_registration);

        regpageQuestion=findViewById(R.id.regpageQuestion);
        regpageQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i =new Intent(activity_doctor_registration.this,Login_main.class);
                startActivity(i);
            }
        });

    }
}