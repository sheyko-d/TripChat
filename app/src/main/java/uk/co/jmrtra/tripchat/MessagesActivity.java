package uk.co.jmrtra.tripchat;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import uk.co.jmrtra.tripchat.adapter.MessagesAdapter;


public class MessagesActivity extends AppCompatActivity {

    public final static String EXTRA_NAME = "name";
    public final static String EXTRA_IMAGE = "image";
    private MessagesAdapter mAdapter;
    private SortedList<MessagesAdapter.Message> mMessages = new SortedList<>(MessagesAdapter
            .Message.class, new SortedList.Callback<MessagesAdapter.Message>() {
                @Override
                public int compare(MessagesAdapter.Message o1, MessagesAdapter.Message o2) {
                    return o1.getTimestamp().compareTo(o2.getTimestamp());
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

        RecyclerView messagesRecycler = (RecyclerView) findViewById(R.id.messages_recycler);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        messagesRecycler.setLayoutManager(layoutManager);

        messagesRecycler.setHasFixedSize(true);

        mAdapter = new MessagesAdapter(this, mMessages);
        messagesRecycler.setAdapter(mAdapter);

        mMessages.clear();
        mMessages.add(new MessagesAdapter.Message("0", "Hello there Barack",
                "1438183791000",
                MessagesAdapter.ITEM_TYPE_MESSAGE_OUT));

        mMessages.add(new MessagesAdapter.Message("1", "Hello there Barack",
                "1438183791000",
                MessagesAdapter.ITEM_TYPE_MESSAGE_IN));
        mMessages.add(new MessagesAdapter.Message("2", "Hello there Barack",
                "1438183791000",
                MessagesAdapter.ITEM_TYPE_MESSAGE_IN));
        mMessages.add(new MessagesAdapter.Message("3", "Hello there Barack",
                "1438183791000",
                MessagesAdapter.ITEM_TYPE_MESSAGE_IN));
        mMessages.add(new MessagesAdapter.Message("4", "Hello there Barack",
                "1438183791000",
                MessagesAdapter.ITEM_TYPE_MESSAGE_OUT));
        mMessages.add(new MessagesAdapter.Message("5", "Hello there Barack",
                "1438183791000",
                MessagesAdapter.ITEM_TYPE_MESSAGE_OUT));
        mMessages.add(new MessagesAdapter.Message("6", "Hello there Barack",
                "1438183791000",
                MessagesAdapter.ITEM_TYPE_MESSAGE_IN));
        mMessages.add(new MessagesAdapter.Message("7", "Hello there Barack",
                "1438183791000",
                MessagesAdapter.ITEM_TYPE_MESSAGE_OUT));

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).showImageOnLoading(R.color.placeholder_bg)
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
                ((ImageView) findViewById(R.id.messages_avatar)));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

}
