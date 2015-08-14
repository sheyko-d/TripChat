package uk.co.jmrtra.tripchat.adapter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.co.jmrtra.tripchat.MainActivity;
import uk.co.jmrtra.tripchat.MessagesActivity;
import uk.co.jmrtra.tripchat.R;
import uk.co.jmrtra.tripchat.Util;

public class TripsAdapter extends
        RecyclerView.Adapter<TripsAdapter.TripHolder> {

    public static final int TRIP_TYPE_BUS = 0;
    public static final int TRIP_TYPE_TRAIN = 1;
    private final SharedPreferences mPrefs;
    private String mMyId;
    private MainActivity mActivity;
    private ImageLoader mImageLoader;
    private SortedList<Trip> trips;

    public TripsAdapter(MainActivity activity, SortedList<Trip> trips) {
        this.trips = trips;
        mActivity = activity;

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).showImageOnLoading(R.color.placeholder_bg)
                .displayer(new FadeInBitmapDisplayer(250, true, false, false))
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                activity).defaultDisplayImageOptions(defaultOptions).build();
        ImageLoader.getInstance().init(config);
        mImageLoader = ImageLoader.getInstance();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        mMyId = mPrefs.getString("id", "");
    }

    public static class Trip {

        public String id;
        public String userId;
        public String departureCode;
        public String departureName;
        public String departureTimestamp;
        public String arrivalCode;
        public String arrivalName;
        public String arrivalTimestamp;
        public String image;
        public Integer tripType;
        public Boolean isFavorite;

        public Trip(String id, String userId, String departureCode, String departureName,
                    String departureTimestamp, String arrivalCode, String arrivalName,
                    String arrivalTimestamp, String image, Integer tripType, Boolean isFavorite) {
            this.id = id;
            this.userId = userId;
            this.departureCode = departureCode;
            this.departureName = departureName;
            this.departureTimestamp = departureTimestamp;
            this.arrivalCode = arrivalCode;
            this.arrivalName = arrivalName;
            this.arrivalTimestamp = arrivalTimestamp;
            this.image = image;
            this.tripType = tripType;
            this.isFavorite = isFavorite;
        }

        public String getId() {
            return id;
        }

        public String getUserId() {
            return userId;
        }

        public String getDepartureCode() {
            return departureCode;
        }

        public String getDepartureName() {
            return departureName;
        }

        public String getArrivalCode() {
            return arrivalCode;
        }

        public String getArrivalName() {
            return arrivalName;
        }

        public String getImage() {
            return image;
        }

        public Integer getTripType() {
            return tripType;
        }

        public Boolean isFavorite() {
            return isFavorite;
        }

        @SuppressLint("SimpleDateFormat")
        public String getDepartureTime() {
            return new SimpleDateFormat("HH:mm").format(Long.parseLong(departureTimestamp));
        }

        public String getDepartureTimestamp() {
            return departureTimestamp;
        }

        @SuppressLint("SimpleDateFormat")
        public String getDepartureDate() {
            return new SimpleDateFormat("EE dd MMM").format(Long.parseLong(departureTimestamp));
        }

        @SuppressLint("SimpleDateFormat")
        public String getArrivalTime() {
            return new SimpleDateFormat("HH:mm").format(Long.parseLong(arrivalTimestamp));
        }

        @SuppressLint("SimpleDateFormat")
        public String getArrivalDate() {
            return new SimpleDateFormat("EE dd MMM").format(Long.parseLong(arrivalTimestamp));
        }

    }

    public class TripHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView departureCodeTxt;
        public TextView departureNameTxt;
        public TextView departureTimeTxt;
        public TextView departureDateTxt;
        public TextView arrivalCodeTxt;
        public TextView arrivalNameTxt;
        public TextView arrivalTimeTxt;
        public TextView arrivalDateTxt;
        public ImageView img;
        public ImageView typeImg;
        public ImageButton chatBtn;
        public ImageView removeImg;
        public ImageView favoriteImg;

        public TripHolder(View v) {
            super(v);
            img = (ImageView) v.findViewById(R.id.trips_img);
            typeImg = (ImageView) v.findViewById(R.id.trips_type_img);
            departureCodeTxt = (TextView) v
                    .findViewById(R.id.trips_departure_code_txt);
            departureNameTxt = (TextView) v.findViewById(R.id.trips_departure_name_txt);
            departureTimeTxt = (TextView) v.findViewById(R.id.trips_departure_time_txt);
            departureDateTxt = (TextView) v.findViewById(R.id.trips_departure_date_txt);
            arrivalCodeTxt = (TextView) v.findViewById(R.id.trips_arrival_code_txt);
            arrivalNameTxt = (TextView) v.findViewById(R.id.trips_arrival_name_txt);
            arrivalTimeTxt = (TextView) v.findViewById(R.id.trips_arrival_time_txt);
            arrivalDateTxt = (TextView) v.findViewById(R.id.trips_arrival_date_txt);
            chatBtn = (ImageButton) v.findViewById(R.id.trips_chat_btn);
            removeImg = (ImageView) v.findViewById(R.id.trips_remove_img);
            favoriteImg = (ImageView) v.findViewById(R.id.trips_favorite_img);

            chatBtn.setOnClickListener(this);
            removeImg.setOnClickListener(this);
            favoriteImg.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.trips_chat_btn) {
                tripClickListener.onItemClick(v, getAdapterPosition());
            } else if (v.getId() == R.id.trips_remove_img) {
                removeClickListener.onItemClick(v, getAdapterPosition());
            } else {
                favoriteClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TripHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        return new TripHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_trips, parent, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(TripHolder holder, int position) {
        Trip trip = trips.get(position);

        holder.departureNameTxt.setText(trip.getDepartureName());
        holder.departureCodeTxt.setText(trip.getDepartureCode());
        holder.departureTimeTxt.setText(trip.getDepartureTime());
        holder.departureDateTxt.setText(trip.getDepartureDate());
        holder.arrivalNameTxt.setText(trip.getArrivalName());
        holder.arrivalCodeTxt.setText(trip.getArrivalCode());
        holder.arrivalTimeTxt.setText(trip.getArrivalTime());
        holder.arrivalDateTxt.setText(trip.getArrivalDate());
        mImageLoader.displayImage(trip.getImage(), holder.img);
        if (trip.getTripType() == TRIP_TYPE_BUS) {
            holder.typeImg.setImageResource(R.drawable.ic_type_bus);
        } else {
            holder.typeImg.setImageResource(R.drawable.ic_type_train);
        }

        holder.favoriteImg.setImageResource(trip.isFavorite() ? R.drawable.ic_favorite_on
                : R.drawable.ic_favorite_off);
        holder.removeImg.setVisibility(mMyId.equals(trip.getUserId()) ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    OnItemClickListener tripClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(View v, int position) {
            mActivity.startActivity(new Intent(mActivity, MessagesActivity.class)
                    .putExtra(MessagesActivity.EXTRA_TRIP_ID, trips.get(position).getId())
                    .putExtra(MessagesActivity.EXTRA_IMAGE, trips.get(position).getImage())
                    .putExtra(MessagesActivity.EXTRA_NAME, trips.get(position).getDepartureName()
                            + " - " + trips.get(position).getArrivalName()));
        }

    };

    OnItemClickListener removeClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(View v, int position) {
            deleteTrip(position);
        }

    };

    public void deleteTrip(final int position) {
        final String id = trips.get(position).getId();

        final ProgressDialog dialog = new ProgressDialog(mActivity);
        dialog.setMessage("Loading...");
        dialog.show();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Util.URL_DELETE_TRIP,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        dialog.cancel();

                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            if (responseJSON.getString("result").equals("success")) {
                                trips.removeItemAt(position);
                            } else {
                                Util.Log("Unknown server error");
                                Toast.makeText(mActivity, "Unknown server error",
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Util.Log("JSON error: " + e);
                            Toast.makeText(mActivity, "JSON error: " + e,
                                    Toast.LENGTH_LONG).show();
                        }
                        Util.Log(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Util.Log("Server error: " + error);
                Toast.makeText(mActivity, "Server error: " + error, Toast.LENGTH_LONG)
                        .show();
                dialog.cancel();
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
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(1000 * 30,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue.
        Volley.newRequestQueue(mActivity).add(stringRequest);
    }

    OnItemClickListener favoriteClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(View v, int position) {
            Set<String> favoriteSet = mPrefs.getStringSet("favorite_trips", new HashSet<String>());

            String id = trips.get(position).getId();
            if (favoriteSet.contains(id)) {
                favoriteSet.remove(id);
            } else {
                favoriteSet.add(id);
            }

            mPrefs.edit().putStringSet("favorite_trips", favoriteSet).apply();

            Trip checkedTrip = trips.get(position);
            checkedTrip.isFavorite = !checkedTrip.isFavorite;
            trips.updateItemAt(position, checkedTrip);
        }

    };

}