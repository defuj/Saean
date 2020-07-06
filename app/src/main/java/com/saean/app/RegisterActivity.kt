package com.saean.app

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        database = FirebaseDatabase.getInstance()
        sharedPreferences = getSharedPreferences(Cache.cacheName,0)

        setupFunctions()
    }

    private fun setupFunctions() {
        btnSign.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }

        btnBuatAkun.setOnClickListener {
            if(regis_name.text.toString().isNotEmpty() &&
                regis_address.text.toString().isNotEmpty() &&
                regis_email.text.toString().isNotEmpty() &&
                regis_password.text.toString().isNotEmpty() &&
                regis_password.text.toString().isNotEmpty()){
                if(MyFunctions.isEmailValid(regis_email.text.toString())){
                    if(regis_password.text.length >=6){
                        if(regis_password.text.toString() == regis_confirm_password.text.toString()){
                            register()
                        }else{
                            showDialog("Oops","Konfirmasi kata sandi tidak sama",SweetAlertDialog.WARNING_TYPE)
                        }
                    }else{
                        showDialog("Oops","Kata sandi minimal 6 digit",SweetAlertDialog.WARNING_TYPE)
                    }
                }else{
                    showDialog("Oops","Email tidak valid",SweetAlertDialog.WARNING_TYPE)
                }
            }else{
                showDialog("Perhatian","Harap untuk melengkapi form",SweetAlertDialog.ERROR_TYPE)
            }
        }
    }

    private fun register() {
        val email = MyFunctions.changeToUnderscore(regis_email.text.toString())
        if(MyFunctions.getConnectivityStatus(this)){
            val progress = SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE)
            progress.titleText = "Silahkan tunggu"
            progress.setCancelable(false)
            progress.show()

            database.getReference("user/$email").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    progress.dismissWithAnimation()
                    showDialog("Oops","Koneksi bermasalah",SweetAlertDialog.ERROR_TYPE)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        progress.dismissWithAnimation()
                        showDialog("Oops","Email telah ada yang menggunakan!",SweetAlertDialog.ERROR_TYPE)
                    }else{
                        val register = database.getReference("user/$email")
                        register.child("userName").setValue(regis_name.text.toString())
                        register.child("userAddress").setValue("${regis_address.text}")
                        register.child("userPassword").setValue(MyFunctions.encrypt(regis_password.text.toString()))
                        register.child("userPicture").setValue("")
                        register.child("userPhone").setValue("${regis_address.text}")
                        register.child("userGender").setValue(regis_gender.selectedItem.toString())

                        val edit = sharedPreferences!!.edit()
                        edit.putBoolean(Cache.logged,true)
                        edit.putString(Cache.email,regis_email.text.toString())
                        try {
                            edit.putString(Cache.name,regis_name.text.toString())
                            edit.putString(Cache.pass,MyFunctions.encrypt(regis_password.text.toString()))
                            edit.putString(Cache.address,regis_address.text.toString())
                            edit.putString(Cache.picture,"")
                            edit.putString(Cache.phone,"${regis_address.text}")
                        }catch (e:Exception){

                        }
                        edit.apply()

                        progress.dismissWithAnimation()
                        startActivity(Intent(this@RegisterActivity,HomeActivity::class.java))
                        finish()
                    }
                }
            })
        }else{
            showDialog("Oops","Tidak ada koneksi internet",SweetAlertDialog.ERROR_TYPE)
        }
    }

    private fun showDialog(title : String, content : String, type : Int){
        val dialog = SweetAlertDialog(this,type)
        dialog.titleText = title
        dialog.contentText = content
        dialog.show()
    }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}