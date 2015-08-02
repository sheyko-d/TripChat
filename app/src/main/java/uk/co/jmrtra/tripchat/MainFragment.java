package uk.co.jmrtra.tripchat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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

import uk.co.jmrtra.tripchat.adapter.TripsAdapter;
import uk.co.jmrtra.tripchat.view.EmptyRecyclerView;

public class MainFragment extends Fragment {
    private SortedList<TripsAdapter.Trip> mTrips = new SortedList<>(TripsAdapter.Trip.class,
            new SortedList.Callback<TripsAdapter.Trip>() {
                @Override
                public int compare(TripsAdapter.Trip o1, TripsAdapter.Trip o2) {
                    // Sort items, new ones first
                    return o2.getDepartureTime().compareTo(o1.getDepartureTime());
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
                    //TODO: Change to real ids
                    return item1.getArrivalName().equals(item2.getArrivalName())
                            && item1.getDepartureName().equals(item2.getDepartureName());
                }
            });
    private TripsAdapter mAdapter;
    private EmptyRecyclerView mMainRecycler;
    private SwipeRefreshLayout mRefreshLayout;
    private int mScrollOffset = 0;

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
        mRefreshLayout = (SwipeRefreshLayout) getActivity()
                .findViewById(R.id.threads_refresh_layout);

        initRecycler();
        getTrips(null);

        return rootView;
    }

    private void initRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mMainRecycler.setLayoutManager(layoutManager);
        mMainRecycler.setHasFixedSize(true);

        mAdapter = new TripsAdapter(getActivity(), mTrips);
        mMainRecycler.setAdapter(mAdapter);

        mMainRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                mScrollOffset += dy;
                updateRefreshLayout();
            }
        });
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            updateRefreshLayout();
        }
    }

    private void updateRefreshLayout() {
        Boolean enabled = mScrollOffset <= 0;
        if (mRefreshLayout != null && mRefreshLayout.isEnabled() != enabled) {
            mRefreshLayout.setEnabled(enabled);
        }
    }

    public void getTrips(final String query) {
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Util.URL_GET_TRIPS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            if (responseJSON.getString("result").equals("success")) {
                                JSONArray tripsJSON = responseJSON.getJSONArray("trips");
                                int tripsCount = tripsJSON.length();
                                mTrips.clear();
                                mTrips.beginBatchedUpdates();
                                for (int i = 0; i < tripsCount; i++) {
                                    String id = tripsJSON.getJSONObject(i).getString("id");
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
                                    mTrips.add(new TripsAdapter.Trip(id, departureCode,
                                            departureName, departureTimestamp, arrivalCode,
                                            arrivalName, arrivalTimestamp, image, type));
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
        // Add the request to the RequestQueue.
        Volley.newRequestQueue(getActivity()).add(stringRequest);
    }

}