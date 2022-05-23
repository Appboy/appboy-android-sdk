package com.braze.ui.actions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.braze.Constants
import com.appboy.enums.Channel
import com.braze.IBrazeDeeplinkHandler
import com.braze.configuration.BrazeConfigurationProvider
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.Priority.I
import com.braze.support.BrazeLogger.Priority.V
import com.braze.support.BrazeLogger.Priority.W
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.REMOTE_SCHEMES
import com.braze.support.isLocalUri
import com.braze.ui.BrazeDeeplinkHandler
import com.braze.ui.BrazeWebViewActivity
import com.braze.ui.actions.brazeactions.BrazeActionParser
import com.braze.ui.actions.brazeactions.BrazeActionParser.isBrazeActionUri
import com.braze.ui.support.getMainActivityIntent
import com.braze.ui.support.isActivityRegisteredInManifest

open class UriAction : IAction {
    val extras: Bundle?
    override val channel: Channel

    /**
     * @return the [Uri] that represents this [UriAction].
     */
    var uri: Uri

    /**
     * @return whether this [UriAction] should open
     */
    var useWebView: Boolean

    /**
     * @param uri        The Uri.
     * @param extras     Any extras to be passed in the start intent.
     * @param useWebView If this Uri should use the Webview, if the Uri is a remote Uri
     * @param channel    The channel for the Uri. Must not be null.
     */
    constructor(uri: Uri, extras: Bundle?, useWebView: Boolean, channel: Channel) {
        this.uri = uri
        this.extras = extras
        this.useWebView = useWebView
        this.channel = channel
    }

    /**
     * Constructor to copy an existing [UriAction].
     *
     * @param original A [UriAction] to copy parameters from.
     */
    constructor(original: UriAction) {
        uri = original.uri
        extras = original.extras
        useWebView = original.useWebView
        channel = original.channel
    }

    /**
     * Opens the action's Uri properly based on useWebView status and channel.
     */
    override fun execute(context: Context) {
        if (uri.isLocalUri()) {
            brazelog { "Not executing local Uri: $uri" }
            return
        }
        if (uri.isBrazeActionUri()) {
            brazelog(V) { "Executing BrazeActions uri:\n'$uri'" }
            BrazeActionParser.execute(context, uri, channel)
        } else {
            brazelog { "Executing Uri action from channel $channel: $uri. UseWebView: $useWebView. Extras: $extras" }
            if (useWebView && REMOTE_SCHEMES.contains(uri.scheme)) {
                // If the scheme is not a remote scheme, we open it using an ACTION_VIEW intent.
                if (channel == Channel.PUSH) {
                    openUriWithWebViewActivityFromPush(context, uri, extras)
                } else {
                    openUriWithWebViewActivity(context, uri, extras)
                }
            } else {
                if (channel == Channel.PUSH) {
                    openUriWithActionViewFromPush(context, uri, extras)
                } else {
                    openUriWithActionView(context, uri, extras)
                }
            }
        }
    }

