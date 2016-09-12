package com.appboy.helloworld;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.appboy.Appboy;

public class HelloAppboyActivity extends Activity {
  private EditText mNickname;
  private EditText mHighScore;
  private EditText mEmail;
  private Context mApplicationContext;

  // These events will be shown in the Appboy dashboard.
  private static final String HELLO_APPBOY_CUSTOM_CLICK_EVENT = "clicked submit";
  private static final String HELLO_APPBOY_HIGH_SCORE_ATTRIBUTE_KEY = "user high score";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.hello_appboy);

    // It is good practice to always get an instance of the Appboy singleton using the application
    // context.
    mApplicationContext = this.getApplicationContext();

    mNickname = (EditText) findViewById(R.id.com_appboy_hello_high_score_nickname);
    mHighScore = (EditText) findViewById(R.id.com_appboy_hello_high_score);
    mEmail = (EditText) findViewById(R.id.com_appboy_hello_email);
    Button submit = (Button) findViewById(R.id.com_appboy_hello_submit);

    submit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Validate the nickname and high score, then send off to the server.
        String nickname = mNickname.getEditableText().toString();
        String highScore = mHighScore.getEditableText().toString();
        String email = mEmail.getEditableText().toString();

        if (validateNameAndHighScore(nickname, highScore, email)) {
          // Assign the current user an email. You can search for this user using this email on the
          // dashboard
          Appboy.getInstance(mApplicationContext).getCurrentUser().setEmail(email);

          // Send the custom event for the click
          Appboy.getInstance(mApplicationContext).logCustomEvent(HELLO_APPBOY_CUSTOM_CLICK_EVENT);

          // Log the custom attribute of "nickname : highScore"
          String attributeString = String.format("%s : %s", nickname, highScore);
          Appboy.getInstance(mApplicationContext).getCurrentUser()
              .setCustomUserAttribute(HELLO_APPBOY_HIGH_SCORE_ATTRIBUTE_KEY, attributeString);

          displayToast("Sent off event and attribute to Appboy!");
        }
      }
    });
  }

  // Displays a long toast to the user.
  private void displayToast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }

  // Returns true if the name is nonempty, the score is nonempty, and the email is valid
  private boolean validateNameAndHighScore(String name, String score, String email) {
    if (name.length() > 0 && score.length() > 0) {
      if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        return true;
      } else {
        displayToast("Email must be valid");
      }
    } else {
      displayToast("Fields cannot be empty");
    }
    return false;
  }
}
