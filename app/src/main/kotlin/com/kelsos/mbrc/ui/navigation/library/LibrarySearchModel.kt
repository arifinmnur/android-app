package com.kelsos.mbrc.ui.navigation.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class LibrarySearchModel {
  private val _search: MutableLiveData<String> = MutableLiveData()

  fun search(search: String) {
    this._search.postValue(search)
  }

  val term: LiveData<String>
    get() = _search
}