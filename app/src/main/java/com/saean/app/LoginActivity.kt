package com.saean.app

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import cn.pedant.SweetAlert.SweetAlertDialog
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private var callbackManager : CallbackManager? = null
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        database = FirebaseDatabase.getInstance()
        sharedPreferences = getSharedPreferences(Cache.cacheName,0)

        setupFunctions()
    }

    private fun setupFunctions() {
        btnLogin!!.setOnClickListener {
            if(login_email.text.isNotEmpty()){
                if(MyFunctions.isEmailValid(login_email.text.toString())){
                    if(login_password.text.isNotEmpty()){
                        if(login_password.text.length >= 6){
                            login(MyFunctions.changeToUnderscore(login_email.text.toString()),MyFunctions.encrypt(login_password.text.toString()))
                        }else{
                            showDialog("Oops","Sandi minimal 6 digit",SweetAlertDialog.WARNING_TYPE)
                        }
                    }else{
                        showDialog("Oops","Sandi belum diisi",SweetAlertDialog.WARNING_TYPE)
                    }
                }else{
                    showDialog("Oops","Email tidak valid",SweetAlertDialog.WARNING_TYPE)
                }
            }else{
                showDialog("Oops","Email belum diisi",SweetAlertDialog.WARNING_TYPE)
            }
        }

        login_facebook.setOnClickListener {
            showDialog("Dalam Pengembangan","Fitur ini dalam proses pengembangan.",SweetAlertDialog.NORMAL_TYPE)
        }

        forgotPassword.setOnClickListener {
            showDialog("Dalam Pengembangan","Fitur ini dalam proses pengembangan.",SweetAlertDialog.NORMAL_TYPE)
        }

        login_google.setOnClickListener {
            val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(application, gso)
            val signInIntent: Intent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        btn_register.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
            finish()
        }
    }

    private fun login(email: String, password: String) {
        Log.e("PASSWORD", password)
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
                    progress.dismissWithAnimation()
                    if(snapshot.exists()){
                        if(snapshot.child("userPassword").getValue(String::class.java) == password){
                            val edit = sharedPreferences!!.edit()
                            edit.putBoolean(Cache.logged,true)
                            edit.putString(Cache.email,login_email.text.toString())
                            try {
                                edit.putString(Cache.name,snapshot.child("userName").getValue(String::class.java))
                                edit.putString(Cache.pass,snapshot.child("userPassword").getValue(String::class.java))
                                edit.putString(Cache.address,snapshot.child("userAddress").getValue(String::class.java))
                                edit.putString(Cache.picture,snapshot.child("userPicture").getValue(String::class.java))
                                edit.putString(Cache.phone,snapshot.child("userPhone").getValue(String::class.java))
                            }catch (e:Exception){

                            }
                            edit.apply()

                            startActivity(Intent(this@LoginActivity,HomeActivity::class.java))
                            finish()
                        }else{
                            showDialog("Oops","Kata sandi salah",SweetAlertDialog.ERROR_TYPE)
                        }
                    }else{
                        showDialog("Oops","Email tidak terdaftar",SweetAlertDialog.ERROR_TYPE)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //callbackManager!!.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task : Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)!!
            loginGoogle(account)
        }
    }

    private fun loginGoogle(account: GoogleSignInAccount) {
        val email = MyFunctions.changeToUnderscore(account.email!!)
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
                        val edit = sharedPreferences!!.edit()
                        edit.putBoolean(Cache.logged,true)
                        edit.putString(Cache.email,login_email.text.toString())
                        try {
                            edit.putString(Cache.name,snapshot.child("userName").getValue(String::class.java))
                            edit.putString(Cache.pass,snapshot.child("userPassword").getValue(String::class.java))
                            edit.putString(Cache.address,snapshot.child("userAddress").getValue(String::class.java))
                            edit.putString(Cache.picture,snapshot.child("userPicture").getValue(String::class.java))
                            edit.putString(Cache.phone,snapshot.child("userPhone").getValue(String::class.java))
                        }catch (e:Exception){

                        }
                        edit.apply()

                        startActivity(Intent(this@LoginActivity,HomeActivity::class.java))
                        finish()
                    }else{
                        Log.e("REGISTER","Email = ${account.email}")
                        Log.e("REGISTER","Picture = ${account.photoUrl}")
                        Log.e("REGISTER","DisplayName = ${account.displayName}")
                        Log.e("REGISTER","FamilyName = ${account.familyName}")
                        Log.e("REGISTER","GivenName = ${account.givenName}")

                        database.getReference("user").child(email).child("userAddress").setValue("")
                        database.getReference("user").child(email).child("userPassword").setValue("")
                        database.getReference("user").child(email).child("userPicture").setValue("${account.photoUrl}")
                        database.getReference("user").child(email).child("userName").setValue("${account.displayName}")
                        database.getReference("user").child(email).child("userPhone").setValue("")
                        database.getReference("user").child(email).child("userVerify").setValue(0)

                        val edit = sharedPreferences!!.edit()
                        edit.putBoolean(Cache.logged,true)
                        edit.putString(Cache.email,account.email)
                        try {
                            edit.putString(Cache.name,account.displayName)
                            edit.putString(Cache.pass,"")
                            edit.putString(Cache.address,"")
                            edit.putString(Cache.picture,if(account.photoUrl != null){account.photoUrl.toString()}else{""})
                            edit.putString(Cache.phone,"")
                        }catch (e:Exception){

                        }
                        edit.apply()
                        progress.dismissWithAnimation()

                        startActivity(Intent(this@LoginActivity,HomeActivity::class.java))
                        finish()
                    }
                }
            })
        }else{
            showDialog("Oops","Tidak ada koneksi internet",SweetAlertDialog.ERROR_TYPE)
        }
    }
}