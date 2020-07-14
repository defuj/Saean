package com.saean.app.store

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saean.app.R
import com.saean.app.helper.Cache
import com.saean.app.store.model.BannerModel
import com.saean.app.store.model.ProductAdapter
import com.saean.app.store.model.ProductModel
import kotlinx.android.synthetic.main.activity_store_setting_product.*

class StoreSettingProductActivity : AppCompatActivity() {
    private var product : ArrayList<ProductModel>? = null
    private lateinit var database: FirebaseDatabase
    private var sharedPreferences : SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_setting_product)
        database = FirebaseDatabase.getInstance()
        sharedPreferences =getSharedPreferences(Cache.cacheName,0)

        setupFunctions()
    }

    private fun setupFunctions() {
        toolbarProduct.setNavigationOnClickListener {
            finish()
        }

        btnAddProduct.setOnClickListener {
            startActivity(Intent(this,StoreAddProductActivity::class.java))
        }

        recyclerStoreProduct!!.addItemDecoration(object : RecyclerView.ItemDecoration(){
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val position = parent.getChildAdapterPosition(view)
                val spanCount = 2
                val spacing = 10
                if(position>=0){
                    val column = position % spanCount
                    outRect.left = spacing - column * spacing / spanCount
                    outRect.right = (column + 1) * spacing / spanCount

                    if (position < spanCount) {
                        outRect.top = spacing
                    }

                    outRect.bottom = spacing
                }else {
                    outRect.left = 0
                    outRect.right = 0
                    outRect.top = 0
                    outRect.bottom = 0
                }
            }
        })

        setupProduct()
    }

    private fun setupProduct() {
        val storeID = sharedPreferences!!.getString(Cache.storeID,"")

        database.getReference("product/goods/$storeID").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                recyclerStoreProduct.visibility = View.GONE
                containerNoProduct.visibility = View.VISIBLE
                containerNoticeProduct.visibility = View.VISIBLE
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChildren()){
                        recyclerStoreProduct.visibility = View.VISIBLE
                        containerNoProduct.visibility = View.GONE
                        containerNoticeProduct.visibility = View.GONE

                        product = ArrayList()
                        product!!.clear()
                        recyclerStoreProduct.layoutManager = GridLayoutManager(this@StoreSettingProductActivity,2, GridLayoutManager.VERTICAL,false)

                        for(products in snapshot.children){
                            val model = ProductModel()
                            model.productID = products.key.toString()
                            model.productName = products.child("goodsName").getValue(String::class.java)
                            model.productDescription = products.child("goodsDescription").getValue(String::class.java)
                            model.productStock = products.child("goodsStock").getValue(Int::class.java)
                            try {
                                model.productPrice = products.child("goodsPrice").getValue(Double::class.java)!!
                            }catch (e:Exception){
                                model.productPrice = 0.0
                            }

                            var bannerProduct: ArrayList<BannerModel>
                            if(products.child("goodsPicture").exists()){
                                bannerProduct = ArrayList()
                                bannerProduct.clear()
                                for(images in products.child("goodsPicture").children){
                                    val banner = BannerModel()
                                    banner.bannerID = images.key.toString()
                                    banner.bannerImage = images.getValue(String::class.java)
                                    bannerProduct.add(banner)
                                }
                                model.productImage = bannerProduct
                            }
                            model.storeID = storeID
                            product!!.add(model)
                        }

                        val adapter = ProductAdapter(this@StoreSettingProductActivity,product!!)
                        recyclerStoreProduct.adapter = adapter

                    }else{
                        recyclerStoreProduct.visibility = View.GONE
                        containerNoProduct.visibility = View.VISIBLE
                        containerNoticeProduct.visibility = View.VISIBLE
                    }
                }else{
                    recyclerStoreProduct.visibility = View.GONE
                    containerNoProduct.visibility = View.VISIBLE
                    containerNoticeProduct.visibility = View.VISIBLE
                }
            }
        })
    }
}