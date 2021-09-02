package com.appboy.ui.actions;

import android.os.Bundle;

import com.appboy.enums.Channel;

/**
 * @deprecated Please use {@link com.braze.ui.actions.NewsfeedAction} instead. Deprecated since 7/27/21
 */
@Deprecated
public class NewsfeedAction extends com.braze.ui.actions.NewsfeedAction {
  public NewsfeedAction(Bundle extras, Channel channel) {
    super(extras, channel);
  }
}
