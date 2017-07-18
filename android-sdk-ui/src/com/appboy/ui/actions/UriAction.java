package com.appboy.ui.actions;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.appboy.Constants;
import com.appboy.enums.Channel;
import com.appboy.support.AppboyFileUtils;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.AppboyWebViewActivity;
import com.appboy.ui.support.UriUtils;

import java.util.List;

public class UriAction implements IAction {
  private static final String TAG = AppboyLogger.getAppboyLogTag(UriAction.class);

  private final Bundle mExtras;
  private final Channel mChannel;
  private Uri mUri;
  private boolean mUseWebView;

  /**
   * @param uri The Uri.
   * @param extras Any extras to be passed in the start intent.
   * @param useWebView If this Uri should use the Webview, if the Uri is a remote Uri
   * @param channel The channel for the Uri. Must not be null.
   */
  UriAction(@NonNull Uri uri, Bundle extras, boolean useWebView, @NonNull Channel channel) {
    mUri = uri;
    mExtras = extras;
    mUseWebView = useWebView;
    mChannel = channel;
  }

  @Override
  public Channel getChannel() {
    return mChannel;
  }

  /**
   * Opens the action's Uri properly based on mUseWebView status and channel.
   */
  @Override
  public void execute(Context context) {
    if (AppboyFileUtils.isLocalUri(mUri)) {
      AppboyLogger.d(TAG, "Not executing local Uri: " + mUri);
      return;
    }
    AppboyLogger.d(TAG, "Executing Uri action from channel " + mChannel + ": " + mUri + ". UseWebView: " + mUseWebView);
    if (mUseWebView) {
      openUriWithWebView(context, mUri, mExtras);
    } else {
      if (mChannel.equals(Channel.PUSH)) {
        openUriWithActionViewFromPush(context, mUri, mExtras);
      } else {
        openUriWithActionView(context, mUri, mExtras);
      }
    }
  }

  public void setUri(@NonNull Uri uri) {
    mUri = uri;
  }

  public void setUseWebView(boolean openInWebView) {
    mUseWebView = openInWebView;
  }

  @NonNull
  public Uri getUri() {
    return mUri;
  }

  public boolean getUseWebView() {
    return mUseWebView;
  }

  public Bundle getExtras() {
    return mExtras;
  }

  static void openUriWithWebView(Context context, Uri uri, Bundle extras) {
    // If the scheme is not a remote scheme, we open it using an ACTION_VIEW intent.
    if (!AppboyFileUtils.REMOTE_SCHEMES.contains(uri.getScheme())) {
      Log.d(TAG, "Opening non-remote scheme uri with action_view intent.");
      openUriWithActionView(context, uri, extras);
      return;
    }
    try {
      Intent intent = new Intent(context, AppboyWebViewActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      if (extras != null) {
        intent.putExtras(extras);
      }
      intent.putExtra(Constants.APPBOY_WEBVIEW_URL_EXTRA, uri.toString());
      context.startActivity(intent);
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Appboy AppboyWebViewActivity not opened successfully.", e);
    }
  }

  /**
   * Uses an Intent.ACTION_VIEW intent to open the Uri.
   */
  private static void openUriWithActionView(Context context, Uri uri, Bundle extras) {
    Intent intent = getActionViewIntent(context, uri, extras);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    if (intent.resolveActivity(context.getPackageManager()) != null) {
      context.startActivity(intent);
    } else {
      Log.w(TAG, String.format("Could not find appropriate activity to open for deep link %s.", uri));
    }
  }

  /**
   * Uses an Intent.ACTION_VIEW intent to open the Uri and places the main activity of the
   * activity on the back stack. Primarily used to open Uris from push.
   */
  private static void openUriWithActionViewFromPush(Context context, Uri uri, Bundle extras) {
    Intent uriIntent = getActionViewIntent(context, uri, extras);
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    stackBuilder.addNextIntent(UriUtils.getMainActivityIntent(context, extras));
    stackBuilder.addNextIntent(uriIntent);
    try {
      stackBuilder.startActivities(extras);
    } catch (ActivityNotFoundException e) {
      Log.w(TAG, String.format("Could not find appropriate activity to open for deep link %s.", uri));
    }
  }

  private static Intent getActionViewIntent(Context context, Uri uri, Bundle extras) {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setData(uri);

    if (extras != null) {
      intent.putExtras(extras);
    }

    // If the current app can already handle the intent, default to using it
    List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, 0);
    if (resolveInfos.size() > 1) {
      for (ResolveInfo resolveInfo : resolveInfos) {
        if (resolveInfo.activityInfo.packageName.equals(context.getPackageName())) {
          Log.d(TAG, String.format("Setting deep link activity to %s.", resolveInfo.activityInfo.packageName));
          intent.setPackage(resolveInfo.activityInfo.packageName);
          break;
        }
      }
    }

    return intent;
  }
}
