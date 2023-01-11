package com.braze.unity

import android.content.Intent
import android.os.Bundle
import com.unity3d.player.UnityPlayerActivity

/**
 * This is a wrapper subclass of the [com.unity3d.player.UnityPlayerActivity] class. It calls the necessary Braze methods
 * to ensure that analytics are collected and that push notifications are properly forwarded to
 * the Unity application.
 *
 * NOTE: This Activity is not compatible with Prime31 plugins. If you are using any Prime31 plugins, you
 * must use the [BrazeUnityPlayerActivity] in the com.braze.unity.prime31compatible package instead.
 */
open class BrazeUnityPlayerActivity : UnityPlayerActivity() {
    private lateinit var unityActivityWrapper: BrazeUnityActivityWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        unityActivityWrapper = BrazeUnityActivityWrapper()
        unityActivityWrapper.onCreateCalled(this)
    }

    override fun onStart() {
        super.onStart()
        unityActivityWrapper.onStartCalled(this)
    }

    public override fun onResume() {
        super.onResume()
        unityActivityWrapper.onResumeCalled(this)
    }

    public override fun onPause() {
        unityActivityWrapper.onPauseCalled(this)
        super.onPause()
    }

    override fun onStop() {
        unityActivityWrapper.onStopCalled(this)
        super.onStop()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        unityActivityWrapper.onNewIntentCalled(intent, this)
    }

    @JvmName("onNewUnityInAppMessageManagerAction")
    fun onNewUnityInAppMessageManagerAction(actionEnumValue: Int) {
        unityActivityWrapper.onNewUnityInAppMessageManagerAction(actionEnumValue)
    }

    @JvmName("launchContentCardsActivity")
    fun launchContentCardsActivity() {
        unityActivityWrapper.launchContentCardsActivity(this)
    }

    @JvmName("setInAppMessageListener")
    fun setInAppMessageListener() {
        unityActivityWrapper.setInAppMessageListener()
    }

    @JvmName("requestDisplayInAppMessage")
    fun requestDisplayInAppMessage() {
        unityActivityWrapper.requestInAppMessageDisplay()
    }
}
