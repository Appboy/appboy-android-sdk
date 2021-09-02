package com.braze.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.appboy.Constants;
import com.appboy.models.push.BrazeNotificationPayload;
import com.braze.Braze;
import com.braze.IBrazeNotificationFactory;
import com.braze.configuration.BrazeConfigurationProvider;
import com.braze.support.BrazeLogger;

public class BrazeAmazonDeviceMessagingReceiver extends BrazePushReceiver {}
