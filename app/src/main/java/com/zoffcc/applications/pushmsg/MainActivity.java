/**
 * [TRIfA], FCM part of Tox Reference Implementation for Android
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

/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zoffcc.applications.pushmsg;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.zoffcc.applications.pushmsg.databinding.ActivityMainBinding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class MainActivity extends AppCompatActivity
{

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "MainActivity.onCreate start");
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String PREF_night_mode = settings.getString("theme", "0");
        int night_mode = MODE_NIGHT_FOLLOW_SYSTEM;
        if (PREF_night_mode.equals("1"))
        {
            night_mode = MODE_NIGHT_YES;
        }
        else if (PREF_night_mode.equals("2"))
        {
            night_mode = MODE_NIGHT_NO;
        }

        AppCompatDelegate.setDefaultNightMode(night_mode);
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            // Create channel to show notifications.
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(
                    new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW));
        }

        binding.startSetting.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent myIntent = new Intent(v.getContext(), SettingsActivity.class);
                startActivity(myIntent);
            }
        });


        binding.logTokenButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Get token
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>()
                {
                    @Override
                    public void onComplete(@NonNull Task<String> task)
                    {
                        if (!task.isSuccessful())
                        {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        try
                        {
                            // wake up trifa here ------------------
                            final Intent intent = new Intent();
                            intent.setAction("com.zoffcc.applications.trifa.TOKEN_CHANGED");
                            intent.putExtra("token", task.getResult());
                            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            intent.setComponent(new ComponentName("com.zoffcc.applications.trifa",
                                                                  "com.zoffcc.applications.trifa.MyTokenReceiver"));
                            MainActivity.this.sendBroadcast(intent);
                            // wake up trifa here ------------------

                            String msg = MainActivity.this.getString(R.string.msg_token_fmt, task.getResult());
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        Log.i(TAG, "MainActivity.onCreate end");
    }
}
