package uk.co.jmrtra.tripchat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.text.SimpleDateFormat;

import uk.co.jmrtra.tripchat.R;

public class TripsAdapter extends
        RecyclerView.Adapter<TripsAdapter.TripHolder> {

    public static final int TRIP_TYPE_BUS = 0;
    public static final int TRIP_TYPE_TRAIN = 1;
    private ImageLoader mImageLoader;
    private SortedList<Trip> trips;

    public TripsAdapter(Context context, SortedList<Trip> trips) {
        this.trips = trips;

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).showImageOnLoading(R.color.placeholder_bg)
                .displayer(new FadeInBitmapDisplayer(250, true, false, false))
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).defaultDisplayImageOptions(defaultOptions).build();
        ImageLoader.getInstance().init(config);
        mImageLoader = ImageLoader.getInstance();
    }

    public static class Trip {

        public String departureCode;
        public String departureName;
        public String departureTimestamp;
        public String arrivalCode;
        public String arrivalName;
        public String arrivalTimestamp;
        public String image;
        public Integer tripType;

        public Trip(String departureCode, String departureName, String departureTimestamp,
                    String arrivalCode, String arrivalName, String arrivalTimestamp, String image,
                    Integer tripType) {
            this.departureCode = departureCode;
            this.departureName = departureName;
            this.departureTimestamp = departureTimestamp;
            this.arrivalCode = arrivalCode;
            this.arrivalName = arrivalName;
            this.arrivalTimestamp = arrivalTimestamp;
            this.image = image;
            this.tripType = tripType;
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

        @SuppressLint("SimpleDateFormat")
        public String getDepartureTime() {
            return new SimpleDateFormat("HH:mm").format(Long.parseLong(departureTimestamp));
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

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            tripClickListener.onItemClick(v, getAdapterPosition());
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
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    OnItemClickListener tripClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(View v, int position) {
        }

    };
}