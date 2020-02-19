package com.appboy.sample.activity

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.appboy.enums.inappmessage.DismissType
import com.appboy.models.InAppMessageModal
import com.appboy.models.MessageButton
import com.appboy.sample.R
import com.appboy.ui.inappmessage.AppboyInAppMessageManager

/**
 * Activity whose sole purpose is to host a button that shows a basic IAM on screen.
 */
class InAppMessageSandboxActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_in_app_message_sandbox)

    findViewById<View>(R.id.bSandboxDisplayMessage).setOnClickListener { this.displayMessage() }
    findViewById<View>(R.id.bSandboxDummyButton).setOnClickListener { Toast.makeText(this, "dummy button pressed!", Toast.LENGTH_SHORT).show() }
  }

  private fun displayMessage() {
    // Create the message
    val modal = InAppMessageModal()
    modal.header = "hello"
    modal.message = "world"
    modal.imageUrl = getString(R.string.appboy_image_url_1600w_500h)
    modal.dismissType = DismissType.MANUAL

    val button1 = MessageButton()
    button1.text = "Button 1"
    button1.borderColor = Color.RED
    button1.backgroundColor = Color.BLUE

    val button2 = MessageButton()
    button2.text = "Button 2"
    button2.borderColor = Color.CYAN
    button2.backgroundColor = Color.BLUE

    modal.messageButtons = listOf(button1, button2)
    AppboyInAppMessageManager.getInstance().addInAppMessage(modal)
    AppboyInAppMessageManager.getInstance().requestDisplayInAppMessage()
  }
}
