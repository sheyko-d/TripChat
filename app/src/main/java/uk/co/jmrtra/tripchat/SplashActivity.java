package uk.co.jmrtra.tripchat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class SplashActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Button mBtnEmail;
    private Button mBtnGoogle;
    private Button mBtnFacebook;
    private static final int REQUEST_CODE_GOOGLE = 0;
    private static final int REQUEST_CODE_FACEBOOK = 1;
    private CallbackManager mCallbackManager;
    private boolean mIntentInProgress;
    private boolean mSignInClicked;
    private GoogleApiClient mGoogleApiClient;
    private ConnectionResult mConnectionResult;
    public static Activity sActivity;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        FacebookSdk.sdkInitialize(this);

        sActivity = this;

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefs.edit().remove("id").remove("name").remove("email").remove("avatar")
                .remove("is_social").apply();

        mBtnEmail = (Button) findViewById(R.id.splash_button_email);
        mBtnGoogle = (Button) findViewById(R.id.splash_button_google);
        mBtnFacebook = (Button) findViewById(R.id.splash_button_facebook);

        mBtnEmail.setOnClickListener(mLoginClickListener);
        mBtnGoogle.setOnClickListener(mLoginClickListener);
        mBtnFacebook.setOnClickListener(mLoginClickListener);

        initGoogle();
        initFacebook();

        //TODO: Remove this debug method before releasing to Google Play
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Util.Log("Facebook keyhash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("name not found", e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        }
    }

    private void initGoogle() {
        // Initializing google plus api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API,
                        Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
    }

    private void initFacebook() {
        LoginManager.getInstance().logOut();

        mCallbackManager = CallbackManager.Factory.create();
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("Log", "Facebook keyhash: " + Base64.encodeToString(md.digest(),
                        Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("name not found", e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        }

        // Callback registration
        LoginManager.getInstance().registerCallback(mCallbackManager, new
                        FacebookCallback<LoginResult>() {
                            @Override
                            public void onSuccess(LoginResult loginResult) {
                                GraphRequest request = GraphRequest.newMeRequest(loginResult
                                                .getAccessToken(),
                                        new GraphRequest.GraphJSONObjectCallback() {
                                            @Override
                                            public void onCompleted(JSONObject object,
                                                                    GraphResponse response) {
                                                Util.Log(response);
                                                String id;
                                                String email;
                                                String name;
                                                String username;
                                                try {
                                                    id = object.getString("id");
                                                } catch (JSONException e) {
                                                    id = "";
                                                }
                                                try {
                                                    email = object.getString("email");
                                                } catch (JSONException e) {
                                                    email = "";
                                                }
                                                // Username field is deprecated for Facebook API
                                                // with versions 2.0 and higher, so just extract
                                                // username from email.
                                                //
                                                // http://stackoverflow.com/a/23527664
                                                try {
                                                    name = object.getString("name");
                                                } catch (JSONException e) {
                                                    name = "";
                                                }

                                                signInOnServer(id, email, name,
                                                        "http://graph.facebook.com/" + id
                                                                + "/picture?width=2000");
                                            }

                                        });
                                Bundle parameters = new Bundle();
                                parameters.putString("fields", "id,name,email");
                                request.setParameters(parameters);
                                request.executeAsync();
                            }

                            @Override
                            public void onCancel() {
                                // Do nothing
                            }

                            @Override
                            public void onError(FacebookException exception) {
                                if (Util.isDebugging()) {
                                    Toast.makeText(SplashActivity.this,
                                            "Can't sign up with facebook. Try again later ("
                                                    + exception + ")", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(SplashActivity.this,
                                            "Can't sign up with facebook. Try again later",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        }
        );
    }

    View.OnClickListener mLoginClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.splash_button_email) {
                loginEmail();
            } else if (v.getId() == R.id.splash_button_google) {
                loginGoogle();
            } else {
                loginFacebook();
            }
        }
    };

    private void loginEmail() {
        startActivity(new Intent(SplashActivity.this, RegisterActivity.class));
    }

    private void loginGoogle() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    private void loginFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this,
                Collections.singletonList("email"));
    }

    private void onLoginSuccess() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_GOOGLE:
                if (resultCode != RESULT_OK) {
                    mSignInClicked = false;
                }

                mIntentInProgress = false;

                if (!mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.connect();
                }
                break;
            default:
                mCallbackManager.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        FlurryAgent.onEndSession(this);
        super.onStop();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    0).show();
            return;
        }

        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }

    }

    @Override
    public void onConnected(Bundle arg0) {
        if (mSignInClicked) {
            // Get user's information
            try {
                if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                    Person currentPerson = Plus.PeopleApi
                            .getCurrentPerson(mGoogleApiClient);

                    String id = currentPerson.getId();
                    String name = currentPerson.getDisplayName();
                    String personPhotoUrl = currentPerson.getImage().getUrl();
                    String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

                    personPhotoUrl = personPhotoUrl.substring(0,
                            personPhotoUrl.length() - 2)
                            + 2000;

                    signInOnServer(id, email, name, personPhotoUrl);


                    // by default the profile url gives 50x50 px image only
                    // we can replace the value with whatever dimension we want by
                    // replacing sz=X


                } else {
                    Toast.makeText(getApplicationContext(),
                            "Person information is null", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            Plus.AccountApi
                    .revokeAccessAndDisconnect(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            Log.d("Log", "Disconnected");
                        }
                    });
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }
        mSignInClicked = false;
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    /**
     * Method to resolve any sign in errors
     */
    private void resolveSignInError() {
        if (mConnectionResult != null && mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, REQUEST_CODE_GOOGLE);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    private void signInOnServer(final String id, final String email, final String name,
                                final String avatar) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        final ProgressDialog progressDialog = ProgressDialog.show(this, "",
                "Connecting...");

        final String token = mPrefs.getString("token", "");

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                Util.URL_SIGN_IN_SOCIAL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.cancel();
                try {
                    JSONObject responseJSON = new JSONObject(response);
                    if (responseJSON.getString("result").equals("success")) {
                        String id = responseJSON.getString("id");
                        savePreferences(id, name, email, avatar);
                        onLoginSuccess();
                    } else if (responseJSON.getString("result").equals("empty")) {
                        Toast.makeText(SplashActivity.this, "Some fields are empty",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SplashActivity.this, "Unknown server error",
                                Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    if (Util.isDebugging()) {
                        Toast.makeText(SplashActivity.this, "JSON error: " + response,
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SplashActivity.this, "Unknown server error",
                                Toast.LENGTH_LONG).show();
                    }
                }
                Util.Log(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.cancel();
                Toast.makeText(SplashActivity.this, "Server error",
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
                params.put("id", id);
                params.put("name", name);
                params.put("email", email);
                params.put("avatar", avatar);
                params.put("token", token);
                return params;
            }


        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void savePreferences(String id, String name, String email, String avatar) {
        mPrefs.edit().putString("id", id).putString("name", name).putString("email", email)
                .putString("avatar", avatar).putBoolean("is_social", true).apply();
    }

}
