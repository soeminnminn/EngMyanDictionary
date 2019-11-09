package com.s16.engmyan.adapters

import android.view.ViewGroup
import com.s16.engmyan.R
import com.s16.engmyan.data.HistoryItem
import com.s16.view.RecyclerViewArrayAdapter
import com.s16.view.RecyclerViewHolder

class RecentListAdapter : RecyclerViewArrayAdapter<RecyclerViewHolder, HistoryItem>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        return RecyclerViewHolder.inflate(R.layout.list_item_simple, parent).apply {
            + android.R.id.text1
        }
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, item: HistoryItem) {
        holder.setText(item.word to android.R.id.text1)
    }

}