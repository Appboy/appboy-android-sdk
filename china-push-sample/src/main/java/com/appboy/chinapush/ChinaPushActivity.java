package com.appboy.chinapush;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.appboy.Appboy;
import com.appboy.push.AppboyNotificationUtils;
import com.appboy.support.AppboyLogger;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

public class ChinaPushActivity extends Activity {
  public static final String TAG = ChinaPushActivity.class.getSimpleName();
  public static final String API_KEY = "api_key";

  private Context mApplicationContext;
  private boolean mStartedBaidu = false;
  private String mBaiduApiKey;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.china_push);
    AppboyLogger.setLogLevel(Log.VERBOSE);

    // It is good practice to always get an instance of the Appboy singleton using the application
    // context.
    mApplicationContext = this.getApplicationContext();

    mBaiduApiKey = readChinaPushApiKey();
    if (!mStartedBaidu && mBaiduApiKey != null) {
      PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY, mBaiduApiKey);
      mStartedBaidu = true;
    }

    TextView messageLog = (TextView) findViewById(R.id.com_appboy_china_push_message_log);
    messageLog.setMovementMethod(new ScrollingMovementMethod());

    final EditText userIdInput = (EditText) findViewById(R.id.com_appboy_china_push_edit_text_user_id);
    Button submitUserId = (Button) findViewById(R.id.com_appboy_china_push_button_change_user);

    submitUserId.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        String userId = userIdInput.getText().toString();
        if (userId == null || userId.length() == 0) {
          showMessage("User Id should not be null or empty. Doing nothing.");
          return;
        } else {
          showMessage(String.format("Changed user to %s and requested flush to Appboy", userId));
          Appboy.getInstance(mApplicationContext).changeUser(userId);
        }

        Appboy.getInstance(mApplicationContext).requestImmediateDataFlush();
      }
    });
  }

  @Override
  protected void onNewIntent(Intent intent) {
    if (intent.getStringExtra(ChinaPushMessageReceiver.LOG_MESSAGE_KEY) != null) {
      updateMessageLog(intent.getStringExtra(ChinaPushMessageReceiver.LOG_MESSAGE_KEY));
    }
    if (intent.getStringExtra(ChinaPushMessageReceiver.NOTIFICATION_CLICKED_KEY) != null) {
      String customContentString = intent.getStringExtra(ChinaPushMessageReceiver.NOTIFICATION_CLICKED_KEY);
      AppboyNotificationUtils.logBaiduNotificationClick(mApplicationContext, customContentString);
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    // When opening and closing a session, use the current activity
    Appboy.getInstance(mApplicationContext).openSession(this);
  }

  @Override
  protected void onStop() {
    super.onStop();
    Appboy.getInstance(mApplicationContext).closeSession(this);
  }

  /**
   * Convenience method to obtain the Baidu API key defined in AndroidManifest.xml.
   *
   * @return Baidu API key.
   */
  private String readChinaPushApiKey() {
    try {
      return getPackageManager().getApplicationInfo(getPackageName(),
          PackageManager.GET_META_DATA).metaData.getString(API_KEY);
    } catch (NameNotFoundException e) {
      Log.w(TAG, "Could not retrieve China push API key from AndroidManifest.xml. Cannot receive push messages!", e);
      return null;
    }
  }

  /**
   * Updates the Baidu event message log which is displayed to users.
   *
   * @param logMessage the new message.
   */
  private void updateMessageLog(String logMessage) {
    TextView messageLog = (TextView) findViewById(R.id.com_appboy_china_push_message_log);
    messageLog.setText(logMessage);
  }

  private void showMessage(String message) {
    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
  }
}
