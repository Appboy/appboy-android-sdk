package com.appboy.ui.actions;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.appboy.Constants;

/**
 * Action that launches a new Activity.
 */
public final class ActivityAction implements IAction {
  private final Intent mIntent;

  public ActivityAction(Intent intent) {
    mIntent = intent;
  }

  public ActivityAction(Context ctx, Uri uri) {
    mIntent.setClassName(ctx, uri.getHost());
    setQueryParameters(uri);
  }
    mIntent = intent;
  }

  @Override
  public void execute(Context context) {
    context.startActivity(mIntent);
  }

  /** Mostly copy paste from SDK 11 **/
  private void setQueryParameters(Uri uri) {
    if (uri.isOpaque()) {
      throw new UnsupportedOperationException("This isn't a hierarchical URI.");
    }

    String query = uri.getEncodedQuery();
    if (query == null) {
      return;
    }

    int start = 0;
    do {
      int next = query.indexOf('&', start);
      int end = (next == -1) ? query.length() : next;

      int separator = query.indexOf('=', start);
      if (separator <= end && separator >= 0) {
        String key = query.substring(start, separator);
        String value = query.substring(separator + 1, end);
        mIntent.putExtra(Uri.decode(key), Uri.decode(value));
      }
      // Move start to end of name.
      start = end + 1;
    } while (start < query.length());
  }
}
