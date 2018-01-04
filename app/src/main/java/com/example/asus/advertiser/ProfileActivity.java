package com.example.asus.advertiser;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "Advertiser_Profile";
    // Write a message to the database
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        CallFeature();

    }

    private void CallFeature ()
    {

        Button callButton = (Button) findViewById(R.id.buttonCall);

        callButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                myRef.child("users").child(currentUserUid).child("phone").addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (null != dataSnapshot && null != dataSnapshot.getValue())
                        {
                            // <GET PHONE NUMBER>
                            String currentUserPhoneNumber = dataSnapshot.getValue().toString();
                            Log.d(TAG, "Phone number = " + currentUserPhoneNumber);
                            // </GET PHONE NUMBER>

                            // <CALL>
                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:"+ currentUserPhoneNumber));

                            if (ActivityCompat.checkSelfPermission(ProfileActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }

                            startActivity(callIntent);
                            // </CALL>
                            Toast.makeText(ProfileActivity.this, "Calling " + currentUserPhoneNumber, Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(ProfileActivity.this, "No Phone Number set", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

    }

}
