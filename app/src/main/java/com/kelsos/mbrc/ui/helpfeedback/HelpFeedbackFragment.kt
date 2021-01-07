package com.kelsos.mbrc.ui.helpfeedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kelsos.mbrc.R
import kotterknife.bindView

class HelpFeedbackFragment : Fragment() {

  private val tabLayout: TabLayout by bindView(R.id.feedback_tab_layout)
  private val viewPager: ViewPager2 by bindView(R.id.pager_help_feedback)

  private lateinit var pagerAdapter: HelpFeedbackPagerAdapter

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_help_feedback, container, false)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    pagerAdapter = HelpFeedbackPagerAdapter(requireActivity())
    viewPager.apply {
      adapter = pagerAdapter
    }
    TabLayoutMediator(tabLayout, viewPager) { currentTab, currentPosition ->
      currentTab.text = when (currentPosition) {
        HelpFeedbackPagerAdapter.HELP -> getString(R.string.tab_help)
        HelpFeedbackPagerAdapter.FEEDBACK -> getString(R.string.tab_feedback)
        else -> throw IllegalArgumentException("invalid position")
      }
    }.attach()
  }
}
