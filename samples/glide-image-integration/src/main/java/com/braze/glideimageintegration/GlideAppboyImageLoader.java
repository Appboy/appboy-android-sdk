package com.braze.glideimageintegration;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appboy.models.cards.Card;
import com.braze.enums.BrazeViewBounds;
import com.braze.images.IBrazeImageLoader;
import com.braze.models.inappmessage.IInAppMessage;
import com.braze.support.BrazeLogger;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class GlideAppboyImageLoader implements IBrazeImageLoader {
  private static final String TAG = GlideAppboyImageLoader.class.getName();

  private RequestOptions mRequestOptions = new RequestOptions();

  @Override
  public void renderUrlIntoCardView(@NonNull Context context,
                                    @Nullable Card card,
                                    @NonNull String imageUrl,
                                    @NonNull ImageView imageView,
                                    @Nullable BrazeViewBounds viewBounds) {
    renderUrlIntoView(context, imageUrl, imageView, viewBounds);
  }

  @Override
  public void renderUrlIntoInAppMessageView(@NonNull Context context,
                                            @NonNull IInAppMessage inAppMessage,
                                            @NonNull String imageUrl,
                                            @NonNull ImageView imageView,
                                            @NonNull BrazeViewBounds viewBounds) {
    renderUrlIntoView(context, imageUrl, imageView, viewBounds);
  }

  @Override
  public Bitmap getPushBitmapFromUrl(@NonNull Context context,
                                     @Nullable Bundle extras,
                                     @NonNull String imageUrl,
                                     @Nullable BrazeViewBounds viewBounds) {
    return getBitmapFromUrl(context, imageUrl, viewBounds);
  }

  @Override
  public Bitmap getInAppMessageBitmapFromUrl(@NonNull Context context,
                                             @NonNull IInAppMessage inAppMessage,
                                             @NonNull String imageUrl,
                                             @Nullable BrazeViewBounds viewBounds) {
    return getBitmapFromUrl(context, imageUrl, viewBounds);
  }

  private void renderUrlIntoView(Context context,
                                 String imageUrl,
                                 ImageView imageView,
                                 BrazeViewBounds viewBounds) {
    Glide.with(context)
        .load(imageUrl)
        .apply(mRequestOptions)
        .into(imageView);
  }

  private Bitmap getBitmapFromUrl(Context context,
                                  String imageUrl,
                                  BrazeViewBounds viewBounds) {
    try {
      return Glide.with(context)
          .asBitmap()
          .apply(mRequestOptions)
          .load(imageUrl).submit().get();
    } catch (Exception e) {
      BrazeLogger.e(TAG, "Failed to retrieve bitmap at url: " + imageUrl, e);
    }
    return null;
  }

  @Override
  public void setOffline(boolean isOffline) {
    // If the loader is offline, then we should only be retrieving from the cache
    mRequestOptions = mRequestOptions.onlyRetrieveFromCache(isOffline);
  }
}
