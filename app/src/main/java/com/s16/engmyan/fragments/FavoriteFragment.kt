package com.s16.engmyan.fragments

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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
import kotlinx.android.synthetic.main.fragment_favorite.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FavoriteFragment : DialogFragment(),
    FavoriteListAdapter.OnItemClickListener, FavoriteListAdapter.OnItemSelectListener {

    private lateinit var adapter: FavoriteListAdapter

    private var backgroundScope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NO_TITLE, R.style.AppTheme_Dialog_NoTitle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoriteList.layoutManager = LinearLayoutManager(context!!)
        favoriteList.addItemDecoration(DividerItemDecoration(context!!, LinearLayoutManager.VERTICAL))

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

        val height = (getScreenHeight(activity!!) * 0.9).toInt()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height)
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

    private fun getScreenHeight(activity: Activity): Int {
        val size = Point()
        activity.windowManager.defaultDisplay.getSize(size)
        return size.y
    }

    private fun dataBind(recyclerView: RecyclerView) {
        adapter = FavoriteListAdapter()
        recyclerView.adapter = adapter

        val model = activity?.let { context ->
            val modelFactory = FavoriteModel.of(DbManager(context).provider())
            ViewModelProviders.of(context, modelFactory).get(FavoriteModel::class.java)
        }

        model?.data!!.observe(this, Observer<List<FavoriteItem>> {
            adapter.submitList(it)
        })
    }

    private fun performDelete() {
        if (!adapter.hasSelectedItems) return

        context?.let { ctx ->
            val dialogBuilder = AlertDialog.Builder(ctx).apply {
                setIcon(android.R.drawable.ic_dialog_info)
                setTitle(R.string.favorites_edit_title)
                setMessage(R.string.favorites_delete_message)
                setNegativeButton(android.R.string.cancel) { di, _ ->
                    di.cancel()
                }
                setPositiveButton(android.R.string.ok) { di, _ ->
                    removeSelected(ctx)
                    di.dismiss()
                }
            }

            dialogBuilder.show()
        }
    }

    private fun removeSelected(ctx: Context) {
        val provider = DbManager(ctx).provider()
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
        startActivity<DetailsActivity>(Constants.ARG_PARAM_ID to id)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            FavoriteFragment().apply {
                arguments = Bundle()
            }
    }
}