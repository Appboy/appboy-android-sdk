package com.appboy.sample.util;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SpinnerUtils {
  public static final String NOT_SET = "not set";

  public static void setUpSpinner(Spinner spinner, OnItemSelectedListener listener, int arrayId) {
    ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(spinner.getContext(), arrayId, android.R.layout.simple_spinner_item);
    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(arrayAdapter);
    spinner.setOnItemSelectedListener(listener);
  }

  public static boolean SpinnerItemNotSet(String spinnerItem) {
    return (spinnerItem == null || spinnerItem.equalsIgnoreCase(NOT_SET));
  }

  public static String handleSpinnerItemSelected(AdapterView<?> parent, int arrayId) {
    String spinnerItem = parent.getResources().getStringArray(arrayId)[parent.getSelectedItemPosition()];
    if (spinnerItem != null && spinnerItem.length() > 0) {
      return spinnerItem;
    } else {
      return SpinnerUtils.NOT_SET;
    }
  }
}
