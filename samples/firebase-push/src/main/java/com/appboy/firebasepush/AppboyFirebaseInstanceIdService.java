package com.appboy.firebasepush;

import android.util.Log;

import com.appboy.Appboy;
import com.appboy.Constants;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class AppboyFirebaseInstanceIdService extends FirebaseInstanceIdService {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyFirebaseInstanceIdService.class.getName());

  @Override
  public void onTokenRefresh() {
    try {
      String firebaseSenderId = getString(R.string.sender_id);
      String token = FirebaseInstanceId.getInstance().getToken(firebaseSenderId, getString(R.string.firebase_scope));
      Log.i(TAG, "================");
      Log.i(TAG, "================");
      Log.i(TAG, "Registering firebase token with Appboy: " + token);
      Log.i(TAG, "================");
      Log.i(TAG, "================");
      Appboy.getInstance(getApplicationContext()).registerAppboyPushMessages(token);
    } catch (Exception e) {
      Log.e(TAG, "Exception while automatically registering Firebase token with Appboy.", e);
    }
  }
}
