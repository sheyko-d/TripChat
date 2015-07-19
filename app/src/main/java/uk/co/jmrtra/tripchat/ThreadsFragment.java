package uk.co.jmrtra.tripchat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.co.jmrtra.tripchat.adapter.ThreadsAdapter;

public class ThreadsFragment extends Fragment {
    private ThreadsAdapter mAdapter;
    private SortedList mThreads = new SortedList<>(ThreadsAdapter.Thread.class,
            new SortedList.Callback<ThreadsAdapter.Thread>() {
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

    public static ThreadsFragment newInstance() {
        ThreadsFragment fragment = new ThreadsFragment();
        return fragment;
    }

    public ThreadsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView threadsRecycler = (RecyclerView) inflater.inflate(R.layout.fragment_messages,
                container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        threadsRecycler.setLayoutManager(layoutManager);

        threadsRecycler.setHasFixedSize(true);

        mAdapter = new ThreadsAdapter(getActivity(), mThreads);
        threadsRecycler.setAdapter(mAdapter);

        mThreads.clear();
        mThreads.add(new ThreadsAdapter.Thread("0", "Barack Obama", "Hello there Barack",
                System.currentTimeMillis() + "",
                "https://pbs.twimg.com/profile_images/451007105391022080/iu1f7brY_400x400.png"));
        mThreads.add(new ThreadsAdapter.Thread("1", "Taylor Swift", "Blah blah blah",
                System.currentTimeMillis() + "",
                "http://a.abcnews.com/images/Entertainment/gty_taylor_swift_jc_150127_16x9_992.jpg"));
        mThreads.add(new ThreadsAdapter.Thread("2", "Angelina Jolie", "Test message, hey",
                System.currentTimeMillis() + "",
                "http://tvbythenumbers.zap2it.com/wp-content/uploads/2014/12/Angelina-Jolie-arrives-on-the-red-carpet-for-the-86th-Academy-Awards.jpg"));

        return threadsRecycler;
    }

}