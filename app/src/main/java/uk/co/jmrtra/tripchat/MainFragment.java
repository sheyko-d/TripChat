package uk.co.jmrtra.tripchat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.co.jmrtra.tripchat.adapter.TripsAdapter;
import uk.co.jmrtra.tripchat.view.EmptyRecyclerView;

public class MainFragment extends Fragment {
    private SortedList<TripsAdapter.Trip> mTrips = new SortedList<>(TripsAdapter.Trip.class,
            new SortedList.Callback<TripsAdapter.Trip>() {
                @Override
                public int compare(TripsAdapter.Trip o1, TripsAdapter.Trip o2) {
                    int i = o2.isFavorite().compareTo(o1.isFavorite());
                    if (i != 0) return i;
                    // Sort items, new ones first
                    return o2.getDepartureTimestamp().compareTo(o1.getDepartureTimestamp());
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
                public boolean areContentsTheSame(TripsAdapter.Trip oldItem,
                                                  TripsAdapter.Trip newItem) {
                    // return whether the items' visual representations are the same or not.
                    return oldItem.getDepartureName().equalsIgnoreCase(oldItem.getDepartureName())
                            && oldItem.getArrivalName().equalsIgnoreCase(oldItem.getArrivalName());
                }

                @Override
                public boolean areItemsTheSame(TripsAdapter.Trip item1, TripsAdapter.Trip item2) {
                    return item1.getId().equals(item2.getId());
                }
            });
    private TripsAdapter mAdapter;
    private EmptyRecyclerView mMainRecycler;
    private SharedPreferences mPrefs;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main,
                container, false);

        mMainRecycler = (EmptyRecyclerView) rootView.findViewById(R.id.main_recycler_view);
        mMainRecycler.setEmptyView(rootView.findViewById(R.id.main_placeholder_layout));

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        initRecycler();
        getTrips(null);

        return rootView;
    }

    private void initRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mMainRecycler.setLayoutManager(layoutManager);
        mMainRecycler.setHasFixedSize(true);

        mAdapter = new TripsAdapter(MainActivity.sActivity, mTrips);
        mMainRecycler.setAdapter(mAdapter);
    }

    public void getTrips(final String query) {
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Util.URL_GET_TRIPS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Set<String> favoriteSet = mPrefs.getStringSet("favorite_trips", new HashSet<String>());
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            if (responseJSON.getString("result").equals("success")) {
                                JSONArray tripsJSON = responseJSON.getJSONArray("trips");
                                int tripsCount = tripsJSON.length();
                                mTrips.clear();
                                mTrips.beginBatchedUpdates();
                                for (int i = 0; i < tripsCount; i++) {
                                    String id = tripsJSON.getJSONObject(i).getString("id");
                                    String userId = tripsJSON.getJSONObject(i).getString("user_id");
                                    int type = tripsJSON.getJSONObject(i).getInt("type");
                                    String departureName = tripsJSON.getJSONObject(i)
                                            .getString("departure_name");
                                    String departureCode = tripsJSON.getJSONObject(i)
                                            .getString("departure_code");
                                    String departureTimestamp = tripsJSON.getJSONObject(i)
                                            .getString("departure_timestamp");
                                    String arrivalName = tripsJSON.getJSONObject(i)
                                            .getString("arrival_name");
                                    String arrivalCode = tripsJSON.getJSONObject(i)
                                            .getString("arrival_code");
                                    String arrivalTimestamp = tripsJSON.getJSONObject(i)
                                            .getString("arrival_timestamp");
                                    String image = tripsJSON.getJSONObject(i).getString("image");

                                    Boolean isFavorite = favoriteSet.contains(id);
                                    mTrips.add(new TripsAdapter.Trip(id, userId, departureCode,
                                            departureName, departureTimestamp, arrivalCode,
                                            arrivalName, arrivalTimestamp, image, type,
                                            isFavorite));
                                }
                                mTrips.endBatchedUpdates();
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
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Util.Log("Server error: " + error);
                Toast.makeText(getActivity(), "Server error: " + error, Toast.LENGTH_LONG)
                        .show();
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
                if (!TextUtils.isEmpty(query)) {
                    params.put("query", query);
                }
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(1000 * 30,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue.
        Volley.newRequestQueue(getActivity()).add(stringRequest);
    }

}