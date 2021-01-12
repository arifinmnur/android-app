package com.kelsos.mbrc.ui.helpfeedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.kelsos.mbrc.R
import com.kelsos.mbrc.databinding.FragmentHelpFeedbackBinding

class HelpFeedbackFragment : Fragment() {
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val binding: FragmentHelpFeedbackBinding = DataBindingUtil.inflate(
      inflater,
      R.layout.fragment_help_feedback,
      container,
      false
    )
    val viewPager = binding.pagerHelpFeedback
    val tabLayout = binding.feedbackTabLayout
    viewPager.adapter = HelpFeedbackPagerAdapter(requireActivity())
    TabLayoutMediator(tabLayout, viewPager) { currentTab, currentPosition ->
      currentTab.text = when (currentPosition) {
        HelpFeedbackPagerAdapter.HELP -> getString(R.string.tab_help)
        HelpFeedbackPagerAdapter.FEEDBACK -> getString(R.string.tab_feedback)
        else -> throw IllegalArgumentException("invalid position")
      }
    }.attach()
    return binding.root
  }
}
