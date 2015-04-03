package com.appboy.ui.support;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.appboy.Constants;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

public class DownloadUtils {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, DownloadUtils.class.getName());

  /**
   * Downloads an image and returns it as a Bitmap.
   *
   * According to http://developer.android.com/guide/appendix/media-formats.html, supported file
   * types are jpg and png.
   *
   * @param imageUrl The image's Url.
   * @return A Bitmap object representing the image. If the image cannot be downloaded or decoded into
   * a bitmap, null is returned.
   */
  public static Bitmap downloadImageBitmap(String imageUrl) {
    if (StringUtils.isNullOrBlank(imageUrl)) {
      Log.i(TAG, "Null or empty Url string passed to image bitmap download. Aborting download.");
      return null;
    }
    Bitmap bitmap = null;
    try {
      InputStream in = new java.net.URL(imageUrl).openStream();
      bitmap = BitmapFactory.decodeStream(in);
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
}
