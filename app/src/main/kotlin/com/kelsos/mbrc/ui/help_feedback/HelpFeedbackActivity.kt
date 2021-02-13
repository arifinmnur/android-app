package com.kelsos.mbrc.ui.help_feedback

import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kelsos.mbrc.R
import com.kelsos.mbrc.ui.activities.FontActivity

class HelpFeedbackActivity : FontActivity() {

  @BindView(R.id.toolbar)
  lateinit var toolbar: MaterialToolbar

  @BindView(R.id.feedback_tab_layout)
  lateinit var tabLayout: TabLayout

  @BindView(R.id.pager_help_feedback)
  lateinit var viewPager: ViewPager2

  private lateinit var pagerAdapter: HelpFeedbackPagerAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_help_feedback)
    ButterKnife.bind(this)
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
