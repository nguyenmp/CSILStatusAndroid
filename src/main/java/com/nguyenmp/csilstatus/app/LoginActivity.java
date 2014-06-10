package com.nguyenmp.csilstatus.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jcraft.jsch.JSchException;
import com.nguyenmp.csil.concurrency.CommandExecutor;
import com.nguyenmp.csilstatus.app.dao.ComputerDbHelper;

import java.io.IOException;

import static com.nguyenmp.csilstatus.app.dao.ComputerContract.ComputerEntry;


/**
 * A login screen that offers login via email/password.

 */
public class LoginActivity extends Activity {
    private static final String KEY_USERNAME = "LoginActivity.USERNAME";
    private static final String KEY_PASSWORD = "LoginActivity.PASSWORD";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // If the activity has already
        String[] credentials = LoginActivity.getCredentials(this);
        if (credentials[0] != null && credentials[1] != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.login);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
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

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, String> {
        private static final String DEFAULT_HOST = "csil.cs.ucsb.edu";
        private static final String DEFAULT_COMMAND = "cat /etc/hosts";
        private final String mUsername;
        private final String mPassword;

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return CommandExecutor.performBlockingExecution(mUsername, mPassword, DEFAULT_HOST, DEFAULT_COMMAND);
            } catch (JSchException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(final String response) {
            mAuthTask = null;
            showProgress(false);

            if (response != null) {
                ComputerDbHelper dbHelper = new ComputerDbHelper(LoginActivity.this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.delete(ComputerEntry.TABLE_NAME, null, null);
                String[] split;
                for (String substring : response.split("\n")) {
                    if (substring.startsWith("128.111.43") && (split = substring.split("\t")).length >= 1) {
                        String ipaddress = split[0];
                        String hostname = split[1];

                        ContentValues values = new ContentValues();
                        values.put(ComputerEntry.COLUMN_NAME_HOSTNAME, hostname);
                        values.put(ComputerEntry.COLUMN_NAME_IP_ADDRESS, ipaddress);

                        long newRowID = db.insert(ComputerEntry.TABLE_NAME, null, values);
                        Log.d("", "Inserted row at: " + newRowID);
                    }
                }

                // Save creds
                saveCredentials(mUsername, mPassword);

                // Start main activity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void saveCredentials(String username, String password) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.edit().putString(KEY_USERNAME, username).putString(KEY_PASSWORD, password).commit();
    }

    public static String[] getCredentials(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String username = pref.getString(KEY_USERNAME, null);
        String password = pref.getString(KEY_PASSWORD, null);

        return new String[] {username, password};
    }
}



