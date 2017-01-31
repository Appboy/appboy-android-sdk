package com.appboy.sample;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.appboy.Constants;
import com.appboy.sample.util.SpinnerUtils;
import com.appboy.support.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileDataDialog extends DialogPreference implements AdapterView.OnItemSelectedListener {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, FileDataDialog.class.getName());
  private static final String FILES_DIR = "Files Dir";
  private static final String CACHE_FILES_DIR = "Cache Files Dir";
  private List mOptionList;
  private View mMainView;

  public FileDataDialog(Context context, AttributeSet attrs) {
    super(context, attrs);
    setDialogLayoutResource(R.layout.data_dialog);
    setPersistent(false);
  }

  @Override
  public View onCreateDialogView() {
    mMainView = super.onCreateDialogView();
    mOptionList = Arrays.asList(FILES_DIR, CACHE_FILES_DIR);
    SpinnerUtils.setUpSpinnerWithList((Spinner) mMainView.findViewById(R.id.file_chooser_spinner), this, mOptionList);
    return mMainView;
  }

  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    String selection = (String) mOptionList.get(position);
    List<String> filesData = new ArrayList<String>();
    if (FILES_DIR.equals(selection)) {
      List<String> getFilesDirFiles = new ArrayList<String>();
      getContentsOfDirectory(getContext().getFilesDir(), getFilesDirFiles);
      filesData.add("getFilesDir(): " + getFilesDirFiles.size() + " items");
      filesData.addAll(getFilesDirFiles);
      ((TextView) mMainView.findViewById(R.id.data_dialog_text_view)).setText(StringUtils.join(filesData, "\n"));
    } else if (CACHE_FILES_DIR.equals(selection)) {
      List<String> getCacheDirFiles = new ArrayList<String>();
      getContentsOfDirectory(getContext().getCacheDir(), getCacheDirFiles);
      filesData.add("getCacheDir(): " + getCacheDirFiles.size() + " items");
      filesData.addAll(getCacheDirFiles);
      ((TextView) mMainView.findViewById(R.id.data_dialog_text_view)).setText(StringUtils.join(filesData, "\n"));
    }
  }

  public void onNothingSelected(AdapterView<?> parent) {
    // Do nothing
  }

  @Override
  protected void onBindDialogView(View view) {
    super.onBindDialogView(view);
  }

  public static void getContentsOfDirectory(File fileOrDirectory, List<String> inputArray) {
    if (fileOrDirectory == null || !fileOrDirectory.exists()) {
      return;
    }

    if (fileOrDirectory.isFile() && !fileOrDirectory.getName().endsWith(".cnt")) {
      try {
        inputArray.add(fileOrDirectory.getCanonicalPath());
      } catch (IOException e) {
        Log.w(TAG, "Experienced IOException while retrieving contents of directory: " + fileOrDirectory.getPath(), e);
      }
      return;
    }

    // If the file is a directory, then recursively add all of its children
    if (fileOrDirectory.isDirectory()) {
      for (String childFileName : fileOrDirectory.list()) {
        getContentsOfDirectory(new File(fileOrDirectory, childFileName), inputArray);
      }
    }
  }
}