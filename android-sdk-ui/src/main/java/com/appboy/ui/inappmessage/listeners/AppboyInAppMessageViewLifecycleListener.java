package com.appboy.ui.inappmessage.listeners;

import android.app.Activity;
import android.net.Uri;
import android.view.View;

import com.appboy.enums.Channel;
import com.appboy.enums.inappmessage.ClickAction;
import com.appboy.models.IInAppMessage;
import com.appboy.models.IInAppMessageHtml;
import com.appboy.models.IInAppMessageImmersive;
import com.appboy.models.MessageButton;
import com.appboy.support.AppboyFileUtils;
import com.appboy.support.AppboyLogger;
import com.appboy.support.BundleUtils;
import com.appboy.support.WebContentUtils;
import com.appboy.ui.AppboyNavigator;
import com.appboy.ui.actions.ActionFactory;
import com.appboy.ui.actions.NewsfeedAction;
import com.appboy.ui.actions.UriAction;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;
import com.appboy.ui.inappmessage.InAppMessageCloser;

import java.io.File;

public class AppboyInAppMessageViewLifecycleListener implements IInAppMessageViewLifecycleListener {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyInAppMessageViewLifecycleListener.class);

  @Override
  public void beforeOpened(View inAppMessageView, IInAppMessage inAppMessage) {
    // Note that the client method must be called before any default processing below
    getInAppMessageManager().getInAppMessageManagerListener().beforeInAppMessageViewOpened(inAppMessageView, inAppMessage);
    AppboyLogger.d(TAG, "IInAppMessageViewLifecycleListener.beforeOpened called.");
    inAppMessage.logImpression();
  }

  @Override
  public void afterOpened(View inAppMessageView, IInAppMessage inAppMessage) {
    AppboyLogger.d(TAG, "IInAppMessageViewLifecycleListener.afterOpened called.");

    // Note that the client method must be called after any default processing above
    getInAppMessageManager().getInAppMessageManagerListener().afterInAppMessageViewOpened(inAppMessageView, inAppMessage);
  }

  @Override
  public void beforeClosed(View inAppMessageView, IInAppMessage inAppMessage) {
    // Note that the client method must be called before any default processing below
    getInAppMessageManager().getInAppMessageManagerListener().beforeInAppMessageViewClosed(inAppMessageView, inAppMessage);
    AppboyLogger.d(TAG, "IInAppMessageViewLifecycleListener.beforeClosed called.");
  }

  @Override
  public void afterClosed(IInAppMessage inAppMessage) {
    AppboyLogger.d(TAG, "IInAppMessageViewLifecycleListener.afterClosed called.");
    getInAppMessageManager().resetAfterInAppMessageClose();
    if (inAppMessage instanceof IInAppMessageHtml) {
      startClearHtmlInAppMessageAssetsThread();
    }
    inAppMessage.onAfterClosed();

    // Note that the client method must be called after any default processing above
    getInAppMessageManager().getInAppMessageManagerListener().afterInAppMessageViewClosed(inAppMessage);
  }

  @Override
  public void onClicked(InAppMessageCloser inAppMessageCloser, View inAppMessageView, IInAppMessage inAppMessage) {
    AppboyLogger.d(TAG, "IInAppMessageViewLifecycleListener.onClicked called.");
    inAppMessage.logClick();

    // Perform the in-app message clicked listener action from the host application first. This give
    // the app the option to override the values that are sent from the server and handle the
    // in-app message differently depending on where the user is in the app.
    //
    // To modify the default in-app message clicked behavior, mutate the necessary in-app message members. As
    // an example, if the in-app message were to navigate to the news feed when it was clicked, the
    // behavior can be cancelled by setting the click action to NONE.
    boolean handled = getInAppMessageManager().getInAppMessageManagerListener().onInAppMessageClicked(inAppMessage, inAppMessageCloser);

    if (!handled) {
      // Perform the default (or modified) in-app message clicked behavior.
      performInAppMessageClicked(inAppMessage, inAppMessageCloser);
    }
  }

  @Override
  public void onButtonClicked(InAppMessageCloser inAppMessageCloser, MessageButton messageButton, IInAppMessageImmersive inAppMessageImmersive) {
    AppboyLogger.d(TAG, "IInAppMessageViewLifecycleListener.onButtonClicked called.");
    inAppMessageImmersive.logButtonClick(messageButton);

    boolean handled = getInAppMessageManager().getInAppMessageManagerListener().onInAppMessageButtonClicked(inAppMessageImmersive, messageButton, inAppMessageCloser);

    if (!handled) {
      // Perform the default (or modified) in-app message button clicked behavior.
      performInAppMessageButtonClicked(messageButton, inAppMessageImmersive, inAppMessageCloser);
    }
  }

  @Override
  public void onDismissed(View inAppMessageView, IInAppMessage inAppMessage) {
    AppboyLogger.d(TAG, "IInAppMessageViewLifecycleListener.onDismissed called.");
    getInAppMessageManager().getInAppMessageManagerListener().onInAppMessageDismissed(inAppMessage);
  }

  private void performInAppMessageButtonClicked(MessageButton messageButton, IInAppMessage inAppMessage, InAppMessageCloser inAppMessageCloser) {
    performClickAction(messageButton.getClickAction(), inAppMessage, inAppMessageCloser, messageButton.getUri(), messageButton.getOpenUriInWebview());
  }

  private void performInAppMessageClicked(IInAppMessage inAppMessage, InAppMessageCloser inAppMessageCloser) {
    performClickAction(inAppMessage.getClickAction(), inAppMessage, inAppMessageCloser, inAppMessage.getUri(), inAppMessage.getOpenUriInWebView());
  }

  private void performClickAction(ClickAction clickAction, IInAppMessage inAppMessage, InAppMessageCloser inAppMessageCloser, Uri clickUri, boolean openUriInWebview) {
    if (getInAppMessageManager().getActivity() == null) {
      AppboyLogger.w(TAG, "Can't perform click action because the cached activity is null.");
      return;
    }
    switch (clickAction) {
      case NEWS_FEED:
        inAppMessageCloser.close(false);
        NewsfeedAction newsfeedAction = new NewsfeedAction(BundleUtils.mapToBundle(inAppMessage.getExtras()),
            Channel.INAPP_MESSAGE);
        AppboyNavigator.getAppboyNavigator().gotoNewsFeed(getInAppMessageManager().getActivity(), newsfeedAction);
        break;
      case URI:
        inAppMessageCloser.close(false);
        UriAction uriAction = ActionFactory.createUriActionFromUri(clickUri, BundleUtils.mapToBundle(inAppMessage.getExtras()),
            openUriInWebview, Channel.INAPP_MESSAGE);
        AppboyNavigator.getAppboyNavigator().gotoUri(getInAppMessageManager().getActivity(), uriAction);
        break;
      case NONE:
        inAppMessageCloser.close(inAppMessage.getAnimateOut());
        break;
      default:
        inAppMessageCloser.close(false);
        break;
    }
  }

  private AppboyInAppMessageManager getInAppMessageManager() {
    return AppboyInAppMessageManager.getInstance();
  }

  private void startClearHtmlInAppMessageAssetsThread() {
    new Thread(() -> {
      Activity inAppMessageActivity = AppboyInAppMessageManager.getInstance().getActivity();
      if (inAppMessageActivity != null) {
        File internalStorageCacheDirectory = WebContentUtils.getHtmlInAppMessageAssetCacheDirectory(inAppMessageActivity);
        AppboyFileUtils.deleteFileOrDirectory(internalStorageCacheDirectory);
      }
    }).start();
  }
}
