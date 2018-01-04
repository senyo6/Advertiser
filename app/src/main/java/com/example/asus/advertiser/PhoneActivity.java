package com.example.asus.advertiser;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class PhoneActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private static final String TAG = "Advertiser_Phone";

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };

    // UI references.
    private AutoCompleteTextView phoneView;
    private View mProgressView;
    private View mLoginFormView;

    private FirebaseAuth mAuth;

    // Write a message to the database
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        // Set up the login form.
        phoneView = (AutoCompleteTextView) findViewById(R.id.set_phone_view);
        populateAutoComplete();

        Button phoneButton = (Button) findViewById(R.id.set_phone_button);
        phoneButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view) {
                attemptSetPhone();
            }
        });

        mLoginFormView = findViewById(R.id.set_phone_layout);
        mProgressView = findViewById(R.id.login_progress);

        mAuth = FirebaseAuth.getInstance();

        initPhoneView();
    }

    private void initPhoneView ()
    {
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        myRef.child("users").child(currentUserUid).child("phone").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String currentUserPhoneNumber = "";
                if (null != dataSnapshot && null != dataSnapshot.getValue())
                {
                    currentUserPhoneNumber = dataSnapshot.getValue().toString();
                }
                phoneView.setText(currentUserPhoneNumber);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void attemptSetPhone()
    {

        // Reset errors.
        phoneView.setError(null);

        // Store values at the time of the login attempt.
        String phoneNumber = phoneView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(phoneNumber))
        {
            phoneView.setError(getString(R.string.error_field_required));
            focusView = phoneView;
            cancel = true;
        } else if (!isValidPhone(phoneNumber)) {
            phoneView.setError(getString(R.string.error_invalid_phone));
            focusView = phoneView;
            cancel = true;
        }

        if (cancel)
        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else
        {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            SetPhoneNumber(mAuth.getCurrentUser(), phoneNumber);
            //SendCode();
            //SetPhoneNumber(mAuth.getCurrentUser(), phoneNumber);
            //finish();
            //Toast.makeText(this, "Display name set to " + displayname, Toast.LENGTH_LONG).show();
        }
    }

    private void SetPhoneNumber(final FirebaseUser user, final String value)
    {
        myRef.child("users").child(user.getUid()).child("phone").setValue(value);

        finish();
        Intent intent = new Intent (PhoneActivity.this, UserActivity.class);
        startActivity(intent);

        Toast.makeText(PhoneActivity.this, "Phone number set to " + value, Toast.LENGTH_LONG).show();
    }

    // PHONE AUTH
    // https://firebase.google.com/docs/reference/node/firebase.auth.PhoneAuthProvider

    // https://www.youtube.com/watch?v=HA0IX2G7aBU
    // https://github.com/delaroy/EmailPasswordAuth/blob/PhoneAuthentication/app/src/main/java/com/delaroystudios/emailpasswordauth/MainActivity.java

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    private String phoneVerificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    public void SendCode()
    {
        String phoneNumber = phoneView.getText().toString();

        SetupPhone();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,            // Phone number to verify
                60,                  // Timeout duration
                TimeUnit.SECONDS,       // Unit of timeout
                this,           // Activity (for callback binding)
                verificationCallbacks);
    }

    private void SetupPhone ()
    {
        verificationCallbacks =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
                {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
                    {
                        SetPhoneNumber(mAuth.getCurrentUser(), phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e)
                    {
                        Log.d(TAG, "phone verification failed");
                        Toast.makeText(PhoneActivity.this, "Verification Failed", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token)
                    {

                        phoneVerificationId = verificationId;
                        resendToken = token;
                    }
                };
    }

    // https://www.tutorialspoint.com/android/android_sending_email.htm
    private void SetPhoneNumber(final FirebaseUser user, final PhoneAuthCredential phoneAuthCredential)
    {
        user.updatePhoneNumber(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    if (mAuth.getCurrentUser().getDisplayName() != null) {
                        Log.d("UPDATE PROFILE", "username=" + mAuth.getCurrentUser().getDisplayName());
                    } else {
                        Log.d("UPDATE PROFILE", "username=NULL");
                    }
                    if (mAuth.getCurrentUser().getPhotoUrl() != null) {
                        Log.d("UPDATE PROFILE", "photoUrl=" + mAuth.getCurrentUser().getPhotoUrl().toString());
                    } else {
                        Log.d("UPDATE PROFILE", "photoUrl=NULL");
                    }
                    finish();
                    Intent intent = new Intent (PhoneActivity.this, UserActivity.class);
                    startActivity(intent);
                    Toast.makeText(PhoneActivity.this, "Phone number set to " + user.getPhoneNumber(), Toast.LENGTH_LONG).show();
                }
                else
                {
                    Log.e("UPDATE PROFILE", task.getException().getMessage());
                    Toast.makeText(PhoneActivity.this, "Phone number set failed", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    // http://codesfor.in/how-to-validate-phone-number-in-android/
    public boolean isValidPhone(CharSequence phone)
    {
        if (TextUtils.isEmpty(phone)) {
            return false;
        } else {
            return android.util.Patterns.PHONE.matcher(phone).matches();
        }
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(phoneView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        phoneView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }
}

