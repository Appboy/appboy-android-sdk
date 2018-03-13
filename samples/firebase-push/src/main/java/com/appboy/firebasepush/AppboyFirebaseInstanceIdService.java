package com.appboy.firebasepush;

import android.util.Log;

import com.appboy.Appboy;
import com.appboy.support.AppboyLogger;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class AppboyFirebaseInstanceIdService extends FirebaseInstanceIdService {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyFirebaseInstanceIdService.class);

  @Override
  public void onTokenRefresh() {
    try {
      String firebaseSenderId = getString(R.string.sender_id);
      String token = FirebaseInstanceId.getInstance().getToken(firebaseSenderId, getString(R.string.firebase_scope));
      Log.i(TAG, "================");
      Log.i(TAG, "================");
      Log.i(TAG, "Registering firebase token: " + token);
      Log.i(TAG, "================");
      Log.i(TAG, "================");
      Appboy.getInstance(getApplicationContext()).registerAppboyPushMessages(token);
    } catch (Exception e) {
      Log.e(TAG, "Exception while automatically registering Firebase token with Appboy.", e);
    }
  }
}
