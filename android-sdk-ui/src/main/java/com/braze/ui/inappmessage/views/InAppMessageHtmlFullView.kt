package com.braze.ui.inappmessage.views

import android.content.Context
import android.util.AttributeSet
import com.braze.ui.R

open class InAppMessageHtmlFullView(context: Context?, attrs: AttributeSet?) :
    InAppMessageHtmlBaseView(context, attrs) {

    override fun getWebViewViewId(): Int = R.id.com_braze_inappmessage_html_full_webview
}
