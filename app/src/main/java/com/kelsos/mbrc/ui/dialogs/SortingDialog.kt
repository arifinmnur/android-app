package com.kelsos.mbrc.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.albums.Sorting
import com.kelsos.mbrc.content.library.albums.Sorting.Fields
import com.kelsos.mbrc.content.library.albums.Sorting.Order

class SortingDialog : DialogFragment() {
  private lateinit var orderButton: Button
  private lateinit var sortingOption: RadioGroup

  private lateinit var dialog: AlertDialog
  private lateinit var fm: FragmentManager
  private lateinit var orderChange: (order: Int) -> Unit
  private lateinit var sortingChange: (sorting: Int) -> Unit

  @Fields
  private var sorting: Int = Sorting.ALBUM_ARTIST__ALBUM

  @Order
  private var order: Int = Sorting.ORDER_ASCENDING

  private fun setOrder(@Order order: Int) {

    fun Button.set(@StringRes stringId: Int, @DrawableRes drawableId: Int) {
      text = getString(stringId)
      val drawable = ContextCompat.getDrawable(context, drawableId) ?: return
      val wrapped = DrawableCompat.wrap(drawable)
      DrawableCompat.setTint(wrapped, ContextCompat.getColor(context, R.color.accent))
      setCompoundDrawablesRelativeWithIntrinsicBounds(wrapped, null, null, null)
    }

    when (order) {
      Sorting.ORDER_ASCENDING -> {
        orderButton.set(
          R.string.sorting_dialog__descending,
          R.drawable.ic_arrow_drop_down_black_24dp
        )
      }
      Sorting.ORDER_DESCENDING -> {
        orderButton.set(R.string.sorting_dialog__ascending, R.drawable.ic_arrow_drop_up_black_24dp)
      }
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    dialog = MaterialAlertDialogBuilder(requireContext())
      .setTitle(R.string.album_sorting__dialog_title)
      .setView(R.layout.dialog__sorting)
      .setPositiveButton(android.R.string.ok) { _, _ -> }
      .setNegativeButton(android.R.string.cancel) { _, _ -> }
      .show()

    orderButton = checkNotNull(dialog.findViewById(R.id.sorting_dialog__order))
    sortingOption = checkNotNull(dialog.findViewById(R.id.sorting_dialog__sorting_options))
    orderButton.setOnClickListener {
      this.order = when (order) {
        Sorting.ORDER_DESCENDING -> Sorting.ORDER_ASCENDING
        Sorting.ORDER_ASCENDING -> Sorting.ORDER_DESCENDING
        else -> error("unknown order value: $order")
      }

      setOrder(this.order)
      orderChange(this.order)
    }

    setOrder(order)
    sortingOption.check(sortingOption.getChildAt(sorting).id)
    sortingOption.setOnCheckedChangeListener { radioGroup, _ ->
      val radioButtonID = radioGroup.checkedRadioButtonId
      val radioButton = radioGroup.findViewById<RadioButton>(radioButtonID)
      val idx = radioGroup.indexOfChild(radioButton)
      sortingChange(1 + idx)
    }

    return dialog
  }

  fun show() {
    show(fm, TAG)
  }

  override fun dismiss() {
    dialog.dismiss()
  }

  companion object {
    const val TAG = "com.kelsos.mbrc.ui.dialog.SortingDialog"

    fun create(
      fm: FragmentManager,
      @Fields sorting: Int,
      @Order order: Int,
      orderChange: (order: Int) -> Unit,
      sortingChange: (sorting: Int) -> Unit
    ): SortingDialog = SortingDialog().apply {
      this.fm = fm
      this.sorting = sorting
      this.order = order
      this.orderChange = orderChange
      this.sortingChange = sortingChange
    }
  }
}