    /**
     * Opens the remote scheme Uri in [BrazeWebViewActivity].
     */
    protected fun openUriWithWebViewActivity(context: Context, uri: Uri, extras: Bundle?) {
        val intent = getWebViewActivityIntent(context, uri, extras)
        intent.flags = BrazeDeeplinkHandler.getInstance()
            .getIntentFlags(IBrazeDeeplinkHandler.IntentFlagPurpose.URI_ACTION_OPEN_WITH_WEBVIEW_ACTIVITY)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            brazelog(E, e) { "BrazeWebViewActivity not opened successfully." }
        }
    }

    /**
     * Uses an Intent.ACTION_VIEW intent to open the Uri.
     */
    protected open fun openUriWithActionView(context: Context, uri: Uri, extras: Bundle?) {
        val intent = getActionViewIntent(context, uri, extras)
        intent.flags = BrazeDeeplinkHandler.getInstance()
            .getIntentFlags(IBrazeDeeplinkHandler.IntentFlagPurpose.URI_ACTION_OPEN_WITH_ACTION_VIEW)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            brazelog(E, e) { "Failed to handle uri $uri with extras: $extras" }
        }
    }

    /**
     * Opens the remote scheme Uri in [BrazeWebViewActivity] while also populating the back stack.
     *
     * @see [UriAction.getIntentArrayWithConfiguredBackStack]
     */
    protected fun openUriWithWebViewActivityFromPush(context: Context, uri: Uri, extras: Bundle?) {
        val configurationProvider = BrazeConfigurationProvider(context)
        try {
            val webViewIntent = getWebViewActivityIntent(context, uri, extras)
            context.startActivities(
                getIntentArrayWithConfiguredBackStack(
                    context,
                    extras,
                    webViewIntent,
                    configurationProvider
                )
            )
        } catch (e: Exception) {
            brazelog(E, e) { "Braze WebView Activity not opened successfully." }
        }
    }

    /**
     * Uses an [Intent.ACTION_VIEW] intent to open the [Uri] and places the main activity of the
     * activity on the back stack.
     *
     * @see [UriAction.getIntentArrayWithConfiguredBackStack]
     */
    protected fun openUriWithActionViewFromPush(context: Context, uri: Uri, extras: Bundle?) {
        val configurationProvider = BrazeConfigurationProvider(context)
        try {
            val uriIntent = getActionViewIntent(context, uri, extras)
            context.startActivities(
                getIntentArrayWithConfiguredBackStack(
                    context,
                    extras,
                    uriIntent,
                    configurationProvider
                )
            )
        } catch (e: ActivityNotFoundException) {
            brazelog(W, e) { "Could not find appropriate activity to open for deep link $uri" }
        }
    }

    /**
     * Returns an intent that opens the uri inside of a [BrazeWebViewActivity].
     */
    protected fun getWebViewActivityIntent(context: Context, uri: Uri, extras: Bundle?): Intent {
        val configurationProvider = BrazeConfigurationProvider(context)
        val customWebViewActivityClassName = configurationProvider.customHtmlWebViewActivityClassName

        // If the class is valid and is manifest registered, use it as the launching intent
        val webViewActivityIntent: Intent = if (!customWebViewActivityClassName.isNullOrBlank()
            && isActivityRegisteredInManifest(
                    context,
                    customWebViewActivityClassName
                )
        ) {
            brazelog { "Launching custom WebView Activity with class name: $customWebViewActivityClassName" }
            Intent()
                .setClassName(context, customWebViewActivityClassName)
        } else {
            Intent(context, BrazeWebViewActivity::class.java)
        }
        if (extras != null) {
            webViewActivityIntent.putExtras(extras)
        }
        webViewActivityIntent.putExtra(Constants.BRAZE_WEBVIEW_URL_EXTRA, uri.toString())
        return webViewActivityIntent
    }

    protected fun getActionViewIntent(context: Context, uri: Uri, extras: Bundle?): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri
        if (extras != null) {
            intent.putExtras(extras)
        }

        // If the current app can already handle the intent, default to using it
        val resolveInfos = context.packageManager.queryIntentActivities(intent, 0)
        if (resolveInfos.size > 1) {
            for (resolveInfo in resolveInfos) {
                if (resolveInfo.activityInfo.packageName == context.packageName) {
                    brazelog { "Setting deep link intent package to ${resolveInfo.activityInfo.packageName}." }
                    intent.setPackage(resolveInfo.activityInfo.packageName)
                    break
                }
            }
        }
        return intent
    }

    /**
     * Gets an [Intent] array that has the configured back stack functionality.
     *
     * @param targetIntent The ultimate intent to be followed. For example, the main/launcher intent would be the penultimate [Intent].
     * @see [BrazeConfigurationProvider.isPushDeepLinkBackStackActivityEnabled]
     * @see [BrazeConfigurationProvider.pushDeepLinkBackStackActivityClassName]
     */
    @VisibleForTesting
    @Suppress("NestedBlockDepth")
    fun getIntentArrayWithConfiguredBackStack(
        context: Context,
        extras: Bundle?,
        targetIntent: Intent,
        configurationProvider: BrazeConfigurationProvider
    ): Array<Intent> {
        // The root intent will either point to the launcher activity,
        // some custom activity, or nothing if the back-stack is disabled.
        var rootIntent: Intent? = null
        if (configurationProvider.isPushDeepLinkBackStackActivityEnabled) {
            // If a custom back stack class is defined, then set it
            val activityClass = configurationProvider.pushDeepLinkBackStackActivityClassName
            if (activityClass.isNullOrBlank()) {
                brazelog(I) { "Adding main activity intent to back stack while opening uri from push" }
                rootIntent = getMainActivityIntent(context, extras)
            } else {
                // Check if the activity is registered in the manifest. If not, then add nothing to the back stack
                if (isActivityRegisteredInManifest(context, activityClass)) {
                    brazelog(I) { "Adding custom back stack activity while opening uri from push: $activityClass" }
                    rootIntent = extras?.let {
                        Intent()
                            .setClassName(context, activityClass)
                            .setFlags(
                                BrazeDeeplinkHandler.getInstance()
                                    .getIntentFlags(IBrazeDeeplinkHandler.IntentFlagPurpose.URI_ACTION_BACK_STACK_GET_ROOT_INTENT)
                            )
                            .putExtras(it)
                    }
                } else {
                    brazelog(I) { "Not adding unregistered activity to the back stack while opening uri from push: $activityClass" }
                }
            }
        } else {
            brazelog(I) { "Not adding back stack activity while opening uri from push due to disabled configuration setting." }
        }
        return if (rootIntent == null) {
            // Calling startActivities() from outside of an Activity
            // context requires the FLAG_ACTIVITY_NEW_TASK flag on the first Intent
            targetIntent.flags = BrazeDeeplinkHandler.getInstance()
                .getIntentFlags(IBrazeDeeplinkHandler.IntentFlagPurpose.URI_ACTION_BACK_STACK_ONLY_GET_TARGET_INTENT)

            // Just return the target intent by itself
            arrayOf(targetIntent)
        } else {
            // Return the intents in their stack order
            arrayOf(rootIntent, targetIntent)
        }
    }
}
