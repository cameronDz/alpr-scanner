package org.openalpr.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Edited by Cameron on 3/23/2016.
 * https://www.sinch.com/tutorials/send-push-notifications-android-messaging-app-using-gcm/
 * Used to receive GCM notifications containing messages from other users.
 */
class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName comp = new ComponentName(context.getPackageName(),
                RegistrationIntentService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}