package com.suda.yzune.wakeupschedule.schedule

import android.animation.StateListAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.setMargins
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.slider.Slider
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.Ui
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.ViewUtils
import com.suda.yzune.wakeupschedule.utils.ViewUtils.getStatusBarHeight
import com.suda.yzune.wakeupschedule.utils.getPrefer
import splitties.dimensions.dip
import splitties.dimensions.dp
import splitties.resources.colorSL
import splitties.resources.styledColor

class ScheduleActivityUI(override val ctx: Context) : Ui {

    private val statusBarMargin = getStatusBarHeight(ctx) + ctx.dip(8)
    private val outValue = TypedValue()

    val viewPager: ViewPager = ViewPager(ctx).apply {
        id = R.id.anko_vp_schedule
    }

    val bg = AppCompatImageView(ctx).apply {
        id = R.id.anko_iv_bg
        scaleType = ImageView.ScaleType.CENTER_CROP
    }

    val dateView = AppCompatTextView(ctx).apply {
        id = R.id.anko_tv_date
        gravity = Gravity.CENTER
        setTextColor(Color.BLACK)
        textSize = 20f
        typeface = Typeface.DEFAULT_BOLD
    }

    val weekView = AppCompatTextView(ctx).apply {
        id = R.id.anko_tv_week
        setTextColor(Color.BLACK)
    }

    val weekDayView = AppCompatTextView(ctx).apply {
        id = R.id.anko_tv_weekday
        setTextColor(Color.BLACK)
    }

    val addBtn = AppCompatImageButton(ctx).apply {
        id = R.id.anko_ib_add
        setImageResource(R.drawable.ic_outline_add_24)
        setBackgroundResource(outValue.resourceId)
    }

    val importBtn = AppCompatImageButton(ctx).apply {
        id = R.id.anko_ib_import
        setImageResource(R.drawable.ic_outline_get_app_24)
        setBackgroundResource(outValue.resourceId)
    }

    val shareBtn = AppCompatImageButton(ctx).apply {
        id = R.id.anko_ib_share
        setImageResource(R.drawable.ic_outline_share1_24)
        setBackgroundResource(outValue.resourceId)
    }

    val moreBtn = AppCompatImageButton(ctx).apply {
        id = R.id.anko_ib_more
        setImageResource(R.drawable.ic_outline_more_vert_24)
        setBackgroundResource(outValue.resourceId)
    }

    val content = ConstraintLayout(ctx).apply {
        id = R.id.anko_cl_schedule
        context.theme.resolveAttribute(R.attr.selectableItemBackgroundBorderless, outValue, true)
        addView(bg, ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT).apply {
            startToStart = PARENT_ID
            endToEnd = PARENT_ID
            topToTop = PARENT_ID
            bottomToBottom = PARENT_ID
        })

