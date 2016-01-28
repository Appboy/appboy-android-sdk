package com.appboy.ui.inappmessage;

import android.view.View;

import com.appboy.ui.inappmessage.views.AppboyInAppMessageBaseView;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageFullView;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageImmersiveBaseView;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageModalView;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageSlideupView;

/**
 * InAppMessageBase is the base view interface for all in-app messages.
 *
 * It implicitly defines policy for in-app message views that can be
 * manipulated from outside the object.
 *
 * All Known Implementing Classes:
 * {@link AppboyInAppMessageBaseView}
 * {@link AppboyInAppMessageSlideupView}
 * {@link AppboyInAppMessageImmersiveBaseView}
 * {@link AppboyInAppMessageModalView}
 * {@link AppboyInAppMessageFullView}
 */
public interface IInAppMessageView {

  /**
   * Gets the clickable portion of the in-app message so that Appboy can add click listeners to it.
   * @return the View that displays the clickable portion of the in-app message.
   * If the entire message is clickable, return this.
   */
  View getMessageClickableView();
}
