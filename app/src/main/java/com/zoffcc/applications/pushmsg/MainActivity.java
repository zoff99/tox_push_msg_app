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
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.zoffcc.applications.pushmsg.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "MainActivity.onCreate start");
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(
                    new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW));
        }


        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        // if (getIntent().getExtras() != null) {
        //     for (String key : getIntent().getExtras().keySet()) {
        //         Object value = getIntent().getExtras().get(key);
        //     }
        // }

        Log.i(TAG, "MainActivity.onCreate themeGroup setting default, id before = " + binding.themeGroup.getCheckedRadioButtonId());
        binding.themeGroup.check(R.id.themeSystem);
        binding.themeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            /**
             * The onCreate method is rerun every time
             * we switch the theme by calling AppCompatDelegate.setDefaultNightMode(..)
             *
             * This reruns the above call: binding.themeGroup.check(R.id.themeSystem);
             * which causes our themeGroup.onCheckedChanged to run after every change is applied
             * giving R.id.themeSystem.
             *
             * To prevent an automated back and forth switching of themes in an endless loop,
             * we remember the last applied theme and don't apply the first theme if it's
             * the default R.id.themeSystem.
             */
            private int previousTheme = -1;

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.i(TAG, "themeGroup.onCheckedChanged: " + checkedId);
                if(checkedId == R.id.themeSystem && previousTheme != -1)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                else if(checkedId == R.id.themeDark)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                else if(checkedId == R.id.themeLight)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                previousTheme = checkedId;
            }
        });

        binding.logTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get token
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        IntentSender.sendTokenIntent(task.getResult(), MainActivity.this);
                    }
                });
            }
        });

        setContentView(binding.getRoot());
        Log.i(TAG, "MainActivity.onCreate end ; currentTheme = " + binding.themeGroup.getCheckedRadioButtonId());
    }
}
