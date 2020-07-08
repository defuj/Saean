package com.saean.app.createStore.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.pedant.SweetAlert.SweetAlertDialog
import com.saean.app.R
import com.saean.app.createStore.CreateStoreActivity
import kotlinx.android.synthetic.main.fragment_create_store1.*

class CreateStore1Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_store1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFunctions()
    }

    private fun setupFunctions() {
        toolbarCreateStore1.setNavigationOnClickListener {
            val dialog = SweetAlertDialog(activity!!,SweetAlertDialog.WARNING_TYPE)
            dialog.titleText = "Oops"
            dialog.contentText = "Anda ingin membatalkan pembuatan toko?"
            dialog.confirmText = "Iya"
            dialog.cancelText = "Tidak"
            dialog.setConfirmClickListener {
                dialog.dismissWithAnimation()
                activity!!.finish()
            }
            dialog.setOnCancelListener {
                dialog.dismissWithAnimation()
            }
            dialog.show()
        }

        setupForm()
    }

    private fun setupForm() {
        storeName.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if(s!!.isNotEmpty()){
                    if(storeDescription.text.toString().isNotEmpty()){
                        btnNext1.setBackgroundResource(R.drawable.background_button_create_store_active)
                        btnNext1.isEnabled = true
                    }else{
                        btnNext1.setBackgroundResource(R.drawable.background_button_create_store_disabled)
                        btnNext1.isEnabled = false
                    }
                }else{
                    btnNext1.setBackgroundResource(R.drawable.background_button_create_store_disabled)
                    btnNext1.isEnabled = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        storeDescription.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if(s!!.isNotEmpty()){
                    if(storeName.text.toString().isNotEmpty()){
                        btnNext1.setBackgroundResource(R.drawable.background_button_create_store_active)
                        btnNext1.isEnabled = true
                    }else{
                        btnNext1.setBackgroundResource(R.drawable.background_button_create_store_disabled)
                        btnNext1.isEnabled = false
                    }
                }else{
                    btnNext1.setBackgroundResource(R.drawable.background_button_create_store_disabled)
                    btnNext1.isEnabled = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        btnNext1.setOnClickListener {
            if(storeName.text.toString().isNotEmpty() && storeDescription.text.toString().isNotEmpty()){
                (activity as CreateStoreActivity).storeName = storeName.text.toString()
                (activity as CreateStoreActivity).storeDescription = storeDescription.text.toString()

                (activity as CreateStoreActivity).setCurrentItem(1)
            }else{
                val dialog = SweetAlertDialog(activity!!,SweetAlertDialog.WARNING_TYPE)
                dialog.titleText = "Oops"
                dialog.contentText = "Harap lengkapi form"
                dialog.show()
            }
        }
    }
}