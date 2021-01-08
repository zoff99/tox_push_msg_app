package com.zoffcc.applications.pushmsg;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class MyWorker extends Worker
{

    private static final String TAG = "MyWorker";

    public MyWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams)
    {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork()
    {
        Log.d(TAG, "Performing long running task in scheduled job");
        // TODO: send incoming ping message to trifa (trifa will wake up and connect to the Tox Network)
        return Result.success();
    }
}
