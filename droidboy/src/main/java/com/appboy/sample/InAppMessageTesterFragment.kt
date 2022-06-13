package com.appboy.sample

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.appboy.sample.util.SpinnerUtils
import com.braze.Braze
import com.braze.enums.inappmessage.ClickAction
import com.braze.enums.inappmessage.CropType
import com.braze.enums.inappmessage.DismissType
import com.braze.enums.inappmessage.ImageStyle
import com.braze.enums.inappmessage.MessageType
import com.braze.enums.inappmessage.Orientation
import com.braze.enums.inappmessage.SlideFrom
import com.braze.enums.inappmessage.TextAlign
import com.braze.models.inappmessage.IInAppMessage
import com.braze.models.inappmessage.IInAppMessageHtml
import com.braze.models.inappmessage.IInAppMessageImmersive
import com.braze.models.inappmessage.IInAppMessageWithImage
import com.braze.models.inappmessage.IInAppMessageZippedAssetHtml
import com.braze.models.inappmessage.InAppMessageFull
import com.braze.models.inappmessage.InAppMessageHtml
import com.braze.models.inappmessage.InAppMessageHtmlFull
import com.braze.models.inappmessage.InAppMessageModal
import com.braze.models.inappmessage.InAppMessageSlideup
import com.braze.models.inappmessage.MessageButton
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.brazelog
import com.braze.ui.BrazeDeeplinkHandler.Companion.setBrazeDeeplinkHandler
import com.braze.ui.inappmessage.BrazeInAppMessageManager
import com.braze.ui.inappmessage.config.BrazeInAppMessageParams
import com.braze.ui.inappmessage.config.BrazeInAppMessageParams.graphicModalMaxHeightDp
import com.braze.ui.inappmessage.config.BrazeInAppMessageParams.graphicModalMaxWidthDp
import com.braze.ui.inappmessage.config.BrazeInAppMessageParams.modalizedImageRadiusDp
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

