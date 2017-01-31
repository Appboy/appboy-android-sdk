package com.appboy.sample;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.appboy.Constants;
import com.appboy.support.AppboyLogger;

public class LogLevelDialogPreference extends DialogPreference {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, LogLevelDialogPreference.class.getName());
  private static final String WARN = "warn";
  private static final String ERROR = "error";
  private static final String VERBOSE = "verbose";
  private static final String DEBUG = "debug";
  private static final String INFO = "info";
  private static final String SUPPRESS = "suppress";
  private static final String LOG_SELECT_PREFIX = "Updated minimum log level to ";
  private static final String LOG_LEVEL_TOAST = "Appboy Logging Level set to %s.";
  private static final String[] OPTIONS = new String[]{VERBOSE, DEBUG, INFO, WARN, ERROR, SUPPRESS};
  private View mView;
  private Spinner mLogLevelSpinner;

  public LogLevelDialogPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    setDialogLayoutResource(R.layout.log_level_dialog);
    setPersistent(false);
  }

  @Override
  protected View onCreateDialogView() {
    mView = super.onCreateDialogView();
    mLogLevelSpinner = (Spinner) mView.findViewById(R.id.log_level_spinner);

    ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, OPTIONS);
    mLogLevelSpinner.setAdapter(adapter);

    int currentLogLevel = AppboyLogger.LogLevel;
    int initialSelection;

    switch (currentLogLevel) {
      case Log.VERBOSE:
        initialSelection = 0;
        break;
      case Log.DEBUG:
        initialSelection = 1;
        break;
      case Log.INFO:
        initialSelection = 2;
        break;
      case Log.WARN:
        initialSelection = 3;
        break;
      case Log.ERROR:
        initialSelection = 4;
        break;
      case AppboyLogger.SUPPRESS:
        initialSelection = 5;
        break;
      default:
        initialSelection = 0;
        break;
    }

    mLogLevelSpinner.setSelection(initialSelection);

    mLogLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (OPTIONS[position]) {
          case VERBOSE:
            if (AppboyLogger.LogLevel != Log.VERBOSE) {
              showToast(LOG_LEVEL_TOAST, OPTIONS[position]);
            }
            AppboyLogger.LogLevel = Log.VERBOSE;
            AppboyLogger.v(TAG, LOG_SELECT_PREFIX + VERBOSE);
            saveLogLevel(Log.VERBOSE);
            break;
          case DEBUG:
            if (AppboyLogger.LogLevel != Log.DEBUG) {
              showToast(LOG_LEVEL_TOAST, OPTIONS[position]);
            }
            AppboyLogger.LogLevel = Log.DEBUG;
            AppboyLogger.d(TAG, LOG_SELECT_PREFIX + DEBUG);
            saveLogLevel(Log.DEBUG);
            break;
          case INFO:
            if (AppboyLogger.LogLevel != Log.INFO) {
              showToast(LOG_LEVEL_TOAST, OPTIONS[position]);
            }
            AppboyLogger.LogLevel = Log.INFO;
            AppboyLogger.i(TAG, LOG_SELECT_PREFIX + INFO);
            saveLogLevel(Log.INFO);
            break;
          case WARN:
            if (AppboyLogger.LogLevel != Log.WARN) {
              showToast(LOG_LEVEL_TOAST, OPTIONS[position]);
            }
            AppboyLogger.LogLevel = Log.WARN;
            AppboyLogger.w(TAG, LOG_SELECT_PREFIX + WARN);
            saveLogLevel(Log.WARN);
            break;
          case ERROR:
            if (AppboyLogger.LogLevel != Log.ERROR) {
              showToast(LOG_LEVEL_TOAST, OPTIONS[position]);
            }
            AppboyLogger.LogLevel = Log.ERROR;
            AppboyLogger.e(TAG, LOG_SELECT_PREFIX + ERROR);
            saveLogLevel(Log.ERROR);
            break;
          case SUPPRESS:
            if (AppboyLogger.LogLevel != AppboyLogger.SUPPRESS) {
              showToast("Disabled Appboy Logging.", null);
            }
            AppboyLogger.LogLevel = AppboyLogger.SUPPRESS;
            saveLogLevel(AppboyLogger.SUPPRESS);
            break;
          default:
            break;
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });
    return mView;
  }

  // Displays a toast to the user
  private void showToast(String message, String inputString) {
    if (inputString != null) {
      message = String.format(message, inputString);
    }
    Toast.makeText(this.getContext(), message, Toast.LENGTH_LONG).show();
  }

  private void saveLogLevel(int logLevel) {
    SharedPreferences.Editor sharedPreferencesEditor = this.getContext().getSharedPreferences(this.getContext().getString(R.string.log_level_dialog_title), Context.MODE_PRIVATE).edit();
    sharedPreferencesEditor.putInt(this.getContext().getString(R.string.current_log_level), logLevel);
    sharedPreferencesEditor.apply();
  }
}
