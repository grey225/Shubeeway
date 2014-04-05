package com.jerrymannel.smsgateway;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// Class which we launch the app with, after boot of the device
public class BootBroadcastReceiver extends BroadcastReceiver {

	static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
            Intent sayHelloIntent = new Intent(context, MySMSGatewayMainActivity.class);
            sayHelloIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(sayHelloIntent);
        }
    }

}
