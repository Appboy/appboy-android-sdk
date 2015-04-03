package com.appboy.sample;

import android.graphics.Color;
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
import com.appboy.enums.inappmessage.ClickAction;
import com.appboy.enums.inappmessage.DismissType;
import com.appboy.enums.inappmessage.SlideFrom;
import com.appboy.models.IInAppMessage;
import com.appboy.models.IInAppMessageImmersive;
import com.appboy.models.InAppMessageFull;
import com.appboy.models.InAppMessageModal;
import com.appboy.models.InAppMessageSlideup;
import com.appboy.models.MessageButton;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;
import com.appboy.ui.support.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InAppMessageTesterActivity extends AppboyFragmentActivity {
  private static final String CUSTOM_INAPPMESSAGE_VIEW_KEY = "inapmessages_custom_inappmessage_view";
  private static final String CUSTOM_INAPPMESSAGE_MANAGER_LISTENER_KEY = "inappmessages_custom_inappmessage_manager_listener";
  private static final String CUSTOM_APPBOY_NAVIGATOR_KEY = "inappmessages_custom_appboy_navigator";
  private static final String IMAGE_URL_BLUE = "https://s3.amazonaws.com/appboy-staging-test/android-sdk-image-push-integration-test3.jpg";
  private static final String IMAGE_URL_APPBOY = "https://pbs.twimg.com/profile_images/489799002259329024/9yeNWMU_.png";

  // color reference: http://www.google.com/design/spec/style/color.html
  private static final int GOOGLE_PURPLE = 0xFF673AB7;
  private static final int APPBOY_RED = 0xFFf33e3e;

  private ListView mSlideFromListView;
  private ListView mClickActionListView;
  private ListView mDismissTypeListView;
  private ListView mTextColorListView;
  private ListView mBackgroundColorListView;
  private ListView mIconColorListView;
  private ListView mCloseButtonColorListView;
  private ListView mMessageTypeListView;
  private ListView mHeaderTextColorListView;
  private ListView mIconListView;
  private ListView mMessageButtonListView;
  private EditText mUriEditText;
  private EditText mMessageEditText;
  private EditText mHeaderEditText;
  private EditText mIconEditText;
  private EditText mDurationSecondsEditText;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.inappmessage_tester);
    setTitle("In App Messages");

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

    List<String> backgroundColorValues = Arrays.asList(getResources().getStringArray(R.array.backgroundColorTypeValues));
    ArrayAdapter<String> backgroundColorListAdapter = new ArrayAdapter<String>(this, R.layout.default_list_view, R.id.text, backgroundColorValues);
    mBackgroundColorListView = (ListView) findViewById(R.id.bg_color_list_view);
    mBackgroundColorListView.setAdapter(backgroundColorListAdapter);

    List<String> iconColorValues = Arrays.asList(getResources().getStringArray(R.array.iconColorTypeValues));
    ArrayAdapter<String> iconColorListAdapter = new ArrayAdapter<String>(this, R.layout.default_list_view, R.id.text, iconColorValues);
    mIconColorListView = (ListView) findViewById(R.id.icon_color_list_view);
    mIconColorListView.setAdapter(iconColorListAdapter);

    List<String> closeButtonColorValues = Arrays.asList(getResources().getStringArray(R.array.closeButtonColorTypeValues));
    ArrayAdapter<String> closeButtonColorListAdapter = new ArrayAdapter<String>(this, R.layout.default_list_view, R.id.text, closeButtonColorValues);
    mCloseButtonColorListView = (ListView) findViewById(R.id.close_btn_color_list_view);
    mCloseButtonColorListView.setAdapter(closeButtonColorListAdapter);

    List<String> textColorValues = Arrays.asList(getResources().getStringArray(R.array.textColorTypeValues));
    ArrayAdapter<String>textColorListAdapter = new ArrayAdapter<String>(this, R.layout.default_list_view, R.id.text, textColorValues);
    mTextColorListView = (ListView) findViewById(R.id.text_color_list_view);
    mTextColorListView.setAdapter(textColorListAdapter);

    List<String> messageTypeValues = Arrays.asList(getResources().getStringArray(R.array.messageTypeTypeValues));
    ArrayAdapter<String>messageTypeListAdapter = new ArrayAdapter<String>(this, R.layout.default_list_view, R.id.text, messageTypeValues);
    mMessageTypeListView = (ListView) findViewById(R.id.message_type_list_view);
    mMessageTypeListView.setAdapter(messageTypeListAdapter);

    List<String> headerTextColorAlignValues = Arrays.asList(getResources().getStringArray(R.array.headerTextColorTypeValues));
    ArrayAdapter<String>headerTextColorListAdapter = new ArrayAdapter<String>(this, R.layout.default_list_view, R.id.text, headerTextColorAlignValues);
    mHeaderTextColorListView = (ListView) findViewById(R.id.header_text_list_view);
    mHeaderTextColorListView.setAdapter(headerTextColorListAdapter);

    List<String> iconValues = Arrays.asList(getResources().getStringArray(R.array.iconTypeValues));
    ArrayAdapter<String>iconListAdapter = new ArrayAdapter<String>(this, R.layout.default_list_view, R.id.text, iconValues);
    mIconListView = (ListView) findViewById(R.id.icon_list_view);
    mIconListView.setAdapter(iconListAdapter);

    List<String> messageButtonValues = Arrays.asList(getResources().getStringArray(R.array.messageButtonTypeValues));
    ArrayAdapter<String>messageButtonListAdapter = new ArrayAdapter<String>(this, R.layout.default_list_view, R.id.text, messageButtonValues);
    mMessageButtonListView = (ListView) findViewById(R.id.message_buttons_list_view);
    mMessageButtonListView.setAdapter(messageButtonListAdapter);

    mUriEditText = (EditText) findViewById(R.id.uri_edit_text);
    mMessageEditText = (EditText) findViewById(R.id.message_edit_text);
    mHeaderEditText = (EditText) findViewById(R.id.header_edit_text);
    mIconEditText = (EditText) findViewById(R.id.icon_edit_text);
    mDurationSecondsEditText = (EditText) findViewById(R.id.duration_seconds_edit_text);

    CheckBox customInAppMessageViewCheckBox = (CheckBox) findViewById(R.id.custom_inappmessage_view_factory_checkbox);
    customInAppMessageViewCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          AppboyInAppMessageManager.getInstance().setCustomInAppMessageViewFactory(new CustomInAppMessageViewFactory());
        } else {
          AppboyInAppMessageManager.getInstance().setCustomInAppMessageViewFactory(null);
        }
        getPreferences(MODE_PRIVATE).edit().putBoolean(CUSTOM_INAPPMESSAGE_VIEW_KEY, isChecked).commit();
      }
    });
    boolean usingCustomInAppMessageView = getPreferences(MODE_PRIVATE).getBoolean(CUSTOM_INAPPMESSAGE_VIEW_KEY, false);
    customInAppMessageViewCheckBox.setChecked(usingCustomInAppMessageView);

    CheckBox customInAppMessageManagerListenerCheckBox = (CheckBox) findViewById(R.id.custom_inappmessage_manager_listener_checkbox);
    customInAppMessageManagerListenerCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          AppboyInAppMessageManager.getInstance().setCustomInAppMessageManagerListener(new CustomInAppMessageManagerListener(InAppMessageTesterActivity.this));
        } else {
          AppboyInAppMessageManager.getInstance().setCustomInAppMessageManagerListener(null);
        }
        getPreferences(MODE_PRIVATE).edit().putBoolean(CUSTOM_INAPPMESSAGE_MANAGER_LISTENER_KEY, isChecked).commit();
      }
    });
    boolean usingCustomInAppMessageManagerListener = getPreferences(MODE_PRIVATE).getBoolean(CUSTOM_INAPPMESSAGE_MANAGER_LISTENER_KEY, false);
    customInAppMessageManagerListenerCheckBox.setChecked(usingCustomInAppMessageManagerListener);

    CheckBox customAppboyNavigatorCheckBox = (CheckBox) findViewById(R.id.custom_appboy_navigator_checkbox);
    customAppboyNavigatorCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          Appboy.getInstance(InAppMessageTesterActivity.this).setAppboyNavigator(new CustomAppboyNavigator());
        } else {
          Appboy.getInstance(InAppMessageTesterActivity.this).setAppboyNavigator(null);
        }
        getPreferences(MODE_PRIVATE).edit().putBoolean(CUSTOM_APPBOY_NAVIGATOR_KEY, isChecked).commit();
      }
    });
    boolean usingCustomAppboyNavigator = getPreferences(MODE_PRIVATE).getBoolean(CUSTOM_APPBOY_NAVIGATOR_KEY, false);
    customAppboyNavigatorCheckBox.setChecked(usingCustomAppboyNavigator);

    Button createAndAddInAppMessageButton = (Button) findViewById(R.id.create_and_add_inappmessage_button);
    createAndAddInAppMessageButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        if (getPreferences(MODE_PRIVATE).getBoolean(CUSTOM_INAPPMESSAGE_VIEW_KEY, false)) {
          // current custom in-app message view is an implementation of a base in-app message.
          addInAppMessage(new CustomInAppMessage());
        } else if (mMessageTypeListView.getCheckedItemPosition() >= 0) {
          // initialize inappmessage with defaults and set message type.
          String messageTypeString = mMessageTypeListView.getItemAtPosition(mMessageTypeListView.getCheckedItemPosition()).toString();
          if ("Slideup".equals(messageTypeString)) {
            addInAppMessage(new InAppMessageSlideup());
          } else if ("Modal".equals(messageTypeString)) {
            addInAppMessage(new InAppMessageModal());
          } else if ("Full".equals(messageTypeString)) {
            addInAppMessage(new InAppMessageFull());
          } else {
            return;
          }
        } else {
          Toast.makeText(InAppMessageTesterActivity.this, "Please select a Message Type.", Toast.LENGTH_LONG).show();
          return;
        }
      }
    });

    Button displayNextInAppMessageButton = (Button) findViewById(R.id.display_next_inappmessage_button);
    displayNextInAppMessageButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AppboyInAppMessageManager.getInstance().requestDisplayInAppMessage();
      }
    });

    Button requestInAppMessageFromServerButton = (Button) findViewById(R.id.request_inappmessage_from_server_button);
    requestInAppMessageFromServerButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Appboy.getInstance(InAppMessageTesterActivity.this).requestInAppMessageRefresh();
      }
    });

    Button hideCurrentInAppMessageButton = (Button) findViewById(R.id.hide_current_inappmessage_button);
    hideCurrentInAppMessageButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AppboyInAppMessageManager.getInstance().hideCurrentInAppMessage(true);
      }
    });
  }

  private void addInAppMessageImmersive(IInAppMessageImmersive inAppMessage) {
    if (inAppMessage instanceof InAppMessageModal) {
      inAppMessage.setMessage("Welcome to Appboy! Appboy is Marketing Automation for Apps.  This is a modal in-app message.");
      inAppMessage.setHeader("Hello from Appboy!");
      inAppMessage.setIcon("\uf091");
    } else if (inAppMessage instanceof InAppMessageFull) {
      inAppMessage.setMessage("Welcome to Appboy! Appboy is Marketing Automation for Apps. This is an example of a full in-app message.  Full in-app messages can contain many lines of text as well as a header, image, and action buttons.");
      inAppMessage.setHeader("Hello from Appboy!");
      inAppMessage.setImageUrl(IMAGE_URL_APPBOY);

    }
    ArrayList<MessageButton> messageButtons = new ArrayList<MessageButton>();
    MessageButton buttonOne = new MessageButton();
    buttonOne.setText("NEWSFEED");
    buttonOne.setClickAction(ClickAction.NEWS_FEED);
    messageButtons.add(buttonOne);
    inAppMessage.setMessageButtons(messageButtons);
    addMessageButtons(inAppMessage);
    addHeader(inAppMessage);
    addCloseButtonColor(inAppMessage);
  }

  private void addInAppMessageSlideup(InAppMessageSlideup inAppMessage) {
    inAppMessage.setMessage("Welcome to Appboy! This is a slideup in-app message.");
    inAppMessage.setIcon("\uf091");
    inAppMessage.setClickAction(ClickAction.NEWS_FEED);
    addSlideFrom(inAppMessage);
    addChevronColor(inAppMessage);
  }

  private void addInAppMessageCustom(IInAppMessage inAppMessage) {
    inAppMessage.setMessage("Welcome to Appboy! This is a custom in-app message.");
    inAppMessage.setIcon("\uf091");
  }

  private void addInAppMessage(IInAppMessage inAppMessage) {
    if (inAppMessage instanceof IInAppMessageImmersive) {
      addInAppMessageImmersive((IInAppMessageImmersive) inAppMessage);
    } else if (inAppMessage instanceof InAppMessageSlideup) {
      addInAppMessageSlideup((InAppMessageSlideup) inAppMessage);
    } else if (inAppMessage instanceof IInAppMessage) {
      addInAppMessageCustom(inAppMessage);
    }
    if(!addClickAction(inAppMessage)) {
      return;
    }
    addDuration(inAppMessage);
    addDismissType(inAppMessage);
    addBackgroundColor(inAppMessage);
    addMessage(inAppMessage);
    addImage(inAppMessage);
    AppboyInAppMessageManager.getInstance().addInAppMessage(inAppMessage);
  }

  private void addDuration(IInAppMessage inAppMessage) {
    // set in-app message duration if defined
    if (!StringUtils.isNullOrEmpty(mDurationSecondsEditText.getText().toString())) {
      inAppMessage.setDurationInMilliseconds(Integer.parseInt(mDurationSecondsEditText.getText().toString()) * 1000);
    }
  }

  private void addDismissType(IInAppMessage inAppMessage) {
    // set dismiss type if defined
    if (mDismissTypeListView.getCheckedItemPosition() >= 0) {
      String dismissTypeString = mDismissTypeListView.getItemAtPosition(mDismissTypeListView.getCheckedItemPosition()).toString();
      if ("Auto".equals(dismissTypeString)) {
        inAppMessage.setDismissType(DismissType.AUTO_DISMISS);
      } else if ("Manual".equals(dismissTypeString)) {
        inAppMessage.setDismissType(DismissType.MANUAL);
      }
    }
  }

  private void addBackgroundColor(IInAppMessage inAppMessage) {
    // set background color if defined
    if (mBackgroundColorListView.getCheckedItemPosition() >= 0) {
      String backgroundColorString = mBackgroundColorListView.getItemAtPosition(mBackgroundColorListView.getCheckedItemPosition()).toString();
      if ("Default".equals(backgroundColorString)) {
        // Do nothing.
      } else if ("Red".equals(backgroundColorString)) {
        inAppMessage.setBackgroundColor(APPBOY_RED);
      } else if ("Black".equals(backgroundColorString)) {
        inAppMessage.setBackgroundColor(Color.BLACK);
      } else if ("White".equals(backgroundColorString)) {
        inAppMessage.setBackgroundColor(Color.WHITE);
      }
    }
  }

  private void addChevronColor(InAppMessageSlideup inAppMessage) {
    // set close button color if defined
    if (mCloseButtonColorListView.getCheckedItemPosition() >= 0) {
      String closeButtonColorString = mCloseButtonColorListView.getItemAtPosition(mCloseButtonColorListView.getCheckedItemPosition()).toString();
      if ("Default".equals(closeButtonColorString)) {
        // Do nothing.
      } else if ("Red".equals(closeButtonColorString)) {
        inAppMessage.setChevronColor(APPBOY_RED);
      } else if ("White".equals(closeButtonColorString)) {
        inAppMessage.setChevronColor(Color.WHITE);
      }
    }
  }

  private void addCloseButtonColor(IInAppMessageImmersive inAppMessage) {
    // set close button color if defined
    if (mCloseButtonColorListView.getCheckedItemPosition() >= 0) {
      String closeButtonColorString = mCloseButtonColorListView.getItemAtPosition(mCloseButtonColorListView.getCheckedItemPosition()).toString();
      if ("Default".equals(closeButtonColorString)) {
        // Do nothing.
      } else if ("Red".equals(closeButtonColorString)) {
        inAppMessage.setCloseButtonColor(APPBOY_RED);
      } else if ("White".equals(closeButtonColorString)) {
        inAppMessage.setCloseButtonColor(Color.WHITE);
      }
    }
  }

  private void addMessage(IInAppMessage inAppMessage) {
    // set text color if defined
    if (mTextColorListView.getCheckedItemPosition() >= 0) {
      String textColorString = mTextColorListView.getItemAtPosition(mTextColorListView.getCheckedItemPosition()).toString();
      if ("Default".equals(textColorString)) {
        // Do nothing.
      } else if ("Red".equals(textColorString)) {
        inAppMessage.setMessageTextColor(APPBOY_RED);
      } else if ("Purple".equals(textColorString)) {
        inAppMessage.setMessageTextColor(GOOGLE_PURPLE);
      } else if ("White".equals(textColorString)) {
        inAppMessage.setMessageTextColor(Color.WHITE);
      }
    }
    if (mMessageEditText.getText().toString().length() > 0) {
      inAppMessage.setMessage(mMessageEditText.getText().toString());
    }
  }

  private void addImage(IInAppMessage inAppMessage) {
    // set icon color if defined
    if (mIconColorListView.getCheckedItemPosition() >= 0) {
      String iconColorString = mIconColorListView.getItemAtPosition(mIconColorListView.getCheckedItemPosition()).toString();
      if ("Default".equals(iconColorString)) {
        // Do nothing.
      } else if ("Red".equals(iconColorString)) {
        inAppMessage.setIconColor(Color.WHITE);
        inAppMessage.setIconBackgroundColor(APPBOY_RED);
      } else if ("Purple".equals(iconColorString)) {
        inAppMessage.setIconColor(Color.WHITE);
        inAppMessage.setIconBackgroundColor(GOOGLE_PURPLE);
      } else if ("White".equals(iconColorString)) {
        inAppMessage.setIconColor(Color.GRAY);
        inAppMessage.setIconBackgroundColor(Color.WHITE);
      }
    }
    // set in-app message image url
    if (mIconListView.getCheckedItemPosition() >= 0) {
      String iconString = mIconListView.getItemAtPosition(mIconListView.getCheckedItemPosition()).toString();
      if ("None".equals(iconString)) {
        inAppMessage.setImageUrl(null);
        inAppMessage.setIcon(null);
      } else if ("Blue".equals(iconString)) {
        inAppMessage.setImageUrl(IMAGE_URL_BLUE);
      } else if ("Appboy".equals(iconString)) {
        inAppMessage.setImageUrl(IMAGE_URL_APPBOY);
      } else if ("Horn".equals(iconString)) {
        inAppMessage.setIcon("\uf0a1");
      } else if ("Money".equals(iconString)) {
        inAppMessage.setIcon("\uf0d6");
      } else if ("Play".equals(iconString)) {
        inAppMessage.setIcon("\uf04b");
      } else if ("Star".equals(iconString)) {
        inAppMessage.setIcon("\uf005");
      } else if ("Trophy".equals(iconString)) {
        inAppMessage.setIcon("\uf091");
      } else if ("Video".equals(iconString)) {
        inAppMessage.setIcon("\uf03d");
      }
    }
    if (mIconEditText.getText().toString().length() > 0) {
      try {
        inAppMessage.setIcon(new String(Character.toChars(Integer.decode("0x" + mIconEditText.getText().toString()))));
      } catch (Exception e) {
        Toast.makeText(this.getApplicationContext(), "Invalid Fontawesome code. Please enter a valid four character code, e.g. \"f042\" for 'fa-adjust'.  See http://fortawesome.github.io/Font-Awesome/cheatsheet/", Toast.LENGTH_LONG).show();

      }
    }
  }

  private boolean addClickAction(IInAppMessage inAppMessage) {
    // set click action if defined
    if (mClickActionListView.getCheckedItemPosition() >= 0) {
      String clickActionString = mClickActionListView.getItemAtPosition(mClickActionListView.getCheckedItemPosition()).toString();
      if ("News Feed".equals(clickActionString)) {
        inAppMessage.setClickAction(ClickAction.NEWS_FEED);
      } else if ("URI".equals(clickActionString)) {
        String uriString = mUriEditText.getText().toString();
        if (StringUtils.isNullOrEmpty(uriString)) {
          Toast.makeText(InAppMessageTesterActivity.this, "Please enter a URI.", Toast.LENGTH_LONG).show();
          return false;
        } else {
          inAppMessage.setClickAction(ClickAction.URI, Uri.parse(uriString));

        }
      } else if ("None".equals(clickActionString)) {
        inAppMessage.setClickAction(ClickAction.NONE);
      }
    }
    return true;
  }

  private void addSlideFrom(InAppMessageSlideup inAppMessage) {
    // set slide from if defined
    if (mSlideFromListView.getCheckedItemPosition() >= 0){
      String slideFromString = mSlideFromListView.getItemAtPosition(mSlideFromListView.getCheckedItemPosition()).toString();
      if ("Top".equals(slideFromString)) {
        inAppMessage.setSlideFrom(SlideFrom.TOP);
      } else if ("Bottom".equals(slideFromString)) {
        inAppMessage.setSlideFrom(SlideFrom.BOTTOM);
      }
    }
  }

  private void addHeader(IInAppMessageImmersive inAppMessage) {
    // set header text color if defined
    if (mHeaderTextColorListView.getCheckedItemPosition() >= 0) {
      String headerTextColorString = mHeaderTextColorListView.getItemAtPosition(mHeaderTextColorListView.getCheckedItemPosition()).toString();
      if ("Default".equals(headerTextColorString)) {
        // Do nothing.
      } else if ("Red".equals(headerTextColorString)) {
        inAppMessage.setHeaderTextColor(APPBOY_RED);
      } else if ("Purple".equals(headerTextColorString)) {
        inAppMessage.setHeaderTextColor(GOOGLE_PURPLE);
      } else if ("White".equals(headerTextColorString)) {
        inAppMessage.setHeaderTextColor(Color.WHITE);
      }
    }
    if (mHeaderEditText.getText().toString().length() > 0) {
      inAppMessage.setHeader(mHeaderEditText.getText().toString());
    }
  }

  private void addMessageButtons(IInAppMessageImmersive inAppMessage) {
    // add message buttons.
    if (mMessageButtonListView.getCheckedItemPosition() >= 0) {
      String buttonString = mMessageButtonListView.getItemAtPosition(mMessageButtonListView.getCheckedItemPosition()).toString();
      ArrayList<MessageButton> messageButtons = new ArrayList<MessageButton>();
      if ("None".equals(buttonString)) {
        inAppMessage.setMessageButtons(null);
      } else if ("One".equals(buttonString)) {
        MessageButton buttonOne = new MessageButton();
        buttonOne.setText("NEWSFEED");
        buttonOne.setBackgroundColor(Color.BLACK);
        buttonOne.setClickAction(ClickAction.NEWS_FEED);
        messageButtons.add(buttonOne);
        inAppMessage.setMessageButtons(messageButtons);
      } else if ("Two".equals(buttonString)) {
        MessageButton buttonOne = new MessageButton();
        buttonOne.setText("ACCEPT");
        buttonOne.setClickAction(ClickAction.URI, Uri.parse("http://www.appboy.com"));
        inAppMessage.setMessageButtons(messageButtons);
        messageButtons.add(buttonOne);
        MessageButton buttonTwo = new MessageButton();
        buttonTwo.setText("CLOSE");
        buttonTwo.setClickAction(ClickAction.NONE);
        messageButtons.add(buttonTwo);
        inAppMessage.setMessageButtons(messageButtons);
      }
    }
  }
}
