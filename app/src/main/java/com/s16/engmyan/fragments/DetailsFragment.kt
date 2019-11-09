package com.s16.engmyan.fragments

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.s16.engmyan.Constants
import com.s16.engmyan.R
import com.s16.engmyan.data.*
import com.s16.engmyan.utils.DefinitionBuilder
import com.s16.utils.*
import kotlinx.android.synthetic.main.content_definition.*
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.coroutines.*


class DetailsFragment : Fragment() {

    private var uiScope = CoroutineScope(Dispatchers.Main)
    private var job: Job? = null

    private lateinit var picFragment : PictureViewFragment

    private val isShowSynonym: Boolean
        get() = context?.run {
            defaultSharedPreferences booleanOf Constants.PREFS_SHOW_SYNONYM
        } ?: true

    private lateinit var definitionBuilder: DefinitionBuilder

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        picFragment = PictureViewFragment()

        context?.let { ctx ->
            definitionBuilder = DefinitionBuilder().apply {
                setClickableWordLink(ctx.defaultSharedPreferences booleanOf Constants.PREFS_WORD_CLICKABLE)
            }

            if (activity is DefinitionBuilder.OnWordLinkClickListener) {
                definitionBuilder.setOnWordLinkClickListener(activity as DefinitionBuilder.OnWordLinkClickListener)
            }

            val optionsModel = PreferencesLiveData(ctx.defaultSharedPreferences,
                Constants.PREFS_FONT_SIZE, Constants.PREFS_FORCE_ZAWGYI,
                Constants.PREFS_WORD_CLICKABLE, Constants.PREFS_SHOW_SYNONYM)
            optionsModel.observe(this, Observer {
                // it[Constants.PREFS_WORD_CLICKABLE].
                dataBind()
            })
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.imageContainer, picFragment, "picture")
            .hide(picFragment)
            .commit()
    }

    private fun dataBind() {
        textViewDetails.text = ""
        textViewDetails.movementMethod = LinkMovementMethod.getInstance()

        var forceZawgyi = false

        context?.let { context ->
            val textSize = context.defaultSharedPreferences.getStringAsFloat(Constants.PREFS_FONT_SIZE, Constants.TEXT_SIZE)

            textViewDetails.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize)

            textViewSynonymTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize * 1.1f)
            textViewSynonym.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize)

            forceZawgyi = context.defaultSharedPreferences booleanOf Constants.PREFS_FORCE_ZAWGYI
            textViewDetails.typeface = if (forceZawgyi) {
                Constants.getZawgyiTypeface(context)
            } else {
                Constants.getMMTypeFace(context)
            }
        }

        layoutSynonymView.gone()

        val model = context?.let { context ->
            val modelFactory = DefinitionModel.of(DbManager(context).provider())
            ViewModelProviders.of(this, modelFactory).get(DefinitionModel::class.java)
        }

        model?.data!!.observe(
            this, Observer<DefinitionItem> { item ->
                definitionBuilder.setDefinition("${item.definition}")
                    .setKeywords(item.keywords)

                job = uiScope.launch {
                    if (forceZawgyi) {
                        var converted = item.convertedZawgyi ?: ""
                        if (converted.isEmpty()) {
                            converted = definitionBuilder.convertToZawgyi()

                            withContext(Dispatchers.IO) {
                                val convertedItem = ConvertedItem(refId = item.id,
                                    mode = "zawgyi", value = converted,
                                    timestamp = System.currentTimeMillis())
                                DbManager(context!!).provider().insertConverted(convertedItem)
                            }
                        }

                        definitionBuilder.setDefinition(converted)
                    }
                    textViewDetails.setText(definitionBuilder.build(), TextView.BufferType.NORMAL)
                }

                if (isShowSynonym && (item.synonym ?: "").isNotEmpty()) {
                    textViewSynonym.setText(item.synonym!!, TextView.BufferType.NORMAL)
                    layoutSynonymView.visible()
                } else {
                    layoutSynonymView.gone()
                }

                detailsProgress.gone()
                detailsContent.visible()

                picFragment.setBitmap(resources, item.image)
            }
        )

        arguments?.getLong(Constants.ARG_PARAM_ID)?.let {
            model.fetch(it)
        }
    }

    fun togglePicture() {
        childFragmentManager.findFragmentByTag("picture")?.let {
            val transaction = childFragmentManager.beginTransaction()

            transaction.setCustomAnimations(
                R.anim.slide_down_enter, R.anim.slide_down_exit,
                R.anim.slide_down_pop_enter, R.anim.slide_down_pop_exit
            )

            if (it.isHidden) {
                transaction.show(it).commit()
            } else {
                transaction.hide(it).commit()
            }
        }
    }

    fun onBackPressed(): Boolean {
        return childFragmentManager.findFragmentByTag("picture")?.let {
            if (!it.isHidden) {
                val transaction = childFragmentManager.beginTransaction()
                transaction.setCustomAnimations(
                    R.anim.slide_down_enter, R.anim.slide_down_exit,
                    R.anim.slide_down_pop_enter, R.anim.slide_down_pop_exit
                )

                transaction.hide(it).commit()
                true
            } else {
                false
            }
        } ?: false
    }

    override fun onDestroy() {
        job?.cancel()
        super.onDestroy()
    }

    companion object {
        @JvmStatic
        fun newInstance(id: Long) =
            DetailsFragment().apply {
                arguments = bundleOf(Constants.ARG_PARAM_ID to id)
            }
    }
}