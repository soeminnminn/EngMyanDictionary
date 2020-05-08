package com.s16.engmyan.utils

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView

import com.s16.engmyan.R

class SearchUiHelper(
    private val editText: EditText,
    private val actionClearSearch: ImageButton? = null
) {

    private val mTextSearchTextWatcher = object : TextWatcher {

        override fun onTextChanged(
            s: CharSequence, start: Int, before: Int,
            count: Int
        ) {
            onQueryTextChanged(s, count)
        }

        override fun beforeTextChanged(
            s: CharSequence, start: Int, count: Int,
            after: Int
        ) {
        }

        override fun afterTextChanged(s: Editable) {
            updateClearSearch()
        }

    }

    private val mTextSearchOnKeyListener = View.OnKeyListener { _, keyCode, event ->
        if (event.action == KeyEvent.ACTION_UP) {
            when (keyCode) {
                KeyEvent.KEYCODE_ENTER -> {
                    onQuerySubmit()
                    return@OnKeyListener true
                }
                KeyEvent.KEYCODE_ESCAPE -> {
                    clearText()
                    return@OnKeyListener true
                }
            }
        }

        false
    }

    private val mTextSearchOnEditorActionListener =
        TextView.OnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_GO,
                EditorInfo.IME_ACTION_NEXT,
                EditorInfo.IME_ACTION_DONE,
                EditorInfo.IME_ACTION_SEARCH,
                EditorInfo.IME_ACTION_SEND,
                EditorInfo.IME_ACTION_UNSPECIFIED -> {
                    onQuerySubmit()
                    true
                }
                else -> false
            }
        }

    private val mSearchClearClick = View.OnClickListener {
        editText.setText(R.string.empty_string)
    }
    private var mSearchText: CharSequence? = null
    private var mSearchListener: OnSearchListener? = null
    var isSearching: Boolean = false

    val searchText: CharSequence?
        get() {
            mSearchText = editText.text
            return mSearchText
        }

    interface OnSearchListener {
        fun onSearchTextChanged(searchText: CharSequence?)
        fun onSearchSubmit(searchText: CharSequence?)
        fun onClearSearch()
    }

    init {
        editText.addTextChangedListener(mTextSearchTextWatcher)
        editText.setOnKeyListener(mTextSearchOnKeyListener)
        editText.setOnEditorActionListener(mTextSearchOnEditorActionListener)
        editText.imeOptions = EditorInfo.IME_ACTION_SEARCH
        editText.isFocusableInTouchMode = true
        editText.isSaveEnabled = true

        if (actionClearSearch != null) {
            actionClearSearch.setOnClickListener(mSearchClearClick)
            updateClearSearch()
        }
    }

    private fun onQueryTextChanged(s: CharSequence, count: Int) {
        mSearchText = s
        if (!isSearching && mSearchListener != null) {
            mSearchListener!!.onSearchTextChanged(mSearchText)
        }
    }

    private fun onQuerySubmit() {
        if (!isSearching && mSearchListener != null) {
            mSearchText = editText.text
            mSearchListener!!.onSearchSubmit(mSearchText)
        }
    }

    private fun updateClearSearch() {
        if (actionClearSearch != null) {
            if (TextUtils.isEmpty(editText.text)) {
                actionClearSearch.visibility = View.GONE
            } else {
                actionClearSearch.visibility = View.VISIBLE
            }
        }
    }

    fun clearText() {
        editText.clearComposingText()
        editText.setText(EMPTY_STRING)
        mSearchText = EMPTY_STRING

        if (mSearchListener != null) {
            mSearchListener!!.onClearSearch()
        }
    }

    fun setOnSearchListener(listener: OnSearchListener) {
        mSearchListener = listener
    }

    companion object {
        private val EMPTY_STRING: CharSequence = ""
    }
}

