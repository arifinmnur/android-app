package com.kelsos.mbrc.ui.navigation.library

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kelsos.mbrc.ui.navigation.library.albums.BrowseAlbumFragment
import com.kelsos.mbrc.ui.navigation.library.artists.BrowseArtistFragment
import com.kelsos.mbrc.ui.navigation.library.genres.BrowseGenreFragment
import com.kelsos.mbrc.ui.navigation.library.tracks.BrowseTrackFragment

class LibraryPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

  override fun createFragment(position: Int): Fragment = when (position) {
    Search.SECTION_GENRE -> BrowseGenreFragment()
    Search.SECTION_ARTIST -> BrowseArtistFragment()
    Search.SECTION_ALBUM -> BrowseAlbumFragment()
    Search.SECTION_TRACK -> BrowseTrackFragment()
    else -> throw IllegalStateException("invalid position")
  }

  override fun getItemCount(): Int = COUNT

  companion object {
    const val COUNT = 4
  }
}
