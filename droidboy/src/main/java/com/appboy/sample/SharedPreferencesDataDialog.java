package com.appboy.sample;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.appboy.sample.util.SpinnerUtils;
import com.appboy.support.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SharedPreferencesDataDialog extends DialogPreference implements AdapterView.OnItemSelectedListener {
  private Context mContext;
  private List mOptionList;
  private View mMainView;

  public SharedPreferencesDataDialog(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
    setDialogLayoutResource(R.layout.data_dialog);
    setPersistent(false);
    mOptionList = Arrays.asList((new File(mContext.getApplicationInfo().dataDir, "shared_prefs")).list());
  }

  @Override
  public View onCreateDialogView() {
    mMainView = super.onCreateDialogView();
    SpinnerUtils.setUpSpinnerWithList((Spinner) mMainView.findViewById(R.id.file_chooser_spinner), this, mOptionList);
    return mMainView;
  }

  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    String sharedPreferencesFileName = (String) mOptionList.get(position);
    List<String> sharedPreferencesData = new ArrayList<String>();
    SharedPreferences sharedPreferences = mContext.getApplicationContext().getSharedPreferences(sharedPreferencesFileName.replace(".xml", ""), Context.MODE_PRIVATE);
    Map<String,?> keys = sharedPreferences.getAll();
    for (Map.Entry<String,?> entry : keys.entrySet()) {
      sharedPreferencesData.add(entry.getKey() + ": " + entry.getValue().toString());
    }
    ((TextView) mMainView.findViewById(R.id.data_dialog_text_view)).setText(StringUtils.join(sharedPreferencesData, "\n"));
  }

  public void onNothingSelected(AdapterView<?> parent) {
    // Do nothing
  }


  @Override
  protected void onBindDialogView(View view) {
    super.onBindDialogView(view);
  }
}