@Suppress("LargeClass")
class InAppMessageTesterFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private enum class HtmlMessageType(val fileName: String, val zippedAssetUrl: String?) {
        NO_JS(
            "html_inapp_message_body_no_js.html",
            "https://appboy-staging-dashboard-uploads.s3.amazonaws.com/zip_uploads/files/" +
                "585c1776bf5cea3cbe1b36b2/124fae83d6ba4023d4ede28e9177980e6373747c/original.zip?1482430326"
        ),
        INLINE_JS("html_inapp_message_body_inline_js.html", null), EXTERNAL_JS(
            "html_inapp_message_body_external_js.html",
            "https://appboy-staging-dashboard-uploads.s3.amazonaws.com/zip_uploads/files/" +
                "585c18c3bf5cea3c861b36ba/b0c7e536230b34ef800c8e0ef0747eaac53545a5/original.zip?1482430659"
        ),
        STAR_WARS(
            "html_inapp_message_body_star_wars.html",
            null
        ),
        YOUTUBE("html_inapp_message_body_youtube_iframe.html", null), BRIDGE_TESTER(
            "html_in_app_message_bridge_tester.html",
            "https://appboy-images.com/HTML_ZIP_STOPWATCH.zip"
        ),
        SLOW_LOADING(
            "html_inapp_message_delayed_open.html",
            null
        ),
        DARK_MODE(
            "html_inapp_message_dark_mode.html",
            null
        ),
        UNIFIED_HTML_BOOTSTRAP_ALBUM(
            "html_in_app_message_unified_bootstrap_album.html",
            null
        ),
        SHARK_HTML("html_shark_unified.html", null);
    }

    private var messageType: String? = null
    private var clickAction: String? = null
    private var dismissType: String? = null
    private var slideFrom: String? = null
    private var uri: String? = null
    private var header: String? = null
    private var message: String? = null
    private var backgroundColor: String? = null
    private var iconColor: String? = null
    private var iconBackgroundColor: String? = null
    private var closeButtonColor: String? = null
    private var buttonBorderColor: String? = null
    private var textColor: String? = null
    private var headerTextColor: String? = null
    private var buttonColor: String? = null
    private var buttonTextColor: String? = null
    private var frameColor: String? = null
    private var icon: String? = null
    private var image: String? = null
    private var buttons: String? = null
    private var orientation: String? = null
    private var messageTextAlign: String? = null
    private var headerTextAlign: String? = null
    private var animateIn: String? = null
    private var animateOut: String? = null
    private var useInWebView: String? = null

    private val settingsPreferences: SharedPreferences
        get() = requireActivity().getPreferences(Context.MODE_PRIVATE)

    @Suppress("ComplexMethod")
    override fun onCreateView(
        layoutInflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = layoutInflater.inflate(R.layout.inappmessage_tester, container, false)
        for (key in spinnerOptionMap.keys) {
            spinnerOptionMap[key]?.let {
                SpinnerUtils.setUpSpinner(
                    view.findViewById(key), this,
                    it
                )
            }
        }
        setupCheckbox(
            view.findViewById(R.id.custom_inappmessage_view_factory_checkbox)
        ) { _: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                BrazeInAppMessageManager.getInstance()
                    .setCustomInAppMessageViewFactory(CustomInAppMessageViewFactory())
            } else {
                BrazeInAppMessageManager.getInstance().setCustomInAppMessageViewFactory(null)
            }
        }
        setupCheckbox(
            view.findViewById(R.id.custom_inappmessage_manager_listener_checkbox)
        ) { _: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                BrazeInAppMessageManager.getInstance().setCustomInAppMessageManagerListener(
                    CustomInAppMessageManagerListener(
                        activity
                    )
                )
            } else {
                BrazeInAppMessageManager.getInstance().setCustomInAppMessageManagerListener(null)
            }
        }
        setupCheckbox(
            view.findViewById(R.id.custom_appboy_navigator_checkbox)
        ) { _: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                setBrazeDeeplinkHandler(CustomBrazeDeeplinkHandler())
            } else {
                setBrazeDeeplinkHandler(null)
            }
        }
        setupCheckbox(
            view.findViewById(R.id.custom_appboy_graphic_modal_max_size_checkbox)
        ) { _: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                graphicModalMaxHeightDp = 420.0
                graphicModalMaxWidthDp = 320.0
            } else {
                graphicModalMaxHeightDp = BrazeInAppMessageParams.GRAPHIC_MODAL_MAX_HEIGHT_DP
                graphicModalMaxWidthDp = BrazeInAppMessageParams.GRAPHIC_MODAL_MAX_WIDTH_DP
            }
        }
        setupCheckbox(
            view.findViewById(R.id.custom_appboy_image_radius_checkbox)
        ) { _: CompoundButton, isChecked: Boolean ->
            modalizedImageRadiusDp = if (isChecked) {
                0.0
            } else {
                BrazeInAppMessageParams.MODALIZED_IMAGE_RADIUS_DP
            }
        }
        setupCheckbox(
            view.findViewById(R.id.disable_back_button_dismiss_behavior)
        ) { _: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                BrazeInAppMessageManager.getInstance().setBackButtonDismissesInAppMessageView(false)
            } else {
                BrazeInAppMessageManager.getInstance().setBackButtonDismissesInAppMessageView(true)
            }
        }
        setupCheckbox(
            view.findViewById(R.id.enable_tap_outside_modal_dismiss_behavior)
        ) { _: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                BrazeInAppMessageManager.getInstance().setClickOutsideModalViewDismissInAppMessageView(true)
            } else {
                BrazeInAppMessageManager.getInstance()
                    .setClickOutsideModalViewDismissInAppMessageView(false)
            }
        }
        setupCheckbox(
            view.findViewById(R.id.custom_appboy_animation_checkbox)
        ) { _: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                BrazeInAppMessageManager.getInstance()
                    .setCustomInAppMessageAnimationFactory(CustomInAppMessageAnimationFactory())
            } else {
                BrazeInAppMessageManager.getInstance().setCustomInAppMessageAnimationFactory(null)
            }
        }
        setupCheckbox(
            view.findViewById(R.id.custom_appboy_html_inappmessage_action_listener_checkbox)
        ) { _: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                BrazeInAppMessageManager.getInstance().setCustomHtmlInAppMessageActionListener(
                    CustomHtmlInAppMessageActionListener(
                        context
                    )
                )
            } else {
                BrazeInAppMessageManager.getInstance().setCustomHtmlInAppMessageActionListener(null)
            }
        }
        val createAndAddInAppMessageButton =
            view.findViewById<Button>(R.id.create_and_add_inappmessage_button)
        createAndAddInAppMessageButton.setOnClickListener {
            if (settingsPreferences.getBoolean(
                    CUSTOM_INAPPMESSAGE_VIEW_KEY, false
                )
            ) {
                addInAppMessage(CustomInAppMessage())
            } else {
                when (messageType) {
                    "modal" -> addInAppMessage(InAppMessageModal())
                    "modal_graphic" -> {
                        val inAppMessageModal = InAppMessageModal()
                        inAppMessageModal.imageStyle = ImageStyle.GRAPHIC
                        // graphic modals must be center cropped, the default for newly constructed modals
                        // is center_fit
                        inAppMessageModal.cropType = CropType.CENTER_CROP
                        addInAppMessage(inAppMessageModal)
                    }
                    "full" -> addInAppMessage(InAppMessageFull())
                    "full_graphic" -> {
                        val inAppMessageFull = InAppMessageFull()
                        inAppMessageFull.imageStyle = ImageStyle.GRAPHIC
                        addInAppMessage(inAppMessageFull)
                    }
                    "html_full_no_js" -> addInAppMessage(InAppMessageHtmlFull(), HtmlMessageType.NO_JS)
                    "html_full_inline_js" -> addInAppMessage(
                        InAppMessageHtmlFull(),
                        HtmlMessageType.INLINE_JS
                    )
                    "html_full_external_js" -> addInAppMessage(
                        InAppMessageHtmlFull(),
                        HtmlMessageType.EXTERNAL_JS
                    )
                    "html_full_star_wars" -> addInAppMessage(
                        InAppMessageHtmlFull(),
                        HtmlMessageType.STAR_WARS
                    )
                    "html_full_youtube" -> addInAppMessage(InAppMessageHtmlFull(), HtmlMessageType.YOUTUBE)
                    "html_full_bridge_tester" -> addInAppMessage(
                        InAppMessageHtmlFull(),
                        HtmlMessageType.BRIDGE_TESTER
                    )
                    "html_full_slow_loading" -> addInAppMessage(
                        InAppMessageHtmlFull(),
                        HtmlMessageType.SLOW_LOADING
                    )
                    "html_full_unified_bootstrap" -> addInAppMessage(
                        InAppMessageHtml(),
                        HtmlMessageType.UNIFIED_HTML_BOOTSTRAP_ALBUM
                    )
                    "html_shark_unified" -> addInAppMessage(InAppMessageHtml(), HtmlMessageType.SHARK_HTML)
                    "html_full_dark_mode" -> addInAppMessage(
                        InAppMessageHtmlFull(),
                        HtmlMessageType.DARK_MODE
                    )
                    "modal_dark_theme" -> {
                        val darkModeJson = context?.let { context ->
                            getStringFromAssets(
                                context, "modal_inapp_message_with_dark_theme.json"
                            )
                        }
                        darkModeJson?.let { json -> addInAppMessageFromString(json) }
                    }
                    "slideup" -> addInAppMessage(InAppMessageSlideup())
                    else -> addInAppMessage(InAppMessageSlideup())
                }
            }
        }
        val displayNextInAppMessageButton =
            view.findViewById<Button>(R.id.display_next_inappmessage_button)
        displayNextInAppMessageButton.setOnClickListener {
            BrazeInAppMessageManager.getInstance().requestDisplayInAppMessage()
        }
        val hideCurrentInAppMessageButton =
            view.findViewById<Button>(R.id.hide_current_inappmessage_button)
        hideCurrentInAppMessageButton.setOnClickListener {
            BrazeInAppMessageManager.getInstance().hideCurrentlyDisplayingInAppMessage(false)
        }
        return view
    }

    private fun addInAppMessageImmersive(inAppMessage: IInAppMessageImmersive) {
        if (inAppMessage is InAppMessageModal) {
            inAppMessage.message = "Welcome to Braze! Braze is Marketing Automation for Apps!"
            if (inAppMessage.imageStyle == ImageStyle.GRAPHIC) {
                inAppMessage.remoteImageUrl =
                    resources.getString(R.string.appboy_image_url_1000w_1000h)
            } else {
                inAppMessage.remoteImageUrl =
                    resources.getString(R.string.appboy_image_url_1160w_400h)
            }
        } else if (inAppMessage is InAppMessageFull) {
            inAppMessage.message =
                "Welcome to Braze! Braze is Marketing Automation for Apps. This is an example of a full in-app message."
            if (inAppMessage.imageStyle == ImageStyle.GRAPHIC) {
                if (inAppMessage.orientation == Orientation.LANDSCAPE) {
                    inAppMessage.remoteImageUrl =
                        resources.getString(R.string.appboy_image_url_1600w_1000h)
                } else {
                    inAppMessage.remoteImageUrl =
                        resources.getString(R.string.appboy_image_url_1000w_1600h)
                }
            } else {
                if (inAppMessage.orientation == Orientation.LANDSCAPE) {
                    inAppMessage.remoteImageUrl =
                        resources.getString(R.string.appboy_image_url_1600w_500h)
                } else {
                    inAppMessage.remoteImageUrl =
                        resources.getString(R.string.appboy_image_url_1000w_800h)
                }
            }
        }
        inAppMessage.header = "Hello from Braze!"
        val messageButtons = ArrayList<MessageButton>()
        val buttonOne = MessageButton()
        buttonOne.text = "NewsFeed"
        buttonOne.setClickBehavior(ClickAction.NEWS_FEED)
        messageButtons.add(buttonOne)
        inAppMessage.messageButtons = messageButtons
        addMessageButtons(inAppMessage)
        setHeader(inAppMessage)
        setCloseButtonColor(inAppMessage)
        setFrameColor(inAppMessage)
        setHeaderTextAlign(inAppMessage)
    }

    /**
     * Adds an [IInAppMessage] from its [IInAppMessage.forJsonPut] form.
     */
    private fun addInAppMessageFromString(serializedInAppMessage: String) {
        val inAppMessage =
            Braze.getInstance(context).deserializeInAppMessageString(serializedInAppMessage)
        BrazeInAppMessageManager.getInstance().addInAppMessage(inAppMessage)
    }

    private fun addInAppMessageSlideup(inAppMessage: InAppMessageSlideup) {
        inAppMessage.message = "Welcome to Braze! This is a slideup in-app message."
        inAppMessage.icon = "\uf091"
        inAppMessage.setClickBehavior(ClickAction.NEWS_FEED)
        setSlideFrom(inAppMessage)
        setChevronColor(inAppMessage)
    }

    private fun addInAppMessageCustom(inAppMessage: IInAppMessage) {
        inAppMessage.message = "Welcome to Braze! This is a custom in-app message."
        inAppMessage.icon = "\uf091"
    }

    private fun addInAppMessageHtml(
        inAppMessage: IInAppMessageHtml,
        htmlMessageType: HtmlMessageType
    ) {
        inAppMessage.message = context?.let { getStringFromAssets(it, htmlMessageType.fileName) }
        if (htmlMessageType.zippedAssetUrl != null && inAppMessage is IInAppMessageZippedAssetHtml) {
            inAppMessage.assetsZipRemoteUrl = htmlMessageType.zippedAssetUrl
        }
    }

    private fun addInAppMessage(inAppMessage: IInAppMessage, messageType: HtmlMessageType? = null) {
        // set orientation early to help determine which default image to use
        setOrientation(inAppMessage)
        when (inAppMessage.messageType) {
            MessageType.SLIDEUP -> addInAppMessageSlideup(inAppMessage as InAppMessageSlideup)
            MessageType.MODAL, MessageType.FULL -> addInAppMessageImmersive(inAppMessage as IInAppMessageImmersive)
            MessageType.HTML, MessageType.HTML_FULL -> messageType?.let {
                addInAppMessageHtml(
                    inAppMessage as IInAppMessageHtml,
                    it
                )
            }
            else -> addInAppMessageCustom(inAppMessage)
        }
        if (!addClickAction(inAppMessage)) {
            return
        }
        setDismissType(inAppMessage)
        setBackgroundColor(inAppMessage)
        setMessage(inAppMessage)
        setIcon(inAppMessage)
        if (inAppMessage is IInAppMessageWithImage) {
            setImage(inAppMessage as IInAppMessageWithImage)
        }
        setMessageTextAlign(inAppMessage)
        setAnimation(inAppMessage)
        setUseWebview(inAppMessage)
        BrazeInAppMessageManager.getInstance().addInAppMessage(inAppMessage)
    }

    private fun setUseWebview(inAppMessage: IInAppMessage) {
        if (!SpinnerUtils.spinnerItemNotSet(useInWebView)) {
            if (useInWebView == "true") {
                inAppMessage.openUriInWebView = true
            } else if (useInWebView == "false") {
                inAppMessage.openUriInWebView = false
            }
        }
    }

    private fun setAnimation(inAppMessage: IInAppMessage) {
        if (!SpinnerUtils.spinnerItemNotSet(animateIn)) {
            if (animateIn == "true") {
                inAppMessage.animateIn = true
            } else if (animateIn == "false") {
                inAppMessage.animateIn = false
            }
        }
        if (!SpinnerUtils.spinnerItemNotSet(animateOut)) {
            if (animateOut == "true") {
                inAppMessage.animateOut = true
            } else if (animateOut == "false") {
                inAppMessage.animateOut = false
            }
        }
    }

    private fun setDismissType(inAppMessage: IInAppMessage) {
        // set dismiss type if defined
        when (dismissType) {
            "auto" -> {
                inAppMessage.dismissType = DismissType.AUTO_DISMISS
            }
            "auto-short" -> {
                inAppMessage.dismissType = DismissType.AUTO_DISMISS
                inAppMessage.durationInMilliseconds = 1000
            }
            "manual" -> {
                inAppMessage.dismissType = DismissType.MANUAL
            }
            else -> {
                inAppMessage.dismissType = DismissType.MANUAL
            }
        }
    }

    private fun setBackgroundColor(inAppMessage: IInAppMessage) {
        // set background color if defined
        if (!SpinnerUtils.spinnerItemNotSet(backgroundColor)) {
            backgroundColor?.let { inAppMessage.backgroundColor = parseColorFromString(it) }
        }
    }

    private fun setChevronColor(inAppMessage: InAppMessageSlideup) {
        // set chevron color if defined
        if (!SpinnerUtils.spinnerItemNotSet(closeButtonColor)) {
            closeButtonColor?.let { inAppMessage.chevronColor = parseColorFromString(it) }
        }
    }

    private fun setCloseButtonColor(inAppMessage: IInAppMessageImmersive) {
        // set close button color if defined
        if (!SpinnerUtils.spinnerItemNotSet(closeButtonColor)) {
            closeButtonColor?.let { inAppMessage.closeButtonColor = parseColorFromString(it) }
        }
    }

    private fun setMessage(inAppMessage: IInAppMessage) {
        // set text color if defined
        if (!SpinnerUtils.spinnerItemNotSet(textColor)) {
            textColor?.let { inAppMessage.messageTextColor = parseColorFromString(it) }
        }
        // don't replace message on html in-app messages
        if (inAppMessage is IInAppMessageHtml) {
            return
        }
        if (!SpinnerUtils.spinnerItemNotSet(message)) {
            inAppMessage.message = message
        }
    }

    private fun setIcon(inAppMessage: IInAppMessage) {
        // set icon color if defined
        if (!SpinnerUtils.spinnerItemNotSet(iconColor)) {
            iconColor?.let { inAppMessage.iconColor = parseColorFromString(it) }
        }
        // set icon background color if defined
        if (!SpinnerUtils.spinnerItemNotSet(iconBackgroundColor)) {
            iconBackgroundColor?.let { inAppMessage.iconBackgroundColor = parseColorFromString(it) }
        }
        // set in-app message icon
        if (!SpinnerUtils.spinnerItemNotSet(icon)) {
            if (icon == getString(R.string.none)) {
                inAppMessage.icon = null
            } else {
                inAppMessage.icon = icon
            }
        }
    }

    private fun setImage(inAppMessage: IInAppMessageWithImage) {
        // set in-app message image url
        if (!SpinnerUtils.spinnerItemNotSet(image)) {
            if (image == getString(R.string.none)) {
                inAppMessage.remoteImageUrl = null
            } else {
                inAppMessage.remoteImageUrl = image
            }
        }
    }

    private fun setOrientation(inAppMessage: IInAppMessage) {
        // set in-app message preferred orientation
        if (!SpinnerUtils.spinnerItemNotSet(orientation)) {
            when (orientation) {
                "any" -> inAppMessage.orientation = Orientation.ANY
                "portrait" -> inAppMessage.orientation = Orientation.PORTRAIT
                "landscape" -> inAppMessage.orientation = Orientation.LANDSCAPE
                else -> {}
            }
        }
    }

    private fun addClickAction(inAppMessage: IInAppMessage): Boolean {
        // set click action if defined
        if ("newsfeed" == clickAction) {
            inAppMessage.setClickBehavior(ClickAction.NEWS_FEED)
        } else if ("uri" == clickAction) {
            if (SpinnerUtils.spinnerItemNotSet(uri)) {
                Toast.makeText(context, "Please choose a URI.", Toast.LENGTH_LONG).show()
                return false
            } else {
                inAppMessage.setClickBehavior(ClickAction.URI, Uri.parse(uri))
            }
        } else if (getString(R.string.none) == clickAction) {
            inAppMessage.setClickBehavior(ClickAction.NONE)
        }
        return true
    }

    private fun setSlideFrom(inAppMessage: InAppMessageSlideup) {
        // set slide from if defined
        if ("top" == slideFrom) {
            inAppMessage.slideFrom = SlideFrom.TOP
        } else if ("bottom" == slideFrom) {
            inAppMessage.slideFrom = SlideFrom.BOTTOM
        }
    }

    private fun setHeader(inAppMessage: IInAppMessageImmersive) {
        // set header text color if defined
        if (!SpinnerUtils.spinnerItemNotSet(headerTextColor)) {
            headerTextColor?.let { inAppMessage.headerTextColor = parseColorFromString(it) }
        }
        if (!SpinnerUtils.spinnerItemNotSet(header)) {
            if (getString(R.string.none) == header) {
                inAppMessage.header = null
            } else {
                inAppMessage.header = header
            }
        }
    }

    private fun setFrameColor(inAppMessage: IInAppMessageImmersive) {
        if (!SpinnerUtils.spinnerItemNotSet(frameColor)) {
            inAppMessage.frameColor = frameColor?.let { parseColorFromString(it) }
        }
    }

    private fun setHeaderTextAlign(inAppMessage: IInAppMessageImmersive) {
        if (!SpinnerUtils.spinnerItemNotSet(headerTextAlign)) {
            headerTextAlign?.let { inAppMessage.headerTextAlign = parseTextAlign(it) }
        }
    }

    private fun setMessageTextAlign(inAppMessage: IInAppMessage) {
        if (!SpinnerUtils.spinnerItemNotSet(messageTextAlign)) {
            messageTextAlign?.let { inAppMessage.messageTextAlign = parseTextAlign(it) }
        }
    }

    private fun parseTextAlign(textAlign: String): TextAlign {
        return when (textAlign) {
            "start" -> TextAlign.START
            "end" -> TextAlign.END
            "center" -> TextAlign.CENTER
            else -> TextAlign.START
        }
    }

    @Suppress("ComplexMethod")
    private fun addMessageButtons(inAppMessage: IInAppMessageImmersive) {
        if (!SpinnerUtils.spinnerItemNotSet(buttons)) {
            if (getString(R.string.none) == buttons) {
                inAppMessage.messageButtons = emptyList()
                return
            }
            val messageButtons = ArrayList<MessageButton>()
            val buttonOne = MessageButton()
            val buttonTwo = MessageButton()
            when (buttons) {
                "one" -> {
                    buttonOne.setClickBehavior(ClickAction.NEWS_FEED)
                    buttonOne.text = "News Feed"
                    messageButtons.add(buttonOne)
                }
                "one_long" -> {
                    buttonOne.setClickBehavior(ClickAction.NEWS_FEED)
                    buttonOne.text = getString(R.string.message_2400)
                    messageButtons.add(buttonOne)
                }
                "push_prompt_one" -> {
                    val pushPromptBrazeActionUri = Uri.parse(getStringFromAssets(requireContext(), "braze_actions/show_push_prompt.txt"))
                    buttonOne.setClickBehavior(ClickAction.URI, pushPromptBrazeActionUri)
                    buttonOne.text = "Show Push Prompt"
                    messageButtons.add(buttonOne)
                }
            }
            when (buttons) {
                "two", "long" -> {
                    buttonOne.text = "No Webview"
                    buttonOne.setClickBehavior(
                        ClickAction.URI,
                        Uri.parse(resources.getString(R.string.braze_homepage_url))
                    )
                    buttonTwo.text = "Webview"
                    buttonTwo.setClickBehavior(
                        ClickAction.URI,
                        Uri.parse(resources.getString(R.string.braze_homepage_url))
                    )
                    buttonTwo.openUriInWebview = true
                    if ("long" == buttons) {
                        buttonOne.text = "No Webview WITH A VERY LONG TITLE"
                        buttonTwo.text = "Webview WITH A VERY LONG TITLE"
                    }
                    messageButtons.add(buttonOne)
                    messageButtons.add(buttonTwo)
                }
                "deeplink" -> {
                    buttonOne.text = "TELEPHONE"
                    buttonOne.setClickBehavior(
                        ClickAction.URI,
                        Uri.parse(resources.getString(R.string.telephone_uri))
                    )
                    buttonTwo.text = "PLAY STORE"
                    buttonTwo.setClickBehavior(
                        ClickAction.URI,
                        Uri.parse(resources.getString(R.string.play_store_uri))
                    )
                    messageButtons.add(buttonOne)
                    messageButtons.add(buttonTwo)
                }
            }
            inAppMessage.messageButtons = messageButtons
        }
        if (!SpinnerUtils.spinnerItemNotSet(buttonColor) && inAppMessage.messageButtons.isNotEmpty()) {
            for (button in inAppMessage.messageButtons) {
                buttonColor?.let { button.backgroundColor = parseColorFromString(it) }
            }
        }
        if (!SpinnerUtils.spinnerItemNotSet(buttonTextColor) && inAppMessage.messageButtons.isNotEmpty()) {
            for (button in inAppMessage.messageButtons) {
                buttonTextColor?.let { button.textColor = parseColorFromString(it) }
            }
        }
        if (!SpinnerUtils.spinnerItemNotSet(buttonBorderColor) && inAppMessage.messageButtons.isNotEmpty()) {
            for (button in inAppMessage.messageButtons) {
                buttonBorderColor?.let { button.borderColor = parseColorFromString(it) }
            }
        }
    }

    @Suppress("ComplexMethod")
    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        when (parent.id) {
            R.id.inapp_set_message_type_spinner ->
                messageType =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_message_type_values)
            R.id.inapp_click_action_spinner ->
                clickAction =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_click_action_values)
            R.id.inapp_dismiss_type_spinner ->
                dismissType =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_dismiss_type_values)
            R.id.inapp_slide_from_spinner ->
                slideFrom =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_slide_from_values)
            R.id.inapp_uri_spinner ->
                uri =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_uri_values)
            R.id.inapp_header_spinner ->
                header =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_header_values)
            R.id.inapp_message_spinner ->
                message =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_message_values)
            R.id.inapp_background_color_spinner ->
                backgroundColor =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_color_values)
            R.id.inapp_icon_color_spinner ->
                iconColor =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_color_values)
            R.id.inapp_icon_background_color_spinner ->
                iconBackgroundColor =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_color_values)
            R.id.inapp_close_button_color_spinner ->
                closeButtonColor =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_color_values)
            R.id.inapp_text_color_spinner ->
                textColor =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_color_values)
            R.id.inapp_header_text_color_spinner ->
                headerTextColor =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_color_values)
            R.id.inapp_button_color_spinner ->
                buttonColor =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_color_values)
            R.id.inapp_button_border_color_spinner ->
                buttonBorderColor =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_color_values)
            R.id.inapp_button_text_color_spinner ->
                buttonTextColor =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_color_values)
            R.id.inapp_frame_spinner ->
                frameColor =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_frame_values)
            R.id.inapp_icon_spinner ->
                icon =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_icon_values)
            R.id.inapp_image_spinner ->
                image =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_image_values)
            R.id.inapp_button_spinner ->
                buttons =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_button_values)
            R.id.inapp_orientation_spinner ->
                orientation =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_orientation_values)
            R.id.inapp_header_align_spinner ->
                headerTextAlign =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_align_values)
            R.id.inapp_message_align_spinner ->
                messageTextAlign =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_align_values)
            R.id.inapp_animate_in_spinner ->
                animateIn =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_boolean_values)
            R.id.inapp_animate_out_spinner ->
                animateOut =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_boolean_values)
            R.id.inapp_open_uri_in_webview_spinner ->
                useInWebView =
                    SpinnerUtils.handleSpinnerItemSelected(parent, R.array.inapp_boolean_values)
            else -> brazelog(E) { "Item selected for unknown spinner" }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Do nothing
    }

    private fun parseColorFromString(colorString: String): Int {
        return when (colorString) {
            "red" -> BRAZE_RED
            "orange" -> GOOGLE_ORANGE
            "yellow" -> GOOGLE_YELLOW
            "green" -> GOOGLE_GREEN
            "blue" -> BRAZE_BLUE
            "purple" -> GOOGLE_PURPLE
            "brown" -> GOOGLE_BROWN
            "grey" -> GOOGLE_GREY
            "black" -> BLACK
            "white" -> WHITE
            "transparent" -> Color.argb(0, 0, 0, 0)
            "almost_transparent_blue" -> TRANSPARENT_BRAZE_BLUE
            else -> 0
        }
    }

    private fun setupCheckbox(
        checkBoxView: CheckBox,
        listener: CompoundButton.OnCheckedChangeListener
    ) {
        // Generate the preferences id. Note that this will change
        // if the id changes but that is ok for this use-case
        val key = "checkbox_pref_" + checkBoxView.id

        // Set the initial checked state
        checkBoxView.isChecked = settingsPreferences.getBoolean(key, false)

        // Call the provided listener
        checkBoxView.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            listener.onCheckedChanged(buttonView, isChecked)
            settingsPreferences
                .edit()
                .putBoolean(key, isChecked)
                .apply()
        }
    }

    companion object {
        private const val CUSTOM_INAPPMESSAGE_VIEW_KEY = "inapmessages_custom_inappmessage_view"

        // color reference: http://www.google.com/design/spec/style/color.html
        private const val BRAZE_RED = -0xcc1c2
        private const val GOOGLE_ORANGE = -0xa8de
        private const val GOOGLE_YELLOW = -0x14c5
        private const val GOOGLE_GREEN = -0xb350b0
        private const val BRAZE_BLUE = -0xff8c2b
        private const val TRANSPARENT_BRAZE_BLUE = 0x220073d5
        private const val GOOGLE_PURPLE = -0x98c549
        private const val GOOGLE_BROWN = -0x86aab8
        private const val GOOGLE_GREY = -0x616162
        private const val BLACK = -0x1000000
        private const val WHITE = -0x1
        private var spinnerOptionMap: Map<Int, Int>

        init {
            val spinnerMap: HashMap<Int, Int> = HashMap()
            spinnerMap[R.id.inapp_set_message_type_spinner] = R.array.inapp_message_type_options
            spinnerMap[R.id.inapp_click_action_spinner] = R.array.inapp_click_action_options
            spinnerMap[R.id.inapp_dismiss_type_spinner] = R.array.inapp_dismiss_type_options
            spinnerMap[R.id.inapp_slide_from_spinner] = R.array.inapp_slide_from_options
            spinnerMap[R.id.inapp_header_spinner] = R.array.inapp_header_options
            spinnerMap[R.id.inapp_message_spinner] = R.array.inapp_message_options
            spinnerMap[R.id.inapp_background_color_spinner] = R.array.inapp_color_options
            spinnerMap[R.id.inapp_icon_color_spinner] = R.array.inapp_color_options
            spinnerMap[R.id.inapp_icon_background_color_spinner] = R.array.inapp_color_options
            spinnerMap[R.id.inapp_close_button_color_spinner] = R.array.inapp_color_options
            spinnerMap[R.id.inapp_text_color_spinner] = R.array.inapp_color_options
            spinnerMap[R.id.inapp_header_text_color_spinner] = R.array.inapp_color_options
            spinnerMap[R.id.inapp_button_color_spinner] = R.array.inapp_color_options
            spinnerMap[R.id.inapp_button_border_color_spinner] = R.array.inapp_color_options
            spinnerMap[R.id.inapp_button_text_color_spinner] = R.array.inapp_color_options
            spinnerMap[R.id.inapp_frame_spinner] = R.array.inapp_frame_options
            spinnerMap[R.id.inapp_uri_spinner] = R.array.inapp_uri_options
            spinnerMap[R.id.inapp_icon_spinner] = R.array.inapp_icon_options
            spinnerMap[R.id.inapp_image_spinner] = R.array.inapp_image_options
            spinnerMap[R.id.inapp_button_spinner] = R.array.inapp_button_options
            spinnerMap[R.id.inapp_orientation_spinner] = R.array.inapp_orientation_options
            spinnerMap[R.id.inapp_header_align_spinner] = R.array.inapp_align_options
            spinnerMap[R.id.inapp_message_align_spinner] = R.array.inapp_align_options
            spinnerMap[R.id.inapp_animate_in_spinner] = R.array.inapp_boolean_options
            spinnerMap[R.id.inapp_animate_out_spinner] = R.array.inapp_boolean_options
            spinnerMap[R.id.inapp_open_uri_in_webview_spinner] = R.array.inapp_boolean_options
            spinnerOptionMap = Collections.unmodifiableMap(spinnerMap)
        }

        private fun getStringFromAssets(context: Context, filename: String): String? {
            // Get the text of the html from the assets folder
            try {
                val reader = BufferedReader(
                    InputStreamReader(
                        context.assets.open(filename), "UTF-8"
                    )
                )
                var line: String?
                val stringBuilder = StringBuilder()
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                reader.close()
                return stringBuilder.toString()
            } catch (e: IOException) {
                brazelog(E, e) { "Error while reading html body from assets." }
            }
            return null
        }
    }
}
