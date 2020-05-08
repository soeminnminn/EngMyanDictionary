package com.s16.engmyan.fragments

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.s16.engmyan.R
import kotlinx.android.synthetic.main.fragment_picture.*

class PictureViewFragment : Fragment() {

    private var drawable: Drawable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_picture, container, false)
    }

    fun setBitmap(resource: Resources, bitmap: Bitmap?) {
        if (bitmap != null) {
            drawable = BitmapDrawable(resource, bitmap)
            image?.setImageDrawable(drawable)
        }
    }
}