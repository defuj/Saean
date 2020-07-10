package com.saean.app.store

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.saean.app.R
import kotlinx.android.synthetic.main.activity_store_setting_services.*

class StoreSettingServicesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_setting_services)

        setupFunctions()
    }

    private fun setupFunctions() {
        toolbarService.setNavigationOnClickListener {
            finish()
        }

        btnClose.setOnClickListener {
            containerNoticeService.visibility = View.GONE
        }

        btnAddService.setOnClickListener {
            startActivity(Intent(this,StoreAddProductActivity::class.java))
        }
    }
}