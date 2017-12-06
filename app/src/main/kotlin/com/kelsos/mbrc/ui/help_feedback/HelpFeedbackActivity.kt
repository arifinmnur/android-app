package com.kelsos.mbrc.ui.help_feedback

import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kelsos.mbrc.R
import com.kelsos.mbrc.ui.activities.FontActivity
import kotterknife.bindView

class HelpFeedbackActivity : FontActivity() {

  private val toolbar: MaterialToolbar by bindView(R.id.toolbar)
  private val tabLayout: TabLayout by bindView(R.id.feedback_tab_layout)
  private val viewPager: ViewPager2 by bindView(R.id.pager_help_feedback)

  private lateinit var pagerAdapter: HelpFeedbackPagerAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_help_feedback)

    setSupportActionBar(toolbar)
    val actionBar = supportActionBar

    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true)
      actionBar.setHomeButtonEnabled(true)
    }

    pagerAdapter = HelpFeedbackPagerAdapter(this)
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
