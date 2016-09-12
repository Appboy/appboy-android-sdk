package com.appboy.sample;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.enums.inappmessage.ClickAction;
import com.appboy.enums.inappmessage.CropType;
import com.appboy.enums.inappmessage.DismissType;
import com.appboy.enums.inappmessage.ImageStyle;
import com.appboy.enums.inappmessage.Orientation;
import com.appboy.enums.inappmessage.SlideFrom;
import com.appboy.enums.inappmessage.TextAlign;
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
import com.appboy.ui.inappmessage.config.AppboyInAppMessageParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InAppMessageTesterFragment extends Fragment implements AdapterView.OnItemSelectedListener {
  protected static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, InAppMessageTesterFragment.class.getName());

  private enum HtmlMessageType {
    NO_JS, INLINE_JS, EXTERNAL_JS, STAR_WARS
  }

  private static final String CUSTOM_INAPPMESSAGE_VIEW_KEY = "inapmessages_custom_inappmessage_view";
  private static final String CUSTOM_INAPPMESSAGE_MANAGER_LISTENER_KEY = "inappmessages_custom_inappmessage_manager_listener";
  private static final String CUSTOM_APPBOY_NAVIGATOR_KEY = "inappmessages_custom_appboy_navigator";
  private static final String CUSTOM_INAPPMESSAGE_ANIMATION_KEY = "inappmessages_custom_inappmessage_animation";
  private static final String CUSTOM_HTML_INAPPMESSAGE_ACTION_LISTENER_KEY = "inappmessages_custom_appboy_html_inappmessage_action_listener";
  private static final String CUSTOM_INAPPMESSAGE_GRAPHIC_MODAL_MAX_SIZE = "inappmessages_custom_appboy_graphic_modal_max_size";
  private static final String CUSTOM_INAPPMESSAGE_IMAGE_RADIUS = "inappmessages_custom_appboy_image_radius";

  // color reference: http://www.google.com/design/spec/style/color.html
  private static final int APPBOY_RED = 0xFFf33e3e;
  private static final int GOOGLE_ORANGE = 0xFFFF5722;
  private static final int GOOGLE_YELLOW = 0xFFFFEB3B;
  private static final int GOOGLE_GREEN = 0xFF4CAF50;
  private static final int APPBOY_BLUE = 0xFF0073d5;
  private static final int TRANSPARENT_APPBOY_BLUE = 0x220073d5;
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
    spinnerOptionMap.put(R.id.inapp_button_color_spinner, R.array.inapp_color_options);
    spinnerOptionMap.put(R.id.inapp_button_text_color_spinner, R.array.inapp_color_options);
    spinnerOptionMap.put(R.id.inapp_frame_spinner, R.array.inapp_frame_options);
    spinnerOptionMap.put(R.id.inapp_uri_spinner, R.array.inapp_uri_options);
    spinnerOptionMap.put(R.id.inapp_icon_spinner, R.array.inapp_icon_options);
    spinnerOptionMap.put(R.id.inapp_image_spinner, R.array.inapp_image_options);
    spinnerOptionMap.put(R.id.inapp_button_spinner, R.array.inapp_button_options);
    spinnerOptionMap.put(R.id.inapp_orientation_spinner, R.array.inapp_orientation_options);
    spinnerOptionMap.put(R.id.inapp_header_align_spinner, R.array.inapp_align_options);
    spinnerOptionMap.put(R.id.inapp_message_align_spinner, R.array.inapp_align_options);
    spinnerOptionMap.put(R.id.inapp_animate_in_spinner, R.array.inapp_boolean_options);
    spinnerOptionMap.put(R.id.inapp_animate_out_spinner, R.array.inapp_boolean_options);
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
  private String mButtonColor;
  private String mButtonTextColor;
  private String mFrameColor;
  private String mIcon;
  private String mImage;
  private String mButtons;
  private String mOrientation;
  private String mMessageTextAlign;
  private String mHeaderTextAlign;
  private String mAnimateIn;
  private String mAnimateOut;
  private String mHtmlBodyFromAssets;
  private String mHtmlBodyFromAssetsInlineJs;
  private String mHtmlBodyFromAssetsExternalJs;
  private String mHtmlBodyFromAssetsStarWars;

  @Override
  public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
    View view = layoutInflater.inflate(R.layout.inappmessage_tester, container, false);

    for (Integer key : sSpinnerOptionMap.keySet()) {
      SpinnerUtils.setUpSpinner((Spinner) view.findViewById(key), this, sSpinnerOptionMap.get(key));
    }

    CheckBox customInAppMessageViewCheckBox = (CheckBox) view.findViewById(R.id.custom_inappmessage_view_factory_checkbox);
    customInAppMessageViewCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          AppboyInAppMessageManager.getInstance().setCustomInAppMessageViewFactory(new CustomInAppMessageViewFactory());
        } else {
          AppboyInAppMessageManager.getInstance().setCustomInAppMessageViewFactory(null);
        }
        getActivity().getPreferences(getActivity().MODE_PRIVATE).edit().putBoolean(CUSTOM_INAPPMESSAGE_VIEW_KEY, isChecked).apply();
      }
    });
    boolean usingCustomInAppMessageView = getActivity().getPreferences(getActivity().MODE_PRIVATE).getBoolean(CUSTOM_INAPPMESSAGE_VIEW_KEY, false);
    customInAppMessageViewCheckBox.setChecked(usingCustomInAppMessageView);

    CheckBox customInAppMessageManagerListenerCheckBox = (CheckBox) view.findViewById(R.id.custom_inappmessage_manager_listener_checkbox);
    customInAppMessageManagerListenerCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          AppboyInAppMessageManager.getInstance().setCustomInAppMessageManagerListener(new CustomInAppMessageManagerListener(getActivity()));
        } else {
          AppboyInAppMessageManager.getInstance().setCustomInAppMessageManagerListener(null);
        }
        getActivity().getPreferences(getActivity().MODE_PRIVATE).edit().putBoolean(CUSTOM_INAPPMESSAGE_MANAGER_LISTENER_KEY, isChecked).apply();
      }
    });
    boolean usingCustomInAppMessageManagerListener = getActivity().getPreferences(getActivity().MODE_PRIVATE).getBoolean(CUSTOM_INAPPMESSAGE_MANAGER_LISTENER_KEY, false);
    customInAppMessageManagerListenerCheckBox.setChecked(usingCustomInAppMessageManagerListener);

    CheckBox customAppboyNavigatorCheckBox = (CheckBox) view.findViewById(R.id.custom_appboy_navigator_checkbox);
    customAppboyNavigatorCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          Appboy.getInstance(getContext()).setAppboyNavigator(new CustomAppboyNavigator());
        } else {
          Appboy.getInstance(getContext()).setAppboyNavigator(null);
        }
        getActivity().getPreferences(getActivity().MODE_PRIVATE).edit().putBoolean(CUSTOM_APPBOY_NAVIGATOR_KEY, isChecked).apply();
      }
    });
    boolean usingCustomAppboyNavigator = getActivity().getPreferences(getActivity().MODE_PRIVATE).getBoolean(CUSTOM_APPBOY_NAVIGATOR_KEY, false);
    customAppboyNavigatorCheckBox.setChecked(usingCustomAppboyNavigator);

    CheckBox maxSizeCheckBox = (CheckBox) view.findViewById(R.id.custom_appboy_graphic_modal_max_size_checkbox);
    maxSizeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          AppboyInAppMessageParams.setGraphicModalMaxHeightDp(420);
          AppboyInAppMessageParams.setGraphicModalMaxWidthDp(320);
        } else {
          AppboyInAppMessageParams.setGraphicModalMaxHeightDp(AppboyInAppMessageParams.GRAPHIC_MODAL_MAX_HEIGHT_DP);
          AppboyInAppMessageParams.setGraphicModalMaxWidthDp(AppboyInAppMessageParams.GRAPHIC_MODAL_MAX_WIDTH_DP);
        }
        getActivity().getPreferences(getActivity().MODE_PRIVATE).edit().putBoolean(CUSTOM_INAPPMESSAGE_GRAPHIC_MODAL_MAX_SIZE, isChecked).apply();
      }
    });
    boolean maxSizedChecked = getActivity().getPreferences(getActivity().MODE_PRIVATE).getBoolean(CUSTOM_INAPPMESSAGE_GRAPHIC_MODAL_MAX_SIZE, false);
    maxSizeCheckBox.setChecked(maxSizedChecked);

    CheckBox imageRadiusCheckBox = (CheckBox) view.findViewById(R.id.custom_appboy_image_radius_checkbox);
    imageRadiusCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          AppboyInAppMessageParams.setModalizedImageRadiusDp(0);
        } else {
          AppboyInAppMessageParams.setModalizedImageRadiusDp(AppboyInAppMessageParams.MODALIZED_IMAGE_RADIUS_DP);
        }
        getActivity().getPreferences(getActivity().MODE_PRIVATE).edit().putBoolean(CUSTOM_INAPPMESSAGE_IMAGE_RADIUS, isChecked).apply();
      }
    });
    boolean customRadiusChecked = getActivity().getPreferences(getActivity().MODE_PRIVATE).getBoolean(CUSTOM_INAPPMESSAGE_IMAGE_RADIUS, false);
    imageRadiusCheckBox.setChecked(customRadiusChecked);

    Button createAndAddInAppMessageButton = (Button) view.findViewById(R.id.create_and_add_inappmessage_button);
    createAndAddInAppMessageButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (getActivity().getPreferences(getActivity().MODE_PRIVATE).getBoolean(CUSTOM_INAPPMESSAGE_VIEW_KEY, false)) {
          // current custom in-app message view is an implementation of a base in-app message.
          addInAppMessage(new CustomInAppMessage());
        } else {
          if ("slideup".equals(mMessageType)) {
            addInAppMessage(new InAppMessageSlideup());
          } else if ("modal".equals(mMessageType)) {
            addInAppMessage(new InAppMessageModal());
          } else if ("modal_graphic".equals(mMessageType)) {
            InAppMessageModal inAppMessageModal = new InAppMessageModal();
            inAppMessageModal.setImageStyle(ImageStyle.GRAPHIC);
            // graphic modals must be center cropped, the default for newly constructed modals
            // is center_fit
            inAppMessageModal.setCropType(CropType.CENTER_CROP);
            addInAppMessage(inAppMessageModal);
          } else if ("full".equals(mMessageType)) {
            addInAppMessage(new InAppMessageFull());
          } else if ("full_graphic".equals(mMessageType)) {
            InAppMessageFull inAppMessageFull = new InAppMessageFull();
            inAppMessageFull.setImageStyle(ImageStyle.GRAPHIC);
            addInAppMessage(inAppMessageFull);
          } else if ("html_full_no_js".equals(mMessageType)) {
            addInAppMessage(new InAppMessageHtmlFull(), HtmlMessageType.NO_JS);
          } else if ("html_full_inline_js".equals(mMessageType)) {
            addInAppMessage(new InAppMessageHtmlFull(), HtmlMessageType.INLINE_JS);
          } else if ("html_full_external_js".equals(mMessageType)) {
            addInAppMessage(new InAppMessageHtmlFull(), HtmlMessageType.EXTERNAL_JS);
          } else if ("html_full_star_wars".equals(mMessageType)) {
            addInAppMessage(new InAppMessageHtmlFull(), HtmlMessageType.STAR_WARS);
          } else {
            addInAppMessage(new InAppMessageSlideup());
          }
        }
      }
    });
    CheckBox customInAppMessageAnimationCheckBox = (CheckBox) view.findViewById(R.id.custom_appboy_animation_checkbox);
    customInAppMessageAnimationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          AppboyInAppMessageManager.getInstance().setCustomInAppMessageAnimationFactory(new CustomInAppMessageAnimationFactory());
        } else {
          AppboyInAppMessageManager.getInstance().setCustomInAppMessageAnimationFactory(null);
        }
        getActivity().getPreferences(getActivity().MODE_PRIVATE).edit().putBoolean(CUSTOM_INAPPMESSAGE_ANIMATION_KEY, isChecked).apply();
      }
    });
    boolean usingCustomInAppAnimation = getActivity().getPreferences(getActivity().MODE_PRIVATE).getBoolean(CUSTOM_INAPPMESSAGE_ANIMATION_KEY, false);
    customInAppMessageAnimationCheckBox.setChecked(usingCustomInAppAnimation);

    CheckBox customHtmlInAppMessageActionListenerCheckBox = (CheckBox) view.findViewById(R.id.custom_appboy_html_inappmessage_action_listener_checkbox);
    customHtmlInAppMessageActionListenerCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          AppboyInAppMessageManager.getInstance().setCustomHtmlInAppMessageActionListener(new CustomHtmlInAppMessageActionListener(getContext()));
        } else {
          AppboyInAppMessageManager.getInstance().setCustomHtmlInAppMessageActionListener(null);
        }
        getActivity().getPreferences(getActivity().MODE_PRIVATE).edit().putBoolean(CUSTOM_HTML_INAPPMESSAGE_ACTION_LISTENER_KEY, isChecked).apply();
      }
    });
    boolean usingCustomHtmlInAppActionListener = getActivity().getPreferences(getActivity().MODE_PRIVATE).getBoolean(CUSTOM_HTML_INAPPMESSAGE_ACTION_LISTENER_KEY, false);
    customHtmlInAppMessageActionListenerCheckBox.setChecked(usingCustomHtmlInAppActionListener);

    Button displayNextInAppMessageButton = (Button) view.findViewById(R.id.display_next_inappmessage_button);
    displayNextInAppMessageButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        AppboyInAppMessageManager.getInstance().requestDisplayInAppMessage();
      }
    });

    Button requestInAppMessageFromServerButton = (Button) view.findViewById(R.id.request_inappmessage_from_server_button);
    requestInAppMessageFromServerButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Appboy.getInstance(getContext()).requestInAppMessageRefresh();
      }
    });

    Button hideCurrentInAppMessageButton = (Button) view.findViewById(R.id.hide_current_inappmessage_button);
    hideCurrentInAppMessageButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        AppboyInAppMessageManager.getInstance().hideCurrentlyDisplayingInAppMessage(false);
      }
    });
    return view;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mHtmlBodyFromAssets = readHtmlBodyFromAssets(HtmlMessageType.NO_JS);
    mHtmlBodyFromAssetsInlineJs = readHtmlBodyFromAssets(HtmlMessageType.INLINE_JS);
    mHtmlBodyFromAssetsExternalJs = readHtmlBodyFromAssets(HtmlMessageType.EXTERNAL_JS);
    mHtmlBodyFromAssetsStarWars = readHtmlBodyFromAssets(HtmlMessageType.STAR_WARS);
  }

  @SuppressWarnings("checkstyle:avoidescapedunicodecharacters")
  private void addInAppMessageImmersive(IInAppMessageImmersive inAppMessage) {
    if (inAppMessage instanceof InAppMessageModal) {
      inAppMessage.setMessage("Welcome to Appboy! Appboy is Marketing Automation for Apps!");
      if (inAppMessage.getImageStyle().equals(ImageStyle.GRAPHIC)) {
        inAppMessage.setRemoteImageUrl(getResources().getString(R.string.appboy_image_url_1000w_1000h));
      } else {
        inAppMessage.setRemoteImageUrl(getResources().getString(R.string.appboy_image_url_1160w_400h));
      }
    } else if (inAppMessage instanceof InAppMessageFull) {
      inAppMessage.setMessage("Welcome to Appboy! Appboy is Marketing Automation for Apps. This is an example of a full in-app message.");
      if (inAppMessage.getImageStyle().equals(ImageStyle.GRAPHIC)) {
        if (inAppMessage.getOrientation().equals(Orientation.LANDSCAPE)) {
          inAppMessage.setRemoteImageUrl(getResources().getString(R.string.appboy_image_url_1600w_1000h));
        } else {
          inAppMessage.setRemoteImageUrl(getResources().getString(R.string.appboy_image_url_1000w_1600h));
        }
      } else {
        if (inAppMessage.getOrientation().equals(Orientation.LANDSCAPE)) {
          inAppMessage.setRemoteImageUrl(getResources().getString(R.string.appboy_image_url_1600w_500h));
        } else {
          inAppMessage.setRemoteImageUrl(getResources().getString(R.string.appboy_image_url_1000w_800h));
        }
      }
    }
    inAppMessage.setHeader("Hello from Appboy!");
    ArrayList<MessageButton> messageButtons = new ArrayList<MessageButton>();
    MessageButton buttonOne = new MessageButton();
    buttonOne.setText("NEWSFEED");
    buttonOne.setClickAction(ClickAction.NEWS_FEED);
    messageButtons.add(buttonOne);
    inAppMessage.setMessageButtons(messageButtons);
    addMessageButtons(inAppMessage);
    setHeader(inAppMessage);
    setCloseButtonColor(inAppMessage);
    setFrameColor(inAppMessage);
    setHeaderTextAlign(inAppMessage);
  }

  @SuppressWarnings("checkstyle:avoidescapedunicodecharacters")
  private void addInAppMessageSlideup(InAppMessageSlideup inAppMessage) {
    inAppMessage.setMessage("Welcome to Appboy! This is a slideup in-app message.");
    inAppMessage.setIcon("\uf091");
    inAppMessage.setClickAction(ClickAction.NEWS_FEED);
    setSlideFrom(inAppMessage);
    setChevronColor(inAppMessage);
  }

  @SuppressWarnings("checkstyle:avoidescapedunicodecharacters")
  private void addInAppMessageCustom(IInAppMessage inAppMessage) {
    inAppMessage.setMessage("Welcome to Appboy! This is a custom in-app message.");
    inAppMessage.setIcon("\uf091");
  }

  private void addInAppMessageHtmlFull(IInAppMessageHtml inAppMessage, HtmlMessageType jsType) {
    switch (jsType) {
      case NO_JS:
        inAppMessage.setMessage(mHtmlBodyFromAssets);
        inAppMessage.setAssetsZipRemoteUrl(getString(R.string.html_assets_no_js_remote_url));
        break;
      case INLINE_JS:
        inAppMessage.setMessage(mHtmlBodyFromAssetsInlineJs);
        break;
      case EXTERNAL_JS:
        inAppMessage.setMessage(mHtmlBodyFromAssetsExternalJs);
        inAppMessage.setAssetsZipRemoteUrl(getString(R.string.html_assets_external_js_remote_url));
        break;
      case STAR_WARS:
        inAppMessage.setMessage(mHtmlBodyFromAssetsStarWars);
        break;
      default:
        break;
    }
  }

  private void addInAppMessage(IInAppMessage inAppMessage) {
    addInAppMessage(inAppMessage, null);
  }

  private void addInAppMessage(IInAppMessage inAppMessage, HtmlMessageType messageType) {
    // set orientation early to help determine which default image to use
    setOrientation(inAppMessage);
    if (inAppMessage instanceof IInAppMessageImmersive) {
      addInAppMessageImmersive((IInAppMessageImmersive) inAppMessage);
    } else if (inAppMessage instanceof InAppMessageSlideup) {
      addInAppMessageSlideup((InAppMessageSlideup) inAppMessage);
    } else if (inAppMessage instanceof InAppMessageHtmlFull) {
      addInAppMessageHtmlFull((InAppMessageHtmlFull) inAppMessage, messageType);
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
    setMessageTextAlign(inAppMessage);
    setAnimation(inAppMessage);
    AppboyInAppMessageManager.getInstance().addInAppMessage(inAppMessage);
  }

  private void setAnimation(IInAppMessage inAppMessage) {
    if (!SpinnerUtils.spinnerItemNotSet(mAnimateIn)) {
      if (mAnimateIn.equals("true")) {
        inAppMessage.setAnimateIn(true);
      } else if (mAnimateIn.equals("false")) {
        inAppMessage.setAnimateIn(false);
      }
    }
    if (!SpinnerUtils.spinnerItemNotSet(mAnimateOut)) {
      if (mAnimateOut.equals("true")) {
        inAppMessage.setAnimateOut(true);
      } else if (mAnimateOut.equals("false")) {
        inAppMessage.setAnimateOut(false);
      }
    }
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
    if (!SpinnerUtils.spinnerItemNotSet(mBackgroundColor)) {
      inAppMessage.setBackgroundColor(parseColorFromString(mBackgroundColor));
    }
  }

  private void setChevronColor(InAppMessageSlideup inAppMessage) {
    // set chevron color if defined
    if (!SpinnerUtils.spinnerItemNotSet(mCloseButtonColor)) {
      inAppMessage.setChevronColor(parseColorFromString(mCloseButtonColor));
    }
  }

  private void setCloseButtonColor(IInAppMessageImmersive inAppMessage) {
    // set close button color if defined
    if (!SpinnerUtils.spinnerItemNotSet(mCloseButtonColor)) {
      inAppMessage.setCloseButtonColor(parseColorFromString(mCloseButtonColor));
    }
  }

  private void setMessage(IInAppMessage inAppMessage) {
    // set text color if defined
    if (!SpinnerUtils.spinnerItemNotSet(mTextColor)) {
      inAppMessage.setMessageTextColor(parseColorFromString(mTextColor));
    }
    // don't replace message on html IAMs
    if (inAppMessage instanceof IInAppMessageHtml) {
      return;
    }
    if (!SpinnerUtils.spinnerItemNotSet(mMessage)) {
      inAppMessage.setMessage(mMessage);
    }
  }

  private void setIcon(IInAppMessage inAppMessage) {
    // set icon color if defined
    if (!SpinnerUtils.spinnerItemNotSet(mIconColor)) {
      inAppMessage.setIconColor(parseColorFromString(mIconColor));
    }
    // set icon background color if defined
    if (!SpinnerUtils.spinnerItemNotSet(mIconBackgroundColor)) {
      inAppMessage.setIconBackgroundColor(parseColorFromString(mIconBackgroundColor));
    }
    // set in-app message icon
    if (!SpinnerUtils.spinnerItemNotSet(mIcon)) {
      if (mIcon.equals(getString(R.string.none))) {
        inAppMessage.setIcon(null);
      } else {
        inAppMessage.setIcon(mIcon);
      }
    }
  }

  private void setImage(IInAppMessage inAppMessage) {
    // set in-app message image url
    if (!SpinnerUtils.spinnerItemNotSet(mImage)) {
      if (mImage.equals(getString(R.string.none))) {
        inAppMessage.setRemoteImageUrl(null);
      } else {
        inAppMessage.setRemoteImageUrl(mImage);
      }
    }
  }

  private void setOrientation(IInAppMessage inAppMessage) {
    // set in-app message preferred orientation
    if (!SpinnerUtils.spinnerItemNotSet(mOrientation)) {
      if (mOrientation.equals("any")) {
        inAppMessage.setOrientation(Orientation.ANY);
      } else if (mOrientation.equals("portrait")) {
        inAppMessage.setOrientation(Orientation.PORTRAIT);
      } else if (mOrientation.equals("landscape")) {
        inAppMessage.setOrientation(Orientation.LANDSCAPE);
      }
    }
  }

  private boolean addClickAction(IInAppMessage inAppMessage) {
    // set click action if defined
    if ("newsfeed".equals(mClickAction)) {
      inAppMessage.setClickAction(ClickAction.NEWS_FEED);
    } else if ("uri".equals(mClickAction)) {
      if (SpinnerUtils.spinnerItemNotSet(mUri)) {
        Toast.makeText(getContext(), "Please choose a URI.", Toast.LENGTH_LONG).show();
        return false;
      } else {
        inAppMessage.setClickAction(ClickAction.URI, Uri.parse(mUri));
      }
    } else if (getString(R.string.none).equals(mClickAction)) {
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
    if (!SpinnerUtils.spinnerItemNotSet(mHeaderTextColor)) {
      inAppMessage.setHeaderTextColor(parseColorFromString(mHeaderTextColor));
    }
    if (!SpinnerUtils.spinnerItemNotSet(mHeader)) {
      if (getString(R.string.none).equals(mHeader)) {
        inAppMessage.setHeader(null);
      } else {
        inAppMessage.setHeader(mHeader);
      }
    }
  }

  private void setFrameColor(IInAppMessageImmersive inAppMessage) {
    if (!SpinnerUtils.spinnerItemNotSet(mFrameColor)) {
      inAppMessage.setFrameColor(parseColorFromString(mFrameColor));
    }
  }

  private void setHeaderTextAlign(IInAppMessageImmersive inAppMessage) {
    if (!SpinnerUtils.spinnerItemNotSet(mHeaderTextAlign)) {
      inAppMessage.setHeaderTextAlign(parseTextAlign(mHeaderTextAlign));
    }
  }

  private void setMessageTextAlign(IInAppMessage inAppMessage) {
    if (!SpinnerUtils.spinnerItemNotSet(mMessageTextAlign)) {
      inAppMessage.setMessageTextAlign(parseTextAlign(mMessageTextAlign));
    }
  }

  private TextAlign parseTextAlign(String textAlign) {
    if (textAlign.equals("start")) {
      return TextAlign.START;
    } else if (textAlign.equals("end")) {
      return TextAlign.END;
    } else if (textAlign.equals("center")) {
      return TextAlign.CENTER;
    }
    return null;
  }

  private void addMessageButtons(IInAppMessageImmersive inAppMessage) {
    // add message buttons.
    if (!SpinnerUtils.spinnerItemNotSet(mButtons)) {
      if (getString(R.string.none).equals(mButtons)) {
        inAppMessage.setMessageButtons(null);
        return;
      }
      ArrayList<MessageButton> messageButtons = new ArrayList<MessageButton>();
      MessageButton buttonOne = new MessageButton();
      if ("one".equals(mButtons)) {
        buttonOne.setClickAction(ClickAction.NEWS_FEED);
        buttonOne.setText("NEWSFEED");
        messageButtons.add(buttonOne);
        inAppMessage.setMessageButtons(messageButtons);
        return;
      }
      MessageButton buttonTwo = new MessageButton();
      if ("two".equals(mButtons) || "long".equals(mButtons)) {
        buttonOne.setText("ACCEPT");
        buttonOne.setClickAction(ClickAction.URI, Uri.parse(getResources().getString(R.string.appboy_homepage_url)));
        buttonTwo.setText("CLOSE");
        buttonTwo.setClickAction(ClickAction.NONE);
        if ("long".equals(mButtons)) {
          buttonOne.setText("ACCEPT WITH A VERY LONG TITLE");
          buttonTwo.setText("CLOSE WITH A VERY LONG TITLE");
        }
      } else if ("deeplink".equals(mButtons)) {
        buttonOne.setText("TELEPHONE");
        buttonOne.setClickAction(ClickAction.URI, Uri.parse(getResources().getString(R.string.telephone_uri)));
        buttonTwo.setText("PLAY STORE");
        buttonTwo.setClickAction(ClickAction.URI, Uri.parse(getResources().getString(R.string.play_store_uri)));
      }
      messageButtons.add(buttonOne);
      messageButtons.add(buttonTwo);
      inAppMessage.setMessageButtons(messageButtons);
    }
    if (!SpinnerUtils.spinnerItemNotSet(mButtonColor) && inAppMessage.getMessageButtons() != null) {
      for (MessageButton button : inAppMessage.getMessageButtons()) {
        button.setBackgroundColor(parseColorFromString(mButtonColor));
      }
    }
    if (!SpinnerUtils.spinnerItemNotSet(mButtonTextColor) && inAppMessage.getMessageButtons() != null) {
      for (MessageButton button : inAppMessage.getMessageButtons()) {
        button.setTextColor(parseColorFromString(mButtonTextColor));
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
      case R.id.inapp_button_color_spinner:
        mButtonColor = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_color_values);
        break;
      case R.id.inapp_button_text_color_spinner:
        mButtonTextColor = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_color_values);
        break;
      case R.id.inapp_frame_spinner:
        mFrameColor = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_frame_values);
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
      case R.id.inapp_orientation_spinner:
        mOrientation = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_orientation_values);
        break;
      case R.id.inapp_header_align_spinner:
        mHeaderTextAlign = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_align_values);
        break;
      case R.id.inapp_message_align_spinner:
        mMessageTextAlign = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_align_values);
        break;
      case R.id.inapp_animate_in_spinner:
        mAnimateIn = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_boolean_values);
        break;
      case R.id.inapp_animate_out_spinner:
        mAnimateOut = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_boolean_values);
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
    } else if (colorString.equals("transparent")) {
      return 0;
    } else if (colorString.equals("almost_transparent_blue")) {
      return TRANSPARENT_APPBOY_BLUE;
    } else {
      return 0;
    }
  }

  /**
   * @return the html body string from the assets folder or null if the read fails.
   */
  private String readHtmlBodyFromAssets(HtmlMessageType jsType) {
    return readHtmlBodyFromAssetsWithFileName(jsType);
  }

  private String readHtmlBodyFromAssetsWithFileName(HtmlMessageType jsType) {
    String htmlBody = null;
    String filename = "html_inapp_message_body_no_js.html";
    switch (jsType) {
      case INLINE_JS:
        filename = "html_inapp_message_body_inline_js.html";
        break;
      case EXTERNAL_JS:
        filename = "html_inapp_message_body_external_js.html";
        break;
      case STAR_WARS:
        filename = "html_inapp_message_body_star_wars.html";
        break;
      default:
        break;
    }

    // Get the text of the html from the assets folder
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(getActivity()
          .getAssets().open(filename), "UTF-8"));
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
