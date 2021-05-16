/**
 * [TRIfA], UnifiedPush part of Tox Reference Implementation for Android
 * Copyright (C) 2021 Zoff <zoff@zoff.cc>
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

package com.zoffcc.applications.pushmsg;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unifiedpush.android.connector.MessagingReceiverHandler;

import androidx.core.app.NotificationCompat;

import static com.zoffcc.applications.pushmsg.UnifiedPushReceiver.TAG;

public class UnifiedPushReceiver extends org.unifiedpush.android.connector.MessagingReceiver
{
    static final String TAG = "UnifiedPushReceiver";

    public UnifiedPushReceiver()
    {
        super(new UPHandler());
    }
}

class UPHandler implements MessagingReceiverHandler
{
    private SharedPreferences settings = null;

    public SharedPreferences getSettings(Context context)
    {
        if (settings == null)
        {
            settings = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return settings;
    }

    @Override
    public void onNewEndpoint(@Nullable Context context, @NotNull String endpoint, @NotNull String instance)
    {
        // Called when a new endpoint be used for sending push messages
        Log.i(TAG, "onNewEndpoint:instance=" + instance + " endpoint=" + endpoint);
        String correct_endpoint = endpoint; //;endpoint.replace("/UP?token=", "/message?token=");
        Log.i(TAG, "onNewEndpoint:instance=" + instance + " correct_endpoint=" + correct_endpoint);

        try
        {
            if (!getSettings(context).getBoolean("prefer_fcm", true))
            {
                // wake up trifa here ------------------
                final Intent intent = new Intent();
                intent.setAction("com.zoffcc.applications.trifa.TOKEN_CHANGED");
                intent.putExtra("token", correct_endpoint);
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                intent.setComponent(new ComponentName("com.zoffcc.applications.trifa",
                                                      "com.zoffcc.applications.trifa.MyTokenReceiver"));
                context.sendBroadcast(intent);
                // wake up trifa here ------------------

                final String msg = context.getString(R.string.msg_token_fmt, correct_endpoint);

                try
                {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                catch (Exception e3)
                {
                    e3.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onRegistrationFailed(@Nullable Context context, @NotNull String instance)
    {
        // called when the registration is not possible, eg. no network
        Log.i(TAG, "onRegistrationFailed:instance=" + instance);
    }

    @Override
    public void onRegistrationRefused(@Nullable Context context, @NotNull String instance)
    {
        // called when the registration is refused, eg. an application with the same Id and another token is registered
        Log.i(TAG, "onRegistrationRefused:instance=" + instance);
    }

    @Override
    public void onUnregistered(@Nullable Context context, @NotNull String instance)
    {
        // called when this application is unregistered from receiving push messages
        Log.i(TAG, "onUnregistered:instance=" + instance);
    }

    @Override
    public void onMessage(@Nullable Context context, @NotNull String message, @NotNull String instance)
    {
        // Called when a new message is received. The String contains the full POST body of the push message
        new Thread(() -> {
            if (context != null)
            {
                Log.i(TAG, "onMessage:instance=" + instance + " message=" + message);
                try
                {
                    if (getSettings(context).getBoolean("show_notifications", false))
                    {
                        sendNotification(context, message);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    // wake up trifa here ------------------
                    final Intent intent = new Intent();
                    intent.setAction("com.zoffcc.applications.trifa.EXTERN_RECV");
                    intent.putExtra("task", "wakeup");
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    intent.setComponent(new ComponentName("com.zoffcc.applications.trifa",
                                                          "com.zoffcc.applications.trifa.MyExternReceiver"));
                    context.sendBroadcast(intent);
                    // wake up trifa here ------------------
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void sendNotification(Context context, String messageBody)
    {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                                                                PendingIntent.FLAG_ONE_SHOT);

        String channelId = context.getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,
                                                                                        channelId).setSmallIcon(
                R.drawable.ic_stat_ic_notification).setContentTitle(
                context.getString(R.string.fcm_message)).setContentText(messageBody).setAutoCancel(true).setSound(
                defaultSoundUri).setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(channelId, "Channel human readable title",
                                                                  NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}

