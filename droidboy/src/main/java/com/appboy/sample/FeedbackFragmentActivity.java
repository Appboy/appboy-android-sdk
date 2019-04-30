package com.appboy.sample;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;

import com.appboy.ui.AppboyFeedbackFragment;

/**
 * @deprecated The feedback feature is disabled for new accounts, and will be removed in a future SDK release.
 */
@Deprecated()
public class FeedbackFragmentActivity extends AppboyFragmentActivity {

  private CheckBox mUseListenerCheckbox;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.com_appboy_feedback_activity);

    Toolbar toolbar = findViewById(R.id.com_appboy_feedback_toolbar);

    toolbar.setNavigationIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_back_button_droidboy, null));
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onBackPressed();
      }
    });

    mUseListenerCheckbox = findViewById(R.id.com_appboy_feedback_use_listener);
    final FragmentManager fragmentManager = getSupportFragmentManager();
    final AppboyFeedbackFragment appboyFeedbackFragment = (AppboyFeedbackFragment) fragmentManager.findFragmentById(R.id.com_appboy_feedback);

    final AppboyFeedbackFragment.IFeedbackFinishedListener droidboyFeedbackFinishedListener = new AppboyFeedbackFragment.IFeedbackFinishedListener() {
      @Override
      public void onFeedbackFinished(AppboyFeedbackFragment.FeedbackResult disposition) {
        finish();
      }

      @Override
      public String beforeFeedbackSubmitted(String message) {
        return message + " -from Droidboy";
      }
    };

    appboyFeedbackFragment.setFeedbackFinishedListener(droidboyFeedbackFinishedListener);
    View.OnClickListener checkboxListener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mUseListenerCheckbox.isChecked()) {
          appboyFeedbackFragment.setFeedbackFinishedListener(droidboyFeedbackFinishedListener);
        } else {
          appboyFeedbackFragment.setFeedbackFinishedListener(null);
        }
      }
    };
    mUseListenerCheckbox.setOnClickListener(checkboxListener);
  }
}
