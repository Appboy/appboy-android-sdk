package com.appboy.firebasepush;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.appboy.Appboy;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final Context applicationContext = getApplicationContext();
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
          showMessage(String.format("Changed user to %s and requested flush to Appboy", userId));
          Appboy.getInstance(applicationContext).changeUser(userId);
        }

        Appboy.getInstance(applicationContext).requestImmediateDataFlush();
      }
    });
  }

  private void showMessage(String message) {
    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
  }
}
