package com.s16.app

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.s16.engmyan.R

abstract class BackStackActivity : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {

    val backStackEntryCount: Int
        get() = supportFragmentManager.backStackEntryCount

    override fun onStart() {
        super.onStart()
        supportFragmentManager.addOnBackStackChangedListener(this)
    }

    override fun onStop() {
        supportFragmentManager.removeOnBackStackChangedListener(this)
        super.onStop()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun onBackStackChanged() {
        supportFragmentManager.run {
            val name = getBackStackEntryAt(backStackEntryCount - 1).name
            findFragmentByTag(name)
        }?.let {
          onBackStackChanged(it)
        }
    }

    abstract fun onBackStackChanged(activeFragment: Fragment)

    @Suppress("UNCHECKED_CAST")
    fun <T: Fragment> getTopFragment(): T? {
        return supportFragmentManager.run {
            val name = getBackStackEntryAt(backStackEntryCount - 1).name
            findFragmentByTag(name)
        } as? T
    }

    fun <T: Fragment> createNewFragment(@IdRes containerId: Int, fragment: T, fragmentName: String) {
        supportFragmentManager.beginTransaction()
            .add(containerId, fragment, fragmentName)
            .addToBackStack(fragmentName)
            .commit()
    }

    fun <T: Fragment> addNewFragment(@IdRes containerId: Int, fragment: T, fragmentName: String) {
        val current = getTopFragment<T>()

        val transaction =  supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit, R.anim.fragment_pop_enter, R.anim.fragment_pop_exit)
            .add(containerId, fragment, fragmentName)

        if (current != null) {
            transaction.hide(current)
        }

        transaction.addToBackStack(fragmentName)
            .commit()
    }
}