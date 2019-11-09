package com.s16.engmyan.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import android.widget.CheckedTextView
import com.s16.engmyan.R
import com.s16.engmyan.data.FavoriteItem
import com.s16.view.LongClickSelectable
import com.s16.view.RecyclerViewArrayAdapter
import com.s16.view.SelectableViewHolder


class FavoriteListAdapter :
    RecyclerViewArrayAdapter<FavoriteListAdapter.FavoriteItemHolder, FavoriteItem>(), LongClickSelectable {

    interface OnItemSelectListener {
        fun onItemSelectStart()
        fun onItemSelectionChange(position: Int, count: Int)
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, id: Long, position: Int)
    }

    private var mItemClickListener: OnItemClickListener? = null
    private var mItemSelectListener: OnItemSelectListener? = null

    private val mCheckedItems: MutableList<FavoriteItem> = mutableListOf()
    private var mSelectMode = false

    val hasSelectedItems: Boolean
        get() = mCheckedItems.size > 0

    private fun getCheckMarkDrawable(context: Context): Drawable? {
        val attrs = intArrayOf(android.R.attr.listChoiceIndicatorMultiple)
        val ta = context.theme.obtainStyledAttributes(attrs)
        val drawable = ta.getDrawable(0)
        ta.recycle()
        return drawable
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_selectable, parent, false)
        return FavoriteItemHolder(view, this)
    }

    override fun onBindViewHolder(holder: FavoriteItemHolder, item: FavoriteItem) {
        val textView : CheckedTextView = holder.findViewById(android.R.id.text1)
        textView.text = item.word
        textView.isChecked = mCheckedItems.count { d -> d.id == item.id } > 0

        if (isSelectedMode()) {
            textView.checkMarkDrawable = getCheckMarkDrawable(textView.context)
        } else {
            textView.checkMarkDrawable = null
        }
    }

    fun setItemClickListener(listener: OnItemClickListener) {
        mItemClickListener = listener;
    }

    fun setItemSelectListener(listener: OnItemSelectListener) {
        mItemSelectListener = listener;
    }

    override fun onItemClick(view: View, position: Int) {
        if (mItemClickListener != null) {
            getItem(position)?.let { item ->
                mItemClickListener!!.onItemClick(view, item.id, position)
            }
        }
    }

    override fun setSelectMode(mode: Boolean) {
        mSelectMode = mode
        notifyDataSetChanged()
    }

    override fun isSelectedMode(): Boolean = mSelectMode

    override fun onSelectStart() {
        if (!mSelectMode && mItemSelectListener != null) {
            mItemSelectListener!!.onItemSelectStart()
        }
    }

    override fun onSelectionChange(position: Int, checked: Boolean) {
        getItem(position)?.let { item ->
            if (checked) {
                if (!mCheckedItems.contains(item)) mCheckedItems.add(item)
            } else {
                if (mCheckedItems.contains(item)) mCheckedItems.remove(item)
            }

            if (mItemSelectListener != null) {
                mItemSelectListener!!.onItemSelectionChange(position, mCheckedItems.size)
            }
        }
    }

    fun getSelectedItems(): List<FavoriteItem> = mCheckedItems

    fun endSelection() {
        mSelectMode = false
        mCheckedItems.clear()
        notifyDataSetChanged()
    }

    class FavoriteItemHolder(view: View, adapter: LongClickSelectable) : SelectableViewHolder(view, adapter) {

        override fun getCheckableView(): Checkable = findViewById<CheckedTextView>(android.R.id.text1)

    }
}