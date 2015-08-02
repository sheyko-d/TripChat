package uk.co.jmrtra.tripchat;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.flurry.android.FlurryAgent;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import uk.co.jmrtra.tripchat.adapter.MessagesAdapter;


public class MessagesActivity extends AppCompatActivity {

    public final static String EXTRA_NAME = "name";
    public final static String EXTRA_IMAGE = "image";
    public final static String EXTRA_THREAD_ID = "thread_id";
    public final static String EXTRA_TRIP_ID = "trip_id";
    public final static String EXTRA_MESSAGE_ID = "message_id";
    public final static String EXTRA_MESSAGE_THREAD_ID = "message_thread_id";
    public final static String EXTRA_MESSAGE_TRIP_ID = "message_trip_id";
    public final static String EXTRA_MESSAGE_TEXT = "message_text";
    public final static String EXTRA_MESSAGE_TIMESTAMP = "message_timestamp";
    public static final String BROADCAST_RECEIVED_MESSAGE
            = "uk.co.jmrtra.tripchat:RECEIVED_MESSAGE";
    private MessagesAdapter mAdapter;
    private SortedList<MessagesAdapter.Message> mMessages = new SortedList<>(MessagesAdapter
            .Message.class, new SortedList.Callback<MessagesAdapter.Message>() {
        @Override
        public int compare(MessagesAdapter.Message o1, MessagesAdapter.Message o2) {
            return o2.getTimestamp().compareTo(o1.getTimestamp());
        }

        @Override
        public void onInserted(int position, int count) {
            mAdapter.notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            mAdapter.notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            mAdapter.notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onChanged(int position, int count) {
            mAdapter.notifyItemRangeChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(MessagesAdapter.Message oldItem, MessagesAdapter.Message newItem) {
            // return whether the items' visual representations are the same or not.
            return oldItem.getType().equals(newItem.getType())
                    && oldItem.getText().equals(newItem.getText())
                    && oldItem.getTimestamp().equals(newItem.getTimestamp());
        }

        @Override
        public boolean areItemsTheSame(MessagesAdapter.Message item1, MessagesAdapter.Message item2) {
            return item1.getMessageId().equals(item2.getMessageId());
        }
    });
    private ImageLoader mImageLoader;
    private View mProgressBar;
    private ImageButton mSendBtn;
    private RecyclerView mMessagesRecycler;
    private EditText mEditTxt;
    public static String sThreadId;
    public static String sTripId;
    public static boolean sActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mProgressBar = findViewById(R.id.messages_progress_bar);
        mSendBtn = (ImageButton) findViewById(R.id.messages_send_btn);
        mMessagesRecycler = (RecyclerView) findViewById(R.id.messages_recycler);
        mEditTxt = (EditText) findViewById(R.id.messages_edit_txt);

        mEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSendBtn.setEnabled(mEditTxt.getText().toString().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mEditTxt.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                            sendMessage(mEditTxt.getText().toString());
                            return true;
                        }
                        return false;
                    }
                });

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(mEditTxt.getText().toString());
            }
        });
        mSendBtn.setEnabled(false);

        initRecycler();

        initTopBar();

        sThreadId = getIntent().getStringExtra(EXTRA_THREAD_ID);
        sTripId = getIntent().getStringExtra(EXTRA_TRIP_ID);
        getMessages();

        // Add a receiver to listen to incoming messages
        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_RECEIVED_MESSAGE);
        registerReceiver(mMessagesReceiver, filter);

        // Clear old notification for this trip/private conversation
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (!TextUtils.isEmpty(sThreadId)) {
            notificationManager.cancel(Integer.parseInt(sThreadId));
        } else {
            notificationManager.cancel(Integer.parseInt(sTripId));
        }
    }

    BroadcastReceiver mMessagesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String id = intent.getStringExtra(EXTRA_MESSAGE_ID);
            String text = intent.getStringExtra(EXTRA_MESSAGE_TEXT);
            String timestamp = intent.getStringExtra(EXTRA_MESSAGE_TIMESTAMP);
            int type = 0;
            mMessages.add(new MessagesAdapter.Message(id, text, timestamp, type));
            mMessagesRecycler.scrollToPosition(0);
        }
    };

    private void initTopBar() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).showImageOnLoading(R.color.placeholder_bg)
                .showImageOnFail(R.color.primary)
                .displayer(new FadeInBitmapDisplayer(250, true, false, false))
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this).defaultDisplayImageOptions(defaultOptions).build();
        ImageLoader.getInstance().init(config);
        mImageLoader = ImageLoader.getInstance();

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(getIntent().getStringExtra(EXTRA_NAME));

        mImageLoader.displayImage(getIntent().getStringExtra(EXTRA_IMAGE),
                ((ImageView) findViewById(R.id.messages_avatar)), defaultOptions);
    }

    private void initRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mMessagesRecycler.setLayoutManager(layoutManager);

        mMessagesRecycler.setHasFixedSize(true);

        mAdapter = new MessagesAdapter(mMessages);
        mMessagesRecycler.setAdapter(mAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    public void getMessages() {
        final String id = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("id", "");

        mProgressBar.setVisibility(View.VISIBLE);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Util.URL_GET_MESSAGES,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            if (responseJSON.getString("result").equals("success")) {
                                JSONArray threadsJSON = responseJSON.getJSONArray("messages");

                                int threadsCount = threadsJSON.length();
                                mMessages.beginBatchedUpdates();
                                mMessages.clear();
                                for (int i = 0; i < threadsCount; i++) {
                                    String id = threadsJSON.getJSONObject(i).getString("id");
                                    String text = threadsJSON.getJSONObject(i)
                                            .getString("text");
                                    String timestamp = threadsJSON.getJSONObject(i)
                                            .getString("timestamp");
                                    Integer type = threadsJSON.getJSONObject(i)
                                            .getInt("type");
                                    mMessages.add(new MessagesAdapter.Message(id, text,
                                            timestamp, type));
                                }

                                mMessages.endBatchedUpdates();
                            } else if (responseJSON.getString("result").equals("empty")) {
                                Util.Log("Some fields are empty");
                                Toast.makeText(MessagesActivity.this, "Some fields are empty",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Util.Log("Unknown server error");
                                Toast.makeText(MessagesActivity.this, "Unknown server error",
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Util.Log("JSON error: " + e);
                            Toast.makeText(MessagesActivity.this, "JSON error: " + e,
                                    Toast.LENGTH_LONG).show();
                        }
                        Util.Log(response);
                        mProgressBar.setVisibility(View.GONE);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Util.Log("Server error: " + error);
                Toast.makeText(MessagesActivity.this, "Server error: " + error, Toast.LENGTH_LONG)
                        .show();
                mProgressBar.setVisibility(View.GONE);
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
                if (!TextUtils.isEmpty(sThreadId)) {
                    params.put("thread_id", sThreadId);
                } else {
                    params.put("trip_id", sTripId);
                }
                params.put("user_id", id);
                return params;
            }
        };
        // Add the request to the RequestQueue.
        Volley.newRequestQueue(MessagesActivity.this).add(stringRequest);
    }

    private void sendMessage(final String text) {
        final String id = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("id", "");

        mEditTxt.setEnabled(false);
        mSendBtn.setEnabled(false);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Util.URL_SEND_MESSAGE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            if (responseJSON.getString("result").equals("success")) {
                                String id = responseJSON.getString("id");
                                String timestamp = responseJSON.getString("timestamp");
                                Integer type = 1;
                                mMessages.add(new MessagesAdapter.Message(id, text,
                                        timestamp, type));
                                mMessagesRecycler.scrollToPosition(0);
                            } else if (responseJSON.getString("result").equals("empty")) {
                                Util.Log("Some fields are empty");
                                Toast.makeText(MessagesActivity.this, "Some fields are empty",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Util.Log("Unknown server error");
                                Toast.makeText(MessagesActivity.this, "Unknown server error",
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Util.Log("JSON error: " + e);
                            Toast.makeText(MessagesActivity.this, "JSON error: " + e,
                                    Toast.LENGTH_LONG).show();
                        }
                        Util.Log(response);
                        mEditTxt.setText("");
                        mEditTxt.setEnabled(true);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Util.Log("Server error: " + error);
                Toast.makeText(MessagesActivity.this, "Server error: " + error, Toast.LENGTH_LONG)
                        .show();
                mEditTxt.setText("");
                mEditTxt.setEnabled(true);
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
                params.put("user_id", id);
                if (!TextUtils.isEmpty(sThreadId)) {
                    params.put("thread_id", sThreadId);
                } else {
                    params.put("trip_id", sTripId);
                }
                params.put("text", text);
                return params;
            }
        };
        // Add the request to the RequestQueue.
        Volley.newRequestQueue(MessagesActivity.this).add(stringRequest);
    }

    @Override
    public void onStart() {
        super.onStart();
        sActive = true;
        FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
    }

    @Override
    public void onStop() {
        sActive = false;
        FlurryAgent.onEndSession(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMessagesReceiver);
    }

}
