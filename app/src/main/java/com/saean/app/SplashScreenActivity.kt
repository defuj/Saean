package com.saean.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import cn.pedant.SweetAlert.SweetAlertDialog

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        object : CountDownTimer(3000,1000){
            override fun onFinish() {
                startActivity(Intent(this@SplashScreenActivity,HomeActivity::class.java))
                finish()
            }

            override fun onTick(millisUntilFinished: Long) {

            }
        }.start()
    }
}