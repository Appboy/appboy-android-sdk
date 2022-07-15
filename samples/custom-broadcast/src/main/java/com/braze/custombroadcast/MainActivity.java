package com.braze.custombroadcast;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.braze.Braze;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    final Context applicationContext = getApplicationContext();
    final EditText userIdInput = findViewById(R.id.editTextUserId);
    Button submitUserId = findViewById(R.id.buttonChangeUser);

    submitUserId.setOnClickListener(view -> {
      String userId = userIdInput.getText().toString();
      if (userId == null || userId.length() == 0) {
        showMessage("User Id should not be null or empty. Doing nothing.");
        return;
      } else {
        showMessage(String.format("Changed user to %s and requested flush to Braze", userId));
        Braze.getInstance(applicationContext).changeUser(userId);
      }

      Braze.getInstance(applicationContext).requestImmediateDataFlush();
    });
  }

  private void showMessage(String message) {
    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
  }
}
