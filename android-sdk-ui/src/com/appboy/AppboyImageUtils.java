package com.appboy;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;

import com.appboy.support.AppboyLogger;
import com.appboy.support.PermissionUtils;

import java.io.File;
import java.io.FileOutputStream;
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
      AppboyLogger.i(TAG, "Null or empty Url string passed to image bitmap download. Not attempting download.");
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
      AppboyLogger.e(TAG, String.format("Out of Memory Error in image bitmap download for Url: %s.", imageUrl), e);
    } catch (UnknownHostException e) {
      AppboyLogger.e(TAG, String.format("Unknown Host Exception in image bitmap download for Url: %s. Device may be offline.", imageUrl), e);
    } catch (MalformedURLException e) {
      AppboyLogger.e(TAG, String.format("Malformed URL Exception in image bitmap download for Url: %s. Image Url may be corrupted.", imageUrl), e);
    } catch (Exception e) {
      AppboyLogger.e(TAG, String.format("Exception in image bitmap download for Url: %s", imageUrl), e);
    }
    return bitmap;
  }

  public static int getPixelsFromDensityAndDp(int dpi, int dp) {
    return (dpi * dp) / BASELINE_SCREEN_DPI;
  }

  /**
   * Store the given bitmap image locally as a png file in the specified directory.
   *
   * @param imageBitmap image bitmap
   * @param imageFilenameBase desired image filename, without file extension
   * @param folderName image folder name
   * @return the image Uri, or null if saving locally failed.
   */
  public static Uri storeBitmapLocally(Context context, Bitmap imageBitmap, String imageFilenameBase, String folderName) {
    if (context == null) {
      AppboyLogger.w(TAG, "Received null context. Doing nothing.");
      return null;
    }
    if (imageBitmap == null) {
      AppboyLogger.w(TAG, "Received null bitmap. Doing nothing.");
      return null;
    }
    if (imageFilenameBase == null) {
      AppboyLogger.w(TAG, "Received null image filename base. Doing nothing.");
      return null;
    }
    if (folderName == null) {
      AppboyLogger.w(TAG, "Received null image folder name. Doing nothing.");
      return null;
    }
    try {
      String imageStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + folderName;
      File imageStorageDirectory = new File(imageStoragePath);
      if (!imageStorageDirectory.exists()) {
        imageStorageDirectory.mkdirs();
      }
      File imageFile = new File(imageStorageDirectory, imageFilenameBase + ".png");

      AppboyLogger.d(TAG, "Storing image locally at " + imageFile.getAbsolutePath());
      FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
      imageBitmap.compress(Bitmap.CompressFormat.PNG, 0, fileOutputStream);
      fileOutputStream.flush();
      fileOutputStream.close();

      // Scan the new file so that it appears in the User's image gallery right away.
      MediaScannerConnection.scanFile(context, new String[]{imageFile.getAbsolutePath()}, null, null);

      return Uri.fromFile(imageFile);
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Exception occurred when attempting to store image locally.", e);
      return null;
    }
  }

  /**
   * Checks whether writing to external storage is allowed.  Writing to external storage is necessary to store an image locally.
   *
   * Client apps must add <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" /> to their AndroidManifest.xml.
   *
   * @param context
   * @return whether writing to external storage is allowed.  Writing to external storage is necessary to store an image locally
   */
  public static boolean isWriteExternalPermissionGranted(Context context) {
    return context != null
        && PermissionUtils.hasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
  }
}
