package com.appboy.ui.inappmessage;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;

import com.appboy.models.IInAppMessage;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.inappmessage.factories.AppboyFullViewFactory;
import com.appboy.ui.inappmessage.factories.AppboyHtmlFullViewFactory;
import com.appboy.ui.inappmessage.factories.AppboyHtmlViewFactory;
import com.appboy.ui.inappmessage.factories.AppboyInAppMessageAnimationFactory;
import com.appboy.ui.inappmessage.factories.AppboyInAppMessageViewWrapperFactory;
import com.appboy.ui.inappmessage.factories.AppboyModalViewFactory;
import com.appboy.ui.inappmessage.factories.AppboySlideupViewFactory;
import com.appboy.ui.inappmessage.listeners.AppboyDefaultHtmlInAppMessageActionListener;
import com.appboy.ui.inappmessage.listeners.AppboyDefaultInAppMessageManagerListener;
import com.appboy.ui.inappmessage.listeners.AppboyInAppMessageWebViewClientListener;
import com.appboy.ui.inappmessage.listeners.IHtmlInAppMessageActionListener;
import com.appboy.ui.inappmessage.listeners.IInAppMessageManagerListener;
import com.appboy.ui.inappmessage.listeners.IInAppMessageWebViewClientListener;

public class AppboyInAppMessageManagerBase {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyInAppMessageManagerBase.class);

  private boolean mClickOutsideModalDismissesInAppMessageView = false;
  private boolean mBackButtonDismissesInAppMessageView = true;
  @Nullable
  Activity mActivity;
  @Nullable
  Context mApplicationContext;

  // view listeners
  private final IInAppMessageWebViewClientListener mInAppMessageWebViewClientListener = new AppboyInAppMessageWebViewClientListener();

  // html action listeners
  private final IHtmlInAppMessageActionListener mDefaultHtmlInAppMessageActionListener = new AppboyDefaultHtmlInAppMessageActionListener();

  // factories
  private final IInAppMessageViewFactory mInAppMessageSlideupViewFactory = new AppboySlideupViewFactory();
  private final IInAppMessageViewFactory mInAppMessageModalViewFactory = new AppboyModalViewFactory();
  private final IInAppMessageViewFactory mInAppMessageFullViewFactory = new AppboyFullViewFactory();
  private final IInAppMessageViewFactory mInAppMessageHtmlFullViewFactory = new AppboyHtmlFullViewFactory(mInAppMessageWebViewClientListener);
  private final IInAppMessageViewFactory mInAppMessageHtmlViewFactory = new AppboyHtmlViewFactory(mInAppMessageWebViewClientListener);

  // animation factory
  private final IInAppMessageAnimationFactory mInAppMessageAnimationFactory = new AppboyInAppMessageAnimationFactory();

  // manager listeners
  private final IInAppMessageManagerListener mDefaultInAppMessageManagerListener = new AppboyDefaultInAppMessageManagerListener();

  // view wrapper factory
  private final IInAppMessageViewWrapperFactory mDefaultInAppMessageViewWrapperFactory = new AppboyInAppMessageViewWrapperFactory();

  // custom listeners
  @Nullable
  private IInAppMessageViewFactory mCustomInAppMessageViewFactory;
  @Nullable
  private IInAppMessageAnimationFactory mCustomInAppMessageAnimationFactory;
  @Nullable
  private IInAppMessageManagerListener mCustomInAppMessageManagerListener;
  @Nullable
  private IInAppMessageViewWrapperFactory mCustomInAppMessageViewWrapperFactory;
  @Nullable
  private IHtmlInAppMessageActionListener mCustomHtmlInAppMessageActionListener;
  /**
   * A custom listener to be fired for control in-app messages.
   * <p/>
   * see {@link IInAppMessage#isControl()}
   */
  @Nullable
  private IInAppMessageManagerListener mCustomControlInAppMessageManagerListener;

  public IInAppMessageManagerListener getInAppMessageManagerListener() {
    return mCustomInAppMessageManagerListener != null ? mCustomInAppMessageManagerListener : mDefaultInAppMessageManagerListener;
  }

  /**
   * A {@link IInAppMessageManagerListener} to be used only for control in-app messages.
   * <p/>
   * see {@link IInAppMessage#isControl()}
   */
  public IInAppMessageManagerListener getControlInAppMessageManagerListener() {
    return mCustomControlInAppMessageManagerListener != null ? mCustomControlInAppMessageManagerListener : mDefaultInAppMessageManagerListener;
  }

  public IHtmlInAppMessageActionListener getHtmlInAppMessageActionListener() {
    return mCustomHtmlInAppMessageActionListener != null ? mCustomHtmlInAppMessageActionListener : mDefaultHtmlInAppMessageActionListener;
  }

  /**
   * Gets the default {@link IInAppMessageViewFactory} as returned by the {@link AppboyInAppMessageManager}
   * for the given {@link IInAppMessage}.
   *
   * @return The {@link IInAppMessageViewFactory} or null if the message type does not have a {@link IInAppMessageViewFactory}.
   */
  public IInAppMessageViewFactory getDefaultInAppMessageViewFactory(IInAppMessage inAppMessage) {
    switch (inAppMessage.getMessageType()) {
      case SLIDEUP:
        return mInAppMessageSlideupViewFactory;
      case MODAL:
        return mInAppMessageModalViewFactory;
      case FULL:
        return mInAppMessageFullViewFactory;
      case HTML_FULL:
        return mInAppMessageHtmlFullViewFactory;
      case HTML:
        return mInAppMessageHtmlViewFactory;
      default:
        AppboyLogger.w(TAG, "Failed to find view factory for in-app message with type: " + inAppMessage.getMessageType());
        return null;
    }
  }

  public IInAppMessageViewWrapperFactory getInAppMessageViewWrapperFactory() {
    return mCustomInAppMessageViewWrapperFactory != null ? mCustomInAppMessageViewWrapperFactory : mDefaultInAppMessageViewWrapperFactory;
  }

  public boolean getDoesBackButtonDismissInAppMessageView() {
    return mBackButtonDismissesInAppMessageView;
  }

  public boolean getDoesClickOutsideModalViewDismissInAppMessageView() {
    return mClickOutsideModalDismissesInAppMessageView;
  }

  public IInAppMessageAnimationFactory getInAppMessageAnimationFactory() {
    return mCustomInAppMessageAnimationFactory != null ? mCustomInAppMessageAnimationFactory : mInAppMessageAnimationFactory;
  }

  public IInAppMessageViewFactory getInAppMessageViewFactory(IInAppMessage inAppMessage) {
    if (mCustomInAppMessageViewFactory != null) {
      return mCustomInAppMessageViewFactory;
    } else {
      return getDefaultInAppMessageViewFactory(inAppMessage);
    }
  }

  @Nullable
  public Activity getActivity() {
    return mActivity;
  }

  @Nullable
  public Context getApplicationContext() {
    return mApplicationContext;
  }

  /**
   * Sets whether the hardware back button dismisses in-app messages. Defaults to true.
   * Note that the hardware back button default behavior will be used instead (i.e. the host {@link Activity}'s
   * {@link Activity#onKeyDown(int, KeyEvent)} method will be called).
   */
  public void setBackButtonDismissesInAppMessageView(boolean backButtonDismissesInAppMessageView) {
    AppboyLogger.d(TAG, "In-App Message back button dismissal set to " + backButtonDismissesInAppMessageView);
    mBackButtonDismissesInAppMessageView = backButtonDismissesInAppMessageView;
  }

  /**
   * Sets whether the tapping outside the modal in-app message content dismiss the
   * message. Defaults to false.
   */
  public void setClickOutsideModalViewDismissInAppMessageView(boolean doesDismiss) {
    AppboyLogger.d(TAG, "Modal In-App Message outside tap dismissal set to " + doesDismiss);
    mClickOutsideModalDismissesInAppMessageView = doesDismiss;
  }

  /**
   * Assigns a custom {@link IInAppMessageManagerListener} that will be used when displaying in-app messages. To revert
   * back to the default {@link IInAppMessageManagerListener}, call this method with null.
   * <p/>
   * see {@link IInAppMessage#isControl()}
   *
   * @param inAppMessageManagerListener A custom {@link IInAppMessageManagerListener} or null (to revert back to the
   *                                    default {@link IInAppMessageManagerListener}).
   */
  public void setCustomInAppMessageManagerListener(IInAppMessageManagerListener inAppMessageManagerListener) {
    AppboyLogger.d(TAG, "Custom InAppMessageManagerListener set");
    mCustomInAppMessageManagerListener = inAppMessageManagerListener;
  }

  /**
   * Assigns a custom {@link IInAppMessageManagerListener} that will be used when displaying control in-app messages. To revert
   * back to the default {@link IInAppMessageManagerListener}, call this method with null.
   *
   * @param inAppMessageManagerListener A custom {@link IInAppMessageManagerListener} for control in-app messages or null (to revert back to the
   *                                    default {@link IInAppMessageManagerListener}).
   */
  public void setCustomControlInAppMessageManagerListener(IInAppMessageManagerListener inAppMessageManagerListener) {
    AppboyLogger.d(TAG, "Custom ControlInAppMessageManagerListener set. This listener will only be used for control in-app messages.");
    mCustomControlInAppMessageManagerListener = inAppMessageManagerListener;
  }

  /**
   * Assigns a custom IHtmlInAppMessageActionListener that will be used during the display of Html in-app messages.
   *
   * @param htmlInAppMessageActionListener A custom IHtmlInAppMessageActionListener or null (to revert back to the
   *                                       default IHtmlInAppMessageActionListener).
   */
  public void setCustomHtmlInAppMessageActionListener(IHtmlInAppMessageActionListener htmlInAppMessageActionListener) {
    AppboyLogger.d(TAG, "Custom htmlInAppMessageActionListener set");
    mCustomHtmlInAppMessageActionListener = htmlInAppMessageActionListener;
  }

  /**
   * Assigns a custom IInAppMessageAnimationFactory that will be used to animate the in-app message View. To revert
   * back to the default IInAppMessageAnimationFactory, call the setCustomInAppMessageAnimationFactory method with null.
   *
   * @param inAppMessageAnimationFactory A custom IInAppMessageAnimationFactory or null (to revert back to the default
   *                                     IInAppMessageAnimationFactory).
   */
  public void setCustomInAppMessageAnimationFactory(IInAppMessageAnimationFactory inAppMessageAnimationFactory) {
    AppboyLogger.d(TAG, "Custom InAppMessageAnimationFactory set");
    mCustomInAppMessageAnimationFactory = inAppMessageAnimationFactory;
  }

  /**
   * Assigns a custom IInAppMessageViewFactory that will be used to create the in-app message View. To revert
   * back to the default IInAppMessageViewFactory, call the setCustomInAppMessageViewFactory method with null.
   *
   * @param inAppMessageViewFactory A custom IInAppMessageViewFactory or null (to revert back to the default
   *                                IInAppMessageViewFactory).
   */
  public void setCustomInAppMessageViewFactory(IInAppMessageViewFactory inAppMessageViewFactory) {
    AppboyLogger.d(TAG, "Custom InAppMessageViewFactory set");
    mCustomInAppMessageViewFactory = inAppMessageViewFactory;
  }

  /**
   * Sets a custom {@link IInAppMessageViewWrapperFactory} that will be used to
   * display an {@link IInAppMessage} to the user.
   */
  public void setCustomInAppMessageViewWrapperFactory(@Nullable IInAppMessageViewWrapperFactory inAppMessageViewWrapperFactory) {
    AppboyLogger.d(TAG, "Custom IInAppMessageViewWrapperFactory set");
    mCustomInAppMessageViewWrapperFactory = inAppMessageViewWrapperFactory;
  }
}
