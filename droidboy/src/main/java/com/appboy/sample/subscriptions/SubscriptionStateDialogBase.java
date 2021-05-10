package com.appboy.sample.subscriptions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appboy.sample.R;
import com.appboy.sample.dialog.CustomDialogBase;

public abstract class SubscriptionStateDialogBase extends CustomDialogBase {
  protected static final int SUBSCRIBED_INDEX = 0;
  protected static final int OPTED_IN_INDEX = 1;
  protected static final int UNSUBSCRIBED_INDEX = 2;

  protected RadioGroup mSubscriptionState;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.subscription_state_preferences, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mSubscriptionState = view.findViewById(R.id.subscription_state);
  }
}
