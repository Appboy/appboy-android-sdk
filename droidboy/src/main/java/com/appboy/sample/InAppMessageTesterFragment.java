package com.appboy.sample;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.appboy.Appboy;
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
import com.appboy.models.IInAppMessageWithImage;
import com.appboy.models.IInAppMessageZippedAssetHtml;
import com.appboy.models.InAppMessageFull;
import com.appboy.models.InAppMessageHtml;
import com.appboy.models.InAppMessageHtmlFull;
import com.appboy.models.InAppMessageModal;
import com.appboy.models.InAppMessageSlideup;
import com.appboy.models.MessageButton;
import com.appboy.sample.util.SpinnerUtils;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.AppboyNavigator;
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
  protected static final String TAG = AppboyLogger.getBrazeLogTag(InAppMessageTesterFragment.class);

  private enum HtmlMessageType {
    NO_JS("html_inapp_message_body_no_js.html",
        "https://appboy-staging-dashboard-uploads.s3.amazonaws.com/zip_uploads/files/585c1776bf5cea3cbe1b36b2/124fae83d6ba4023d4ede28e9177980e6373747c/original.zip?1482430326"),
    INLINE_JS("html_inapp_message_body_inline_js.html", null),
    EXTERNAL_JS("html_inapp_message_body_external_js.html",
        "https://appboy-staging-dashboard-uploads.s3.amazonaws.com/zip_uploads/files/585c18c3bf5cea3c861b36ba/b0c7e536230b34ef800c8e0ef0747eaac53545a5/original.zip?1482430659"),
    STAR_WARS("html_inapp_message_body_star_wars.html", null),
    YOUTUBE("html_inapp_message_body_youtube_iframe.html", null),
    BRIDGE_TESTER("html_in_app_message_bridge_tester.html",
        "https://appboy-images.com/HTML_ZIP_STOPWATCH.zip"),
    SLOW_LOADING("html_inapp_message_delayed_open.html", null),
    DARK_MODE("html_inapp_message_dark_mode.html", null),
    UNIFIED_HTML_BOOTSTRAP_ALBUM("html_in_app_message_unified_bootstrap_album.html", null),
    SHARK_HTML("html_shark_unified.html", null);

    private final String mFileName;
    private final String mZipUri;

    HtmlMessageType(String filename, String zipUri) {
      mFileName = filename;
      mZipUri = zipUri;
    }

    @NonNull
    public String getFileName() {
      return mFileName;
    }

    @Nullable
    public String getZippedAssetUrl() {
      return mZipUri;
    }
  }

  private static final String CUSTOM_INAPPMESSAGE_VIEW_KEY = "inapmessages_custom_inappmessage_view";

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
    Map<Integer, Integer> spinnerOptionMap = new HashMap<>();
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
    spinnerOptionMap.put(R.id.inapp_button_border_color_spinner, R.array.inapp_color_options);
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
    spinnerOptionMap.put(R.id.inapp_open_uri_in_webview_spinner, R.array.inapp_boolean_options);
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
  private String mButtonBorderColor;
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
  private String mUseWebview;

  @Override
  public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
    View view = layoutInflater.inflate(R.layout.inappmessage_tester, container, false);

    for (Integer key : sSpinnerOptionMap.keySet()) {
      SpinnerUtils.setUpSpinner(view.findViewById(key), this, sSpinnerOptionMap.get(key));
    }

    setupCheckbox(view.findViewById(R.id.custom_inappmessage_view_factory_checkbox),
        (buttonView, isChecked) -> {
          if (isChecked) {
            AppboyInAppMessageManager.getInstance().setCustomInAppMessageViewFactory(new CustomInAppMessageViewFactory());
          } else {
            AppboyInAppMessageManager.getInstance().setCustomInAppMessageViewFactory(null);
          }
        });

    setupCheckbox(view.findViewById(R.id.custom_inappmessage_manager_listener_checkbox),
        (buttonView, isChecked) -> {
          if (isChecked) {
            AppboyInAppMessageManager.getInstance().setCustomInAppMessageManagerListener(new CustomInAppMessageManagerListener(getActivity()));
          } else {
            AppboyInAppMessageManager.getInstance().setCustomInAppMessageManagerListener(null);
          }
        });

    setupCheckbox(view.findViewById(R.id.custom_appboy_navigator_checkbox),
        (buttonView, isChecked) -> {
          if (isChecked) {
            AppboyNavigator.setAppboyNavigator(new CustomAppboyNavigator());
          } else {
            AppboyNavigator.setAppboyNavigator(null);
          }
        });

    setupCheckbox(view.findViewById(R.id.custom_appboy_graphic_modal_max_size_checkbox),
        (buttonView, isChecked) -> {
          if (isChecked) {
            AppboyInAppMessageParams.setGraphicModalMaxHeightDp(420);
            AppboyInAppMessageParams.setGraphicModalMaxWidthDp(320);
          } else {
            AppboyInAppMessageParams.setGraphicModalMaxHeightDp(AppboyInAppMessageParams.GRAPHIC_MODAL_MAX_HEIGHT_DP);
            AppboyInAppMessageParams.setGraphicModalMaxWidthDp(AppboyInAppMessageParams.GRAPHIC_MODAL_MAX_WIDTH_DP);
          }
        });

    setupCheckbox(view.findViewById(R.id.custom_appboy_image_radius_checkbox),
        (buttonView, isChecked) -> {
          if (isChecked) {
            AppboyInAppMessageParams.setModalizedImageRadiusDp(0);
          } else {
            AppboyInAppMessageParams.setModalizedImageRadiusDp(AppboyInAppMessageParams.MODALIZED_IMAGE_RADIUS_DP);
          }
        });

    setupCheckbox(view.findViewById(R.id.disable_back_button_dismiss_behavior),
        (buttonView, isChecked) -> {
          if (isChecked) {
            AppboyInAppMessageManager.getInstance().setBackButtonDismissesInAppMessageView(false);
          } else {
            AppboyInAppMessageManager.getInstance().setBackButtonDismissesInAppMessageView(true);
          }
        });

    setupCheckbox(view.findViewById(R.id.enable_tap_outside_modal_dismiss_behavior),
        (buttonView, isChecked) -> {
          if (isChecked) {
            AppboyInAppMessageManager.getInstance().setClickOutsideModalViewDismissInAppMessageView(true);
          } else {
            AppboyInAppMessageManager.getInstance().setClickOutsideModalViewDismissInAppMessageView(false);
          }
        });

    setupCheckbox(view.findViewById(R.id.custom_appboy_animation_checkbox),
        (buttonView, isChecked) -> {
          if (isChecked) {
            AppboyInAppMessageManager.getInstance().setCustomInAppMessageAnimationFactory(new CustomInAppMessageAnimationFactory());
          } else {
            AppboyInAppMessageManager.getInstance().setCustomInAppMessageAnimationFactory(null);
          }
        });

    setupCheckbox(view.findViewById(R.id.custom_appboy_html_inappmessage_action_listener_checkbox),
        (buttonView, isChecked) -> {
          if (isChecked) {
            AppboyInAppMessageManager.getInstance().setCustomHtmlInAppMessageActionListener(new CustomHtmlInAppMessageActionListener(getContext()));
          } else {
            AppboyInAppMessageManager.getInstance().setCustomHtmlInAppMessageActionListener(null);
          }
        });

    Button createAndAddInAppMessageButton = view.findViewById(R.id.create_and_add_inappmessage_button);
    createAndAddInAppMessageButton.setOnClickListener(view1 -> {
      if (getSettingsPreferences().getBoolean(CUSTOM_INAPPMESSAGE_VIEW_KEY, false)) {
        addInAppMessage(new CustomInAppMessage());
      } else {
        switch (mMessageType) {
          case "modal":
            addInAppMessage(new InAppMessageModal());
            break;
          case "modal_graphic":
            InAppMessageModal inAppMessageModal = new InAppMessageModal();
            inAppMessageModal.setImageStyle(ImageStyle.GRAPHIC);
            // graphic modals must be center cropped, the default for newly constructed modals
            // is center_fit
            inAppMessageModal.setCropType(CropType.CENTER_CROP);
            addInAppMessage(inAppMessageModal);
            break;
          case "full":
            addInAppMessage(new InAppMessageFull());
            break;
          case "full_graphic":
            InAppMessageFull inAppMessageFull = new InAppMessageFull();
            inAppMessageFull.setImageStyle(ImageStyle.GRAPHIC);
            addInAppMessage(inAppMessageFull);
            break;
          case "html_full_no_js":
            addInAppMessage(new InAppMessageHtmlFull(), HtmlMessageType.NO_JS);
            break;
          case "html_full_inline_js":
            addInAppMessage(new InAppMessageHtmlFull(), HtmlMessageType.INLINE_JS);
            break;
          case "html_full_external_js":
            addInAppMessage(new InAppMessageHtmlFull(), HtmlMessageType.EXTERNAL_JS);
            break;
          case "html_full_star_wars":
            addInAppMessage(new InAppMessageHtmlFull(), HtmlMessageType.STAR_WARS);
            break;
          case "html_full_youtube":
            addInAppMessage(new InAppMessageHtmlFull(), HtmlMessageType.YOUTUBE);
            break;
          case "html_full_bridge_tester":
            addInAppMessage(new InAppMessageHtmlFull(), HtmlMessageType.BRIDGE_TESTER);
            break;
          case "html_full_slow_loading":
            addInAppMessage(new InAppMessageHtmlFull(), HtmlMessageType.SLOW_LOADING);
            break;
          case "html_full_unified_bootstrap":
            addInAppMessage(new InAppMessageHtml(), HtmlMessageType.UNIFIED_HTML_BOOTSTRAP_ALBUM);
            break;
          case "html_shark_unified":
            addInAppMessage(new InAppMessageHtml(), HtmlMessageType.SHARK_HTML);
            break;
          case "html_full_dark_mode":
            addInAppMessage(new InAppMessageHtmlFull(), HtmlMessageType.DARK_MODE);
            break;
          case "modal_dark_theme":
            final String darkModeJson = getStringFromAssets(getContext(), "modal_inapp_message_with_dark_theme.json");
            addInAppMessageFromString(darkModeJson);
            break;
          case "slideup":
          default:
            addInAppMessage(new InAppMessageSlideup());
            break;
        }
      }
    });

    Button displayNextInAppMessageButton = view.findViewById(R.id.display_next_inappmessage_button);
    displayNextInAppMessageButton.setOnClickListener(v -> AppboyInAppMessageManager.getInstance().requestDisplayInAppMessage());

    Button hideCurrentInAppMessageButton = view.findViewById(R.id.hide_current_inappmessage_button);
    hideCurrentInAppMessageButton.setOnClickListener(v -> AppboyInAppMessageManager.getInstance().hideCurrentlyDisplayingInAppMessage(false));
    return view;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @SuppressWarnings("checkstyle:avoidescapedunicodecharacters")
  private void addInAppMessageImmersive(IInAppMessageImmersive inAppMessage) {
    if (inAppMessage instanceof InAppMessageModal) {
      InAppMessageModal inAppMessageModal = (InAppMessageModal) inAppMessage;

      inAppMessageModal.setMessage("Welcome to Braze! Braze is Marketing Automation for Apps!");
      if (inAppMessageModal.getImageStyle().equals(ImageStyle.GRAPHIC)) {
        inAppMessageModal.setRemoteImageUrl(getResources().getString(R.string.appboy_image_url_1000w_1000h));
      } else {
        inAppMessageModal.setRemoteImageUrl(getResources().getString(R.string.appboy_image_url_1160w_400h));
      }
    } else if (inAppMessage instanceof InAppMessageFull) {
      InAppMessageFull inAppMessageFull = (InAppMessageFull) inAppMessage;

      inAppMessageFull.setMessage("Welcome to Braze! Braze is Marketing Automation for Apps. This is an example of a full in-app message.");
      if (inAppMessageFull.getImageStyle().equals(ImageStyle.GRAPHIC)) {
        if (inAppMessageFull.getOrientation().equals(Orientation.LANDSCAPE)) {
          inAppMessageFull.setRemoteImageUrl(getResources().getString(R.string.appboy_image_url_1600w_1000h));
        } else {
          inAppMessageFull.setRemoteImageUrl(getResources().getString(R.string.appboy_image_url_1000w_1600h));
        }
      } else {
        if (inAppMessageFull.getOrientation().equals(Orientation.LANDSCAPE)) {
          inAppMessageFull.setRemoteImageUrl(getResources().getString(R.string.appboy_image_url_1600w_500h));
        } else {
          inAppMessageFull.setRemoteImageUrl(getResources().getString(R.string.appboy_image_url_1000w_800h));
        }
      }
    }
    inAppMessage.setHeader("Hello from Braze!");
    ArrayList<MessageButton> messageButtons = new ArrayList<>();
    MessageButton buttonOne = new MessageButton();
    buttonOne.setText("NewsFeed");
    buttonOne.setClickAction(ClickAction.NEWS_FEED);
    messageButtons.add(buttonOne);
    inAppMessage.setMessageButtons(messageButtons);
    addMessageButtons(inAppMessage);
    setHeader(inAppMessage);
    setCloseButtonColor(inAppMessage);
    setFrameColor(inAppMessage);
    setHeaderTextAlign(inAppMessage);
  }

  /**
   * Adds an {@link IInAppMessage} from its {@link IInAppMessage#forJsonPut()} form.
   */
  private void addInAppMessageFromString(String serializedInAppMessage) {
    final IInAppMessage inAppMessage = Appboy.getInstance(getContext()).deserializeInAppMessageString(serializedInAppMessage);
    AppboyInAppMessageManager.getInstance().addInAppMessage(inAppMessage);
  }

  @SuppressWarnings("checkstyle:avoidescapedunicodecharacters")
  private void addInAppMessageSlideup(InAppMessageSlideup inAppMessage) {
    inAppMessage.setMessage("Welcome to Braze! This is a slideup in-app message.");
    inAppMessage.setIcon("\uf091");
    inAppMessage.setClickAction(ClickAction.NEWS_FEED);
    setSlideFrom(inAppMessage);
    setChevronColor(inAppMessage);
  }

  @SuppressWarnings("checkstyle:avoidescapedunicodecharacters")
  private void addInAppMessageCustom(IInAppMessage inAppMessage) {
    inAppMessage.setMessage("Welcome to Braze! This is a custom in-app message.");
    inAppMessage.setIcon("\uf091");
  }

  private void addInAppMessageHtml(IInAppMessageHtml inAppMessage, HtmlMessageType htmlMessageType) {
    inAppMessage.setMessage(getStringFromAssets(getContext(), htmlMessageType.getFileName()));
    if (htmlMessageType.getZippedAssetUrl() != null && inAppMessage instanceof IInAppMessageZippedAssetHtml) {
      ((IInAppMessageZippedAssetHtml) inAppMessage).setAssetsZipRemoteUrl(htmlMessageType.getZippedAssetUrl());
    }
  }

  private void addInAppMessage(IInAppMessage inAppMessage) {
    addInAppMessage(inAppMessage, null);
  }

  private void addInAppMessage(IInAppMessage inAppMessage, HtmlMessageType messageType) {
    // set orientation early to help determine which default image to use
    setOrientation(inAppMessage);
    switch (inAppMessage.getMessageType()) {
      case SLIDEUP:
        addInAppMessageSlideup((InAppMessageSlideup) inAppMessage);
        break;
      case MODAL:
      case FULL:
        addInAppMessageImmersive((IInAppMessageImmersive) inAppMessage);
        break;
      case HTML:
      case HTML_FULL:
        addInAppMessageHtml((IInAppMessageHtml) inAppMessage, messageType);
        break;
      default:
        addInAppMessageCustom(inAppMessage);
        break;
    }
    if (!addClickAction(inAppMessage)) {
      return;
    }
    setDismissType(inAppMessage);
    setBackgroundColor(inAppMessage);
    setMessage(inAppMessage);
    setIcon(inAppMessage);
    if (inAppMessage instanceof IInAppMessageWithImage) {
      setImage((IInAppMessageWithImage) inAppMessage);
    }
    setMessageTextAlign(inAppMessage);
    setAnimation(inAppMessage);
    setUseWebview(inAppMessage);
    AppboyInAppMessageManager.getInstance().addInAppMessage(inAppMessage);
  }

  private void setUseWebview(IInAppMessage inAppMessage) {
    if (!SpinnerUtils.spinnerItemNotSet(mUseWebview)) {
      if (mUseWebview.equals("true")) {
        inAppMessage.setOpenUriInWebView(true);
      } else if (mUseWebview.equals("false")) {
        inAppMessage.setOpenUriInWebView(false);
      }
    }
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
    } else {
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
    // don't replace message on html in-app messages
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

  private void setImage(IInAppMessageWithImage inAppMessage) {
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
      switch (mOrientation) {
        case "any":
          inAppMessage.setOrientation(Orientation.ANY);
          break;
        case "portrait":
          inAppMessage.setOrientation(Orientation.PORTRAIT);
          break;
        case "landscape":
          inAppMessage.setOrientation(Orientation.LANDSCAPE);
          break;
        default:
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
    switch (textAlign) {
      case "start":
        return TextAlign.START;
      case "end":
        return TextAlign.END;
      case "center":
        return TextAlign.CENTER;
      default:
        return null;
    }
  }

  @SuppressWarnings("checkstyle:MissingSwitchDefault")
  private void addMessageButtons(IInAppMessageImmersive inAppMessage) {
    if (!SpinnerUtils.spinnerItemNotSet(mButtons)) {
      if (getString(R.string.none).equals(mButtons)) {
        inAppMessage.setMessageButtons(Collections.emptyList());
        return;
      }
      ArrayList<MessageButton> messageButtons = new ArrayList<>();
      MessageButton buttonOne = new MessageButton();
      MessageButton buttonTwo = new MessageButton();

      // Add the first button
      switch (mButtons) {
        case "one":
          buttonOne.setClickAction(ClickAction.NEWS_FEED);
          buttonOne.setText("News Feed");
          messageButtons.add(buttonOne);
          break;
        case "one_long":
          buttonOne.setClickAction(ClickAction.NEWS_FEED);
          buttonOne.setText(getString(R.string.message_2400));
          messageButtons.add(buttonOne);
          break;
      }

      // Add the second button
      switch (mButtons) {
        case "two":
        case "long":
          buttonOne.setText("No Webview");
          buttonOne.setClickAction(ClickAction.URI, Uri.parse(getResources().getString(R.string.braze_homepage_url)));
          buttonTwo.setText("Webview");
          buttonTwo.setClickAction(ClickAction.URI, Uri.parse(getResources().getString(R.string.braze_homepage_url)));
          buttonTwo.setOpenUriInWebview(true);
          if ("long".equals(mButtons)) {
            buttonOne.setText("No Webview WITH A VERY LONG TITLE");
            buttonTwo.setText("Webview WITH A VERY LONG TITLE");
          }
          messageButtons.add(buttonOne);
          messageButtons.add(buttonTwo);
          break;
        case "deeplink":
          buttonOne.setText("TELEPHONE");
          buttonOne.setClickAction(ClickAction.URI, Uri.parse(getResources().getString(R.string.telephone_uri)));
          buttonTwo.setText("PLAY STORE");
          buttonTwo.setClickAction(ClickAction.URI, Uri.parse(getResources().getString(R.string.play_store_uri)));
          messageButtons.add(buttonOne);
          messageButtons.add(buttonTwo);
          break;
      }
      inAppMessage.setMessageButtons(messageButtons);
    }
    if (!SpinnerUtils.spinnerItemNotSet(mButtonColor) && !inAppMessage.getMessageButtons().isEmpty()) {
      for (MessageButton button : inAppMessage.getMessageButtons()) {
        button.setBackgroundColor(parseColorFromString(mButtonColor));
      }
    }
    if (!SpinnerUtils.spinnerItemNotSet(mButtonTextColor) && !inAppMessage.getMessageButtons().isEmpty()) {
      for (MessageButton button : inAppMessage.getMessageButtons()) {
        button.setTextColor(parseColorFromString(mButtonTextColor));
      }
    }
    if (!SpinnerUtils.spinnerItemNotSet(mButtonBorderColor) && !inAppMessage.getMessageButtons().isEmpty()) {
      for (MessageButton button : inAppMessage.getMessageButtons()) {
        button.setBorderColor(parseColorFromString(mButtonBorderColor));
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
      case R.id.inapp_button_border_color_spinner:
        mButtonBorderColor = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_color_values);
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
      case R.id.inapp_open_uri_in_webview_spinner:
        mUseWebview = SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_boolean_values);
        break;
      default:
        Log.e(TAG, "Item selected for unknown spinner");
    }
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    // Do nothing
  }

  private int parseColorFromString(String colorString) {
    switch (colorString) {
      case "red":
        return APPBOY_RED;
      case "orange":
        return GOOGLE_ORANGE;
      case "yellow":
        return GOOGLE_YELLOW;
      case "green":
        return GOOGLE_GREEN;
      case "blue":
        return APPBOY_BLUE;
      case "purple":
        return GOOGLE_PURPLE;
      case "brown":
        return GOOGLE_BROWN;
      case "grey":
        return GOOGLE_GREY;
      case "black":
        return BLACK;
      case "white":
        return WHITE;
      case "transparent":
        return Color.argb(0, 0, 0, 0);
      case "almost_transparent_blue":
        return TRANSPARENT_APPBOY_BLUE;
      default:
        return 0;
    }
  }

  @NonNull
  private SharedPreferences getSettingsPreferences() {
    return getActivity().getPreferences(Context.MODE_PRIVATE);
  }

  private void setupCheckbox(CheckBox checkBoxView, final CompoundButton.OnCheckedChangeListener listener) {
    // Generate the preferences id. Note that this will change
    // if the id changes but that is ok for this use-case
    final String key = "checkbox_pref_" + checkBoxView.getId();

    // Set the initial checked state
    checkBoxView.setChecked(getSettingsPreferences().getBoolean(key, false));

    // Call the provided listener
    checkBoxView.setOnCheckedChangeListener((buttonView, isChecked) -> {
      listener.onCheckedChanged(buttonView, isChecked);
      getSettingsPreferences()
          .edit()
          .putBoolean(key, isChecked)
          .apply();
    });
  }

  private static String getStringFromAssets(Context context, String filename) {
    // Get the text of the html from the assets folder
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename), "UTF-8"));
      String line;
      StringBuilder stringBuilder = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line);
      }
      reader.close();
      return stringBuilder.toString();
    } catch (IOException e) {
      Log.e(TAG, "Error while reading html body from assets.", e);
    }

    return null;
  }
}
