/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.jmrtra.tripchat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;

import com.google.android.gms.gcm.GcmListenerService;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String id = data.getString("id");
        String threadId = data.getString("thread_id");
        String tripId = data.getString("trip_id");
        String text = data.getString("text");
        String timestamp = data.getString("timestamp");
        String name = data.getString("name");
        String avatar = data.getString("avatar");

        Util.Log("onMessageReceived = " + tripId);

        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        if (MessagesActivity.sActive && ((MessagesActivity.sThreadId != null
                && MessagesActivity.sThreadId.equals(threadId))
                || (MessagesActivity.sTripId != null && MessagesActivity.sTripId.equals(tripId)))) {
            sendBroadcast(new Intent(MessagesActivity.BROADCAST_RECEIVED_MESSAGE)
                    .putExtra(MessagesActivity.EXTRA_MESSAGE_ID, id)
                    .putExtra(MessagesActivity.EXTRA_MESSAGE_TRIP_ID, tripId)
                    .putExtra(MessagesActivity.EXTRA_MESSAGE_THREAD_ID, threadId)
                    .putExtra(MessagesActivity.EXTRA_MESSAGE_TEXT, text)
                    .putExtra(MessagesActivity.EXTRA_MESSAGE_TIMESTAMP, timestamp));
        } else {
            sendNotification(threadId, tripId, text, name, avatar);
        }
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(final String threadId, final String tripId, String message,
                                  String name, String avatar) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences
                (getApplicationContext());

        Intent intent = new Intent(this, MessagesActivity.class);
        intent.putExtra(MessagesActivity.EXTRA_THREAD_ID, threadId);
        intent.putExtra(MessagesActivity.EXTRA_TRIP_ID, tripId);
        intent.putExtra(MessagesActivity.EXTRA_IMAGE, avatar);
        intent.putExtra(MessagesActivity.EXTRA_NAME, name);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).showImageOnLoading(R.color.placeholder_bg)
                .showImageOnFail(R.drawable.avatar_placeholder)
                .postProcessor(new BitmapProcessor() {
                    @Override
                    public Bitmap process(Bitmap bmp) {
                        return Bitmap.createScaledBitmap(bmp, (int) getResources().getDimension(
                                        android.R.dimen.notification_large_icon_width),
                                (int) getResources().getDimension(android.R.dimen.
                                        notification_large_icon_height), false);
                    }
                })
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext()).defaultDisplayImageOptions(defaultOptions).build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notif)
                .setContentTitle(name)
                .setContentText(message.replaceAll("<(.*?)\\>"," "))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        if (prefs.getBoolean("sound", true)) {
            notificationBuilder.setSound(defaultSoundUri);
        }

        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        imageLoader.loadImage(avatar, new ImageSize((int) getResources().getDimension(
                        android.R.dimen.notification_large_icon_width),
                        (int) getResources().getDimension(
                                android.R.dimen.notification_large_icon_height)), defaultOptions,
                new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        notificationBuilder
                                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                                        R.drawable.avatar_placeholder));

                        Notification note = notificationBuilder.build();
                        if (prefs.getBoolean("vibrate", true)) {
                            note.defaults |= Notification.DEFAULT_VIBRATE;
                        }

                        if (!TextUtils.isEmpty(threadId)) {
                            notificationManager.notify(Integer.parseInt(threadId), note);
                        } else {
                            notificationManager.notify(Integer.parseInt(tripId), note);
                        }
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        notificationBuilder
                                .setLargeIcon(loadedImage);

                        Notification note = notificationBuilder.build();
                        if (prefs.getBoolean("vibrate", true)) {
                            note.defaults |= Notification.DEFAULT_VIBRATE;
                        }

                        if (!TextUtils.isEmpty(threadId)) {
                            notificationManager.notify(Integer.parseInt(threadId), note);
                        } else {
                            notificationManager.notify(Integer.parseInt(tripId), note);
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                    }
                });
    }
}
