package com.s16.engmyan.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.s16.engmyan.Constants
import com.s16.engmyan.R
import com.s16.engmyan.adapters.SearchListAdapter
import com.s16.engmyan.data.DbManager
import com.s16.engmyan.data.DictionaryItem
import com.s16.engmyan.data.DictionaryModel
import com.s16.engmyan.fragments.FavoriteFragment
import com.s16.engmyan.fragments.RecentFragment
import com.s16.engmyan.utils.SearchUiHelper
import com.s16.utils.startActivity
import com.s16.view.RecyclerViewArrayAdapter

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(),
    RecyclerViewArrayAdapter.OnItemClickListener,
    SearchUiHelper.OnSearchListener {

    private lateinit var model: DictionaryModel
    private lateinit var adapter: SearchListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        adapter = SearchListAdapter()
        adapter.setOnItemClickListener(this)

        searchList.layoutManager = LinearLayoutManager(this)
        searchList.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        searchList.adapter = adapter

        val modelFactory = DictionaryModel.Factory(DbManager(this).provider())
        model = ViewModelProviders.of(this, modelFactory).get(DictionaryModel::class.java)
        model.data.observe(
            this, Observer<List<DictionaryItem>> {
                adapter.submitList(it)
                searchList.scrollToPosition(0)
            }
        )

        val searchHelper = SearchUiHelper(searchText)
        searchHelper.setOnSearchListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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

    }

    override fun onClearSearch() {
    }

    override fun onItemClick(view: View, position: Int) {
        adapter.getItem(position)?.let { item ->
            startActivity<DetailsActivity>(Constants.ARG_PARAM_ID to item.id)
        }
    }

    private fun performManageFavorite() {
        FavoriteFragment.newInstance().show(supportFragmentManager, "favoriteDialog")
    }

    private fun performManageRecent() {
        RecentFragment.newInstance().show(supportFragmentManager, "recentDialog")
    }
}