        addView(dateView, ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
            startToStart = PARENT_ID
            topToTop = PARENT_ID
            marginStart = dip(24)
            topMargin = statusBarMargin
        })

        addView(weekView, ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
            startToStart = R.id.anko_tv_date
            topToBottom = R.id.anko_tv_date
            topMargin = dip(4)
        })

        addView(weekDayView, ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
            startToEnd = R.id.anko_tv_week
            topToBottom = R.id.anko_tv_date
            topMargin = dip(4)
            marginStart = dip(8)
        })

        //添加按钮
        addView(addBtn, ConstraintLayout.LayoutParams(dip(32), dip(32)).apply {
            topMargin = statusBarMargin
            endToStart = R.id.anko_ib_import
            topToTop = PARENT_ID
        })

        //导入按钮
        addView(importBtn, ConstraintLayout.LayoutParams(dip(32), dip(32)).apply {
            topMargin = statusBarMargin
            endToStart = R.id.anko_ib_share
            topToTop = PARENT_ID
        })

        //分享按钮
        addView(shareBtn, ConstraintLayout.LayoutParams(dip(32), dip(32)).apply {
            topMargin = statusBarMargin
            endToStart = R.id.anko_ib_more
            topToTop = PARENT_ID
        })

        addView(moreBtn, ConstraintLayout.LayoutParams(dip(32), dip(32)).apply {
            topMargin = statusBarMargin
            marginEnd = dip(8)
            endToEnd = PARENT_ID
            topToTop = PARENT_ID
        })

        addView(viewPager, ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT).apply {
            topToBottom = R.id.anko_tv_week
            bottomToBottom = PARENT_ID
            startToStart = PARENT_ID
            endToEnd = PARENT_ID
        })

    }

    val rvTableName = RecyclerView(ctx).apply {
        id = R.id.bottom_sheet_rv_table
        overScrollMode = View.OVER_SCROLL_NEVER
        layoutManager = LinearLayoutManager(context).apply {
            orientation = RecyclerView.HORIZONTAL
        }
    }

    val changeWeekBtn = createTextButton().apply {
        id = R.id.bottom_sheet_change_week_btn
        text = "修改当前周"
        minWidth = 0
        minimumWidth = 0
        textSize = 12f
    }

    val createScheduleBtn = createTextButton().apply {
        id = R.id.bottom_sheet_create_schedule_btn
        text = "新建课表"
        minWidth = 0
        minimumWidth = 0
        textSize = 12f
    }

    val manageScheduleBtn = createTextButton().apply {
        id = R.id.bottom_sheet_manage_schedule_btn
        text = "管理"
        minWidth = 0
        minimumWidth = 0
        textSize = 12f
    }

    val weekSlider = Slider(ctx).apply {
        id = R.id.bottom_sheet_slider_week
        stepSize = 1f
        setLabelFormatter {
            "第 ${it.toInt()} 周"
        }
        haloRadius = 0
        thumbRadius = dip(8)
        thumbElevation = 0f
        thumbColor = ViewUtils.createColorStateList(Color.WHITE)
        trackHeight = dip(24)
        tickColor = ViewUtils.createColorStateList(Color.TRANSPARENT)
        isSaveEnabled = false
    }

    private val shortCutTitle = AppCompatTextView(ctx).apply {
        id = R.id.bottom_sheet_title_shortcut
        text = "捷径"
        textSize = 12f
    }

    val bottomNavigationView = BottomNavigationView(ctx).apply {
        id = R.id.bottom_sheet_nav_view
        elevation = 0f
        setBackgroundColor(Color.TRANSPARENT)
        inflateMenu(R.menu.bottom_nav_menu)
        val color = shortCutTitle.textColors
        itemTextColor = color
        itemIconTintList = color
        labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_LABELED
        itemTextAppearanceActive = R.style.BottomNavigationViewText
        itemTextAppearanceInactive = R.style.BottomNavigationViewText
    }

    val bottomNavigationView2 = BottomNavigationView(ctx).apply {
        id = R.id.bottom_sheet_nav_view2
        elevation = 0f
        setBackgroundColor(Color.TRANSPARENT)
        inflateMenu(R.menu.bottom_nav_menu2)
        val color = shortCutTitle.textColors
        itemTextColor = color
        itemIconTintList = color
        labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_LABELED
        itemTextAppearanceActive = R.style.BottomNavigationViewText
        itemTextAppearanceInactive = R.style.BottomNavigationViewText
    }

    val cardContent = ConstraintLayout(ctx).apply {
        val space = dip(16)
        isMotionEventSplittingEnabled = false
        addView(AppCompatTextView(context).apply {
            id = R.id.bottom_sheet_title_week
            text = "周数"
            textSize = 12f
        }, ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
            startToStart = PARENT_ID
            topToTop = PARENT_ID
            topMargin = space
            marginStart = space
        })
        addView(changeWeekBtn, ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
            endToEnd = PARENT_ID
            topToTop = R.id.bottom_sheet_title_week
            bottomToBottom = R.id.bottom_sheet_title_week
            marginEnd = space
        })
        addView(weekSlider, ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
            startToStart = PARENT_ID
            endToEnd = PARENT_ID
            topToBottom = R.id.bottom_sheet_title_week
            marginStart = space
            marginEnd = space
        })
        addView(AppCompatTextView(context).apply {
            id = R.id.bottom_sheet_title_schedule
            text = "多课表"
            textSize = 12f
        }, ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
            startToStart = PARENT_ID
            topToBottom = R.id.bottom_sheet_slider_week
            topMargin = dip(8)
            marginStart = space
        })
        addView(rvTableName, ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
            startToStart = PARENT_ID
            endToEnd = PARENT_ID
            topToBottom = R.id.bottom_sheet_title_schedule
            topMargin = dip(16)
            marginStart = space
            marginEnd = space
        })
        addView(manageScheduleBtn, ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
            endToEnd = PARENT_ID
            topToTop = R.id.bottom_sheet_title_schedule
            bottomToBottom = R.id.bottom_sheet_title_schedule
            marginEnd = space
        })
        addView(createScheduleBtn, ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
            endToStart = R.id.bottom_sheet_manage_schedule_btn
            topToTop = R.id.bottom_sheet_title_schedule
            bottomToBottom = R.id.bottom_sheet_title_schedule
        })
        addView(shortCutTitle, ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
            startToStart = PARENT_ID
            topToBottom = R.id.bottom_sheet_rv_table
            bottomToTop = R.id.bottom_sheet_nav_view
            topMargin = dip(16)
            marginStart = space
        })
        addView(bottomNavigationView, ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, dip(64)).apply {
            startToStart = PARENT_ID
            endToEnd = PARENT_ID
            bottomToTop = R.id.bottom_sheet_nav_view2
            topToBottom = R.id.bottom_sheet_title_shortcut
        })
        addView(bottomNavigationView2, ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, dip(64)).apply {
            startToStart = PARENT_ID
            endToEnd = PARENT_ID
            bottomToBottom = PARENT_ID
            topToBottom = R.id.bottom_sheet_nav_view
            bottomMargin = dip(8)
        })
        if (context.getPrefer().getBoolean(Const.KEY_HIDE_NAV_BAR, false)) {
            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
                insets.consumeSystemWindowInsets()
            }
        }
    }

    val bottomSheet = FrameLayout(ctx).apply {
        addView(MaterialCardView(context).apply {
            setCardBackgroundColor(styledColor(R.attr.colorSurface))
            cardElevation = dp(8)
            addView(cardContent, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
            setMargins(dip(16))
            if (context.getPrefer().getBoolean(Const.KEY_HIDE_NAV_BAR, false)) {
                bottomMargin = dip(16) + ViewUtils.getVirtualBarHeight(ctx)
            }
        })
    }

    override val root = CoordinatorLayout(ctx).apply {

        addView(content, CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.MATCH_PARENT,
                CoordinatorLayout.LayoutParams.MATCH_PARENT)
        )

        addView(bottomSheet, CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.MATCH_PARENT,
                ViewUtils.getScreenInfo(context)[1]).apply {
            behavior = BottomSheetBehavior<FrameLayout>(ctx, null).apply {
                isHideable = true
                peekHeight = 0
            }
        })

    }

    fun createTextButton() = MaterialButton(ctx).apply {
        setTextColor(colorSL(R.color.mtrl_text_btn_text_color_selector))
        val space = dip(8)
        setPadding(space, 0, space, 0)
        backgroundTintList = colorSL(R.color.mtrl_btn_text_btn_bg_color_selector)
        rippleColor = colorSL(R.color.mtrl_btn_text_btn_ripple_color)
        elevation = 0f
        stateListAnimator = StateListAnimator()
    }

}