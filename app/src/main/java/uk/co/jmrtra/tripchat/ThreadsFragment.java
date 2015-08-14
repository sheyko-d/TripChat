package uk.co.jmrtra.tripchat;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import uk.co.jmrtra.tripchat.adapter.ThreadsAdapter;

public class ThreadsFragment extends Fragment {
    private SortedList<ThreadsAdapter.Thread> mThreads = new SortedList<>(ThreadsAdapter.Thread
            .class, new SortedList.Callback<ThreadsAdapter.Thread>() {
        @Override
        public int compare(ThreadsAdapter.Thread o1, ThreadsAdapter.Thread o2) {
            return o1.getLastTimestamp().compareTo(o2.getLastTimestamp());
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
        public boolean areContentsTheSame(ThreadsAdapter.Thread oldItem, ThreadsAdapter.Thread newItem) {
            // return whether the items' visual representations are the same or not.
            return oldItem.getName().equals(newItem.getName()) && oldItem.getSnippet()
                    .equals(newItem.getSnippet()) && oldItem.getLastTimestamp()
                    .equals(newItem.getLastTimestamp());
        }

        @Override
        public boolean areItemsTheSame(ThreadsAdapter.Thread item1, ThreadsAdapter.Thread item2) {
            return item1.getThreadId().equals(item2.getThreadId());
        }
    });
    private ThreadsAdapter mAdapter;
    private View mProgressBar;
    private boolean mForceUpdate;
    private RecyclerView mThreadsRecycler;

    public static ThreadsFragment newInstance() {
        return new ThreadsFragment();
    }

    public ThreadsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_threads, container, false);

        mThreadsRecycler = (RecyclerView) rootView.findViewById(R.id
                .threads_recycler_view);
        mProgressBar = rootView.findViewById(R.id.threads_progress_bar);

        initRecycler();

        mThreads.clear();

        mForceUpdate = false;
        getThreads();

        return rootView;
    }

    private void initRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mThreadsRecycler.setLayoutManager(layoutManager);

        mThreadsRecycler.setHasFixedSize(true);

        mAdapter = new ThreadsAdapter(getActivity(), mThreads);
        mThreadsRecycler.setAdapter(mAdapter);
    }

    public void getThreads() {
        final String id = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString("id", "");

        if (!mForceUpdate) {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Util.URL_GET_THREADS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            if (responseJSON.getString("result").equals("success")) {
                                JSONArray threadsJSON = responseJSON.getJSONArray("threads");

                                int threadsCount = threadsJSON.length();
                                mThreads.beginBatchedUpdates();
                                mThreads.clear();
                                for (int i = 0; i < threadsCount; i++) {
                                    String id = threadsJSON.getJSONObject(i).getString("id");
                                    String avatar = threadsJSON.getJSONObject(i)
                                            .getString("avatar");
                                    String name = threadsJSON.getJSONObject(i)
                                            .getString("name");
                                    String text = threadsJSON.getJSONObject(i)
                                            .getString("text");
                                    String timestamp = threadsJSON.getJSONObject(i)
                                            .getString("timestamp");
                                    mThreads.add(new ThreadsAdapter.Thread(id, name, text,
                                            timestamp, avatar));
                                }

                                mThreads.endBatchedUpdates();
                            } else {
                                Util.Log("Unknown server error");
                                Toast.makeText(getActivity(), "Unknown server error",
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Util.Log("JSON error: " + e);
                            Toast.makeText(getActivity(), "JSON error: " + e,
                                    Toast.LENGTH_LONG).show();
                        }
                        Util.Log(response);
                        mProgressBar.setVisibility(View.GONE);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Util.Log("Server error: " + error);
                Toast.makeText(getActivity(), "Server error: " + error, Toast.LENGTH_LONG).show();
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
                params.put("user_id", id);
                return params;
            }
        };
        // Add the request to the RequestQueue.
        Volley.newRequestQueue(getActivity()).add(stringRequest);
    }

}