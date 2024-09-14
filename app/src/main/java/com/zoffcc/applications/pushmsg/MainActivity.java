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

package com.zoffcc.applications.pushmsg;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.zoffcc.applications.pushmsg.databinding.ActivityMainBinding;

import org.unifiedpush.android.connector.UnifiedPush;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class MainActivity extends AppCompatActivity
{

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding = null;
    static TextView DistributorsTextViewFCM = null;
    private static TextView DistributorsTextView = null;

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

        DistributorsTextViewFCM = findViewById(R.id.DistributorsTextViewFcm);
        DistributorsTextView = findViewById(R.id.DistributorsTextView);

        DistributorsTextViewFCM.setText("none");
        DistributorsTextView.setText("none");

        try
        {
            // HINT: try to detect if FCM is available on this device
            FirebaseApp.getInstance();
            DistributorsTextViewFCM.setText("FCM");
        }
        catch (Exception e)
        {
            DistributorsTextViewFCM.setText("none");
        }

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
                if (settings.getBoolean("prefer_fcm", true))
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
            }
        });

        if (!settings.getBoolean("prefer_fcm", true))
        {
            registerAppWithDialog(this, "com.zoffcc.applications.pushmsg");
        }

        Log.i(TAG, "MainActivity.onCreate end");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (!settings.getBoolean("prefer_fcm", true))
        {
            binding.logTokenButton.setVisibility(View.GONE);
        }
        else
        {
            binding.logTokenButton.setVisibility(View.VISIBLE);
        }
    }

    private static void registerAppWithDialog(Context context, String slug)
    {

        List<String> distributors = UnifiedPush.getDistributors(context, new ArrayList<>());
        if (distributors.size() == 1 || !UnifiedPush.getDistributor(context).isEmpty())
        {
            try
            {
                String available_dist = "";
                for (int i = 0; i < distributors.size(); i++)
                {
                    available_dist = available_dist + distributors.get(i) + "\n";
                }
                DistributorsTextView.setText(available_dist);
            }
            catch (Exception ignored)
            {
            }

            if (distributors.size() == 1)
            {
                UnifiedPush.saveDistributor(context, distributors.get(0));
            }
            else
            {
                UnifiedPush.saveDistributor(context, UnifiedPush.getDistributor(context));
            }
            UnifiedPush.registerApp(context, slug, new ArrayList<>(), "");
            return;
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        if (distributors.size() == 0)
        {
            Toast.makeText(context, "No UnifiedPush Distributors found", Toast.LENGTH_SHORT).show();
        }
        else
        {
            try
            {
                String available_dist = "";
                for (int i = 0; i < distributors.size(); i++)
                {
                    available_dist = available_dist + distributors.get(i) + "\n";
                }
                DistributorsTextView.setText(available_dist);
            }
            catch (Exception ignored)
            {
            }

            alert.setTitle("select_distributors");
            String[] distributorsStr = distributors.toArray(new String[0]);
            alert.setSingleChoiceItems(distributorsStr, -1, (dialog, item) -> {
                String distributor = distributorsStr[item];
                UnifiedPush.saveDistributor(context, distributor);
                UnifiedPush.registerApp(context, slug, new ArrayList<>(), "");
                dialog.dismiss();
            });
        }
        alert.show();
    }
}
