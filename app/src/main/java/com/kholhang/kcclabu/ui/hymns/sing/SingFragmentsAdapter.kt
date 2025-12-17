package com.kholhang.kcclabu.ui.hymns.sing

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kholhang.kcclabu.data.model.Hymn
import com.kholhang.kcclabu.ui.hymns.sing.hymn.HymnFragment

class SingFragmentsAdapter(
    fragment: FragmentActivity,
    val hymns: List<Hymn>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = hymns.size

    override fun createFragment(position: Int): Fragment =
        HymnFragment.newInstance(hymns[position])
}
