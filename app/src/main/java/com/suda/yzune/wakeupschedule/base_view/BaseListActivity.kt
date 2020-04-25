package com.suda.yzune.wakeupschedule.base_view

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OVER_SCROLL_NEVER
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.ViewUtils
import com.suda.yzune.wakeupschedule.utils.getPrefer
import splitties.dimensions.dip
import splitties.resources.color
import splitties.resources.styledColor

abstract class BaseListActivity : BaseActivity() {

    abstract fun onSetupSubButton(): View?
    lateinit var mainTitle: AppCompatTextView
    lateinit var searchView: AppCompatEditText
    protected var showSearch = false
    protected var textWatcher: TextWatcher? = null
    protected lateinit var mRecyclerView: RecyclerView
    lateinit var rootView: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootView = createView()
        setContentView(rootView)
        if (getPrefer().getBoolean(Const.KEY_HIDE_NAV_BAR, false)) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                v.setPadding(0, 0, insets.systemWindowInsetRight, 0)
                insets
            }
        }
    }

    private fun createView() = ConstraintLayout(this).apply {

        val outValue = TypedValue()
        theme.resolveAttribute(R.attr.selectableItemBackgroundBorderless, outValue, true)

        mRecyclerView = RecyclerView(context, null, R.attr.verticalRecyclerViewStyle).apply {
            overScrollMode = OVER_SCROLL_NEVER
        }

        mainTitle = AppCompatTextView(context).apply {
            text = title
            gravity = Gravity.CENTER_VERTICAL
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
        }

        searchView = AppCompatEditText(context).apply {
            hint = "请输入……"
            textSize = 16f
            background = null
            gravity = Gravity.CENTER_VERTICAL
            visibility = View.GONE
            setLines(1)
            setSingleLine()
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            addTextChangedListener(textWatcher)
        }

        addView(mRecyclerView, ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT).apply {
            topToBottom = R.id.anko_layout
            bottomToBottom = ConstraintSet.PARENT_ID
            startToStart = ConstraintSet.PARENT_ID
            endToEnd = ConstraintSet.PARENT_ID
        })

        addView(LinearLayoutCompat(context).apply {
            id = R.id.anko_layout
            setPadding(0, getStatusBarHeight(), 0, 0)
            setBackgroundColor(styledColor(R.attr.colorSurface))

            addView(AppCompatImageButton(context).apply {
                setImageResource(R.drawable.ic_back)
                setBackgroundResource(outValue.resourceId)
                setPadding(dip(8))
                setColorFilter(styledColor(R.attr.colorOnBackground))
                setOnClickListener {
                    onBackPressed()
                }
            }, LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, dip(48)))

            addView(mainTitle,
                    LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, dip(48)).apply {
                        weight = 1f
                    })

            addView(searchView, LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, dip(48)).apply {
                weight = 1f
            })

            if (showSearch) {
                addView(AppCompatImageButton(context).apply {
                    setBackgroundResource(outValue.resourceId)
                    setImageResource(R.drawable.ic_outline_search_24)
                    setOnClickListener {
                        when (searchView.visibility) {
                            View.GONE -> {
                                mainTitle.visibility = View.GONE
                                searchView.visibility = View.VISIBLE
                                imageTintList = ViewUtils.createColorStateList(color(R.color.colorAccent))
                                searchView.isFocusable = true
                                searchView.isFocusableInTouchMode = true
                                searchView.requestFocus()
                                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                                        .showSoftInput(searchView, 0)
                            }
                        }
                    }
                }, LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, dip(48)).apply {
                    marginEnd = dip(24)
                })
            }

            onSetupSubButton()?.let {
                (it as? TextView)?.gravity = Gravity.CENTER_VERTICAL
                it.setBackgroundResource(outValue.resourceId)
                it.setPadding(dip(24), 0, dip(24), 0)
                addView(it, LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, dip(48)))
            }
        }, ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
            topToTop = ConstraintSet.PARENT_ID
            startToStart = ConstraintSet.PARENT_ID
            endToEnd = ConstraintSet.PARENT_ID
        })
    }

    override fun onDestroy() {
        searchView.removeTextChangedListener(textWatcher)
        textWatcher = null
        super.onDestroy()
    }
}
