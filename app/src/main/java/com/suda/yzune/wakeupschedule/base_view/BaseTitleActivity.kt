package com.suda.yzune.wakeupschedule.base_view

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.ViewCompat
import androidx.core.view.setPadding
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.getPrefer
import splitties.dimensions.dip
import splitties.resources.styledColor

abstract class BaseTitleActivity : BaseActivity() {

    @get:LayoutRes
    protected abstract val layoutId: Int

    open fun onSetupSubButton(): View? {
        return null
    }

    lateinit var mainTitle: AppCompatTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)
        val root = findViewById<LinearLayoutCompat>(R.id.ll_root)
        root.addView(createTitleBar(), 0)
        if (getPrefer().getBoolean(Const.KEY_HIDE_NAV_BAR, false)) {
            ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
                v.setPadding(0, 0, insets.systemWindowInsetRight, 0)
                insets
            }
        }
    }

    open fun createTitleBar() = LinearLayoutCompat(this).apply {
        setBackgroundColor(styledColor(R.attr.colorSurface))
        setPadding(0, getStatusBarHeight(), 0, 0)
        val outValue = TypedValue()
        theme.resolveAttribute(R.attr.selectableItemBackgroundBorderless, outValue, true)

        addView(AppCompatImageButton(context).apply {
            setImageResource(R.drawable.ic_back)
            setBackgroundResource(outValue.resourceId)
            setPadding(dip(8))
            setColorFilter(styledColor(R.attr.colorOnBackground))
            setOnClickListener {
                onBackPressed()
            }
        }, LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, dip(48)))

        mainTitle = AppCompatTextView(context).apply {
            text = title
            gravity = Gravity.CENTER_VERTICAL
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
        }

        addView(mainTitle, LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, dip(48)).apply {
            weight = 1f
        })

        onSetupSubButton()?.let {
            (it as? TextView)?.gravity = Gravity.CENTER_VERTICAL
            it.setBackgroundResource(outValue.resourceId)
            it.setPadding(dip(24), 0, dip(24), 0)
            addView(it, LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, dip(48)))
        }

    }


}