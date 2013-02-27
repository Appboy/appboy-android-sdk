package com.appboy.ui;

import java.io.Serializable;

/**
 * Action used inject custom navigation into the user interface
 */
public interface FinishAction extends Serializable {
  public void onFinish();
}
