package com.example.hireready;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        context.stopService(new Intent(context, MyService.class));
    }
}
