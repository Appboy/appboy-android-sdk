package com.appboy.sample.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import com.appboy.sample.R
import com.appboy.sample.activity.settings.SettingsFragment

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
}
