package com.saean.app.home.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.saean.app.home.AccountFragment
import com.saean.app.home.HomeFragment
import com.saean.app.home.MapsFragment
import com.saean.app.home.TransactionsFragment

class HomeAdapter
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