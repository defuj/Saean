package com.saean.app.store

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.saean.app.R
import kotlinx.android.synthetic.main.activity_store_setting_notes.*

class StoreSettingNotesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_setting_notes)

        setupFunctions()
    }

    private fun setupFunctions() {
        toolbarNotes.setNavigationOnClickListener {
            finish()
        }
    }
}