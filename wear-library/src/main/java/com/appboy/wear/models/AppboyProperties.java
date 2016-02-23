package com.appboy.wear.models;

import org.json.JSONObject;

import java.util.Date;//NOPMD

/**
 * This class acts as a wrapper for your JSONObjects. See the specification for AppboyProperties in the
 * appboy-base-private library for what keys and values are allowed. The basic specification is duplicated below.
 * Validation occurs on the phone after transport.
 *
 * Defines and stores metadata about events and purchases.
 *
 * Metadata is stored as properties represented by key-value pairs.
 *
 * Property keys are non-empty strings <= 255 characters, with no leading dollar signs.
 * Property values are be integers, doubles, booleans, java.util.Date objects, or strings <= 255 characters.
 * All strings of length > 255 characters will be truncated.
 *
 * If you need to add a date to your AppboyProperties json, use {@link com.appboy.wear.support.DateTimeUtils#formatDate(Date)}.
 */
@SuppressWarnings("PMD")
public final class AppboyProperties implements IPutIntoJson<JSONObject> {
  private JSONObject mPropertiesJSONObject = new JSONObject();

  public AppboyProperties(JSONObject jsonObject) {
    mPropertiesJSONObject = jsonObject;
  }

  @Override
  public JSONObject forJsonPut() {
    return mPropertiesJSONObject;
  }
}
