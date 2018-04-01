package com.kelsos.mbrc.extensions

import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.ViewHolder.string(@StringRes resId: Int): String {
  return this.itemView.context.getString(resId)
}