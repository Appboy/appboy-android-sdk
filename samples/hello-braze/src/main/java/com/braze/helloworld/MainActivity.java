package com.braze.helloworld;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.appboy.helloworld.R;
import com.braze.Braze;
import com.braze.support.StringUtils;

public class MainActivity extends Activity {
  private EditText mNickname;
  private EditText mHighScore;
  private EditText mUserId;
  private Context mApplicationContext;

  // These events will be shown in the Braze dashboard.
  private static final String CUSTOM_CLICK_EVENT = "clicked submit";
  private static final String HIGH_SCORE_ATTRIBUTE_KEY = "user high score";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);

    // It is good practice to always get an instance of the Braze singleton using the application
    // context.
    mApplicationContext = this.getApplicationContext();

    mNickname = findViewById(R.id.hello_high_score_nickname);
    mHighScore = findViewById(R.id.hello_high_score);
    mUserId = findViewById(R.id.hello_user_id);
    Button submit = findViewById(R.id.hello_submit);

    submit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Validate the nickname and high score, then send off to the server.
        String nickname = mNickname.getEditableText().toString();
        String highScore = mHighScore.getEditableText().toString();
        String userId = mUserId.getEditableText().toString();

        if (!StringUtils.isNullOrBlank(nickname) && !StringUtils.isNullOrBlank(highScore)) {
          // Assign the current user an userId. You can search for this user using this external user id on the
          // dashboard
          Braze.getInstance(mApplicationContext).changeUser(userId);

          // Send the custom event for the click
          Braze.getInstance(mApplicationContext).logCustomEvent(CUSTOM_CLICK_EVENT);

          // Log the custom attribute of "nickname : highScore"
          String attributeString = String.format("%s : %s", nickname, highScore);
          Braze.getInstance(mApplicationContext).getCurrentUser()
              .setCustomUserAttribute(HIGH_SCORE_ATTRIBUTE_KEY, attributeString);
          displayToast("Sent off button click event and updated high score attribute for user " + userId);
        } else {
          displayToast("All fields must be filled to submit.");
        }
      }
    });
  }

  // Displays a long toast to the user.
  private void displayToast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }
}
