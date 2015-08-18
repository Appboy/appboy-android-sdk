package com.appboy.sample;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.appboy.Appboy;
import com.appboy.enums.inappmessage.ClickAction;
import com.appboy.enums.inappmessage.DismissType;
import com.appboy.enums.inappmessage.SlideFrom;
import com.appboy.models.IInAppMessage;
import com.appboy.models.IInAppMessageHtml;
import com.appboy.models.IInAppMessageImmersive;
import com.appboy.models.InAppMessageFull;
import com.appboy.models.InAppMessageHtmlFull;
import com.appboy.models.InAppMessageModal;
import com.appboy.models.InAppMessageSlideup;
import com.appboy.models.MessageButton;
import com.appboy.sample.util.SpinnerUtils;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InAppMessageTesterActivity extends AppboyFragmentActivity implements AdapterView.OnItemSelectedListener {
  private static final String CUSTOM_INAPPMESSAGE_VIEW_KEY = "inapmessages_custom_inappmessage_view";
  private static final String CUSTOM_INAPPMESSAGE_MANAGER_LISTENER_KEY = "inappmessages_custom_inappmessage_manager_listener";
  private static final String CUSTOM_APPBOY_NAVIGATOR_KEY = "inappmessages_custom_appboy_navigator";
  private static final String CUSTOM_INAPPMESSAGE_ANIMATION_KEY = "inappmessages_custom_inappmessage_animation";
  private static final String CUSTOM_INAPPMESSAGE_WEBVIEW_CLIENT_LISTENER_KEY = "inappmessages_custom_inappmessage_webview_client_listener";

  // '#' characters at the end of headers and messages are used to verify that the entire message was displayed.
  private static final String HEADER_10 = "Hey there#";
  private static final String HEADER_20 = "Hey hey from Appboy#";
  private static final String HEADER_30 = "Good morning from us @ Appboy#";
  private static final String HEADER_40 = "Hello from Appboy! Have a fun day today#";

  private static final String MESSAGE_40 = "Hello there!  This is an in-app message#";
  private static final String MESSAGE_90 = "Hello there! This is an in-app message.  Hello again!  Anyways, this is an in-app message#";
  private static final String MESSAGE_140 = "Welcome to Appboy! Appboy is Marketing Automation for Apps. This is an in-app message & this message is exactly one hundred and forty chars#";
  private static final String MESSAGE_240 = "Welcome to Appboy! Appboy is Marketing Automation for Apps. This is an in-app message & this message is exactly two hundred and forty chars!  " +
    "We don't recommend making in-app messages longer than 140 characters due to variations in screens#";
  private static final String MESSAGE_640 = "Welcome to Appboy! Appboy is Marketing Automation for Apps. This is an in-app message & this message is exactly six hundred and forty chars!  " +
    "We don't recommend making in-app messages longer than 140 characters due to variations in screens.  This is an in-app message & this message is exactly six hundred and forty chars!  " +
    "We don't recommend making in-app messages longer than 140 characters due to variations in screens.  This is an in-app message & this message is exactly six hundred and forty chars!  " +
    "We don't recommend making in-app messages longer than 140 characters due to variations in screens.  This is a waaaay too long message#";

  private static final String HTML_CONTENT_REMOTE_URL = "https://s3.amazonaws.com/appboy-development-bucket/droidboy_html_tester_zipped_assets.zip";

  // color reference: http://www.google.com/design/spec/style/color.html
  private static final int APPBOY_RED = 0xFFf33e3e;
  private static final int GOOGLE_ORANGE = 0xFFFF5722;
  private static final int GOOGLE_YELLOW = 0xFFFFEB3B;
  private static final int GOOGLE_GREEN = 0xFF4CAF50;
  private static final int APPBOY_BLUE = 0xFF0073d5;
  private static final int GOOGLE_PURPLE = 0xFF673AB7;
  private static final int GOOGLE_BROWN = 0xFF795548;
  private static final int GOOGLE_GREY = 0xFF9E9E9E;
  private static final int BLACK = 0xFF000000;
  private static final int WHITE = 0xFFFFFFFF;
  private static final Map<Integer, Integer> sSpinnerOptionMap;

  static {
    Map<Integer, Integer> spinnerOptionMap = new HashMap<Integer, Integer>();
    spinnerOptionMap.put(R.id.inapp_set_message_type_spinner, R.array.inapp_message_type_options);
    spinnerOptionMap.put(R.id.inapp_click_action_spinner, R.array.inapp_click_action_options);
    spinnerOptionMap.put(R.id.inapp_dismiss_type_spinner, R.array.inapp_dismiss_type_options);
    spinnerOptionMap.put(R.id.inapp_slide_from_spinner, R.array.inapp_slide_from_options);
    spinnerOptionMap.put(R.id.inapp_header_spinner, R.array.inapp_header_options);
    spinnerOptionMap.put(R.id.inapp_message_spinner, R.array.inapp_message_options);
    spinnerOptionMap.put(R.id.inapp_background_color_spinner, R.array.inapp_color_options);
    spinnerOptionMap.put(R.id.inapp_icon_color_spinner, R.array.inapp_color_options);
    spinnerOptionMap.put(R.id.inapp_icon_background_color_spinner, R.array.inapp_color_options);
    spinnerOptionMap.put(R.id.inapp_close_button_color_spinner, R.array.inapp_color_options);
    spinnerOptionMap.put(R.id.inapp_text_color_spinner, R.array.inapp_color_options);
    spinnerOptionMap.put(R.id.inapp_header_text_color_spinner, R.array.inapp_color_options);
    spinnerOptionMap.put(R.id.inapp_uri_spinner, R.array.inapp_uri_options);
    spinnerOptionMap.put(R.id.inapp_icon_spinner, R.array.inapp_icon_options);
    spinnerOptionMap.put(R.id.inapp_image_spinner, R.array.inapp_image_options);
    spinnerOptionMap.put(R.id.inapp_button_spinner, R.array.inapp_button_options);
    sSpinnerOptionMap = Collections.unmodifiableMap(spinnerOptionMap);
  }

  private String mMessageType;
  private String mClickAction;
  private String mDismissType;
  private String mSlideFrom;
  private String mUri;
  private String mHeader;
  private String mMessage;
  private String mBackgroundColor;
  private String mIconColor;
  private String mIconBackgroundColor;
  private String mCloseButtonColor;
  private String mTextColor;
  private String mHeaderTextColor;
  private String mIcon;
  private String mImage;
  private String mButtons;
  private String mHtmlBodyFromAssets;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.inappmessage_tester);
    setTitle("In App Messages");

    for (Integer key : sSpinnerOptionMap.keySet()) {
      SpinnerUtils.setUpSpinner((Spinner) findViewById(key), this, sSpinnerOptionMap.get(key));
    }

    CheckBox customInAppMessageViewCheckBox = (CheckBox) findViewById(R.id.custom_inappmessage_view_factory_checkbox);
    customInAppMessageViewCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          AppboyInAppMessageManager.getInstance().setCustomInAppMessageViewFactory(new CustomInAppMessageViewFactory());
        } else {
          AppboyInAppMessageManager.getInstance().setCustomInAppMessageViewFactory(null);
        }
        getPreferences(MODE_PRIVATE).edit().putBoolean(CUSTOM_INAPPMESSAGE_VIEW_KEY, isChecked).apply();
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
        getPreferences(MODE_PRIVATE).edit().putBoolean(CUSTOM_INAPPMESSAGE_MANAGER_LISTENER_KEY, isChecked).apply();
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
        getPreferences(MODE_PRIVATE).edit().putBoolean(CUSTOM_APPBOY_NAVIGATOR_KEY, isChecked).apply();
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
        } else {
          if ("slideup".equals(mMessageType)) {
            addInAppMessage(new InAppMessageSlideup());
          } else if ("modal".equals(mMessageType)) {
            addInAppMessage(new InAppMessageModal());
          } else if ("full".equals(mMessageType)) {
            addInAppMessage(new InAppMessageFull());
          } else if ("html_full".equals(mMessageType)) {
            addInAppMessage(new InAppMessageHtmlFull());
          } else {
            addInAppMessage(new InAppMessageSlideup());
          }
        }
      }
    });
    CheckBox customInAppMessageAnimationCheckBox = (CheckBox) findViewById(R.id.custom_appboy_animation_checkbox);
    customInAppMessageAnimationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          AppboyInAppMessageManager.getInstance().setCustomInAppMessageAnimationFactory(new CustomInAppMessageAnimationFactory());
        } else {
          AppboyInAppMessageManager.getInstance().setCustomInAppMessageAnimationFactory(null);
        }
        getPreferences(MODE_PRIVATE).edit().putBoolean(CUSTOM_INAPPMESSAGE_ANIMATION_KEY, isChecked).apply();
      }
    });
    boolean usingCustomInAppAnimation = getPreferences(MODE_PRIVATE).getBoolean(CUSTOM_INAPPMESSAGE_ANIMATION_KEY, false);
    customInAppMessageAnimationCheckBox.setChecked(usingCustomInAppAnimation);

    CheckBox customInAppMessageWebViewClientListenerCheckBox = (CheckBox) findViewById(R.id.custom_appboy_webview_client_listener_checkbox);
    customInAppMessageWebViewClientListenerCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          AppboyInAppMessageManager.getInstance().setCustomHtmlInAppMessageActionListener(new CustomHtmlInAppMessageActionListener(InAppMessageTesterActivity.this));
        } else {
          AppboyInAppMessageManager.getInstance().setCustomHtmlInAppMessageActionListener(null);
        }
        getPreferences(MODE_PRIVATE).edit().putBoolean(CUSTOM_INAPPMESSAGE_WEBVIEW_CLIENT_LISTENER_KEY, isChecked).apply();
      }
    });
    boolean usingCustomInAppWebViewClientListener = getPreferences(MODE_PRIVATE).getBoolean(CUSTOM_INAPPMESSAGE_WEBVIEW_CLIENT_LISTENER_KEY, false);
    customInAppMessageWebViewClientListenerCheckBox.setChecked(usingCustomInAppWebViewClientListener);

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

    mHtmlBodyFromAssets = readHtmlBodyFromAssets();
  }

  private void addInAppMessageImmersive(IInAppMessageImmersive inAppMessage) {
    if (inAppMessage instanceof InAppMessageModal) {
      inAppMessage.setMessage("Welcome to Appboy! Appboy is Marketing Automation for Apps.  This is a modal in-app message.");
      inAppMessage.setHeader("Hello from Appboy!");
      inAppMessage.setIcon("\uf091");
    } else if (inAppMessage instanceof InAppMessageFull) {
      inAppMessage.setMessage("Welcome to Appboy! Appboy is Marketing Automation for Apps. This is an example of a full in-app message.");
      inAppMessage.setHeader("Hello from Appboy!");
      inAppMessage.setImageUrl(getResources().getString(R.string.appboy_url));

    }
    ArrayList<MessageButton> messageButtons = new ArrayList<MessageButton>();
    MessageButton buttonOne = new MessageButton();
    buttonOne.setText("NEWSFEED");
    buttonOne.setClickAction(ClickAction.NEWS_FEED);
    messageButtons.add(buttonOne);
    inAppMessage.setMessageButtons(messageButtons);
    addMessageButtons(inAppMessage);
    setHeader(inAppMessage);
    setCloseButtonColor(inAppMessage);
  }

  private void addInAppMessageSlideup(InAppMessageSlideup inAppMessage) {
    inAppMessage.setMessage("Welcome to Appboy! This is a slideup in-app message.");
    inAppMessage.setIcon("\uf091");
    inAppMessage.setClickAction(ClickAction.NEWS_FEED);
    setSlideFrom(inAppMessage);
    setChevronColor(inAppMessage);
  }

  private void addInAppMessageCustom(IInAppMessage inAppMessage) {
    inAppMessage.setMessage("Welcome to Appboy! This is a custom in-app message.");
    inAppMessage.setIcon("\uf091");
  }

  private void addInAppMessageHtmlFull(IInAppMessageHtml inAppMessage) {
    inAppMessage.setMessage(mHtmlBodyFromAssets);
    inAppMessage.setAssetsZipRemoteUrl(HTML_CONTENT_REMOTE_URL);
  }

  private void addInAppMessage(IInAppMessage inAppMessage) {
    if (inAppMessage instanceof IInAppMessageImmersive) {
      addInAppMessageImmersive((IInAppMessageImmersive) inAppMessage);
    } else if (inAppMessage instanceof InAppMessageSlideup) {
      addInAppMessageSlideup((InAppMessageSlideup) inAppMessage);
    } else if (inAppMessage instanceof InAppMessageHtmlFull) {
      addInAppMessageHtmlFull((InAppMessageHtmlFull) inAppMessage);
    } else if (inAppMessage instanceof IInAppMessage) {
      addInAppMessageCustom(inAppMessage);
    }
    if (!addClickAction(inAppMessage)) {
      return;
    }
    setDismissType(inAppMessage);
    setBackgroundColor(inAppMessage);
    setMessage(inAppMessage);
    setIcon(inAppMessage);
    setImage(inAppMessage);
    AppboyInAppMessageManager.getInstance().addInAppMessage(inAppMessage);
  }

  private void setDismissType(IInAppMessage inAppMessage) {
    // set dismiss type if defined
    if ("auto".equals(mDismissType)) {
      inAppMessage.setDismissType(DismissType.AUTO_DISMISS);
    } else if ("auto-short".equals(mDismissType)) {
      inAppMessage.setDismissType(DismissType.AUTO_DISMISS);
      inAppMessage.setDurationInMilliseconds(1000);
    } else if ("manual".equals(mDismissType)) {
      inAppMessage.setDismissType(DismissType.MANUAL);
    }
  }

  private void setBackgroundColor(IInAppMessage inAppMessage) {
    // set background color if defined
    if (!SpinnerUtils.SpinnerItemNotSet(mBackgroundColor)) {
      inAppMessage.setBackgroundColor(parseColorFromString(mBackgroundColor));
    }
  }

  private void setChevronColor(InAppMessageSlideup inAppMessage) {
    // set chevron color if defined
    if (!SpinnerUtils.SpinnerItemNotSet(mCloseButtonColor)) {
      inAppMessage.setChevronColor(parseColorFromString(mCloseButtonColor));
    }
  }

  private void setCloseButtonColor(IInAppMessageImmersive inAppMessage) {
    // set close button color if defined
    if (!SpinnerUtils.SpinnerItemNotSet(mCloseButtonColor)) {
      inAppMessage.setCloseButtonColor(parseColorFromString(mCloseButtonColor));
    }
  }

  private void setMessage(IInAppMessage inAppMessage) {
    // set text color if defined
    if (!SpinnerUtils.SpinnerItemNotSet(mTextColor)) {
      inAppMessage.setMessageTextColor(parseColorFromString(mTextColor));
    }
    if ("40".equals(mMessage)) {
      inAppMessage.setMessage(MESSAGE_40);
    } else if ("90".equals(mMessage)) {
      inAppMessage.setMessage(MESSAGE_90);
    } else if ("140".equals(mMessage)) {
      inAppMessage.setMessage(MESSAGE_140);
    } else if ("240".equals(mMessage)) {
      inAppMessage.setMessage(MESSAGE_240);
    } else if ("640".equals(mMessage)) {
      inAppMessage.setMessage(MESSAGE_640);
    }
  }

  private void setIcon(IInAppMessage inAppMessage) {
    // set icon color if defined
    if (!SpinnerUtils.SpinnerItemNotSet(mIconColor)) {
      inAppMessage.setIconColor(parseColorFromString(mIconColor));
    }
    // set icon background color if defined
    if (!SpinnerUtils.SpinnerItemNotSet(mIconBackgroundColor)) {
      inAppMessage.setIconBackgroundColor(parseColorFromString(mIconBackgroundColor));
    }
    // set in-app message icon
    if (!SpinnerUtils.SpinnerItemNotSet(mIcon)) {
      if (mIcon.equals("none")) {
        inAppMessage.setIcon(null);
      } else {
        inAppMessage.setIcon(mIcon);
      }
    }
  }

  private void setImage(IInAppMessage inAppMessage) {
    // set in-app message image url
    if (!SpinnerUtils.SpinnerItemNotSet(mImage)) {
      if (mImage.equals("none")) {
        inAppMessage.setImageUrl(null);
      } else {
        inAppMessage.setImageUrl(mImage);
      }
    }
  }

  private boolean addClickAction(IInAppMessage inAppMessage) {
    // set click action if defined
    if ("newsfeed".equals(mClickAction)) {
      inAppMessage.setClickAction(ClickAction.NEWS_FEED);
    } else if ("uri".equals(mClickAction)) {
      if (SpinnerUtils.SpinnerItemNotSet(mUri)) {
        Toast.makeText(InAppMessageTesterActivity.this, "Please choose a URI.", Toast.LENGTH_LONG).show();
        return false;
      } else {
        inAppMessage.setClickAction(ClickAction.URI, Uri.parse(mUri));
      }
    } else if ("none".equals(mClickAction)) {
      inAppMessage.setClickAction(ClickAction.NONE);
    }
    return true;
  }

  private void setSlideFrom(InAppMessageSlideup inAppMessage) {
    // set slide from if defined
    if ("top".equals(mSlideFrom)) {
      inAppMessage.setSlideFrom(SlideFrom.TOP);
    } else if ("bottom".equals(mSlideFrom)) {
      inAppMessage.setSlideFrom(SlideFrom.BOTTOM);
    }
  }

  private void setHeader(IInAppMessageImmersive inAppMessage) {
    // set header text color if defined
    if (!SpinnerUtils.SpinnerItemNotSet(mHeaderTextColor)) {
      inAppMessage.setHeaderTextColor(parseColorFromString(mHeaderTextColor));
    }
    if ("10".equals(mHeader)) {
      inAppMessage.setHeader(HEADER_10);
    } else if ("20".equals(mHeader)) {
      inAppMessage.setHeader(HEADER_20);
    } else if ("30".equals(mHeader)) {
      inAppMessage.setHeader(HEADER_30);
    } else if ("40".equals(mHeader)) {
      inAppMessage.setHeader(HEADER_40);
    } else if ("none".equals(mHeader)) {
      inAppMessage.setHeader(null);
    }
  }

  private void addMessageButtons(IInAppMessageImmersive inAppMessage) {
    // add message buttons.
    if (!SpinnerUtils.SpinnerItemNotSet(mButtons)) {
      ArrayList<MessageButton> messageButtons = new ArrayList<MessageButton>();
      if ("none".equals(mButtons)) {
        inAppMessage.setMessageButtons(null);
      } else if ("one".equals(mButtons)) {
        MessageButton buttonOne = new MessageButton();
        buttonOne.setText("NEWSFEED");
        buttonOne.setBackgroundColor(Color.BLACK);
        buttonOne.setClickAction(ClickAction.NEWS_FEED);
        messageButtons.add(buttonOne);
        inAppMessage.setMessageButtons(messageButtons);
      } else if ("two".equals(mButtons)) {
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
      } else if ("long".equals(mButtons)) {
        MessageButton buttonOne = new MessageButton();
        buttonOne.setText("ACCEPT BUTTON ONE WITH A VERY LONG TITLE");
        buttonOne.setBackgroundColor(Color.BLACK);
        buttonOne.setClickAction(ClickAction.URI, Uri.parse("http://www.appboy.com"));
        inAppMessage.setMessageButtons(messageButtons);
        messageButtons.add(buttonOne);
        MessageButton buttonTwo = new MessageButton();
        buttonTwo.setText("CLOSE BUTTON TWO WITH A VERY LONG TITLE");
        buttonTwo.setBackgroundColor(Color.BLACK);
        buttonTwo.setClickAction(ClickAction.NONE);
        messageButtons.add(buttonTwo);
        inAppMessage.setMessageButtons(messageButtons);
      }
    }
  }

  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    switch (parent.getId()) {
      case R.id.inapp_set_message_type_spinner:
        mMessageType = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_message_type_values);
        break;
      case R.id.inapp_click_action_spinner:
        mClickAction = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_click_action_values);
        break;
      case R.id.inapp_dismiss_type_spinner:
        mDismissType = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_dismiss_type_values);
        break;
      case R.id.inapp_slide_from_spinner:
        mSlideFrom = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_slide_from_values);
        break;
      case R.id.inapp_uri_spinner:
        mUri = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_uri_values);
        break;
      case R.id.inapp_header_spinner:
        mHeader = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_header_values);
        break;
      case R.id.inapp_message_spinner:
        mMessage = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_message_values);
        break;
      case R.id.inapp_background_color_spinner:
        mBackgroundColor = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_color_values);
        break;
      case R.id.inapp_icon_color_spinner:
        mIconColor = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_color_values);
        break;
      case R.id.inapp_icon_background_color_spinner:
        mIconBackgroundColor = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_color_values);
        break;
      case R.id.inapp_close_button_color_spinner:
        mCloseButtonColor = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_color_values);
        break;
      case R.id.inapp_text_color_spinner:
        mTextColor = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_color_values);
        break;
      case R.id.inapp_header_text_color_spinner:
        mHeaderTextColor = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_color_values);
        break;
      case R.id.inapp_icon_spinner:
        mIcon = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_icon_values);
        break;
      case R.id.inapp_image_spinner:
        mImage = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_image_values);
        break;
      case R.id.inapp_button_spinner:
        mButtons = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_button_values);
        break;
      default:
        Log.e(TAG, "Item selected for unknown spinner");
    }
  }

  public void onNothingSelected(AdapterView<?> parent) {
    // Do nothing
  }

  private int parseColorFromString(String colorString) {
    if (colorString.equals("red")) {
      return APPBOY_RED;
    } else if (colorString.equals("orange")) {
      return GOOGLE_ORANGE;
    } else if (colorString.equals("yellow")) {
      return GOOGLE_YELLOW;
    } else if (colorString.equals("green")) {
      return GOOGLE_GREEN;
    } else if (colorString.equals("blue")) {
      return APPBOY_BLUE;
    } else if (colorString.equals("purple")) {
      return GOOGLE_PURPLE;
    } else if (colorString.equals("brown")) {
      return GOOGLE_BROWN;
    } else if (colorString.equals("grey")) {
      return GOOGLE_GREY;
    } else if (colorString.equals("black")) {
      return BLACK;
    } else if (colorString.equals("white")) {
      return WHITE;
    } else {
      return 0;
    }
  }

  /**
   * @return the html body string from the assets folder or null if the read fails.
   */
  private String readHtmlBodyFromAssets() {
    String htmlBody = null;

    // Get the text of the html from the assets folder
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("html_inapp_message_body.txt"), "UTF-8"));
      String line;
      StringBuilder stringBuilder = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line);
      }
      reader.close();
      htmlBody = stringBuilder.toString();
    } catch (IOException e) {
      Log.e(TAG, "Error while reading html body from assets.", e);
    }

    return htmlBody;
  }
}
