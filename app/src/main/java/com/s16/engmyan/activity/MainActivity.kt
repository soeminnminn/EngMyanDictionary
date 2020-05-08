package com.s16.engmyan.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.s16.engmyan.Constants
import com.s16.engmyan.R
import com.s16.engmyan.adapters.SearchListAdapter
import com.s16.engmyan.data.*
import com.s16.engmyan.fragments.DetailsFragment
import com.s16.engmyan.fragments.FavoriteFragment
import com.s16.engmyan.fragments.RecentFragment
import com.s16.engmyan.utils.MenuItemToggle
import com.s16.engmyan.utils.SearchUiHelper
import com.s16.engmyan.utils.TextToSpeechHelper
import com.s16.utils.defaultSharedPreferences
import com.s16.utils.startActivity
import com.s16.view.Adapter
import com.s16.view.RecyclerViewArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(),
    Adapter.OnItemClickListener,
    SearchUiHelper.OnSearchListener {

    private lateinit var model: DictionaryModel
    private lateinit var detailModel: DefinitionModel
    private lateinit var adapter: SearchListAdapter

    private var recordId: Long = -1

    private var uiScope = CoroutineScope(Dispatchers.Main)
    private var backgroundScope = CoroutineScope(Dispatchers.IO)

    private var favoriteJob: Job? = null
    private var historyJob: Job? = null
    private var submitJob: Job? = null

    private val menuFavorite = MenuItemToggle(true).apply {
        setIcon(R.drawable.ic_favorite_off)
        setTitle(R.string.action_favorite)

        setIconChecked(R.drawable.ic_favorite_on)
        setTitleChecked(R.string.action_favorite_remove)
    }
    private val menuPicture = MenuItemToggle()
    private var menuSound : MenuItem? = null
    private lateinit var textToSpeech: TextToSpeechHelper

    private val isTwoPane: Boolean
        get() = resources.getInteger(R.integer.screen_width) >= 900

    private val lastId : Long
        get() = defaultSharedPreferences.getLong(Constants.PREFS_LAST_ID, Constants.WELCOME_ID)

    private val lastKeyword : String
        get() = defaultSharedPreferences.getString(Constants.PREFS_LAST_KEYWORD, "") ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        textToSpeech = TextToSpeechHelper(this)
        textToSpeech.onTextToSpeechListener = object: TextToSpeechHelper.OnTextToSpeechListener {
            override fun onTextToSpeechInit(enabled: Boolean) {
                menuSound?.isVisible = enabled
            }
        }

        adapter = SearchListAdapter()
        adapter.setOnItemClickListener(object: RecyclerViewArrayAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                adapter.getItem(position)?.let { item ->
                    onItemClick(null, view, position, item.id)
                }
            }
        })

        searchList.layoutManager = LinearLayoutManager(this)
        searchList.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        searchList.adapter = adapter

        model = ViewModelProvider(this).get(DictionaryModel::class.java)
        model.data.observe(
            this, Observer<List<DictionaryItem>> {
                adapter.submitList(it)
                searchList.scrollToPosition(0)
            }
        )

        searchText.setText(lastKeyword, TextView.BufferType.EDITABLE)

        val searchHelper = SearchUiHelper(searchText)
        searchHelper.setOnSearchListener(this)

        if (lastKeyword.isNotEmpty()) {
            model.filter(lastKeyword)
        }

        if (isTwoPane) {
            detailModel = ViewModelProvider(this).get(DefinitionModel::class.java)
            detailModel.data.observe(this, Observer<DefinitionItem> { item ->
                saveHistory(item)

                title = getString(R.string.app_name_two_pane, item.word ?: "")

                menuFavorite.setCheck(item.isFavorite)
                menuPicture.isVisible = item.hasImage

                textToSpeech.text = item.word ?: ""
            })

            performDetail(lastId)
        }
    }

    override fun onPause() {
        saveLastValues()
        super.onPause()
    }

    override fun onDestroy() {
        textToSpeech.shutdown()
        submitJob?.cancel()
        favoriteJob?.cancel()
        historyJob?.cancel()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menuFavorite.menuItem = menu.findItem(R.id.action_favorite)

        menuPicture.menuItem = menu.findItem(R.id.action_picture)
        menuPicture.isVisible = false

        menuSound = menu.findItem(R.id.action_sound)
        if (::textToSpeech.isInitialized && recordId > -1) {
            menuSound?.isVisible = textToSpeech.isEnabled
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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
            R.id.action_manage_favorites -> {
                performManageFavorite()
                true
            }
            R.id.action_recent -> {
                performManageRecent()
                true
            }
            R.id.action_settings -> {
                startActivity<SettingsActivity>()
                true
            }
            R.id.action_exit -> {
                finish()
                exitProcess(0)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSearchTextChanged(searchText: CharSequence?) {
        model.filter("${searchText ?: ""}")
    }

    override fun onSearchSubmit(searchText: CharSequence?) {
        val provider = DbManager(this).provider()

        searchText?.let { word ->
            submitJob = uiScope.launch {
                val ids = withContext(Dispatchers.IO) {
                    val searchWord = "$word".replace("'", "''")
                        .replace("%", "").replace("_", "").trim()
                    provider.queryExacted(searchWord)
                }
                if (ids.isNotEmpty() && ids.size == 1) {
                    onItemClick(null, null, 0, ids.first())
                }
            }
        }
    }

    override fun onClearSearch() {
    }

    override fun onItemClick(parent: View?, view: View?, position: Int, id: Long) {
        performDetail(id)
    }

    private fun saveLastValues() {
        val editor = defaultSharedPreferences.edit()
        editor.putString(Constants.PREFS_LAST_KEYWORD, searchText.text?.toString() ?: "")
        editor.putLong(Constants.PREFS_LAST_ID, recordId)
        editor.apply()
    }

    private fun saveHistory(item: DefinitionItem) {
        val provider = DbManager(this).provider()
        historyJob = backgroundScope.launch {
            provider.createHistory(item.word ?: "", item.id)
        }
    }

    private fun performDetail(id: Long) {
        recordId = id;

        if (!isTwoPane) {
            startActivity<DetailsActivity>(Constants.ARG_PARAM_ID to id)
        } else {
            detailModel.fetch(id)

            val transaction =  supportFragmentManager.beginTransaction()
            transaction.replace(R.id.detailsContent, DetailsFragment.newInstance(id, true), "detailFragment")
            transaction.commit()
        }
    }

    private fun performFavorite(isFavorite: Boolean) {
        val provider = DbManager(this).provider()
        val item = FavoriteItem(word = "$title", refId = recordId, timestamp = System.currentTimeMillis())

        favoriteJob = uiScope.launch {
            val result = withContext(Dispatchers.IO) {
                if (isFavorite) {
                    provider.deleteFavoriteByRef(recordId)
                    0
                } else {
                    provider.insertFavorite(item)
                }
            }

            if (result == 0L) {
                Snackbar.make(mainRoot, getText(R.string.remove_favorites_message), Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(mainRoot, getText(R.string.add_favorites_message), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun performSpeak() {
        textToSpeech.speak()
    }

    private fun performPicture() {
        supportFragmentManager.findFragmentById(R.id.detailsContent)?.let {
            val fragment = it as DetailsFragment
            fragment.togglePicture()
        }
    }

    private fun performManageFavorite() {
        FavoriteFragment.newInstance(isTwoPane).show(supportFragmentManager, "favoriteDialog")
    }

    private fun performManageRecent() {
        RecentFragment.newInstance(isTwoPane,this).show(supportFragmentManager, "recentDialog")
    }
}
