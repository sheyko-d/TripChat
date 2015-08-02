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


public class LoginActivity extends AppCompatActivity {

    private EditText mEmailEditTxt;
    private EditText mPasswordEditTxt;
    private TextInputLayout mEmailLayout;
    private TextInputLayout mPasswordLayout;
    private Button mRegisterBtn;
    private Button mRegisterLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailEditTxt = (EditText) findViewById(R.id.login_email_edit_txt);
        mPasswordEditTxt = (EditText) findViewById(R.id.login_password_edit_txt);
        mEmailLayout = (TextInputLayout) findViewById(R.id.login_email_layout);
        mPasswordLayout = (TextInputLayout) findViewById(R.id.login_password_layout);
        mRegisterBtn = (Button) findViewById(R.id.login_button);
        mRegisterLoginBtn = (Button) findViewById(R.id.login_register_btn);

        autoFill();

        initToolbar();

        // Work-around to include error text padding
        mEmailLayout.setError(" ");
        mPasswordLayout.setError(" ");
        mEmailLayout.setError(null);
        mPasswordLayout.setError(null);

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logIn();
            }
        });

        mRegisterLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
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

    private void initToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    public void logIn() {
        String email = mEmailEditTxt.getText().toString();
        String password = mPasswordEditTxt.getText().toString();

        Boolean foundError = false;
        if (TextUtils.isEmpty(email)) {
            mEmailLayout.setError("Email or Username is required");
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

        if (!foundError) {
            logInOnServer(email, password);
        }
    }

    private void autoFill() {
        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account[] accounts = manager.getAccountsByType("com.google");
        if (accounts.length > 0) {
            mEmailEditTxt.setText(accounts[0].name);

            // If auto filled successfully, then focus on password field
            mPasswordEditTxt.requestFocus();
            mPasswordLayout.clearAnimation();
        }
    }

    private void register() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void logInOnServer(final String email, final String password) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        final ProgressDialog progressDialog = ProgressDialog.show(this, "",
                "Connecting...");

        final String token = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("token", "");

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Util.URL_LOG_IN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.cancel();
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            if (responseJSON.getString("result").equals("success")) {
                                String id = responseJSON.getString("id");
                                String name = responseJSON.getString("name");
                                String email = responseJSON.getString("email");
                                String avatar = responseJSON.getString("avatar");
                                savePreferences(id, name, email, avatar);
                                onLoginSuccess();
                            } else if (responseJSON.getString("result").equals("empty")) {
                                Toast.makeText(LoginActivity.this, "Some fields are empty",
                                        Toast.LENGTH_LONG).show();
                            } else if (responseJSON.getString("result").equals("incorrect")) {
                                Toast.makeText(LoginActivity.this,
                                        "Incorrect email or password", Toast.LENGTH_LONG)
                                        .show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Unknown server error",
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            if (Util.isDebugging()) {
                                Toast.makeText(LoginActivity.this, "JSON error: " + response,
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Unknown server error",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                        Util.Log(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.cancel();
                Toast.makeText(LoginActivity.this, "Server error",
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
                params.put("email", email);
                params.put("password", password);
                params.put("token", token);
                return params;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void savePreferences(String id, String name, String email,
                                 String avatar) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString("id", id).putString("name", name).putString("email", email)
                .putString("avatar", avatar).putBoolean("is_social", false).apply();
    }

    private void onLoginSuccess() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        SplashActivity.sActivity.finish();
        finish();
    }
}
