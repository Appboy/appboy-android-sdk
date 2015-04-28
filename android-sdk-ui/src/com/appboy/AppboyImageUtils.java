package com.appboy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

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
    if (imageUrl == null || imageUrl.length() == 0) {
      Log.i(TAG, "Null or empty Url string passed to image bitmap download. Not attempting download.");
      return null;
    }
    Bitmap bitmap = null;
    try {
      InputStream inputStream = new java.net.URL(imageUrl).openStream();
      bitmap = BitmapFactory.decodeStream(inputStream);
      if (inputStream != null) {
        inputStream.close();
      }
    } catch (OutOfMemoryError e) {
      Log.e(TAG, String.format("Out of Memory Error in image bitmap download for Url: %s.", imageUrl), e);
    } catch (UnknownHostException e) {
      Log.e(TAG, String.format("Unknown Host Exception in image bitmap download for Url: %s. Device may be offline.", imageUrl), e);
    } catch (MalformedURLException e) {
      Log.e(TAG, String.format("Malformed URL Exception in image bitmap download for Url: %s. Image Url may be corrupted.", imageUrl), e);
    } catch (Exception e) {
      Log.e(TAG, String.format("Exception in image bitmap download for Url: %s", imageUrl), e);
    }
    return bitmap;
  }

  public static int getPixelsFromDensityAndDp(int dpi, int dp) {
    return (dpi * dp)/BASELINE_SCREEN_DPI;
  }
}
