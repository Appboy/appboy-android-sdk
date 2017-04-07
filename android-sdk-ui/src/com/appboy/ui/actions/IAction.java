package com.appboy.ui.actions;

import android.content.Context;

import com.appboy.enums.Channel;

public interface IAction {

  void execute(Context context);

  Channel getChannel();
}
