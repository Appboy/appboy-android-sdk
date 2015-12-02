package com.appboy.wear.enums;

import com.appboy.wear.models.IPutIntoJson;

public enum WearScreenShape implements IPutIntoJson<String> {
  ROUND("round"),
  SQUARE("square");

  private final String mScreenShape;

  WearScreenShape(String shape) {
    mScreenShape = shape;
  }

  @Override
  public String forJsonPut() {
    return mScreenShape;
  }
}
