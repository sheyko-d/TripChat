package uk.co.jmrtra.tripchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.ArrayList;
import java.util.List;

import uk.co.jmrtra.tripchat.R;
import uk.co.jmrtra.tripchat.Util;
import uk.co.jmrtra.tripchat.Util.Station;

public class StationsAdapter extends ArrayAdapter<Station> implements Filterable {

    private ImageLoader mImageLoader;
    private Context mContext;
    private ArrayList<Station> fullList;
    private ArrayList<Station> mOriginalValues;
    private ArrayFilter mFilter;
    public Station selectedStation;
    public boolean stationIsIncorrect = true;

    public StationsAdapter(Context context, int resource, int textId, List<Station> stations) {
        super(context, resource, textId, stations);
        mContext = context;
        fullList = (ArrayList<Station>) stations;
        mOriginalValues = new ArrayList<>(fullList);

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).showImageOnLoading(R.color.placeholder_bg)
                .displayer(new FadeInBitmapDisplayer(250, true, false, false))
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).defaultDisplayImageOptions(defaultOptions).build();
        ImageLoader.getInstance().init(config);
        mImageLoader = ImageLoader.getInstance();
    }

    @Override
    public int getCount() {
        return fullList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_stations, null);
            viewHolder = new ViewHolder();
            viewHolder.nameTxt = (TextView) convertView
                    .findViewById(R.id.add_trip_station_name_txt);
            viewHolder.distanceTxt = (TextView) convertView
                    .findViewById(R.id.add_trip_station_distance_txt);
            viewHolder.img = (ImageView) convertView.findViewById(R.id.add_trip_station_img);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Util.Station customer = fullList.get(position);

        viewHolder.nameTxt.setText(customer.getName());
        viewHolder.distanceTxt.setText(customer.getDistance());
        mImageLoader.displayImage(customer.getImage(), viewHolder.img);

        return convertView;
    }

    static class ViewHolder {
        TextView nameTxt;
        TextView distanceTxt;
        ImageView img;
    }

    @Override
    public Station getItem(int position) {
        return fullList.get(position);
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    private class ArrayFilter extends Filter {
        private Object lock;

        @Override
        public String convertResultToString(Object resultValue) {
            selectedStation = (Station) (resultValue);
            String str = selectedStation.getName();
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            fullList = (ArrayList<Station>) mOriginalValues.clone();

            FilterResults results = new FilterResults();

            if (constraint != null) {
                ArrayList<Util.Station> suggestions = new ArrayList<>();
                for (Util.Station station : fullList) {
                    if (station.getName().toLowerCase().contains(constraint.toString()
                            .toLowerCase())) {
                        suggestions.add(station);
                    }
                }

                results.values = suggestions;
                results.count = suggestions.size();
            }

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.values != null) {
                fullList = (ArrayList<Station>) results.values;
                stationIsIncorrect = false;
            } else {
                fullList = new ArrayList<>();
            }
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

    public Station getSelectedStation() {
        return selectedStation;
    }
}