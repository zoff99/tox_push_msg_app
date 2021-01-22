package com.zoffcc.applications.pushmsg;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class IntentSender {

    private static final String TAG = "IntentSender";

    private static void sendIntent(Context context, String action, String extraName, String extraValue, String targetClass) {
        try {
            // wake up trifa here ------------------
            final Intent intent = new Intent();
            intent.setAction(action);
            intent.putExtra(extraName, extraValue);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.setComponent(new ComponentName("com.zoffcc.applications.trifa", targetClass));
            context.sendBroadcast(intent);
            // wake up trifa here ------------------

            // Log and toast
            if(extraName == "token") {
                String msg = context.getString(R.string.msg_token_fmt, extraValue);
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                // Log.i(TAG, msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static void sendTokenIntent(String token, Context context) {
        sendIntent(context, "com.zoffcc.applications.trifa.TOKEN_CHANGED",
                "token", token,
                "com.zoffcc.applications.trifa.MyTokenReceiver");
    }

    protected static void sendWakeupIntent(Context context) {
        sendIntent(context, "com.zoffcc.applications.trifa.EXTERN_RECV",
                "task", "wakeup",
                "com.zoffcc.applications.trifa.MyExternReceiver");
    }
}
