package com.s16.engmyan.adapters

import android.view.ViewGroup
import com.s16.engmyan.R
import com.s16.engmyan.data.DictionaryItem
import com.s16.view.RecyclerViewArrayAdapter
import com.s16.view.RecyclerViewHolder


class SearchListAdapter: RecyclerViewArrayAdapter<RecyclerViewHolder, DictionaryItem>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        return RecyclerViewHolder.inflate(R.layout.list_item_simple, parent).apply {
            + android.R.id.text1
        }
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, item: DictionaryItem) {
        holder.setText(item.word to android.R.id.text1)
    }

}
