package com.appboy.sample;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.appboy.Appboy;
import com.appboy.ui.AppboyFeedbackFragment;
import com.appboy.ui.Constants;
import com.appboy.ui.FeedbackCustomStyle;
import com.appboy.ui.FinishAction;

import java.util.ArrayList;
import java.util.List;

/*
 * Appboy integration sample
 *
 * To start tracking analytics using the Appboy Android SDK, you must make sure that you follow these steps
 * to integrate correctly.
 *
 * Step 1: In all activities, call Appboy.openSession() and Appboy.closeSession() in the activity's onStart()
 *         and onStop() respectively. In this sample, we put that code into DroidBoyActivity and PreferenceActivity.
 *
 * Step 2 (Optional): To integrate the Feedback form, first, create a custom style by using FeedbackCustomStyle.Builder.
 *                    Second, define the navigation action that should be performed after the feedback has been sent by
 *                    creating a FinishAction. Finally, either use the FeedbackHelper directly to inflate/attach/wire
 *                    the form or use the AppboyFeedFragment class which will do it all for you.
 */
public class DroidBoyActivity extends FragmentActivity {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, DroidBoyActivity.class.getName());

  private DroidBoyViewPager mViewPager;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.droid_boy);

    PageAdapter pageAdapter = new PageAdapter(getSupportFragmentManager(), initializeFragmentList());
    mViewPager = (DroidBoyViewPager) findViewById(R.id.view_pager);
    mViewPager.setPagingEnabled(false);
    mViewPager.setAdapter(pageAdapter);
    mViewPager.setCurrentItem(0);

    if (android.os.Build.VERSION.SDK_INT >= 11) {
      ActionBar actionBar = getActionBar();
      actionBar.setTitle(null);
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    // Opens a new Appboy session. You can now start logging custom events.
    Appboy.getInstance(this).openSession();
  }

  @Override
  public void onStop() {
    // Closes the Appboy session.
    Appboy.getInstance(this).closeSession();
    super.onStop();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.decision, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.feedback:
        mViewPager.setCurrentItem(1);
        break;
      case R.id.settings:
        startActivity(new Intent(this, PreferencesActivity.class));
        break;
      default:
        Log.e(TAG, String.format("MenuItem not found: [%s]", item.getTitle()));
    }
    return true;
  }

  private AppboyFeedbackFragment createAppboyFeedbackFragment() {
    // Create a custom style using the FeedbackCustomStyle builder.
    FeedbackCustomStyle feedbackCustomStyle = new FeedbackCustomStyle.Builder().setFontColor(Color.WHITE).build();

    // Define a navigation action that should be performed on send/cancel. In this case, we are using our view pager
    // to return back to the DecisionFragment.
    FinishAction finishAction = new FinishAction() {
      public void onFinish() {
        mViewPager.setCurrentItem(0);
      }
    };

    // Create the AppboyFeedbackFragment. This helper class will style the AppboyFeedbackFragment with the style
    // provided and attach the FinishAction.
    return AppboyFeedbackFragment.newInstance(feedbackCustomStyle, finishAction);
  }

  private List<Fragment> initializeFragmentList() {
    List<Fragment> fragments = new ArrayList<Fragment>();
    fragments.add(new DecisionFragment());
    fragments.add(createAppboyFeedbackFragment());
    return fragments;
  }

  private class PageAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragments;

    public PageAdapter(FragmentManager fragmentManager, List<Fragment> fragments) {
      super(fragmentManager);
      mFragments = fragments;
    }

    @Override
    public int getCount() {
      return mFragments.size();
    }

    @Override
    public Fragment getItem(int position) {
      return mFragments.get(position);
    }
  }
}
