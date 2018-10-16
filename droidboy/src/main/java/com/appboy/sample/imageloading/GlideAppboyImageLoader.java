package com.appboy.sample.imageloading;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.appboy.IAppboyImageLoader;
import com.appboy.enums.AppboyViewBounds;
import com.appboy.support.AppboyLogger;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class GlideAppboyImageLoader implements IAppboyImageLoader {
  private static final String TAG = GlideAppboyImageLoader.class.getName();

  private RequestOptions mRequestOptions = new RequestOptions();

  @Override
  public void renderUrlIntoView(Context context, String imageUrl, ImageView imageView, AppboyViewBounds viewBounds) {
    Glide.with(context)
        .load(imageUrl)
        .apply(mRequestOptions)
        .into(imageView);
  }

  @Override
  public Bitmap getBitmapFromUrl(Context context, String imageUrl, AppboyViewBounds viewBounds) {
    try {
      return Glide.with(context)
          .asBitmap()
          .apply(mRequestOptions)
          .load(imageUrl).submit().get();
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Failed to retrieve bitmap at url: " + imageUrl, e);
    }
    return null;
  }

  @Override
  public void setOffline(boolean isOffline) {
    // If the loader is offline, then we should only be retrieving from the cache
    mRequestOptions = mRequestOptions.onlyRetrieveFromCache(isOffline);
  }
}
