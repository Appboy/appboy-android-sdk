package com.braze.ui.inappmessage;

import android.app.Activity;
import android.view.View;

import com.braze.models.inappmessage.IInAppMessage;

public interface IInAppMessageViewWrapper {

  /**
   * Opens an {@link IInAppMessage} on the {@link Activity}. As a
   * result of this call, it is expected that an {@link IInAppMessage}
   * is visible and interactable by the user.
   *
   * Note that this method is expected to be called
   * on the main UI thread and should run synchronously.
   */
  void open(Activity activity);

  /**
   * Closes an {@link IInAppMessage}. As a
   * result of this call, it is expected that an {@link IInAppMessage}
   * is no longer visible and not interactable by the user.
   *
   * Note that this method is expected to be called
   * on the main UI thread and should run synchronously.
   */
  void close();

  /**
   * @return The {@link View} representing the {@link IInAppMessage}
   * that is visible to the user.
   */
  View getInAppMessageView();

  /**
   * @return The {@link IInAppMessage} being wrapped.
   */
  IInAppMessage getInAppMessage();

  /**
   * @return Whether the {@link IInAppMessage} view is
   * currently in the process of its close animation.
   */
  boolean getIsAnimatingClose();
}
