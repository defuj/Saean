package com.saean.app.menus.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.saean.app.R
import com.saean.app.helper.MyFunctions
import kotlinx.android.synthetic.main.fragment_transactions.*
import ru.nikartm.support.ImageBadgeView


class TransactionsFragment : Fragment() {
    private var filter = "Semua"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBadgesToolbar()
        setupFunctions()
    }

    private fun setupBadgesToolbar() {
        val menu = toolbarTransaction.menu

        val menuMessage = menu.getItem(1).setActionView(R.layout.item_badges_toolbar)
        val menuNotification = menu.getItem(2).setActionView(R.layout.item_badges_toolbar)

        val actionViewMessage = menuMessage.actionView
        val actionViewNotification = menuNotification.actionView

        val badgesMessage = actionViewMessage.findViewById<ImageBadgeView>(R.id.badges)
        badgesMessage.setImageResource(R.drawable.ic_menu_toolbar_home_messages)

        val badgesNotification = actionViewNotification.findViewById<ImageBadgeView>(R.id.badges)
        badgesNotification.setImageResource(R.drawable.ic_menu_toolbar_home_notification)

        badgesMessage.badgeValue = 1
        badgesNotification.badgeValue = 3
    }

    private fun setupFunctions() {
        setupFilter()
        setupSearch()
        setupRefresh()
    }

    private fun setupFilter() {
        btnFilter.setOnClickListener {
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(activity!!)
            alertDialog.setTitle("Filter")
            val items = arrayOf("Semua", "Selesai", "Diproses", "Batal")
            val checkedItem = if(filter == "Semua"){0}else if(filter == "Selesai"){1}else if(filter == "Diproses"){2}else{3}
            alertDialog.setSingleChoiceItems(items, checkedItem) { dialog, which ->
                when (which) {
                    0 -> {
                        filter = "Semua"
                        btnFilter.text = "Semua"
                        dialog.dismiss()
                    }
                    1 -> {
                        filter = "Selesai"
                        btnFilter.text = "Selesai"
                        dialog.dismiss()
                    }
                    2 -> {
                        filter = "Diproses"
                        btnFilter.text = "Diproses"
                        dialog.dismiss()
                    }
                    3 -> {
                        filter = "Batal"
                        btnFilter.text = "Batal"
                        dialog.dismiss()
                    }
                }
            }
            val alert: AlertDialog = alertDialog.create()
            //alert.setCanceledOnTouchOutside(false)
            alert.show()
        }
    }

    private fun setupSearch() {
        searchTransaction!!.setOnEditorActionListener { v, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                MyFunctions.closeKeyboard(activity!!)
                v.clearFocus()
                //do search
            }
            return@setOnEditorActionListener true
        }
    }

    private fun setupRefresh() {
        refreshTransaction.setOnRefreshListener {
            object : CountDownTimer(3000,1000){
                override fun onFinish() {
                    refreshTransaction.isRefreshing = false
                }

                override fun onTick(millisUntilFinished: Long) {

                }
            }.start()
        }
    }
}