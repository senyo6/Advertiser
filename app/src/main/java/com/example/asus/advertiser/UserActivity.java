package com.example.asus.advertiser;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class UserActivity extends AppCompatActivity {

    private static final String TAG = "Advertiser_User";
    private FirebaseAuth mAuth;
    private Toolbar toolbar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);


        // DISCONNECT
        FloatingActionButton disconnect = (FloatingActionButton) findViewById(R.id.disconnect);
        disconnect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(UserActivity.this, "Disconnected", Toast.LENGTH_LONG).show();
                mAuth.signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        // ITEMS
        FloatingActionButton items = (FloatingActionButton) findViewById(R.id.items);
        items.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(UserActivity.this, "Items", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), ItemsActivity.class);    // ADD ACTIVITY HERE
                startActivity(intent);
            }
        });

        // PROFILE
        Button profile = (Button) findViewById(R.id.user_profile);
        profile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent (UserActivity.this, ProfileActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        // DISPLAY NAME
        Button displayname = (Button) findViewById(R.id.user_displayname);
        displayname.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent (UserActivity.this, DisplaynameActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // EMAIL
        Button email = (Button) findViewById(R.id.user_email);
        email.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent (UserActivity.this, EmailActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        // PHONE NUMBER
        Button phone = (Button) findViewById(R.id.user_phone);
        phone.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent (UserActivity.this, PhoneActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        // PASSWORD
        Button password = (Button) findViewById(R.id.user_password);
        password.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent (UserActivity.this, PasswordActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "onCreate");
        //SetToolbar();
    }

    @Override
    public void onBackPressed()
    {
        // Intent intent = new Intent (UserActivity.this, UserActivity.class);
        // startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "ONSTART");
        SetToolbar();
    }

    private void SetToolbar ()
    {
        setSupportActionBar(null);
        Toolbar toolbar = (Toolbar) UserActivity.this.findViewById(R.id.toolbar);
        if (null != mAuth)
        {
            Log.d(TAG, "mAuth");
            FirebaseUser user = mAuth.getCurrentUser();
            if (null != user)
            {
                user.reload();
                if (null != user.getDisplayName())
                {
                    Log.d(TAG, user.getDisplayName());
                }
                String username = "";
                username = user.getDisplayName();
                if (null == username)
                {
                    username = SetDisplayNameByEmail(user);
                }
                if (null == getSupportActionBar())
                {
                    Log.d(TAG, "set toolbar title");
                    toolbar.setTitle(username);
                    setSupportActionBar(toolbar);
                }
                else
                {
                    Log.d(TAG, "set supportActionBar title");
                    getSupportActionBar().setTitle(username);
                }
                //UserActivity.this.setTitle(username);
                Log.d(TAG, username);
            }
        }
    }

    private String SetDisplayNameByEmail (FirebaseUser user)
    {
        String newDisplayName = user.getEmail().split("@")[0];
        Log.d(TAG, newDisplayName);
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(newDisplayName).build();
        user.updateProfile(profileUpdates);
        return newDisplayName;
    }
}
