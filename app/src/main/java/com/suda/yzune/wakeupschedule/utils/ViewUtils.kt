package com.suda.yzune.wakeupschedule.utils

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Environment
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.bean.CourseBean
import com.suda.yzune.wakeupschedule.bean.TimeDetailBean
import com.suda.yzune.wakeupschedule.bean.WidgetStyleConfig
import splitties.dimensions.dip
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

object ViewUtils {

    fun judgeColorIsLight(color: Int): Boolean {
        val red = color and 0xff0000 shr 16
        val green = color and 0x00ff00 shr 8
        val blue = color and 0x0000ff
        return (0.213 * red + 0.715 * green + 0.072 * blue > 255 / 2)
    }

    fun getScreenInfo(context: Context): Array<Int> {
        val displayMetrics = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        return arrayOf(width, height)
    }

    fun getHtmlSpannedString(str: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(str, HtmlCompat.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(str)
        }
    }

    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun resizeStatusBar(context: Context, view: View) {
        val layoutParams = view.layoutParams
        layoutParams.height = getStatusBarHeight(context.applicationContext)
        view.layoutParams = layoutParams
    }

    /**
     * 获取是否存在NavigationBar
     * @param context
     * @return
     */
    fun checkDeviceHasNavigationBar(context: Context): Boolean {
        var hasNavigationBar = false
        val rs = context.resources
        val id = rs.getIdentifier("config_showNavigationBar", "bool", "android")
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id)
        }
        try {
            val systemPropertiesClass = Class.forName("android.os.SystemProperties")
            val m = systemPropertiesClass.getMethod("get", String::class.java)
            val navBarOverride = m.invoke(systemPropertiesClass, "qemu.hw.mainkeys") as String
            if ("1" == navBarOverride) {
                hasNavigationBar = false
            } else if ("0" == navBarOverride) {
                hasNavigationBar = true
            }
        } catch (e: Exception) {

        }

        return hasNavigationBar
    }

    /**
     * 获取虚拟功能键高度
     * @param context
     * @return
     */
    fun getVirtualBarHeight(context: Context): Int {
        var vh = 0
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val dm = DisplayMetrics()
        try {
            val c = Class.forName("android.view.Display")
            val method = c.getMethod("getRealMetrics", DisplayMetrics::class.java)
            method.invoke(display, dm)
            val point = Point()
            windowManager.defaultDisplay.getSize(point)
            vh = dm.heightPixels - point.y
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return vh
    }

    fun createColorStateList(color: Int): ColorStateList {
        val colors = intArrayOf(color, color, color, color, color, color)
        val states = arrayOfNulls<IntArray>(6)
        states[0] = intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled)
        states[1] = intArrayOf(android.R.attr.state_enabled, android.R.attr.state_focused)
        states[2] = intArrayOf(android.R.attr.state_enabled)
        states[3] = intArrayOf(android.R.attr.state_focused)
        states[4] = intArrayOf(android.R.attr.state_window_focused)
        states[5] = intArrayOf()
        return ColorStateList(states, colors)
    }

    fun createColorStateList(normal: Int, pressed: Int, focused: Int, unable: Int): ColorStateList {
        val colors = intArrayOf(pressed, focused, normal, focused, unable, normal)
        val states = arrayOfNulls<IntArray>(6)
        states[0] = intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled)
        states[1] = intArrayOf(android.R.attr.state_enabled, android.R.attr.state_focused)
        states[2] = intArrayOf(android.R.attr.state_enabled)
        states[3] = intArrayOf(android.R.attr.state_focused)
        states[4] = intArrayOf(android.R.attr.state_window_focused)
        states[5] = intArrayOf()
        return ColorStateList(states, colors)
    }

    fun getRealSize(activity: Activity): Point {
        val size = Point()
        activity.windowManager.defaultDisplay.getRealSize(size)
        return size
    }

    fun saveImg(bitmap: Bitmap) {
        //把图片存储在哪个文件夹
        val file = File(Environment.getExternalStorageDirectory(), "DCIM")
        if (!file.exists()) {
            file.mkdir()
        }
        //图片的名称
        val name = "mz.jpg"
        val file1 = File(file, name)
        if (!file1.exists()) {
            try {
                val fileOutputStream = FileOutputStream(file1)
                //这个100表示压缩比,100说明不压缩,90说明压缩到原来的90%
                //注意:这是对于占用存储空间而言,不是说占用内存的大小
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        //通知图库即使更新,否则不能看到图片
        //activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file1.getAbsolutePath())));
    }

    fun getViewBitmap(viewGroup: ViewGroup, low: Boolean = false, marginBottom: Int = 0): Bitmap {
        var h = 0
        val bitmap: Bitmap
        // 获取scrollView实际高度,这里很重要
        for (i in 0 until viewGroup.childCount) {
            h += viewGroup.getChildAt(i).height + marginBottom
        }
        // 创建对应大小的bitmap
        bitmap = Bitmap.createBitmap(viewGroup.width, h,
                if (low) Bitmap.Config.ARGB_4444 else Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        viewGroup.draw(canvas)
        return bitmap
    }


    fun layoutView(v: View, width: Int, height: Int) {
        // validate view.width and view.height
        v.layout(0, 0, width, height)
        val measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

        // validate view.measurewidth and view.measureheight
        v.measure(measuredWidth, measuredHeight)
        v.layout(0, 0, v.measuredWidth, v.measuredHeight)
    }

    fun getCustomizedColor(context: Context, index: Int): Int {
        val customizedColors = context.resources.getIntArray(R.array.customizedColors)
        return customizedColors[index]
    }

    fun initTodayCourseView(context: Context, styleConfig: WidgetStyleConfig, course: CourseBean, timeList: List<TimeDetailBean>): View {
        val dp = 2
        val alphaInt = (255 * (styleConfig.itemAlpha.toFloat() / 100)).roundToInt()
        var alphaStr = if (alphaInt != 0) {
            Integer.toHexString(alphaInt)
        } else {
            "00"
        }
        if (alphaStr.length < 2) {
            alphaStr = "0$alphaStr"
        }
        val widgetTextSize = styleConfig.itemTextSize.toFloat()
        return LinearLayout(context).apply {
            id = R.id.anko_layout
            orientation = LinearLayout.VERTICAL

            addView(LinearLayout(context).apply {
                setPadding(dip(dp * 4))

                if (styleConfig.showColor) {
                    background = ContextCompat.getDrawable(context.applicationContext, R.drawable.course_item_bg_today)
                    val myGrad = background as GradientDrawable
//                                myGrad.cornerRadius = dip(dp * 4).toFloat()
                    myGrad.setStroke(dip(dp), styleConfig.strokeColor)
                    when {
                        course.color.length == 7 -> myGrad.setColor(Color.parseColor("#$alphaStr${course.color.substring(1, 7)}"))
                        course.color.isEmpty() -> {
                            myGrad.setColor(Color.parseColor("#${alphaStr}fa6278"))
                        }
                        else -> myGrad.setColor(Color.parseColor("#$alphaStr${course.color.substring(3, 9)}"))
                    }
                }

                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    // 开始节
                    addView(TextView(context).apply {
                        text = course.startNode.toString()
                        alpha = 0.8f
                        setTextColor(styleConfig.courseTextColor)
                        textSize = widgetTextSize
                        typeface = Typeface.DEFAULT_BOLD
                    }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        bottomMargin = dip(dp * 2)
                    })
                    // 结束节
                    addView(TextView(context).apply {
                        text = "${course.startNode + course.step - 1}"
                        alpha = 0.8f
                        setTextColor(styleConfig.courseTextColor)
                        textSize = widgetTextSize
                        typeface = Typeface.DEFAULT_BOLD
                    }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        topMargin = dip(dp * 2)
                    })

                }, LinearLayout.LayoutParams(dip(dp * 10), LinearLayout.LayoutParams.MATCH_PARENT))

                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER

                    addView(TextView(context).apply {
                        alpha = 0.8f
                        text = timeList[course.startNode - 1].startTime
                        setTextColor(styleConfig.courseTextColor)
                        textSize = widgetTextSize
                    }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        setMargins(dip(dp * 2))
                    })

                    addView(TextView(context).apply {
                        text = timeList[course.startNode + course.step - 2].endTime
                        alpha = 0.8f
                        setTextColor(styleConfig.courseTextColor)
                        textSize = widgetTextSize
                    }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        setMargins(dip(dp * 2))
                    })

                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT))

                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_VERTICAL

                    addView(TextView(context).apply {
                        text = course.courseName
                        setTextColor(styleConfig.courseTextColor)
                        textSize = widgetTextSize + 2
                        typeface = Typeface.DEFAULT_BOLD
                    }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

                    if (course.room != "" || course.teacher != "") {
                        addView(LinearLayout(context).apply {
                            if (course.room != "") {

                                addView(ImageView(context).apply {
                                    setImageResource(R.drawable.ic_outline_location_on_24)
                                    alpha = 0.8f
                                    imageTintList = ViewUtils.createColorStateList(styleConfig.courseTextColor)
                                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT))

                                addView(TextView(context).apply {
                                    text = course.room
                                    alpha = 0.8f
                                    setTextColor(styleConfig.courseTextColor)
                                    maxLines = 1
                                    textSize = widgetTextSize + 2
                                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                                    marginEnd = dip(dp * 8)
                                })
                            }
                            if (course.teacher != "") {
                                addView(ImageView(context).apply {
                                    setImageResource(R.drawable.ic_outline_person_outline_24)
                                    alpha = 0.8f
                                    imageTintList = ViewUtils.createColorStateList(styleConfig.courseTextColor)
                                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT))

                                addView(TextView(context).apply {
                                    alpha = 0.8f
                                    text = course.teacher
                                    setTextColor(styleConfig.courseTextColor)
                                    maxLines = 1
                                    ellipsize = TextUtils.TruncateAt.END
                                    textSize = widgetTextSize + 2
                                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {

                                })
                            }
                        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                            topMargin = dip(dp * 4)
                        })
                    }

                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT).apply {
                    marginStart = dip(dp)
                })
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

    }

}
