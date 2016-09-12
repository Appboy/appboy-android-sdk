package com.appboy.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.appboy.wear.AppboyWearableAdapter;
import com.appboy.wear.enums.WearScreenShape;
import com.appboy.wear.models.AppboyProperties;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class WearboyActivity extends Activity implements View.OnClickListener {
  private static final String TAG = WearboyActivity.class.getName();
  private AppboyWearableAdapter mAppboyAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_wearboy);
    mAppboyAdapter = AppboyWearableAdapter.getInstance(this);

    // Custom attribute button
    (findViewById(R.id.wearboy_increment_custom_attribute_button)).setOnClickListener(this);
    // Custom event button
    (findViewById(R.id.wearboy_log_custom_event_button)).setOnClickListener(this);
    // Log purchase button
    (findViewById(R.id.wearboy_log_purchase_button)).setOnClickListener(this);
    // Submit feedback button
    (findViewById(R.id.wearboy_submit_feedback_button)).setOnClickListener(this);
    // Round screen button
    (findViewById(R.id.wearboy_set_screen_type_round_button)).setOnClickListener(this);
    // Square screen button
    (findViewById(R.id.wearboy_set_screen_type_square_button)).setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.wearboy_increment_custom_attribute_button:
        mAppboyAdapter.incrementCustomUserAttribute("times pressed button");
        showToast("Incremented attribute");
        break;

      case R.id.wearboy_log_custom_event_button:
        try {
          JSONObject object = new JSONObject().put("hello", "world");
          mAppboyAdapter.logCustomEvent("pressed button", new AppboyProperties(object));
          showToast("Logged custom event");
        } catch (JSONException e) {
          Log.e(TAG, "Caught JSON exception logging custom event.", e);
        }
        break;

      case R.id.wearboy_log_purchase_button:
        mAppboyAdapter.logPurchase("product id here", "USD", new BigDecimal("3.50"));
        showToast("Logged purchase");
        break;

      case R.id.wearboy_submit_feedback_button:
        mAppboyAdapter.submitFeedback("wearboy@appboy.com", "Appboy supports Android Wear!", false);
        showToast("Submitted feedback");
        break;

      case R.id.wearboy_set_screen_type_round_button:
        mAppboyAdapter.logWearScreenShape(WearScreenShape.ROUND);
        showToast("Logged screen as round");
        break;

      case R.id.wearboy_set_screen_type_square_button:
        mAppboyAdapter.logWearScreenShape(WearScreenShape.SQUARE);
        showToast("Logged screen as square");
        break;

      default:
        break;
    }
  }

  private void showToast(String messageToToast) {
    Toast.makeText(this, messageToToast, Toast.LENGTH_SHORT).show();
  }
}
