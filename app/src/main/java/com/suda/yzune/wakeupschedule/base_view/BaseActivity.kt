package com.suda.yzune.wakeupschedule.base_view

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.ViewUtils
import com.suda.yzune.wakeupschedule.utils.getPrefer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class BaseActivity : AppCompatActivity() {

    fun launch(block: suspend CoroutineScope.() -> Unit): Job = lifecycleScope.launch {
        lifecycle.whenStarted(block)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.TRANSPARENT
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.TRANSPARENT
            }
        }
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                }
            } // Night mode is not active, we're using the light theme
            Configuration.UI_MODE_NIGHT_YES -> {
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            } // Night mode is active, we're using dark theme
        }
        if (getPrefer().getBoolean(Const.KEY_HIDE_NAV_BAR, false)) {
            val decorView = window.decorView
            val currentStatusBar = if (VERSION.SDK_INT >= VERSION_CODES.M) decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else 0
            window.navigationBarColor = Color.TRANSPARENT
            //window.statusBarColor = statusBarColor
            val systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or currentStatusBar
            decorView.systemUiVisibility = systemUiVisibility
        }
        super.onCreate(savedInstanceState)
    }

    fun resizeStatusBar(view: View) {
        ViewUtils.resizeStatusBar(this, view)
    }

    fun getStatusBarHeight(): Int {
        return ViewUtils.getStatusBarHeight(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }
}