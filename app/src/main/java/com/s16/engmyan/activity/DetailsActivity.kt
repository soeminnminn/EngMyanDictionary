package com.s16.engmyan.activity

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.s16.app.BackStackActivity
import com.s16.engmyan.Constants
import com.s16.engmyan.R
import com.s16.engmyan.data.DbManager
import com.s16.engmyan.data.DefinitionItem
import com.s16.engmyan.data.DefinitionModel
import com.s16.engmyan.data.FavoriteItem
import com.s16.engmyan.fragments.DetailsFragment
import com.s16.engmyan.utils.DefinitionBuilder
import com.s16.engmyan.utils.MenuItemToggle
import com.s16.engmyan.utils.TextToSpeechHelper
import com.s16.engmyan.utils.UIManager
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.coroutines.*
import java.lang.Exception

class DetailsActivity : BackStackActivity(),
    DefinitionBuilder.OnWordLinkClickListener {

    private var recordId: Long = 0

    private val menuFavorite = MenuItemToggle(true).apply {
        setIcon(R.drawable.ic_favorite_off)
        setTitle(R.string.action_favorite)

        setIconChecked(R.drawable.ic_favorite_on)
        setTitleChecked(R.string.action_favorite_remove)
    }

    private val menuPicture = MenuItemToggle()
    private lateinit var menuSound : MenuItem

    private lateinit var model: DefinitionModel
    private lateinit var textToSpeech: TextToSpeechHelper

    private var uiScope = CoroutineScope(Dispatchers.Main)
    private var backgroundScope = CoroutineScope(Dispatchers.IO)
    private var favoriteJob: Job? = null
    private var historyJob: Job? = null
    private var linkClickJob: Job? = null

    private val context: Context
        get() = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recordId = intent.getLongExtra(Constants.ARG_PARAM_ID, 0)

        textToSpeech = TextToSpeechHelper(this)
        textToSpeech.onTextToSpeechListener = object: TextToSpeechHelper.OnTextToSpeechListener {
            override fun onTextToSpeechInit(enabled: Boolean) {
                if (::menuSound.isInitialized) {
                    menuSound.isVisible = enabled
                }
            }
        }

        model = ViewModelProvider(this).get(DefinitionModel::class.java)
        model.data.observe(
            this, Observer<DefinitionItem> { item ->
                saveHistory(item)

                title = item.word ?: ""

                menuFavorite.setCheck(item.isFavorite)
                menuPicture.isVisible = item.hasImage

                textToSpeech.text = item.word ?: ""
            }
        )

        if (savedInstanceState == null) {
            createNewFragment(R.id.detailsContainer, DetailsFragment.newInstance(recordId), "details_$recordId")
        }

        model.fetch(recordId)
    }

    override fun onDestroy() {
        textToSpeech.shutdown()
        favoriteJob?.cancel()
        historyJob?.cancel()
        linkClickJob?.cancel()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.details, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menuFavorite.menuItem = menu.findItem(R.id.action_favorite)
        menuPicture.menuItem = menu.findItem(R.id.action_picture)
        menuSound = menu.findItem(R.id.action_sound)
        if (::textToSpeech.isInitialized) {
            menuSound.isVisible = textToSpeech.isEnabled
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_favorite -> {
                performFavorite(item.isChecked)
                true
            }
            R.id.action_sound -> {
                performSpeak()
                true
            }
            R.id.action_picture -> {
                performPicture()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (getTopFragment<DetailsFragment>()?.onBackPressed() == true) {
            return
        }
        super.onBackPressed()
    }

    override fun onBackStackChanged(activeFragment: Fragment) {
        activeFragment.arguments?.let { args ->
            recordId = args.getLong(Constants.ARG_PARAM_ID, 0)
            model.fetch(recordId)
        }
    }

    override fun onWordLinkClick(word: String) {
        linkClickJob = uiScope.launch {
            val id = withContext(Dispatchers.IO) {
                val searchWord = word.replace("'", "''")
                    .replace("%", "").replace("_", "").trim()

                try {
                    val provider = DbManager(context).provider()
                    provider.queryId(searchWord)
                } catch (e: Exception) {
                    e.printStackTrace()
                    -1L
                }
            }

            if (::model.isInitialized && id > 0L) {
                recordId = id
                addNewFragment(R.id.detailsContainer, DetailsFragment.newInstance(recordId), "details_$recordId")
                model.fetch(recordId)
            }
        }
    }

    private fun performFavorite(isFavorite: Boolean) {
        val item = FavoriteItem(word = "$title", refId = recordId, timestamp = System.currentTimeMillis())

        favoriteJob = uiScope.launch {
            val provider = DbManager(context).provider()

            val result = withContext(Dispatchers.IO) {
                try {
                    if (isFavorite) {
                        provider.deleteFavoriteByRef(recordId)
                        0L
                    } else {
                        provider.insertFavorite(item)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    -1L
                }
            }

            try {
                val topFav = provider.queryTopFavorites()
                UIManager.createShortcuts(context, topFav)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (result == 0L) {
                Snackbar.make(detailsRoot, getText(R.string.remove_favorites_message), Snackbar.LENGTH_LONG).show()
            } else if (result > 0L) {
                Snackbar.make(detailsRoot, getText(R.string.add_favorites_message), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun performSpeak() {
        textToSpeech.speak()
    }

    private fun performPicture() {
        getTopFragment<DetailsFragment>()?.togglePicture()
    }

    private fun saveHistory(item: DefinitionItem) {
        historyJob = backgroundScope.launch {
            try {
                val provider = DbManager(context).provider()
                provider.createHistory(item.word ?: "", item.id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
