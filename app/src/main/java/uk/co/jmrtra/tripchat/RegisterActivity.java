package uk.co.jmrtra.tripchat;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.flurry.android.FlurryAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private ActionBar mActionBar;
    private EditText mNameEditTxt;
    private EditText mEmailEditTxt;
    private EditText mPasswordEditTxt;
    private EditText mPasswordRepeatEditTxt;
    private TextInputLayout mNameLayout;
    private TextInputLayout mEmailLayout;
    private TextInputLayout mPasswordLayout;
    private TextInputLayout mPasswordRepeatLayout;
    private Button mRegisterBtn;
    private Button mRegisterLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mNameEditTxt = (EditText) findViewById(R.id.register_name_edit_txt);
        mEmailEditTxt = (EditText) findViewById(R.id.register_email_edit_txt);
        mPasswordEditTxt = (EditText) findViewById(R.id.register_password_edit_txt);
        mPasswordRepeatEditTxt = (EditText) findViewById(R.id.register_password_repeat_edit_txt);
        mNameLayout = (TextInputLayout) findViewById(R.id.register_name_layout);
        mEmailLayout = (TextInputLayout) findViewById(R.id.register_email_layout);
        mPasswordLayout = (TextInputLayout) findViewById(R.id.register_password_layout);
        mPasswordRepeatLayout = (TextInputLayout) findViewById(R.id
                .register_password_repeat_layout);
        mRegisterBtn = (Button) findViewById(R.id.register_button);
        mRegisterLoginBtn = (Button) findViewById(R.id.register_login_btn);

        autoFill();

        initToolbar();

        // Work-around to include error text padding
        mNameLayout.setError(" ");
        mEmailLayout.setError(" ");
        mPasswordLayout.setError(" ");
        mPasswordRepeatLayout.setError(" ");
        mNameLayout.setError(null);
        mEmailLayout.setError(null);
        mPasswordLayout.setError(null);
        mPasswordRepeatLayout.setError(null);

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        mRegisterLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logIn();
            }
        });
    }

    private void initToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowHomeEnabled(true);
        }
    }

    private void autoFill() {
        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account[] accounts = manager.getAccountsByType("com.google");
        if (accounts.length > 0) {
            mEmailEditTxt.setText(accounts[0].name);
            mNameEditTxt.setText(formatName(accounts[0].name));

            // If auto filled successfully, then focus on password field
            mPasswordEditTxt.requestFocus();
        }
    }

    private String formatName(String email) {
        String name = email.substring(0, email.indexOf("@"));

        // Capitalize first letters
        StringBuilder res = new StringBuilder();
        String[] strArr = name.split("\\.");
        for (String str : strArr) {
            char[] stringArray = str.trim().toCharArray();
            stringArray[0] = Character.toUpperCase(stringArray[0]);
            str = new String(stringArray);
            res.append(str).append(" ");
        }
        name = res.toString();

        return name;
    }

    public void register() {
        String name = mNameEditTxt.getText().toString();
        String email = mEmailEditTxt.getText().toString();
        String password = mPasswordEditTxt.getText().toString();
        String passwordRepeat = mPasswordRepeatEditTxt.getText().toString();

        Boolean foundError = false;
        if (TextUtils.isEmpty(name)) {
            mNameLayout.setError("Name is required");
            foundError = true;
        } else {
            mNameLayout.setError(null);
        }
        if (TextUtils.isEmpty(email)) {
            mEmailLayout.setError("Email is required");
            foundError = true;
        } else if (!isEmailValid(email)) {
            mEmailLayout.setError("Email is invalid");
            foundError = true;
        } else {
            mEmailLayout.setError(null);
        }
        if (TextUtils.isEmpty(password)) {
            mPasswordLayout.setError("Password is required");
            foundError = true;
        } else if (password.length() < 6) {
            mPasswordLayout.setError("Password should contain at least 6 characters");
            foundError = true;
        } else {
            mPasswordLayout.setError(null);
        }
        if (TextUtils.isEmpty(passwordRepeat)) {
            mPasswordRepeatLayout.setError("Please repeat password");
            foundError = true;
        } else if (!passwordRepeat.equals(password)) {
            mPasswordRepeatLayout.setError("Passwords don't match");
            foundError = true;
        } else {
            mPasswordRepeatLayout.setError(null);
        }

        if (!foundError) {
            signInOnServer(email, name, password);
        }
    }

    private void logIn() {
        finish();
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void signInOnServer(final String email, final String name, final String password) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        final ProgressDialog progressDialog = ProgressDialog.show(this, "",
                "Connecting...");

        final String token = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("token", "");

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Util.URL_SIGN_IN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.cancel();
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            if (responseJSON.getString("result").equals("success")) {
                                String id = responseJSON.getString("id");
                                String avatar = responseJSON.getString("avatar");
                                savePreferences(id, name, email, avatar);
                                onLoginSuccess();
                            } else if (responseJSON.getString("result").equals("empty")) {
                                Toast.makeText(RegisterActivity.this, "Some fields are empty",
                                        Toast.LENGTH_LONG).show();
                            } else if (responseJSON.getString("result").equals("exists")) {
                                Toast.makeText(RegisterActivity.this,
                                        "User with this email already exists", Toast.LENGTH_LONG)
                                        .show();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Unknown server error",
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            if (Util.isDebugging()) {
                                Toast.makeText(RegisterActivity.this, "JSON error: " + response,
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Unknown server error",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                        Util.Log(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.cancel();
                Toast.makeText(RegisterActivity.this, "Server error",
                        Toast.LENGTH_LONG).show();
                Util.Log("Server error: " + error);
            }
        }) {
            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                if (volleyError.networkResponse != null
                        && volleyError.networkResponse.data != null) {
                    volleyError = new VolleyError(new String(volleyError.networkResponse.data));
                }

                return volleyError;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);
                params.put("token", token);
                return params;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void savePreferences(String id, String name, String email, String avatar) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString("id", id).putString("name", name).putString("email", email)
                .putString("avatar", avatar).putBoolean("is_social", false).apply();
    }

    private void onLoginSuccess() {
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        SplashActivity.sActivity.finish();
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

}