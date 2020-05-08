package com.s16.engmyan.fragments

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.s16.engmyan.Constants
import com.s16.engmyan.R
import com.s16.engmyan.activity.DetailsActivity
import com.s16.engmyan.adapters.FavoriteListAdapter
import com.s16.engmyan.data.DbManager
import com.s16.engmyan.data.FavoriteItem
import com.s16.engmyan.data.FavoriteModel
import com.s16.utils.gone
import com.s16.utils.startActivity
import com.s16.utils.visible
import com.s16.view.Adapter
import kotlinx.android.synthetic.main.fragment_favorite.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FavoriteFragment : DialogFragment(),
    FavoriteListAdapter.OnItemClickListener, FavoriteListAdapter.OnItemSelectListener {

    private lateinit var adapter: FavoriteListAdapter
    private var onItemClickListener: Adapter.OnItemClickListener? = null

    private var backgroundScope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    private val isTwoPane: Boolean
        get() = arguments?.getBoolean(Constants.ARG_PARAM_TWO_PANE) ?: false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NO_TITLE, R.style.AppTheme_Dialog_NoTitle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)

        dialog?.window?.let {
            if (isTwoPane) {
                it.setGravity(Gravity.TOP or GravityCompat.START)

                val metrics = requireContext().resources.displayMetrics
                val px = 16 * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)

                val layoutParams = it.attributes
                layoutParams.x = px.toInt()
                it.attributes = layoutParams
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoriteList.layoutManager = LinearLayoutManager(requireContext())
        favoriteList.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))

        dataBind(favoriteList)
        adapter.setItemClickListener(this)
        adapter.setItemSelectListener(this)

        actionClose.setOnClickListener {
            dialog?.dismiss()
        }

        actionEdit.setOnClickListener {
            changeEditMode(true)
        }

        actionDone.setOnClickListener {
            changeEditMode(false)
        }

        actionDelete.setOnClickListener {
            performDelete()
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.let {
            val size = Point()
            requireActivity().windowManager.defaultDisplay.getSize(size)

            if (isTwoPane) {
                val height = (size.y * 0.94).toInt()
                val width = (size.x * 0.35).toInt()

                it.setLayout(width, height)

            } else {
                val height = (size.y * 0.9).toInt()
                it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height)
            }
        }
    }

    override fun onDestroy() {
        job?.cancel()
        super.onDestroy()
    }

    private fun changeEditMode(edit: Boolean) {
        if (::adapter.isInitialized) {
            if (edit) {
                adapter.setSelectMode(true)

                actionsNormalMode.gone()
                actionsEditMode.visible()
            } else {
                adapter.endSelection()

                actionsNormalMode.visible()
                actionsEditMode.gone()
            }
        }
    }

    private fun dataBind(recyclerView: RecyclerView) {
        adapter = FavoriteListAdapter()
        recyclerView.adapter = adapter

        val model : FavoriteModel by viewModels()
        model.data.observe(viewLifecycleOwner, Observer<List<FavoriteItem>> {
            adapter.submitList(it)
        })
    }

    private fun performDelete() {
        if (!adapter.hasSelectedItems) return

        val dialogBuilder = AlertDialog.Builder(requireContext()).apply {
            setIcon(android.R.drawable.ic_dialog_info)
            setTitle(R.string.favorites_edit_title)
            setMessage(R.string.favorites_delete_message)
            setNegativeButton(android.R.string.cancel) { di, _ ->
                di.cancel()
            }
            setPositiveButton(android.R.string.ok) { di, _ ->
                removeSelected()
                di.dismiss()
            }
        }

        dialogBuilder.show()
    }

    private fun removeSelected() {
        val provider = DbManager(requireContext()).provider()
        val selectedItems = adapter.getSelectedItems().map {
            it.refId
        }

        job = backgroundScope.launch {
            provider.deleteFavoriteAll(selectedItems)
        }
    }

    override fun onItemSelectStart() {
        changeEditMode(true)
    }

    override fun onItemSelectionChange(position: Int, count: Int) {
    }

    override fun onItemClick(view: View, id: Long, position: Int) {
        dialog?.dismiss()
        onItemClickListener?.onItemClick(null, view, position, id)
    }

    companion object {
        @JvmStatic
        fun newInstance(isTwoPane: Boolean, itemClickListener: Adapter.OnItemClickListener? = null) =
            FavoriteFragment().apply {
                arguments = bundleOf(Constants.ARG_PARAM_TWO_PANE to isTwoPane)
                onItemClickListener = itemClickListener
            }
    }
}