package com.s16.view

import android.view.View

object Adapter {
    /**
     * Interface definition for a callback to be invoked when an item in this
     * View has been clicked.
     */
    interface OnItemClickListener {
        /**
         * Callback method to be invoked when an item in this AdapterView has
         * been clicked.
         * <p>
         * Implementers can call getItemAtPosition(position) if they need
         * to access the data associated with the selected item.
         *
         * @param parent The View where the click happened.
         * @param view The view within the AdapterView that was clicked (this
         *            will be a view provided by the adapter)
         * @param position The position of the view in the adapter.
         * @param id The row id of the item that was clicked.
         */
        fun onItemClick(parent: View?, view: View?, position: Int, id: Long)
    }
}
