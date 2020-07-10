package com.saean.app.store

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.saean.app.R
import kotlinx.android.synthetic.main.activity_store_setting_product.*

class StoreSettingProductActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_setting_product)

        setupFunctions()
    }

    private fun setupFunctions() {
        toolbarProduct.setNavigationOnClickListener {
            finish()
        }

        btnAddProduct.setOnClickListener {
            startActivity(Intent(this,StoreAddProductActivity::class.java))
        }
    }
}