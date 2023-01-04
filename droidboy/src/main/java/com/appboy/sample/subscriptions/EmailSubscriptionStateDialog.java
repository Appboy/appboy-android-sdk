package com.appboy.sample.subscriptions;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.braze.enums.NotificationSubscriptionType;
import com.braze.Braze;
import com.braze.support.BrazeLogger;

public class EmailSubscriptionStateDialog extends SubscriptionStateDialogBase {
  private static final String TAG = BrazeLogger.getBrazeLogTag(EmailSubscriptionStateDialog.class);

  @Override
  public void onExitButtonPressed(boolean positiveResult) {
    if (positiveResult) {
      int subscriptionStateId = mSubscriptionState.getCheckedRadioButtonId();
      View subscriptionStateRadioButton = mSubscriptionState.findViewById(subscriptionStateId);
      int subscriptionState = mSubscriptionState.indexOfChild(subscriptionStateRadioButton);
      switch (subscriptionState) {
        case SUBSCRIBED_INDEX:
          Braze.getInstance(getContext()).getCurrentUser().setEmailNotificationSubscriptionType(NotificationSubscriptionType.SUBSCRIBED);
          Toast.makeText(getContext(), "Set email subscription state to subscribed.", Toast.LENGTH_SHORT).show();
          break;
        case OPTED_IN_INDEX:
          Braze.getInstance(getContext()).getCurrentUser().setEmailNotificationSubscriptionType(NotificationSubscriptionType.OPTED_IN);
          Toast.makeText(getContext(), "Set email subscription state to opted-in.", Toast.LENGTH_SHORT).show();
          break;
        case UNSUBSCRIBED_INDEX:
          Braze.getInstance(getContext()).getCurrentUser().setEmailNotificationSubscriptionType(NotificationSubscriptionType.UNSUBSCRIBED);
          Toast.makeText(getContext(), "Set email subscription state to unsubscribed.", Toast.LENGTH_SHORT).show();
          break;
        default:
          Log.w(TAG, "Error parsing subscription state: " + subscriptionState);
          Toast.makeText(getContext(), "Error parsing subscription state: " + subscriptionState, Toast.LENGTH_SHORT).show();
      }
    }
    this.dismiss();
  }
}
