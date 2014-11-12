package com.appboy.sample;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.appboy.Appboy;
import com.appboy.enums.Slideup.ClickAction;
import com.appboy.enums.Slideup.DismissType;
import com.appboy.enums.Slideup.SlideFrom;
import com.appboy.models.Slideup;
import com.appboy.ui.slideups.AppboySlideupManager;
import com.appboy.ui.support.StringUtils;

import java.util.Arrays;
import java.util.List;

public class SlideupTesterActivity extends AppboyFragmentActivity {
  private static final String CUSTOM_SLIDEUP_VIEW_KEY = "slideups_custom_slideup_view";
  private static final String CUSTOM_SLIDEUP_MANAGER_LISTENER_KEY = "slideups_custom_slideup_manager_listener";
  private static final String CUSTOM_APPBOY_NAVIGATOR_KEY = "slideups_custom_appboy_navigator";
  private static final int SLIDEUP_DURATION_DEFAULT_MILLIS = 5000;

  private ListView mSlideFromListView;
  private ListView mClickActionListView;
  private ListView mDismissTypeListView;
  private EditText mUriEditText;
  private EditText mDurationSecondsEditText;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.slideup_tester);
    setTitle("Slideups");

    List<String> slideFromValues = Arrays.asList(getResources().getStringArray(R.array.slideFromValues));
    ArrayAdapter<String> slideFromListAdapter = new ArrayAdapter<String>(this, R.layout.default_list_view, R.id.text, slideFromValues);
    mSlideFromListView = (ListView) findViewById(R.id.slide_from_list_view);
    mSlideFromListView.setAdapter(slideFromListAdapter);

    List<String> clickActionValues = Arrays.asList(getResources().getStringArray(R.array.clickActionValues));
    ArrayAdapter<String> clickActionListAdapter = new ArrayAdapter<String>(this, R.layout.default_list_view, R.id.text, clickActionValues);
    mClickActionListView = (ListView) findViewById(R.id.click_action_list_view);
    mClickActionListView.setAdapter(clickActionListAdapter);

    List<String> dismissTypeValues = Arrays.asList(getResources().getStringArray(R.array.dismissTypeValues));
    ArrayAdapter<String> dismissTypeListAdapter = new ArrayAdapter<String>(this, R.layout.default_list_view, R.id.text, dismissTypeValues);
    mDismissTypeListView = (ListView) findViewById(R.id.dismiss_type_list_view);
    mDismissTypeListView.setAdapter(dismissTypeListAdapter);

    mUriEditText = (EditText) findViewById(R.id.uri_edit_text);
    mDurationSecondsEditText = (EditText) findViewById(R.id.duration_seconds_edit_text);

    CheckBox customSlideupViewCheckBox = (CheckBox) findViewById(R.id.custom_slideup_view_factory_checkbox);
    customSlideupViewCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          AppboySlideupManager.getInstance().setCustomSlideupViewFactory(new CustomSlideupViewFactory());
        } else {
          AppboySlideupManager.getInstance().setCustomSlideupViewFactory(null);
        }
        getPreferences(MODE_PRIVATE).edit().putBoolean(CUSTOM_SLIDEUP_VIEW_KEY, isChecked).commit();
      }
    });
    boolean usingCustomSlideupView = getPreferences(MODE_PRIVATE).getBoolean(CUSTOM_SLIDEUP_VIEW_KEY, false);
    customSlideupViewCheckBox.setChecked(usingCustomSlideupView);

    CheckBox customSlideupManagerListenerCheckBox = (CheckBox) findViewById(R.id.custom_slideup_manager_listener_checkbox);
    customSlideupManagerListenerCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          AppboySlideupManager.getInstance().setCustomSlideupManagerListener(new CustomSlideupManagerListener(SlideupTesterActivity.this));
        } else {
          AppboySlideupManager.getInstance().setCustomSlideupManagerListener(null);
        }
        getPreferences(MODE_PRIVATE).edit().putBoolean(CUSTOM_SLIDEUP_MANAGER_LISTENER_KEY, isChecked).commit();
      }
    });
    boolean usingCustomSlideupManagerListener = getPreferences(MODE_PRIVATE).getBoolean(CUSTOM_SLIDEUP_MANAGER_LISTENER_KEY, false);
    customSlideupManagerListenerCheckBox.setChecked(usingCustomSlideupManagerListener);

    CheckBox customAppboyNavigatorCheckBox = (CheckBox) findViewById(R.id.custom_appboy_navigator_checkbox);
    customAppboyNavigatorCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          Appboy.getInstance(SlideupTesterActivity.this).setAppboyNavigator(new CustomAppboyNavigator());
        } else {
          Appboy.getInstance(SlideupTesterActivity.this).setAppboyNavigator(null);
        }
        getPreferences(MODE_PRIVATE).edit().putBoolean(CUSTOM_APPBOY_NAVIGATOR_KEY, isChecked).commit();
      }
    });
    boolean usingCustomAppboyNavigator = getPreferences(MODE_PRIVATE).getBoolean(CUSTOM_APPBOY_NAVIGATOR_KEY, false);
    customAppboyNavigatorCheckBox.setChecked(usingCustomAppboyNavigator);

    Button createAndAddSlideupButton = (Button) findViewById(R.id.create_and_add_slideup_button);
    createAndAddSlideupButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mSlideFromListView.getCheckedItemPosition() < 0) {
          Toast.makeText(SlideupTesterActivity.this, "Please select from which direction the slideup will animate in.", Toast.LENGTH_LONG).show();
          return;
        }
        SlideFrom slideFrom;
        String slideFromString = mSlideFromListView.getItemAtPosition(mSlideFromListView.getCheckedItemPosition()).toString();
        if ("Top".equals(slideFromString)) {
          slideFrom = SlideFrom.TOP;
        } else if ("Bottom".equals(slideFromString)) {
          slideFrom = SlideFrom.BOTTOM;
        } else {
          return;
        }

        if (mClickActionListView.getCheckedItemPosition() < 0) {
          Toast.makeText(SlideupTesterActivity.this, "Please select a ClickAction.", Toast.LENGTH_LONG).show();
          return;
        }
        ClickAction clickAction;
        String clickActionString = mClickActionListView.getItemAtPosition(mClickActionListView.getCheckedItemPosition()).toString();
        if ("News Feed".equals(clickActionString)) {
          clickAction = ClickAction.NEWS_FEED;
        } else if ("URI".equals(clickActionString)) {
          clickAction = ClickAction.URI;
        } else if ("None".equals(clickActionString)) {
          clickAction = ClickAction.NONE;
        } else {
          return;
        }

        if (mDismissTypeListView.getCheckedItemPosition() < 0) {
          Toast.makeText(SlideupTesterActivity.this, "Please select a DismissType.", Toast.LENGTH_LONG).show();
          return;
        }
        DismissType dismissType;
        String dismissTypeString = mDismissTypeListView.getItemAtPosition(mDismissTypeListView.getCheckedItemPosition()).toString();
        if ("Auto".equals(dismissTypeString)) {
          dismissType = DismissType.AUTO_DISMISS;
        } else if ("Swipe".equals(dismissTypeString)) {
          dismissType = DismissType.SWIPE;
        } else {
          return;
        }

        if (clickAction == ClickAction.URI && StringUtils.isNullOrEmpty(mUriEditText.getText().toString())) {
          Toast.makeText(SlideupTesterActivity.this, "Please enter a URI.", Toast.LENGTH_LONG).show();
          return;
        }

        // initialize slideup
        Slideup slideup = Slideup.createSlideup("This is a test slideup.", slideFrom, dismissType, SLIDEUP_DURATION_DEFAULT_MILLIS);

        // set slideup duration
        if (!StringUtils.isNullOrEmpty(mDurationSecondsEditText.getText().toString())) {
          slideup.setDurationInMilliseconds(Integer.parseInt(mDurationSecondsEditText.getText().toString()) * 1000);
        }

        // set slideup click action
        switch (clickAction) {
          case NEWS_FEED:
            slideup.setClickActionToNewsFeed();
            break;
          case URI:
              Uri uri = Uri.parse(mUriEditText.getText().toString());
              slideup.setClickActionToUri(uri);
            break;
          case NONE:
            slideup.setClickActionToNone();
        }

        AppboySlideupManager.getInstance().addSlideup(slideup);
      }
    });

    Button displayNextSlideupButton = (Button) findViewById(R.id.display_next_slideup_button);
    displayNextSlideupButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AppboySlideupManager.getInstance().requestDisplaySlideup();
      }
    });

    Button requestSlideupFromServerButton = (Button) findViewById(R.id.request_slideup_from_server_button);
    requestSlideupFromServerButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Appboy.getInstance(SlideupTesterActivity.this).requestSlideupRefresh();
      }
    });

    Button hideCurrentSlideupButton = (Button) findViewById(R.id.hide_current_slideup_button);
    hideCurrentSlideupButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AppboySlideupManager.getInstance().hideCurrentSlideup(true);
      }
    });
  }
}
