package com.kelsos.mbrc.extensions

import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView

fun View?.show() {
  this?.visibility = VISIBLE
}

fun RecyclerView.ViewHolder.string(@StringRes resId: Int) : String {
  return this.itemView.context.getString(resId)
}
