package com.saean.app.menus

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.saean.app.menus.fragments.AccountFragment
import com.saean.app.menus.fragments.HomeFragment
import com.saean.app.menus.fragments.MapsFragment
import com.saean.app.menus.fragments.TransactionsFragment

class MenusAdapter
constructor(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    private val pages = listOf(
        HomeFragment(),
        MapsFragment(),
        TransactionsFragment(),
        AccountFragment()
    )

    override fun getItem(position: Int): Fragment {
        return pages[position]
    }

    override fun getCount(): Int {
        return pages.size
    }
}