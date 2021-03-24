package com.braze.googletagmanager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends AppCompatActivity {
  private FirebaseAnalytics mFirebaseAnalytics;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

    final EditText userIdInput = findViewById(R.id.editTextUserId);
    Button submitUserId = findViewById(R.id.buttonChangeUser);

    submitUserId.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        String userId = userIdInput.getText().toString();
        if (userId == null || userId.length() == 0) {
          showMessage("User Id should not be null or empty. Doing nothing.");
          return;
        } else {
          Bundle params = new Bundle();
          params.putString("externalUserId", userId);
          mFirebaseAnalytics.logEvent("changeUser", params);
        }
      }
    });

    findViewById(R.id.bLogEvent).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Bundle params = new Bundle();
        params.putString("eventName", "played_song_event");
        params.putInt("intParam", 1);
        params.putBoolean("boolParam", true);
        params.putString("stringParam", "vantage");
        mFirebaseAnalytics.logEvent("logEvent", params);
      }
    });

    findViewById(R.id.bLogUserProperty).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Bundle params = new Bundle();
        params.putString("customAttributeKey", "favorite song");
        params.putString("customAttributeValue", "Private Eyes");
        mFirebaseAnalytics.logEvent("customAttribute", params);
      }
    });
  }

  private void showMessage(String message) {
    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
  }
}
