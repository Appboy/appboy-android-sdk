package com.iheartradio.appboy;

public class AppboyDependencies {
  public static AppboyIhr ihr = new AppboyIhr() {
    @Override
    public boolean isOptedOut() {
      return false;
    }
  };

  public static AppboyIhrListener listener = new AppboyIhrListener() {
    @Override
    public void onPushSquelched() {
    }
  };
}
