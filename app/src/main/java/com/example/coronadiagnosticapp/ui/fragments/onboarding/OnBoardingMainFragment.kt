package com.example.coronadiagnosticapp.ui.fragments.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.coronadiagnosticapp.R
import kotlinx.android.synthetic.main.fragment_on_boarding_main.*

class OnBoardingMainFragment : Fragment() {

    companion object {
        const val NUM_PAGES = 4
    }

    private val mPager: ViewPager by lazy { pager }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_on_boarding_main, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabDots.setupWithViewPager(mPager, true)
        val pagerAdapter = ScreenSlidePagerAdapter(parentFragmentManager)
        mPager.adapter = pagerAdapter
    }

    /**
     * A simple pager adapter that represents 4 ScreenSlidePageFragment objects,
     * in sequence.
     */
    private class ScreenSlidePagerAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount() = NUM_PAGES

        override fun getItem(position: Int) = when (position) {
            0 -> WelcomeFragment()
            1 -> StepsFragment()
            2 -> DisclosureFragment()
            3 -> ConsentFormFragment()
            else -> WelcomeFragment()
        }
    }
}
