package com.appboy.googletagmanager;

import android.app.Application;
import android.content.Context;

import com.appboy.Appboy;
import com.appboy.AppboyUser;
import com.appboy.models.outgoing.AppboyProperties;
import com.appboy.support.AppboyLogger;
import com.google.android.gms.tagmanager.CustomTagProvider;

import java.util.Date;
import java.util.Map;

public class BrazeGtmTagProvider implements CustomTagProvider {
  private static final String TAG = AppboyLogger.getAppboyLogTag(BrazeGtmTagProvider.class);
  private static final String ACTION_TYPE_KEY = "actionType";

  // Custom Events
  private static final String LOG_EVENT_ACTION_TYPE = "logEvent";
  private static final String EVENT_NAME_VARIABLE = "eventName";

  // Custom Attributes
  private static final String CUSTOM_ATTRIBUTE_ACTION_TYPE = "customAttribute";
  private static final String CUSTOM_ATTRIBUTE_KEY = "customAttributeKey";
  private static final String CUSTOM_ATTRIBUTE_VALUE_KEY = "customAttributeValue";

  // Change User
  private static final String CHANGE_USER_ACTION_TYPE = "changeUser";
  private static final String CHANGE_USER_ID_VARIABLE = "externalUserId";

  private static Context sApplicationContext;

  /**
   * Must be set before calling any of the below methods to
   * ensure that the proper application context is available when needed.
   *
   * Recommended to be called in your {@link Application#onCreate()}.
   */
  public static void setApplicationContext(Context applicationContext) {
    if (applicationContext != null) {
      sApplicationContext = applicationContext.getApplicationContext();
    }
  }

  @Override
  public void execute(Map<String, Object> map) {
    AppboyLogger.i(TAG, "Got Google Tag Manager parameters map: " + map);

    if (sApplicationContext == null) {
      AppboyLogger.w(TAG, "No application context provided to this tag provider.");
      return;
    }

    if (!map.containsKey(ACTION_TYPE_KEY)) {
      AppboyLogger.w(TAG, "Map does not contain the Braze action type key: " + ACTION_TYPE_KEY);
      return;
    }
    String actionType = String.valueOf(map.remove(ACTION_TYPE_KEY));

    switch (actionType) {
      case LOG_EVENT_ACTION_TYPE:
        logEvent(map);
        break;
      case CUSTOM_ATTRIBUTE_ACTION_TYPE:
        setCustomAttribute(map);
        break;
      case CHANGE_USER_ACTION_TYPE:
        changeUser(map);
        break;
      default:
        AppboyLogger.w(TAG, "Got unknown action type: " + actionType);
        break;
    }
  }

  private void logEvent(Map<String, Object> tagParameterMap) {
    String eventName = String.valueOf(tagParameterMap.remove(EVENT_NAME_VARIABLE));
    Appboy.getInstance(sApplicationContext).logCustomEvent(eventName, parseMapIntoProperties(tagParameterMap));
  }

  private AppboyProperties parseMapIntoProperties(Map<String, Object> map) {
    AppboyProperties appboyProperties = new AppboyProperties();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      final Object value = entry.getValue();
      final String key = entry.getKey();
      if (value instanceof Boolean) {
        appboyProperties.addProperty(key, (Boolean) value);
      } else if (value instanceof Integer) {
        appboyProperties.addProperty(key, (Integer) value);
      } else if (value instanceof Date) {
        appboyProperties.addProperty(key, (Date) value);
      } else if (value instanceof Long) {
        appboyProperties.addProperty(key, (Long) value);
      } else if (value instanceof String) {
        appboyProperties.addProperty(key, (String) value);
      } else if (value instanceof Double) {
        appboyProperties.addProperty(key, (Double) value);
      } else {
        AppboyLogger.w(TAG, "Failed to parse value into an AppboyProperties "
            + "accepted type. Key: '" + key + "' Value: '" + value + "'");
      }
    }

    return appboyProperties;
  }

  private void setCustomAttribute(Map<String, Object> tagParameterMap) {
    AppboyUser appboyUser = Appboy.getInstance(sApplicationContext).getCurrentUser();
    if (appboyUser == null) {
      AppboyLogger.w(TAG, "AppboyUser was null. Returning.");
      return;
    }
    String key = String.valueOf(tagParameterMap.get(CUSTOM_ATTRIBUTE_KEY));
    Object value = tagParameterMap.get(CUSTOM_ATTRIBUTE_VALUE_KEY);

    if (value instanceof Boolean) {
      appboyUser.setCustomUserAttribute(key, (Boolean) value);
    } else if (value instanceof Integer) {
      appboyUser.setCustomUserAttribute(key, (Integer) value);
    } else if (value instanceof Long) {
      appboyUser.setCustomUserAttribute(key, (Long) value);
    } else if (value instanceof String) {
      appboyUser.setCustomUserAttribute(key, (String) value);
    } else if (value instanceof Double) {
      appboyUser.setCustomUserAttribute(key, (Double) value);
    } else if (value instanceof Float) {
      appboyUser.setCustomUserAttribute(key, (Float) value);
    } else {
      AppboyLogger.w(TAG, "Failed to parse value into a custom "
          + "attribute accepted type. Key: '" + key + "' Value: '" + value + "'");
    }
  }

  private void changeUser(Map<String, Object> tagParameterMap) {
    String userId = String.valueOf(tagParameterMap.get(CHANGE_USER_ID_VARIABLE));
    Appboy.getInstance(sApplicationContext).changeUser(userId);
  }
}
