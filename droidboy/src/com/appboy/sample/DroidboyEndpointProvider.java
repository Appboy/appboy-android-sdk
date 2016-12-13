package com.appboy.sample;

import android.net.Uri;

import com.appboy.IAppboyEndpointProvider;
import com.appboy.support.StringUtils;

import java.util.HashMap;

/**
 * An AppboyEndpointProvider that sets an override endpoint if given
 */
public class DroidboyEndpointProvider implements IAppboyEndpointProvider {
  public static final String ENDPOINT_REGEX = "https.*\\.com";
  private String mEndpoint = null;
  private boolean mEndpointSet = false;
  private HashMap<Uri, Uri> endpointCache;

  public DroidboyEndpointProvider(String endpoint) {
    if (!StringUtils.isNullOrBlank(endpoint)) {
      mEndpoint = endpoint;
      mEndpointSet = true;
      endpointCache = new HashMap<>();
    }
  }

  public Uri getApiEndpoint(Uri appboyEndpoint) {
    return getEndpoint(appboyEndpoint);
  }

  public Uri getResourceEndpoint(Uri appboyEndpoint) {
    return getEndpoint(appboyEndpoint);
  }

  private Uri getEndpoint(Uri appboyEndpoint) {
    if (mEndpointSet) {
      if (endpointCache.containsKey(appboyEndpoint)) {
        return endpointCache.get(appboyEndpoint);
      }

      Uri endpoint = Uri.parse(appboyEndpoint.toString().replaceAll(ENDPOINT_REGEX, mEndpoint));
      endpointCache.put(appboyEndpoint, endpoint);
      return endpoint;
    }
    return appboyEndpoint;
  }
}
