package com.example.mylistview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {



    GridView list;
    String[] s={"android","java","ios","c++","Python","Ruby On Rails","Java Script","brombo"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list=(GridView) findViewById(R.id.l1);
        ArrayAdapter<String> myad=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,s);
        list.setAdapter(myad);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Toast.makeText(MainActivity.this, "aaaa"+i, Toast.LENGTH_SHORT).show();
            }
        });

    }
}
