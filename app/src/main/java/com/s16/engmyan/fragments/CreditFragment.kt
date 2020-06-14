package com.s16.engmyan.fragments

import android.app.Activity
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.s16.engmyan.Constants
import com.s16.engmyan.R
import com.s16.widget.setPositiveButton
import kotlinx.android.synthetic.main.fragment_credit.*


class CreditFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return inflater.inflate(R.layout.fragment_credit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webViewCredit.settings.allowFileAccess = true
        webViewCredit.loadUrl(Constants.URL_CREDIT)

        dialogButtons.setPositiveButton(android.R.string.ok) { _, _ ->
            dialog!!.cancel()
        }
    }

    override fun onResume() {
        super.onResume()

        val height = (getScreenHeight(requireActivity()) * 0.95).toInt()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height)
    }

    private fun getScreenHeight(activity: Activity): Int {
        val size = Point()
        activity.windowManager.defaultDisplay.getSize(size)
        return size.y
    }

    companion object {
        @JvmStatic
        fun newInstance() = CreditFragment()
    }
}
