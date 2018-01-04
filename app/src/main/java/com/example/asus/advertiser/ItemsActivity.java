package com.example.asus.advertiser;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

public class ItemsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Items");
        setSupportActionBar(toolbar);

        // PROFILE
        FloatingActionButton profile = (FloatingActionButton) findViewById(R.id.profile);
        profile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(ItemsActivity.this, "Profile", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), UserActivity.class);    // ADD ACTIVITY HERE
                startActivity(intent);
            }
        });
    }
}
