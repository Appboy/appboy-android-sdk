package com.appboy.ui.configuration;

import android.content.Context;

import com.appboy.configuration.CachedConfigurationProvider;

public class XmlUIConfigurationProvider extends CachedConfigurationProvider {
  private static final String APPLICATION_ICON_KEY = "application_icon";

  private final Context mContext;

  public XmlUIConfigurationProvider(Context context) {
    super(context);
    mContext = context;
  }
}
