package com.appboy.sample.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import com.appboy.sample.R
import com.appboy.sample.activity.settings.SettingsFragment
import com.appboy.sample.util.EnvironmentUtils.Companion.analyzeBitmapForEnvironmentBarcode

class SettingsActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.settings_page)
    val toolbar: Toolbar = findViewById(R.id.toolbar)
    toolbar.title = getString(R.string.settings)
    toolbar.navigationIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_back_button_droidboy, null)
    toolbar.setNavigationOnClickListener { onBackPressed() }
    supportFragmentManager
        .beginTransaction()
        .replace(R.id.settingsFragmentContainer, SettingsFragment())
        .commit()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == SettingsFragment.REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
      val extras = data?.extras
      val bitmap = extras!!["data"] as Bitmap?
      analyzeBitmapForEnvironmentBarcode(this, bitmap!!)
    }
  }
}
