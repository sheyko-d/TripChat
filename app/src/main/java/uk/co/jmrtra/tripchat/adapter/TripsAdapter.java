package uk.co.jmrtra.tripchat.adapter;

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

import uk.co.jmrtra.tripchat.R;

public class TripsAdapter extends
        RecyclerView.Adapter<TripsAdapter.TripHolder> {

    public static final int ITEM_TYPE_HEADER = 0;
    public static final int ITEM_TYPE_ITEM = 1;
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

        public Integer type;
        public String departureCode;
        public String departureName;
        public String arrivalCode;
        public String arrivalName;
        public String image;
        public Integer tripType;

        public Trip(Integer type, String departureCode, String departureName,
                    String arrivalCode, String arrivalName, String image,
                    Integer tripType) {
            this.type = type;
            this.departureCode = departureCode;
            this.departureName = departureName;
            this.arrivalCode = arrivalCode;
            this.arrivalName = arrivalName;
            this.image = image;
            this.tripType = tripType;
        }

        public Integer getType() {
            return type;
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

    }

    @Override
    public int getItemViewType(int position) {
        return trips.get(position).getType();
    }

    public class TripHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView departureCodeTxt;
        public TextView departureNameTxt;
        public TextView arrivalCodeTxt;
        public TextView arrivalNameTxt;
        public ImageView img;
        public ImageView typeImg;

        public TripHolder(View v) {
            super(v);
            img = (ImageView) v.findViewById(R.id.trips_img);
            typeImg = (ImageView) v.findViewById(R.id.trips_type_img);
            departureCodeTxt = (TextView) v
                    .findViewById(R.id.trips_departure_code_txt);
            departureNameTxt = (TextView) v.findViewById(R.id.trips_departure_name_txt);
            arrivalCodeTxt = (TextView) v.findViewById(R.id.trips_arrival_code_txt);
            arrivalNameTxt = (TextView) v.findViewById(R.id.trips_arrival_name_txt);

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
        if (viewType == ITEM_TYPE_ITEM) {
            return new TripHolder(LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_trips, parent, false));
        } else {
            return new TripHolder(LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_trips_header, parent, false));
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(TripHolder holder, int position) {
        Trip trip = trips.get(position);

        if (getItemViewType(position) == ITEM_TYPE_ITEM) {
            holder.departureNameTxt.setText(trip.getDepartureName());
            holder.departureCodeTxt.setText(trip.getDepartureCode());
            holder.arrivalNameTxt.setText(trip.getArrivalName());
            holder.arrivalCodeTxt.setText(trip.getArrivalCode());
            mImageLoader.displayImage(trip.getImage(), holder.img);
            if (trip.getTripType() == TRIP_TYPE_BUS) {
                holder.typeImg.setImageResource(R.drawable.ic_type_bus);
            } else {
                holder.typeImg.setImageResource(R.drawable.ic_type_train);
            }
        } else {
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