package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText email,passwd;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        email=(EditText)findViewById(R.id.email);
        passwd=(EditText)findViewById(R.id.passwd);
        login=(Button) findViewById(R.id.btn);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (email.getText().toString().isEmpty()||passwd.getText().toString().isEmpty())
                {
                    Toast.makeText(MainActivity.this, "PLEase enter the field", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    String a=email.getText().toString();
                    String b=passwd.getText().toString();
                    Toast.makeText(MainActivity.this, "Email and password"+a+b, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
