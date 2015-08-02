package uk.co.jmrtra.tripchat.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import uk.co.jmrtra.tripchat.MessagesActivity;
import uk.co.jmrtra.tripchat.R;
import uk.co.jmrtra.tripchat.Util;

public class ThreadsAdapter extends
        RecyclerView.Adapter<ThreadsAdapter.ThreadHolder> {

    private final DisplayImageOptions mDefaultOptions;
    private Context mContext;
    private ImageLoader mImageLoader;
    private SortedList<Thread> threads;

    public ThreadsAdapter(Context context, SortedList<Thread> threads) {
        mContext = context;
        this.threads = threads;

        mDefaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).showImageOnLoading(R.color.placeholder_bg)
                .showImageOnFail(R.drawable.avatar_placeholder)
                .displayer(new FadeInBitmapDisplayer(250, true, false, false))
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).defaultDisplayImageOptions(mDefaultOptions).build();
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(config);
    }

    public static class Thread {

        public String threadId;
        public String name;
        public String snippet;
        public String lastTimestamp;
        public String avatar;

        public Thread(String threadId, String name, String snippet, String lastTimestamp,
                      String avatar) {
            this.threadId = threadId;
            this.name = name;
            this.snippet = snippet;
            this.lastTimestamp = lastTimestamp;
            this.avatar = avatar;
        }

        public String getThreadId() {
            return threadId;
        }

        public String getName() {
            return name;
        }

        public String getSnippet() {
            return snippet;
        }

        public String getLastTimestamp() {
            return lastTimestamp;
        }

        public String getLastTime() {
            return Util.formatTime(lastTimestamp);
        }

        public String getAvatar() {
            return avatar;
        }

    }

    public class ThreadHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView avatarImg;
        public TextView nameTxt;
        public TextView snippetTxt;
        public TextView timeTxt;

        public ThreadHolder(View v) {
            super(v);
            avatarImg = (ImageView) v.findViewById(R.id.threads_img);
            nameTxt = (TextView) v.findViewById(R.id.threads_name_txt);
            snippetTxt = (TextView) v.findViewById(R.id.threads_snippet_txt);
            timeTxt = (TextView) v.findViewById(R.id.threads_time_txt);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            threadClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ThreadHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        return new ThreadHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_threads, parent, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ThreadHolder holder, int position) {
        Thread thread = threads.get(position);

        holder.nameTxt.setText(thread.getName());
        holder.snippetTxt.setText(Html.fromHtml(thread.getSnippet()));
        if (!TextUtils.isEmpty(thread.getLastTime())) {
            holder.timeTxt.setText(thread.getLastTime());
            holder.timeTxt.setVisibility(View.VISIBLE);
        } else {
            holder.timeTxt.setVisibility(View.GONE);
        }
        mImageLoader.displayImage(thread.getAvatar(), holder.avatarImg, mDefaultOptions);
    }

    @Override
    public int getItemCount() {
        return threads.size();
    }

    OnItemClickListener threadClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(View v, int position) {
            mContext.startActivity(new Intent(mContext, MessagesActivity.class)
                    .putExtra(MessagesActivity.EXTRA_THREAD_ID, threads.get(position).getThreadId())
                    .putExtra(MessagesActivity.EXTRA_IMAGE, threads.get(position).getAvatar())
                    .putExtra(MessagesActivity.EXTRA_NAME, threads.get(position).getName()));
        }

    };
}