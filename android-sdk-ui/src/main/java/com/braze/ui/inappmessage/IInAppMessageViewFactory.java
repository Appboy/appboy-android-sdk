package com.braze.ui.inappmessage;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;

import com.braze.models.inappmessage.IInAppMessage;

public interface IInAppMessageViewFactory {

  /**
   * This method should either inflate or programmatically create a new View that will be used
   * to display an in-app message. A new View must be created on every call. This
   * prevents the memory leak that would occur if the View was shared by multiple Activity
   * classes.
   *
   * Note: Do not add click/touch listeners directly to the view. They will be ignored. Instead,
   * use an IInAppMessageManagerListener to perform custom logic.
   *
   * @return View that will be used to display the in-app message.
   */
  View createInAppMessageView(@NonNull Activity activity, @NonNull IInAppMessage inAppMessage);
}
