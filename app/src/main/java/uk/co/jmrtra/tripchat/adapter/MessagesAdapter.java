package uk.co.jmrtra.tripchat.adapter;

import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import uk.co.jmrtra.tripchat.R;
import uk.co.jmrtra.tripchat.Util;

public class MessagesAdapter extends
        RecyclerView.Adapter<MessagesAdapter.MessageHolder> {

    public static final int ITEM_TYPE_MESSAGE_IN = 0;
    private SortedList<Message> messages;

    public MessagesAdapter(SortedList<Message> messages) {
        this.messages = messages;

    }

    public static class Message {

        public String messageId;
        public String name;
        public String text;
        public String timestamp;
        public Integer type;

        public Message(String messageId, String text, String timestamp,
                       Integer type) {
            this.messageId = messageId;
            this.text = text;
            this.timestamp = timestamp;
            this.type = type;
        }

        public String getMessageId() {
            return messageId;
        }

        public String getText() {
            return text;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getTime() {
            return Util.formatTime(timestamp);
        }

        public Integer getType() {
            return type;
        }

    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    public class MessageHolder extends RecyclerView.ViewHolder {

        public TextView textTxt;
        public TextView timeTxt;

        public MessageHolder(View v) {
            super(v);
            textTxt = (TextView) v.findViewById(R.id.messages_text_txt);
            timeTxt = (TextView) v.findViewById(R.id.messages_time_txt);
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MessageHolder onCreateViewHolder(ViewGroup parent,
                                            int viewType) {
        if (viewType == ITEM_TYPE_MESSAGE_IN) {
            return new MessageHolder(LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_messages_in, parent, false));
        } else {
            return new MessageHolder(LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_messages_out, parent, false));
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MessageHolder holder, int position) {
        Message message = messages.get(position);

        holder.textTxt.setText(Html.fromHtml(message.getText()));
        holder.timeTxt.setText(message.getTime());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}