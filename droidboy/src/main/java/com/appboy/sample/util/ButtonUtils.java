package com.appboy.sample.util;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ButtonUtils {

  public static void setUpPopulateButton(View view, int buttonId, final EditText output, final String storedString) {
    setUpPopulateButton(view, buttonId, output, storedString, null, null);
  }

  public static Button setUpPopulateButton(View view, int buttonId, final EditText keyOutput, final String keyStoredString, final EditText valueOutput, final String valueStoredString) {
    final Button populateButton = view.findViewById(buttonId);
    populateButton.setOnClickListener(view1 -> {
      handlePopulateButtonClick(keyOutput, keyStoredString);
      if (valueOutput != null && valueStoredString != null) {
        handlePopulateButtonClick(valueOutput, valueStoredString);
      }
    });
    return populateButton;
  }

  public static void handlePopulateButtonClick(EditText buttonEditText, String defaultText) {
    if (buttonEditText.getText().length() == 0) {
      buttonEditText.setText(defaultText);
    } else {
      buttonEditText.getText().clear();
    }
  }
}
