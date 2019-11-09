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
import com.s16.engmyan.adapters.RecentListAdapter
import com.s16.engmyan.data.DbManager
import com.s16.engmyan.data.HistoryItem
import com.s16.engmyan.data.RecentModel
import com.s16.utils.startActivity
import com.s16.view.RecyclerViewArrayAdapter
import kotlinx.android.synthetic.main.fragment_recent.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RecentFragment : DialogFragment(), RecyclerViewArrayAdapter.OnItemClickListener {

    private var backgroundScope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null
    private lateinit var adapter: RecentListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NO_TITLE, R.style.AppTheme_Dialog_NoTitle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recent, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recentList.layoutManager = LinearLayoutManager(context!!)
        recentList.addItemDecoration(DividerItemDecoration(context!!, LinearLayoutManager.VERTICAL))

        dataBind(recentList)
        adapter.setOnItemClickListener(this)

        actionClose.setOnClickListener {
            dialog?.dismiss()
        }

        actionClear.setOnClickListener {
            performClear()
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

    private fun getScreenHeight(activity: Activity): Int {
        val size = Point()
        activity.windowManager.defaultDisplay.getSize(size)
        return size.y
    }

    override fun onItemClick(view: View, position: Int) {
        adapter.getItem(position)?.let {
            dialog?.dismiss()
            startActivity<DetailsActivity>(Constants.ARG_PARAM_ID to it.refId)
        }
    }

    private fun dataBind(recyclerView: RecyclerView) {
        adapter = RecentListAdapter()
        recyclerView.adapter = adapter

        val model = activity?.let { context ->
            val modelFactory = RecentModel.of(DbManager(context).provider())
            ViewModelProviders.of(context, modelFactory).get(RecentModel::class.java)
        }

        model?.data!!.observe(this, Observer<List<HistoryItem>> {
            adapter.submitList(it)
        })
    }

    private fun performClear() {
        if (adapter.itemCount == 0) return

        context?.let { ctx ->
            val dialogBuilder = AlertDialog.Builder(ctx).apply {
                setIcon(android.R.drawable.ic_dialog_info)
                setTitle(R.string.clear_recent_title)
                setMessage(R.string.clear_recent_message)
                setNegativeButton(android.R.string.cancel) { di, _ ->
                    di.cancel()
                }
                setPositiveButton(android.R.string.ok) { di, _ ->
                    removeAllHistory(ctx)
                    di.dismiss()
                }
            }

            dialogBuilder.show()
        }
    }

    private fun removeAllHistory(ctx: Context) {
        val provider = DbManager(ctx).provider()

        job = backgroundScope.launch {
            provider.deleteAllHistory()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            RecentFragment().apply {
                arguments = Bundle()
            }
    }
}