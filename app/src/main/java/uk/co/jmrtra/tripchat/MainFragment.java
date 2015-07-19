package uk.co.jmrtra.tripchat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.co.jmrtra.tripchat.adapter.TripsAdapter;

public class MainFragment extends Fragment {
    private TripsAdapter mAdapter;
    private SortedList mTrips = new SortedList<>(TripsAdapter.Trip.class,
            new SortedList.Callback<TripsAdapter.Trip>() {
                @Override
                public int compare(TripsAdapter.Trip o1, TripsAdapter.Trip o2) {
                    // Sort items, new ones first
                    int i = o1.getType().compareTo(o2.getType());
                    if (i != 0) return i;

                    return o1.getArrivalName().compareTo(o2.getArrivalName());
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
                public boolean areContentsTheSame(TripsAdapter.Trip oldItem, TripsAdapter.Trip newItem) {
                    // return whether the items' visual representations are the same or not.
                    return oldItem.getType().equals(newItem.getType()) && oldItem.getDepartureName()
                            .equalsIgnoreCase(oldItem.getDepartureName()) && oldItem
                            .getArrivalName().equalsIgnoreCase(oldItem.getArrivalName());
                }

                @Override
                public boolean areItemsTheSame(TripsAdapter.Trip item1, TripsAdapter.Trip item2) {
                    //TODO: Change to real ids
                    return item1.getArrivalName().equals(item2.getArrivalName())
                            && item1.getDepartureName().equals(item2.getDepartureName());
                }
            });

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView mainRecycler = (RecyclerView) inflater.inflate(R.layout.fragment_main,
                container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mainRecycler.setLayoutManager(layoutManager);

        mainRecycler.setHasFixedSize(true);

        mAdapter = new TripsAdapter(getActivity(), mTrips);
        mainRecycler.setAdapter(mAdapter);

        mTrips.clear();
        mTrips.add(new TripsAdapter.Trip(TripsAdapter.ITEM_TYPE_ITEM, "ABW", "Abbey Wood", "WNY", "White Notley", "http://www.essexwalks.com/photos/ew_coggeshall/13_0056.jpg", TripsAdapter.TRIP_TYPE_BUS));
        mTrips.add(new TripsAdapter.Trip(TripsAdapter.ITEM_TYPE_ITEM, "EBT", "Edenbridge Town", "WNE", "Wilnecote", "https://upload.wikimedia.org/wikipedia/commons/a/a2/Edenbridge_1.JPG", TripsAdapter.TRIP_TYPE_BUS));
        mTrips.add(new TripsAdapter.Trip(TripsAdapter.ITEM_TYPE_ITEM, "NTC"   , "Newton St Cyres", "DRN", "Duirinish", "https://upload.wikimedia.org/wikipedia/commons/e/ee/Duirinish_station_geograph-3866550-by-Ben-Brooksbank.jpg", TripsAdapter.TRIP_TYPE_TRAIN));
        mTrips.add(new TripsAdapter.Trip(TripsAdapter.ITEM_TYPE_ITEM, "THT", "Thorntonhall", "DNS", "Dinas Powys", "http://4.bp.blogspot.com/_anDpzjfvZpw/TO5s47hORkI/AAAAAAAAC6Q/8lVISlsNFyk/s1600/Threehorseshoesdinas.jpg", TripsAdapter.TRIP_TYPE_BUS));
        mTrips.add(new TripsAdapter.Trip(TripsAdapter.ITEM_TYPE_ITEM, "DKT", "Dorking West", "ORE", "Ore", "http://www.orestationlodge.com/wp-content/themes/orestationlodge/UserStorage/0001779/Units/00002067/large/xtelluride%20vacation%20rental.jpg", TripsAdapter.TRIP_TYPE_BUS));
        mTrips.add(new TripsAdapter.Trip(TripsAdapter.ITEM_TYPE_ITEM, "WCP", "Worcester Park", "AVY", "Aberdovey", "http://www.picpicx.com/wp-content/uploads/2014/10/df00ae0ddf50acb564bca86710b1922b.jpg", TripsAdapter.TRIP_TYPE_TRAIN));

        return mainRecycler;
    }

}