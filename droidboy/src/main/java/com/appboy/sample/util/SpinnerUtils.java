package com.appboy.sample.util;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.appboy.sample.R;

import java.util.List;

public class SpinnerUtils {
  public static final String NOT_SET = "not set";

  public static void setUpSpinner(Spinner spinner, OnItemSelectedListener listener, int arrayId) {
    ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(spinner.getContext(), arrayId, R.layout.spinner_item);
    arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
    spinner.setAdapter(arrayAdapter);
    spinner.setOnItemSelectedListener(listener);
  }

  public static void setUpSpinnerWithList(Spinner spinner, OnItemSelectedListener listener, List list) {
    ArrayAdapter arrayAdapter = new ArrayAdapter(spinner.getContext(), R.layout.spinner_item, list);
    arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
    spinner.setAdapter(arrayAdapter);
    spinner.setOnItemSelectedListener(listener);
  }

  public static boolean spinnerItemNotSet(String spinnerItem) {
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
