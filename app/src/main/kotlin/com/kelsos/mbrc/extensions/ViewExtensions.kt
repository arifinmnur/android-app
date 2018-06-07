package com.kelsos.mbrc.extensions

import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView.ViewHolder

fun ViewHolder.string(@StringRes resId: Int): String {
  return this.itemView.context.getString(resId)
}