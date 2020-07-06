package com.saean.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.saean.app.helper.Cache

class SplashScreenActivity : AppCompatActivity() {
    private var sharedPreferences : SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        sharedPreferences = getSharedPreferences(Cache.cacheName,0)

        object : CountDownTimer(3000,1000){
            override fun onFinish() {
                if(sharedPreferences!!.getBoolean(Cache.logged,false)){
                    startActivity(Intent(this@SplashScreenActivity,HomeActivity::class.java))
                    finish()
                }else{
                    startActivity(Intent(this@SplashScreenActivity,LoginActivity::class.java))
                    finish()
                }
            }

            override fun onTick(millisUntilFinished: Long) {

            }
        }.start()
    }
}