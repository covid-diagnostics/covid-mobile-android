package com.example.coronadiagnosticapp.ui.fragments.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager

import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.fragments.home.HomeFragment
import com.example.coronadiagnosticapp.ui.fragments.welcome.WelcomeFragment
import kotlinx.android.synthetic.main.fragment_on_boarding_main.*

const val NUM_PAGES = 4
class OnBoardingMainFragment : Fragment() {

    private val mPager: ViewPager by lazy { pager }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_on_boarding_main, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabDots.setupWithViewPager(mPager,true)
        val pagerAdapter = ScreenSlidePagerAdapter(parentFragmentManager)
        mPager.adapter = pagerAdapter
    }

    /**
     * A simple pager adapter that represents 4 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = NUM_PAGES

        override fun getItem(position: Int): Fragment = WelcomeFragment()
        //TODO make an array of fragments or switch case
    }
}
