package com.appboy.sample.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.appboy.enums.inappmessage.DismissType
import com.appboy.models.InAppMessageModal
import com.appboy.models.MessageButton
import com.appboy.sample.R
import com.appboy.ui.inappmessage.AppboyInAppMessageManager
import java.util.*

/**
 * Activity whose sole purpose is to host a button that shows a basic IAM on screen.
 */
class InAppMessageSandboxActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_in_app_message_sandbox)

    findViewById<View>(R.id.bSandboxDisplayMessage2).setOnClickListener { this.displayMessage(2) }
    findViewById<View>(R.id.bSandboxDisplayMessage1).setOnClickListener { this.displayMessage(1) }
    findViewById<View>(R.id.bSandboxDisplayMessage0).setOnClickListener { this.displayMessage(0) }
    findViewById<View>(R.id.bSandboxDummyButton).setOnClickListener { Toast.makeText(this, "dummy button pressed!", Toast.LENGTH_SHORT).show() }
  }

  private fun displayMessage(numButtons: Int) {
    // Create the message
    val modal = InAppMessageModal()
    modal.header = "hello"
    modal.message = "world"
    modal.imageUrl = getString(R.string.appboy_image_url_1600w_500h)
    modal.dismissType = DismissType.MANUAL

    val rnd = Random()
    val randomColor = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    modal.closeButtonColor = randomColor

    val button1 = MessageButton()
    button1.text = "Button 1"
    button1.borderColor = Color.RED
    button1.backgroundColor = Color.BLUE

    val button2 = MessageButton()
    button2.text = "Button 2"
    button2.borderColor = Color.CYAN
    button2.backgroundColor = Color.BLUE

    when (numButtons) {
      2 -> modal.messageButtons = listOf(button1, button2)
      1 -> modal.messageButtons = listOf(button1)
    }
    AppboyInAppMessageManager.getInstance().addInAppMessage(modal)
    AppboyInAppMessageManager.getInstance().requestDisplayInAppMessage()
  }
}
