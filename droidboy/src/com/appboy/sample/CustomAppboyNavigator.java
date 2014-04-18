package com.appboy.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.appboy.ui.AppboyNavigator;

public class CustomAppboyNavigator extends AppboyNavigator {
  @Override
  public void gotoNewsFeed(Context context, Bundle extras) {
    Intent intent = new Intent(context, DroidBoyActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    intent.putExtra("source", com.appboy.Constants.APPBOY);
    intent.putExtra("destination", "feed");
    context.startActivity(intent);
  }
}
