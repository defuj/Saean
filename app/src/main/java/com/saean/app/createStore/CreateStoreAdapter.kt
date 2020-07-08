package com.saean.app.createStore

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class CreateStoreAdapter(fm: FragmentManager, private val pages: List<Fragment>) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return pages[position]
    }

    override fun getCount(): Int {
        return pages.size
    }
}