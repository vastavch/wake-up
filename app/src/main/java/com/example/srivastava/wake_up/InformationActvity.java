package com.example.srivastava.wake_up;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class InformationActvity extends AppCompatActivity {
    Button btn_continue;
//this activity is launched from MainActivity,if the application is launched for the first time.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_actvity);

        btn_continue= (Button) findViewById(R.id.button_continue);
        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InformationActvity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }




    }

