package com.appboy.sample;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import com.appboy.Appboy;
import com.appboy.Constants;

import java.util.Locale;

public class DroidboyApplication extends Application
{
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, DroidboyApplication.class.getName());

  @Override
  public void onCreate()
  {
    super.onCreate();
    activateStrictMode();
    if (Locale.getDefault().toString().equals("zh_CN")) {
      Log.i(TAG, "Matched zh_CN locale, configuring Appboy with override key");
      Appboy.configure(this, "f9622241-8e26-4366-8183-1c9e310af6b0");
    } else {
      Log.i(TAG, "Did not match zh_CN locale, configuring Appboy to clear any existing override key");
      Appboy.configure(this, null);
    }
    Appboy.setAppboyEndpointProvider(new DummyEndpointProvider());
  }

  @TargetApi(9)
  private void activateStrictMode() {
    // Set the activity to Strict mode so that we get LogCat warnings when code misbehaves on the main thread.
    if (BuildConfig.DEBUG
      && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
    }
  }
}