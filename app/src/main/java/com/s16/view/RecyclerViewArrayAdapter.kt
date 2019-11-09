package com.s16.view

import android.view.View
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

abstract class RecyclerViewArrayAdapter<VH: RecyclerView.ViewHolder, T>:
    RecyclerView.Adapter<VH>(), Filterable {

    /**
     * Lock used to modify the content of [.mObjects]. Any write operation
     * performed on the array should be synchronized on this lock. This lock is also
     * used by the filter (see [.getFilter] to make a synchronized copy of
     * the original array of data.
     */
    private val mLock = Any()

    /**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    private var mObjects: MutableList<T> = mutableListOf()

    /**
     * Indicates whether or not [.notifyDataSetChanged] must be called whenever
     * [.mObjects] is modified.
     */
    private var mNotifyOnChange = true

    // A copy of the original mObjects array, initialized from and then used instead as soon as
    // the mFilter ArrayFilter is used. mObjects will then only contain the filtered values.
    private var mOriginalValues: MutableList<T>? = null
    private var mFilter: ArrayFilter? = null

    private var mItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        /**
         * Callback method to be invoked when an item in this RecyclerView has
         * been clicked.
         *
         * @param view The view within the AdapterView that was clicked (this
         * will be a view provided by the adapter)
         * @param position The position of the view in the adapter.
         */
        fun onItemClick(view: View, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mItemClickListener = listener
    }

    abstract fun onBindViewHolder(holder: VH, item: T)

    override fun onBindViewHolder(holder: VH, position: Int) {
        getItem(position)?.let { item ->
            onBindViewHolder(holder, item)
        }

        if (mItemClickListener != null) {
            holder.itemView.isClickable = true
            holder.itemView.setOnClickListener {  v ->
                mItemClickListener!!.onItemClick(v, position)
            }
        }
    }

                                  /**
     * Set the new list to be displayed.
     *
     * @param collection The new list to be displayed.
     */
    fun submitList(collection: Collection<T>) {
        synchronized(mLock) {
            mObjects = ArrayList(collection)
            mOriginalValues = null
        }
        if (mNotifyOnChange) notifyDataSetChanged()
    }

    /**
     * Adds the specified object at the end of the array.
     *
     * @param element The object to add at the end of the array.
     * @throws UnsupportedOperationException if the underlying data collection is immutable
     */
    fun add(element: T) {
        var index: Int
        synchronized(mLock) {
            mOriginalValues?.add(element) ?: mObjects.add(element)
            index = mOriginalValues?.indexOf(element) ?: mObjects.indexOf(element)
        }

        if (index != -1) notifyItemInserted(index)
    }

    /**
     * Adds the specified Collection at the end of the array.
     *
     * @param collection The Collection to add at the end of the array.
     * @throws UnsupportedOperationException if the <tt>addAll</tt> operation
     * is not supported by this list
     * @throws ClassCastException if the class of an element of the specified
     * collection prevents it from being added to this list
     * @throws NullPointerException if the specified collection contains one
     * or more null elements and this list does not permit null
     * elements, or if the specified collection is null
     * @throws IllegalArgumentException if some property of an element of the
     * specified collection prevents it from being added to this list
     */
    fun addAll(collection: Collection<T>) {
        synchronized(mLock) {
            mOriginalValues?.addAll(collection) ?: mObjects.addAll(collection)
        }
        if (mNotifyOnChange) notifyDataSetChanged()
    }

    /**
     * Adds the specified items at the end of the array.
     *
     * @param items The items to add at the end of the array.
     * @throws UnsupportedOperationException if the underlying data collection is immutable
     */
    fun addAll(vararg items: T) {
        synchronized(mLock) {
            if (mOriginalValues != null) {
                mOriginalValues!!.addAll(items)
            } else {
                mObjects.addAll(items)
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged()
    }

    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param element The object to insert into the array.
     * @param index The index at which the object must be inserted.
     * @throws UnsupportedOperationException if the underlying data collection is immutable
     */
    fun insert(element: T, index: Int) {
        synchronized(mLock) {
            if (mOriginalValues != null) {
                mOriginalValues!!.add(index, element)
            } else {
                mObjects.add(index, element)
            }
        }
        notifyItemInserted(index)
    }

    /**
     * Removes the specified object from the array.
     *
     * @param element The object to remove.
     * @throws UnsupportedOperationException if the underlying data collection is immutable
     */
    fun remove(element: T) {
        var index: Int
        synchronized(mLock) {
            index = mOriginalValues?.indexOf(element) ?: mObjects.indexOf(element)
            mOriginalValues?.remove(element) ?: mObjects.remove(element)
        }
        if (index != -1) notifyItemRemoved(index)
    }

    /**
     * Removes a object at the specified [index] from the array.
     *
     * @param index The index to remove.
     */
    fun removeAt(index: Int) {
        synchronized(mLock) {
            mOriginalValues?.removeAt(index) ?: mObjects.removeAt(index)
        }
        notifyItemRemoved(index)
    }

    /**
     * Remove all elements from the list.
     *
     * @throws UnsupportedOperationException if the underlying data collection is immutable
     */
    fun clear() {
        synchronized(mLock) {
            if (mOriginalValues != null) {
                mOriginalValues!!.clear()
            } else {
                mObjects.clear()
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged()
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objects contained
     * in this adapter.
     */
    fun sort(comparator: Comparator<in T>) {
        synchronized(mLock) {
            if (mOriginalValues != null) {
                mOriginalValues!!.sortWith(comparator)
            } else {
                mObjects.sortWith(comparator)
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged()
    }

    /**
     * Returns the first element matching the given [predicate], or `null` if no such element was found.
     */
    fun find(predicate: (T) -> Boolean): T? = mObjects.find(predicate)

    /**
     * Returns the index of the first occurrence of the specified element in the list, or -1 if the specified
     * element is not contained in the list.
     */
    fun findIndex(predicate: (T) -> Boolean): Int {
        val element = mObjects.find(predicate)
        return if (element != null) mObjects.indexOf(element) else -1
    }

    /**
     * Control whether methods that change the list ([.add], [.addAll],
     * [.addAll], [.insert], [.remove], [.clear],
     * [.sort]) automatically call [.notifyDataSetChanged].  If set to
     * false, caller must manually call notifyDataSetChanged() to have the changes
     * reflected in the attached view.
     *
     * The default is true, and calling notifyDataSetChanged()
     * resets the flag to true.
     *
     * @param notifyOnChange if true, modifications to the list will
     * automatically call [                       ][.notifyDataSetChanged]
     */
    fun setNotifyOnChange(notifyOnChange: Boolean) {
        mNotifyOnChange = notifyOnChange
    }

    override fun getItemCount(): Int = mObjects.size

    fun getItems(): List<T> {
        return mOriginalValues ?: mObjects
    }

    fun getItem(position: Int): T? = if (position > -1 && position < mObjects.size) {
        mObjects[position]
    } else {
        null
    }

    fun getPosition(element: T): Int = mObjects.indexOf(element)

    override fun getFilter(): Filter {
        if (mFilter == null) {
            mFilter = ArrayFilter()
        }
        return mFilter!!
    }

    private inner class ArrayFilter: Filter() {

        override fun performFiltering(prefix: CharSequence?): FilterResults {
            val results = FilterResults()
            if (mOriginalValues == null) {
                synchronized(mLock) {
                    mOriginalValues = ArrayList(mObjects)
                }
            }

            if (prefix == null || prefix.isEmpty()) {
                val list: ArrayList<T>
                synchronized(mLock) {
                    list = ArrayList(mOriginalValues)
                }
                results.values = list
                results.count = list.size

            } else {
                val prefixString = "$prefix".toLowerCase(Locale.getDefault())
                val values: ArrayList<T>
                synchronized(mLock) {
                    values = ArrayList(mOriginalValues)
                }

                val newValues = values.filter { value ->
                    val valueText = "$value".toLowerCase(Locale.getDefault())
                    if (valueText.startsWith(prefixString)) {
                        true
                    } else {
                        val words = valueText.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                        words.indexOfFirst { word ->
                            word.startsWith(prefixString)
                        } > -1
                    }
                }.toMutableList()

                results.values = newValues
                results.count = newValues.size
            }

            return results
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            // noinspection unchecked
            mObjects = results?.values as MutableList<T>
            notifyDataSetChanged()
        }
    }
}