package com.appboy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.InputStream;

public class AppboyImageUtils {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyImageUtils.class.getName());
  // See http://developer.android.com/guide/practices/screens_support.html under Density-independent pixel
  public static final int BASELINE_SCREEN_DPI = 160;

  /**
   * Downloads an image and returns a bitmap object. The image should be less than 450dp for the
   * push notification. An aspect ratio of 2:1 is recommended. This should always be run in a background
   * thread.
   *
   * According to http://developer.android.com/guide/appendix/media-formats.html, the supported file
   * types are jpg and png.
   *
   * @param imageUrl The url where the image is found
   * @return An image in Bitmap form. If the image cannot be downloaded, or cannot be decoded into
   * a bitmap, then null is returned.
   */
  public static Bitmap downloadImageBitmap(String imageUrl) {
    Bitmap bitmap = null;
    try {
      InputStream in = new java.net.URL(imageUrl).openStream();
      bitmap = BitmapFactory.decodeStream(in);
    } catch (OutOfMemoryError e) {
      Log.e(TAG, "Out of Memory Error in image bitmap download", e);
    } catch (Exception e) {
      Log.e(TAG, "General exception in image bitmap download", e);
    }
    return bitmap;
  }

  public static int getPixelsFromDensityAndDp(int dpi, int dp) {
    return (dpi * dp)/BASELINE_SCREEN_DPI;
  }
}
