package com.appboy.ui.inappmessage;

import android.view.View;

/**
 * InAppMessageBase is the base view interface for all in-app messages.
 *
 * It implicitly defines policy for in-app message views that can be
 * manipulated from outside the object.
 *
 * Known implementing classes include
 * {@link com.appboy.ui.inappmessage.AppboyInAppMessageBaseView}
 * {@link com.appboy.ui.inappmessage.AppboyInAppMessageSlideupView}
 * {@link com.appboy.ui.inappmessage.AppboyInAppMessageImmersiveBaseView}
 * {@link com.appboy.ui.inappmessage.AppboyInAppMessageModalView}
 * {@link com.appboy.ui.inappmessage.AppboyInAppMessageFullView}
 */
public interface IInAppMessageView {

  /**
   * Gets the clickable portion of the in-app message so that Appboy can add click listeners to it.
   * @return the View that displays the clickable portion of the in-app message.
   * If the entire message is clickable, return this.
   */
  View getMessageClickableView();
}
