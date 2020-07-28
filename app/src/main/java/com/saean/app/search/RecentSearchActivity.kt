package com.saean.app.search

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.helper.MyFunctions
import kotlinx.android.synthetic.main.activity_recent_search.*

class RecentSearchActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recent_search)
        database = FirebaseDatabase.getInstance()
        sharedPreferences = getSharedPreferences(Cache.cacheName,0)

        setupFunctions()

        if(MyFunctions.checkBoolean(this,Cache.logged)){
            setupRecentProduct()
            setupRecentSearch()
        }
    }

    private fun setupFunctions() {
        searchEdit.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchEdit, InputMethodManager.SHOW_IMPLICIT)

        toolbarSearchRecent.setNavigationOnClickListener {
            finish()
        }

        btnMicSearch.setOnClickListener {
            Toast.makeText(this,"Under development", Toast.LENGTH_SHORT).show()
        }

        val recentSearch: String = try {
            intent.getStringExtra("search")!!
        }catch (e:Exception){
            ""
        }

        if(recentSearch.isNotEmpty()){
            searchEdit.setText(recentSearch)
            btnCancelSearch!!.visibility = View.VISIBLE
            btnClearSearch!!.visibility = View.VISIBLE
            btnMicSearch!!.visibility = View.GONE
        }else{
            btnCancelSearch!!.visibility = View.GONE
            btnClearSearch!!.visibility = View.GONE
            btnMicSearch!!.visibility = View.VISIBLE
        }

        searchEdit!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(searchEdit!!.text.toString().isNotEmpty()){
                    btnCancelSearch!!.visibility = View.VISIBLE
                    btnClearSearch!!.visibility = View.VISIBLE
                    btnMicSearch!!.visibility = View.GONE
                }else{
                    btnCancelSearch!!.visibility = View.GONE
                    btnClearSearch!!.visibility = View.GONE
                    btnMicSearch!!.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        btnCancelSearch!!.setOnClickListener {
            finish()
        }

        btnClearSearch.setOnClickListener {
            searchEdit.text.clear()
            MyFunctions.closeKeyboard(this)
            btnCancelSearch!!.visibility = View.GONE
            btnClearSearch!!.visibility = View.GONE
            btnMicSearch!!.visibility = View.VISIBLE
        }

        searchEdit!!.setOnEditorActionListener { v, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                MyFunctions.closeKeyboard(this)
                v.clearFocus()

                if(searchEdit!!.text.toString().isNotEmpty()){
                    val intent = Intent(this,SearchActivity::class.java)
                    intent.putExtra("search",searchEdit!!.text.toString())
                    if(MyFunctions.checkBoolean(this,Cache.logged)){
                        val email = MyFunctions.changeToUnderscore(sharedPreferences!!.getString(Cache.email,"")!!)
                        database.getReference("user/$email/recentSearch").child(MyFunctions.changeToUnderscore(searchEdit!!.text.toString())).setValue(MyFunctions.getTime())
                    }

                    startActivity(intent)
                    searchEdit.text.clear()
                    finish()
                }
            }
            return@setOnEditorActionListener true
        }
    }

    private fun setupRecentSearch() {
        if(MyFunctions.checkBoolean(this,Cache.logged)){
            val email = MyFunctions.changeToUnderscore(sharedPreferences!!.getString(Cache.email,"")!!)
            database.getReference("user/$email/recentSearch").addValueEventListener(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    containerLastSearch.visibility = View.GONE
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        if(snapshot.hasChildren()){
                            containerLastSearch.visibility = View.VISIBLE

                            recyclerLastSearch.layoutManager = LinearLayoutManager(this@RecentSearchActivity,
                                LinearLayoutManager.VERTICAL,false)
                            val recent : ArrayList<RecentSearchModel> = ArrayList()
                            recent.clear()

                            for(content in snapshot.children){
                                val model = RecentSearchModel()
                                model.search = content.key.toString().replace("_"," ")
                                model.time = content.getValue(Long::class.java)
                                recent.add(model)
                            }
                            recent.sortWith(Comparator { o1, o2 ->  o1.time!!.compareTo(o2.time!!)})
                            recent.reverse()
                            val adapter = RecentSearchAdapter(this@RecentSearchActivity,recent)
                            recyclerLastSearch.adapter = adapter
                        }else{
                            containerLastSearch.visibility = View.GONE
                        }
                    }else{
                        containerLastSearch.visibility = View.GONE
                    }
                }
            })
        }
    }

    private fun setupRecentProduct() {
        val email = MyFunctions.changeToUnderscore(sharedPreferences!!.getString(Cache.email,"")!!)
        database.getReference("user/$email/recentStore").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                containerLastSeen.visibility = View.GONE
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChildren()){
                        containerLastSeen.visibility = View.VISIBLE

                        recyclerLastSeen.layoutManager = LinearLayoutManager(this@RecentSearchActivity,LinearLayoutManager.HORIZONTAL,false)
                        val product : ArrayList<RecentStoreModel> = ArrayList()
                        product.clear()

                        var i = 0
                        for(content in snapshot.children){
                            if(i <=3 ){
                                val model = RecentStoreModel()
                                model.storeID = content.key.toString()
                                model.storePicture = content.child("storePicture").getValue(String::class.java)
                                model.time = content.child("time").getValue(Long::class.java)
                                product.add(model)
                            }
                            i+=1
                        }

                        product.sortWith(Comparator { o1, o2 ->  o1.time!!.compareTo(o2.time!!)})
                        //product.reverse()

                        val adapter = RecentStoreAdapter(this@RecentSearchActivity,product)
                        recyclerLastSeen.adapter = adapter
                    }else{
                        containerLastSeen.visibility = View.GONE
                    }
                }else{
                    containerLastSeen.visibility = View.GONE
                }
            }
        })
    }
}