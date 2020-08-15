package com.suda.yzune.wakeupschedule.schedule

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.graphics.ColorUtils
import androidx.core.view.setPadding
import androidx.fragment.app.FragmentActivity
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.Ui
import com.suda.yzune.wakeupschedule.bean.CourseBean
import com.suda.yzune.wakeupschedule.bean.ScheduleStyleConfig
import com.suda.yzune.wakeupschedule.bean.TableConfig
import com.suda.yzune.wakeupschedule.bean.TimeDetailBean
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.ViewUtils
import com.suda.yzune.wakeupschedule.utils.getPrefer
import com.suda.yzune.wakeupschedule.widget.TipTextView
import es.dmoral.toasty.Toasty
import splitties.dimensions.dip
import splitties.dimensions.dp
import kotlin.math.roundToInt

class ScheduleUI(override val ctx: Context, private val tableConfig: TableConfig,
                 private val styleConfig: ScheduleStyleConfig, day: Int,
                 private val forWidget: Boolean = false) : Ui {

    private var col = 6
    private val marginTop = ctx.dip(2)
    private val itemHeight = ctx.dip(styleConfig.itemHeight)
    private val itemAlphaInt = (255 * (styleConfig.itemAlpha.toFloat() / 100)).roundToInt()

    var showTimeDetail = styleConfig.showTimeBar

    val dayMap = IntArray(8)

    init {
        for (i in 1..7) {
            if (!tableConfig.sundayFirst || !styleConfig.showSun) {
                if (!styleConfig.showSat && i == 7) {
                    dayMap[i] = 6
                } else {
                    dayMap[i] = i
                }
            } else {
                if (i == 7) {
                    dayMap[i] = 1
                } else {
                    dayMap[i] = i + 1
                }
            }
        }
        if (styleConfig.showSat) {
            col++
        } else {
            dayMap[6] = -1
        }
        if (styleConfig.showSun) {
            col++
        } else {
            dayMap[7] = -1
        }
    }

    val content = ConstraintLayout(ctx).apply {
        id = R.id.anko_cl_content_panel
        val timeSize = when (col) {
            7 -> 11f
            6 -> 12f
            else -> 10f
        }
        for (i in 1..tableConfig.nodes) {
            addView(LinearLayout(context).apply {
                id = R.id.anko_tv_node1 + i - 1
                orientation = LinearLayout.VERTICAL
                if (showTimeDetail) {
                    gravity = Gravity.CENTER_HORIZONTAL
                } else {
                    gravity = Gravity.CENTER
                }
                addView(TextView(context).apply {
                    setTextColor(styleConfig.textColor)
                    text = i.toString()
                    setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
                    setSingleLine()
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
                if (showTimeDetail) {
                    addView(TextView(context).apply {
                        id = R.id.tv_start
                        setTextColor(styleConfig.textColor)
                        //gravity = Gravity.CENTER
                        //textAlignment = View.TEXT_ALIGNMENT_CENTER
                        setSingleLine()
                        setTextSize(TypedValue.COMPLEX_UNIT_DIP, timeSize)
                    }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        //topMargin = dip(4)
                    })
                    addView(TextView(context).apply {
                        id = R.id.tv_end
                        setTextColor(styleConfig.textColor)
                        setSingleLine()
                        setTextSize(TypedValue.COMPLEX_UNIT_DIP, timeSize)
                    }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        //topMargin = dip(2)
                    })
                }
            }, ConstraintLayout.LayoutParams(0, dip(styleConfig.itemHeight)).apply {
                topMargin = dip(2)
                endToStart = R.id.anko_ll_week_panel_0
                horizontalWeight = 0.64f
                startToStart = ConstraintSet.PARENT_ID
                when (i) {
                    1 -> {
                        bottomToTop = R.id.anko_tv_node1 + i
                        topToTop = ConstraintSet.PARENT_ID
                        verticalBias = 0f
                        verticalChainStyle = ConstraintSet.CHAIN_PACKED
                    }
                    tableConfig.nodes -> {
                        //bottomToTop = R.id.anko_navigation_bar_view
                        bottomToBottom = ConstraintSet.PARENT_ID
                        topToBottom = R.id.anko_tv_node1 + i - 2
                    }
                    else -> {
                        bottomToTop = R.id.anko_tv_node1 + i
                        topToBottom = R.id.anko_tv_node1 + i - 2
                    }
                }
            })
        }

        if (!forWidget && context.getPrefer().getBoolean(Const.KEY_SCHEDULE_BLANK_AREA, true)) {
            addView(View(context), ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, dip(styleConfig.itemHeight) * 4).apply {
                topToBottom = R.id.anko_tv_node1 + tableConfig.nodes - 1
                bottomToBottom = ConstraintSet.PARENT_ID
                startToStart = ConstraintSet.PARENT_ID
                endToEnd = ConstraintSet.PARENT_ID
            })
        }

        for (i in 0 until col - 1) {
            addView(FrameLayout(context).apply { id = R.id.anko_ll_week_panel_0 + i }, ConstraintLayout.LayoutParams(0,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
                horizontalWeight = 1f
                when (i) {
                    0 -> {
                        startToEnd = R.id.anko_tv_node1
                        endToStart = R.id.anko_ll_week_panel_0 + i + 1
                        marginEnd = dip(1)
                    }
                    col - 2 -> {
                        startToEnd = R.id.anko_ll_week_panel_0 + i - 1
                        endToEnd = ConstraintSet.PARENT_ID
                        marginStart = dip(1)
                        if (!forWidget) {
                            marginEnd = if (col < 8) {
                                dip(8)
                            } else {
                                dip(4)
                            }
                        }
                    }
                    else -> {
                        startToEnd = R.id.anko_ll_week_panel_0 + i - 1
                        endToStart = R.id.anko_ll_week_panel_0 + i + 1
                        marginStart = dip(1)
                        marginEnd = dip(1)
                    }
                }
            })
        }
    }

    val scrollView = ScrollView(ctx).apply {
        id = R.id.anko_sv_schedule
        overScrollMode = View.OVER_SCROLL_NEVER
        isVerticalScrollBarEnabled = false
        addView(content)
    }

    override val root = ConstraintLayout(ctx).apply {
        val textAlphaColor = ColorUtils.setAlphaComponent(styleConfig.textColor, (0.32 * (styleConfig.textColor shr 24 and 0xff)).toInt())
        for (i in 0 until col) {
            addView(TextView(context).apply {
                id = R.id.anko_tv_title0 + i
                setPadding(0, dip(8), 0, dip(8))
                textSize = 12f
                gravity = Gravity.CENTER
                setLineSpacing(dp(2), 1f)
                if (i == 0 || (day > 0 && i == dayMap[day])) {
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(styleConfig.textColor)
                } else {
                    setTextColor(textAlphaColor)
                }
            }, ConstraintLayout.LayoutParams(0, ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
                when (i) {
                    0 -> {
                        horizontalWeight = 0.64f
                        startToStart = ConstraintSet.PARENT_ID
                        topToTop = ConstraintSet.PARENT_ID
                        endToStart = R.id.anko_tv_title0 + i + 1
                    }
                    col - 1 -> {
                        horizontalWeight = 1f
                        startToEnd = R.id.anko_tv_title0 + i - 1
                        endToEnd = ConstraintSet.PARENT_ID
                        baselineToBaseline = R.id.anko_tv_title0 + i - 1
                        marginStart = dip(1)
                        if (!forWidget) {
                            marginEnd = if (col < 8) {
                                dip(8)
                            } else {
                                dip(4)
                            }
                        }
                    }
                    else -> {
                        horizontalWeight = 1f
                        startToEnd = R.id.anko_tv_title0 + i - 1
                        endToStart = R.id.anko_tv_title0 + i + 1
                        baselineToBaseline = R.id.anko_tv_title0 + i - 1
                        if (i != 1) {
                            marginStart = dip(1)
                        }
                        marginEnd = dip(1)
                    }
                }
            })
        }

        addView(scrollView, ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT).apply {
            bottomToBottom = ConstraintSet.PARENT_ID
            topToBottom = R.id.anko_tv_title0
            startToStart = ConstraintSet.PARENT_ID
            endToEnd = ConstraintSet.PARENT_ID
        })
    }

    fun initWeekPanel(data: List<CourseBean>?, timeList: List<TimeDetailBean>, week: Int, day: Int, fragmentActivity: FragmentActivity? = null) {
        val ll = content.getViewById(R.id.anko_ll_week_panel_0 + dayMap[day] - 1) as FrameLayout?
                ?: return
        ll.removeAllViews()
        if (data == null || data.isEmpty()) return
        var isCovered = false
        var pre = data[0]
        if (pre.tableId != tableConfig.id) return
        for (i in data.indices) {
            val c = data[i]

            // 过期的不显示
            if (c.endWeek < week) {
                continue
            }

            val isOtherWeek = (week % 2 == 0 && c.type == 1) || (week % 2 == 1 && c.type == 2)
                    || (c.startWeek > week)

            if (!styleConfig.showOtherWeekCourse && isOtherWeek) continue

            var isError = false

            val strBuilder = StringBuilder()
            val detailBuilder = StringBuilder()
            if (c.step <= 0) {
                c.step = 1
                isError = true
                if (!forWidget) {
                    Toasty.info(ctx, R.string.error_course_data, Toast.LENGTH_LONG).show()
                }
            }
            if (c.startNode <= 0) {
                c.startNode = 1
                isError = true
                if (!forWidget) {
                    Toasty.info(ctx, R.string.error_course_data, Toast.LENGTH_LONG).show()
                }
            }
            if (c.startNode > tableConfig.nodes) {
                c.startNode = tableConfig.nodes
                isError = true
                if (!forWidget) {
                    Toasty.info(ctx, R.string.error_course_node, Toast.LENGTH_LONG).show()
                }
            }
            if (c.startNode + c.step - 1 > tableConfig.nodes) {
                c.step = tableConfig.nodes - c.startNode + 1
                isError = true
                if (!forWidget) {
                    Toasty.info(ctx, R.string.error_course_node, Toast.LENGTH_LONG).show()
                }
            }

            val textView = TipTextView(ctx)

            if (ll.childCount != 0) {
                isCovered = (pre.startNode == c.startNode)
            }

            textView.setPadding(ctx.dip(4))

            if (c.color.isEmpty()) {
                c.color = "#${Integer.toHexString(ViewUtils.getCustomizedColor(ctx, c.id % 9))}"
            }

            if (styleConfig.showTeacher && c.teacher != "") {
                detailBuilder.append("\n${c.teacher}")
            }

            if (isOtherWeek) {
                when (c.type) {
                    1 -> detailBuilder.append("\n单周")
                    2 -> detailBuilder.append("\n双周")
                }
                detailBuilder.append("\n[非本周]")
                textView.visibility = View.VISIBLE
            } else {
                when (c.type) {
                    1 -> detailBuilder.append("\n单周")
                    2 -> detailBuilder.append("\n双周")
                }
            }

            if (isCovered) {
                val tv = ll.getChildAt(ll.childCount - 1) as TipTextView?
                if (tv != null) {
                    if (tv.tipVisibility == TipTextView.TIP_OTHER_WEEK) {
                        tv.visibility = View.INVISIBLE
                    }
                }
            }

            val tv = ll.findViewWithTag<TipTextView?>(c.startNode)
            if (tv != null) {
                textView.visibility = View.INVISIBLE
                if (tv.tipVisibility != TipTextView.TIP_VISIBLE && !isOtherWeek) {
                    if (tv.tipVisibility != TipTextView.TIP_ERROR) {
                        tv.tipVisibility = TipTextView.TIP_VISIBLE
                    }
                    if (!forWidget && fragmentActivity != null) {
                        tv.setOnClickListener {
                            MultiCourseFragment.newInstance(week, c.day, c.startNode).show(fragmentActivity.supportFragmentManager, "multi")
                        }
                    }
                }
            }

            if (isError) {
                textView.tipVisibility = TipTextView.TIP_ERROR
            }

            if (!isOtherWeek) {
                textView.tag = c.startNode
            } else {
                textView.tipVisibility = TipTextView.TIP_OTHER_WEEK
            }

            if (styleConfig.showTime && timeList.isNotEmpty()) {
                strBuilder.append(timeList[c.startNode - 1].startTime + "\n")
            }
            strBuilder.append(c.courseName)
            if (c.room != "") {
                strBuilder.append("\n@${c.room}")
            }
            if (!styleConfig.showTeacher) {
                strBuilder.append(detailBuilder)
            }

            textView.init(
                    text = strBuilder.toString(),
                    detail = if (styleConfig.showTeacher) detailBuilder.toString() else "",
                    bgColor = Color.parseColor(c.color),
                    bgAlpha = itemAlphaInt,
                    styleConfig = styleConfig
            )

            if (!forWidget && fragmentActivity != null) {
                textView.setOnClickListener {
                    try {
                        val detailFragment = CourseDetailFragment.newInstance(week, c)
                        detailFragment.show(fragmentActivity.supportFragmentManager, "courseDetail")
                    } catch (e: Exception) {
                        //TODO: 提示是否要删除异常的数据
                        Toasty.error(fragmentActivity, ctx.getString(R.string.msg_crash)).show()
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    textView.setOnLongClickListener {
                        val shadowBuilder = View.DragShadowBuilder(it)
                        it.startDragAndDrop(null, shadowBuilder, 1, 0)
                        return@setOnLongClickListener true
                    }
                }

            }

            ll.addView(textView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    itemHeight * c.step + marginTop * (c.step - 1)).apply {
                gravity = Gravity.TOP
                topMargin = (c.startNode - 1) * (itemHeight + marginTop) + marginTop
            })

            pre = c
        }
    }

}
