package com.appboy.sample.subscriptions;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.braze.enums.NotificationSubscriptionType;
import com.braze.Braze;
import com.braze.support.BrazeLogger;

public class PushSubscriptionStateDialog extends SubscriptionStateDialogBase {
  private static final String TAG = BrazeLogger.getBrazeLogTag(PushSubscriptionStateDialog.class);

  @Override
  public void onExitButtonPressed(boolean positiveResult) {
    if (positiveResult) {
      int subscriptionStateId = mSubscriptionState.getCheckedRadioButtonId();
      View subscriptionStateRadioButton = mSubscriptionState.findViewById(subscriptionStateId);
      int subscriptionState = mSubscriptionState.indexOfChild(subscriptionStateRadioButton);
      switch (subscriptionState) {
        case SUBSCRIBED_INDEX:
          Braze.getInstance(getContext()).getCurrentUser().setPushNotificationSubscriptionType(NotificationSubscriptionType.SUBSCRIBED);
          Toast.makeText(getContext(), "Set push subscription state to subscribed.", Toast.LENGTH_SHORT).show();
          break;
        case OPTED_IN_INDEX:
          Braze.getInstance(getContext()).getCurrentUser().setPushNotificationSubscriptionType(NotificationSubscriptionType.OPTED_IN);
          Toast.makeText(getContext(), "Set push subscription state to opted-in.", Toast.LENGTH_SHORT).show();
          break;
        case UNSUBSCRIBED_INDEX:
          Braze.getInstance(getContext()).getCurrentUser().setPushNotificationSubscriptionType(NotificationSubscriptionType.UNSUBSCRIBED);
          Toast.makeText(getContext(), "Set push subscription state to unsubscribed.", Toast.LENGTH_SHORT).show();
          break;
        default:
          Log.w(TAG, "Error parsing subscription state: " + subscriptionState);
          Toast.makeText(getContext(), "Error parsing subscription state: " + subscriptionState, Toast.LENGTH_SHORT).show();
      }
    }
    this.dismiss();
  }
}
