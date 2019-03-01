package com.appboy.ui.inappmessage;

import android.app.Activity;
import android.view.View;

import com.appboy.models.IInAppMessage;

public interface IInAppMessageViewFactory {

  /**
   * This method should either inflate or programmatically create a new View that will be used
   * to display an in-app message. A new View must be created on every call to createInAppMessageView. This
   * prevents the memory leak that would occur if the View was shared by multiple Activity
   * classes.
   *
   * Note: Do not add click/touch listeners directly to the view. They will be ignored. Instead,
   * use an IInAppMessageManagerListener to perform custom logic.
   *
   * @param activity
   * @param inAppMessage
   * @return View that will be used to display the in-app message.
   */
  View createInAppMessageView(Activity activity, IInAppMessage inAppMessage);
}
