package com.s16.engmyan.utils

import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import androidx.core.text.HtmlCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.text.TextUtils
import android.text.style.URLSpan
import android.view.View
import com.s16.utils.RabbitConverter
import java.util.regex.Pattern

class DefinitionBuilder {

    interface OnWordLinkClickListener {
        fun onWordLinkClick(word: String)
    }

    private var definition: String? = null
    private var keywords: String? = null

    private var linkClickListener: OnWordLinkClickListener? = null
    private var clickableWordLink: Boolean = false

    fun setDefinition(definition: String) : DefinitionBuilder {
        this.definition = definition
        return this
    }

    fun setKeywords(keywords: String?) : DefinitionBuilder {
        this.keywords = keywords
        return this
    }

    fun setClickableWordLink(clickable: Boolean): DefinitionBuilder {
        this.clickableWordLink = clickable
        return this
    }

    fun setOnWordLinkClickListener(listener: OnWordLinkClickListener) {
        linkClickListener = listener
    }

    suspend fun convertToZawgyi(): String = withContext(Dispatchers.IO) {
        val converted = definition?.let { text ->
            RabbitConverter.uni2zg(text.trim())
        }

        definition = converted
        converted ?: ""
    }

    suspend fun build(): Spanned? =
        withContext(Dispatchers.IO) {
            definition?.let { text ->
                val html = HtmlCompat.fromHtml(text.trim(), HtmlCompat.FROM_HTML_MODE_COMPACT)

                if (clickableWordLink && keywords != null && keywords!!.isNotEmpty()) {
                    val keywordsMatcher = "[^,]+".toPattern().matcher(keywords)

                    val spannableString = SpannableString(html)

                    val chars = CharArray(html.length)
                    TextUtils.getChars(html, 0, html.length, chars, 0)
                    val plainText = String(chars)

                    while (keywordsMatcher.find()) {
                        val value = "${keywordsMatcher.group()}"
                        val regex = "([^A-Za-z\\/\\?=])($value)([^A-Za-z\\/\\?=])"
                            .toPattern(Pattern.CASE_INSENSITIVE)

                        val textMatcher = regex.matcher(plainText)
                        while (textMatcher.find()) {
                            spannableString.setSpan(
                                LinkSpan("file:///android_asset/definition?w=$value"),
                                textMatcher.start(2),
                                textMatcher.end(2),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }

                    spannableString
                } else {
                    html
                }
            }
        }

    inner class LinkSpan(url: String) : URLSpan(url) {

        override fun onClick(widget: View) {
            val uri = Uri.parse(url)
            val word = uri.getQueryParameter("w")

            if (linkClickListener != null) {
                linkClickListener!!.onWordLinkClick(word)
            }
        }

    }
}