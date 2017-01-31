package com.appboy.sample.util;

public class EmulatorDetectionUtils {
  public static final String AVD_X86_MODEL = "Android SDK built for x86";
  public static final String AVD_X86_64_MODEL = "Android SDK built for x86_64";

  public static String[] getEmulatorModelsForAppboyDeactivation() {
    return new String[]{AVD_X86_MODEL, AVD_X86_64_MODEL};
  }
}
