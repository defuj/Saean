package com.saean.app.menus.fragments

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.saean.app.R
import kotlinx.android.synthetic.main.fragment_favorite.*
import ru.nikartm.support.ImageBadgeView


class FavoriteFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBadgesToolbar()
        setupFunctions()
    }

    private fun setupBadgesToolbar() {
        val menu = toolbarFavorite.menu

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
        setupRefresh()
    }

    private fun setupRefresh() {
        refreshFavorite.setOnRefreshListener {
            object : CountDownTimer(3000,1000){
                override fun onFinish() {
                    refreshFavorite.isRefreshing = false
                }

                override fun onTick(millisUntilFinished: Long) {

                }
            }.start()
        }
    }
}