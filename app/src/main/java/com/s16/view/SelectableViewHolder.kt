package com.s16.view

import android.view.View
import android.widget.Checkable

interface LongClickSelectable {
    fun onItemClick(view: View, position: Int)

    fun setSelectMode(mode: Boolean)
    fun isSelectedMode(): Boolean
    fun onSelectStart()
    fun onSelectionChange(position: Int, checked: Boolean)
}

abstract class SelectableViewHolder(view: View, private val adapter: LongClickSelectable) :
    RecyclerViewHolder(view) {

    private var savedPosition: Int = -1

    private val onClickListener = View.OnClickListener {
        if (adapter.isSelectedMode()) {
            toggle()
        } else {
            adapter.onItemClick(itemView, adapterPosition)
        }
    }

    private val onLongClickListener = View.OnLongClickListener {
        if (!adapter.isSelectedMode()) {
            savedPosition = adapterPosition
            adapter.onSelectStart()
            adapter.setSelectMode(true)
            toggle()
            true

        } else
            false
    }

    private val isEnabled : Boolean
        get() = itemView.isEnabled

    init {
        itemView.isClickable = true
        itemView.setOnClickListener(onClickListener)

        itemView.isLongClickable = true
        itemView.setOnLongClickListener(onLongClickListener)
    }

    private fun onCheckedChanged(v: View?, isChecked: Boolean) {
        val position : Int = if (adapterPosition == -1) savedPosition else adapterPosition
        adapter.onSelectionChange(position, isChecked)
    }

    abstract fun getCheckableView() : Checkable

    private fun toggle() {
        if (isEnabled) {
            val checkable = getCheckableView()
            checkable.isChecked = !checkable.isChecked

            onCheckedChanged(itemView, checkable.isChecked)
        }
    }
}