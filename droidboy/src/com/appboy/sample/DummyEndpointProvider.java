package com.appboy.sample;

import android.net.Uri;

import com.appboy.IAppboyEndpointProvider;

/**
 * A dummy AppboyEndpointProvider to show sample usage
 */
public class DummyEndpointProvider implements IAppboyEndpointProvider {
  public Uri getApiEndpoint(Uri appboyEndpoint) {
    return appboyEndpoint;
  }

  public Uri getResourceEndpoint(Uri appboyEndpoint) {
    return appboyEndpoint;
  }
}
