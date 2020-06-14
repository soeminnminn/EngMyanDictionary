package com.s16.engmyan.fragments

import android.graphics.Point
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
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
import com.s16.engmyan.adapters.RecentListAdapter
import com.s16.engmyan.data.DbManager
import com.s16.engmyan.data.HistoryItem
import com.s16.engmyan.data.RecentModel
import com.s16.view.Adapter
import com.s16.view.RecyclerViewArrayAdapter
import kotlinx.android.synthetic.main.fragment_recent.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception

class RecentFragment : DialogFragment(), RecyclerViewArrayAdapter.OnItemClickListener {

    private var backgroundScope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null
    private lateinit var adapter: RecentListAdapter
    private var onItemClickListener: Adapter.OnItemClickListener? = null

    private val isTwoPane: Boolean
        get() = arguments?.getBoolean(Constants.ARG_PARAM_TWO_PANE) ?: false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recent, container, false)

        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE);
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

        recentList.layoutManager = LinearLayoutManager(requireContext())
        recentList.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))

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

    override fun onItemClick(view: View, position: Int) {
        adapter.getItem(position)?.let {
            dialog?.dismiss()
            onItemClickListener?.onItemClick(null, view, position, it.refId!!)
        }
    }

    private fun dataBind(recyclerView: RecyclerView) {
        adapter = RecentListAdapter()
        recyclerView.adapter = adapter

        val model : RecentModel by viewModels()
        model.data.observe(viewLifecycleOwner, Observer<List<HistoryItem>> {
            adapter.submitList(it)
        })
    }

    private fun performClear() {
        if (adapter.itemCount == 0) return

        val dialogBuilder = AlertDialog.Builder(requireContext()).apply {
            setIcon(android.R.drawable.ic_dialog_info)
            setTitle(R.string.clear_recent_title)
            setMessage(R.string.clear_recent_message)
            setNegativeButton(android.R.string.cancel) { di, _ ->
                di.cancel()
            }
            setPositiveButton(android.R.string.ok) { di, _ ->
                removeAllHistory()
                di.dismiss()
            }
        }

        dialogBuilder.show()
    }

    private fun removeAllHistory() {
        job = backgroundScope.launch {
            try {
                val provider = DbManager(requireContext()).provider()
                provider.deleteAllHistory()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(isTwoPane: Boolean, itemClickListener: Adapter.OnItemClickListener? = null) =
            RecentFragment().apply {
                arguments = bundleOf(Constants.ARG_PARAM_TWO_PANE to isTwoPane)
                onItemClickListener = itemClickListener
            }
    }
}