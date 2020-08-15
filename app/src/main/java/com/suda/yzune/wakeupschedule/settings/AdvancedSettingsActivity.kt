package com.suda.yzune.wakeupschedule.settings

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.edit
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.suda.yzune.wakeupschedule.AppDatabase
import com.suda.yzune.wakeupschedule.BuildConfig
import com.suda.yzune.wakeupschedule.DonateActivity
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseListActivity
import com.suda.yzune.wakeupschedule.dao.AppWidgetDao
import com.suda.yzune.wakeupschedule.settings.items.*
import com.suda.yzune.wakeupschedule.utils.AppWidgetUtils
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.getPrefer
import com.suda.yzune.wakeupschedule.widget.colorpicker.ColorPickerFragment
import es.dmoral.toasty.Toasty
import splitties.activities.start
import splitties.resources.color
import splitties.snackbar.longSnack

class AdvancedSettingsActivity : BaseListActivity(), ColorPickerFragment.ColorPickerDialogListener {

    override fun onColorSelected(dialogId: Int, color: Int) {
        if (Color.alpha(color) < Const.MIN_TEXT_COLOR_ALPHA) {
            getPrefer().edit {
                putInt(Const.KEY_THEME_COLOR, ColorUtils.setAlphaComponent(color, Const.MIN_TEXT_COLOR_ALPHA))
            }
        } else {
            getPrefer().edit {
                putInt(Const.KEY_THEME_COLOR, color)
            }
        }
        mRecyclerView.longSnack("重启App后生效哦~")
    }

    private lateinit var dataBase: AppDatabase
    private lateinit var widgetDao: AppWidgetDao

    private val mAdapter = SettingItemAdapter()

    override fun onSetupSubButton(): View? {
        return if (BuildConfig.CHANNEL == "google" || (BuildConfig.CHANNEL == "huawei" && !getPrefer().getBoolean(Const.KEY_SHOW_DONATE, false))) {
            null
        } else {
            val tvButton = AppCompatTextView(this)
            tvButton.text = "捐赠"
            tvButton.setTextColor(color(R.color.colorAccent))
            tvButton.setOnClickListener {
                start<DonateActivity>()
            }
            tvButton
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataBase = AppDatabase.getDatabase(application)
        widgetDao = dataBase.appWidgetDao()

        val items = mutableListOf<BaseSettingItem>()
        onItemsCreated(items)
        mAdapter.data = items
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.itemAnimator?.changeDuration = 250
        mRecyclerView.adapter = mAdapter
        mAdapter.addChildClickViewIds(R.id.anko_check_box)
        mAdapter.setOnItemChildClickListener { _, view, position ->
            when (val item = items[position]) {
                is SwitchItem -> onSwitchItemCheckChange(item, view.findViewById<AppCompatCheckBox>(R.id.anko_check_box).isChecked)
            }
        }
        mAdapter.setOnItemClickListener { _, view, position ->
            when (val item = items[position]) {
                is VerticalItem -> onVerticalItemClick(item)
                is SwitchItem -> view.findViewById<AppCompatCheckBox>(R.id.anko_check_box).performClick()
                is SeekBarItem -> onSeekBarItemClick(item, position)
            }
        }
    }

    private fun onItemsCreated(items: MutableList<BaseSettingItem>) {
        val colorStr = getPrefer().getInt(Const.KEY_THEME_COLOR, color(R.color.colorAccent))
                .toString(16)
        when {
            BuildConfig.CHANNEL == "google" -> {
                items.add(CategoryItem(R.string.setting_theme, true))
            }
            BuildConfig.CHANNEL == "huawei" && !getPrefer().getBoolean(Const.KEY_SHOW_DONATE, false) -> {
                items.add(CategoryItem(R.string.setting_theme, true))
            }
            else -> {
                items.add(CategoryItem(R.string.setting_pay, true))
                items.add(VerticalItem(R.string.setting_unlock, "<b><font color='#$colorStr'>当前页面</font></b>往下滑能看到的就是高级功能，<b><font color='#$colorStr'>理论上是可以直接使用的</font></b>，" +
                        "但是，像<b><font color='#$colorStr'>无人看守</font></b>的小卖部，付费后再使用是诚信的表现哦~同时也能鼓励我们继续开发、继续完善功能。" +
                        "当然，如果对这些功能的可靠性有疑问，可以<b><font color='#$colorStr'>先试用再付费</font></b>。" +
                        "<br>朋友、校友、亲人，以及在此之前已经捐赠过的用户，已经解锁了高级功能，<b><font color='#$colorStr'>无需再花钱</font></b>。" +
                        "<br>其他用户的解锁方式如下，<b><font color='#$colorStr'>二选一即可：</font></b><br>1. 应用商店5星 + 支付宝付款2元<br>2. 支付宝付款5元<br><b><font color='#$colorStr'>仔细考虑后点击此处进行付款，</font></b>感谢支持！", true))
                items.add(VerticalItem(R.string.setting_after_unlock, "解锁后，你可以在你自用的任何设备上安装使用，并且免费使用后续更新的高级功能。<br><b><font color='#$colorStr'>放心，无论什么版本，App不会有任何形式的广告。</font></b>", true))
                items.add(CategoryItem(R.string.setting_theme, false))
            }
        }

        items.add(VerticalItem(R.string.setting_theme_color, "调整大部分标签的颜色"))
        items.add(SwitchItem(R.string.setting_hide_virtual_key, getPrefer().getBoolean(Const.KEY_HIDE_NAV_BAR, false), "只对有虚拟键的手机有效哦，是为了有更好的沉浸效果~\n" +
                "有实体按键或全面屏手势的手机本身就很棒啦~"))
        items.add(CategoryItem(R.string.setting_notify, false))
        items.add(VerticalItem(R.string.setting_notify_intro, "本功能处于<b><font color='#$colorStr'>试验性阶段</font></b>。由于国产手机对系统的定制不尽相同，本功能可能会在某些手机上失效。<b><font color='#$colorStr'>开启前提：设置好课程时间 + 往桌面添加一个日视图小部件 + 允许App后台运行</font></b>。<br>理论上<b><font color='#$colorStr'>每次设置之后</font></b>需要半天以上的时间才会正常工作，理论上不会很耗电。", true))
        items.add(SwitchItem(R.string.setting_notify_turn_on, getPrefer().getBoolean(Const.KEY_COURSE_REMIND, false)))
        items.add(SwitchItem(R.string.setting_notify_on_going, getPrefer().getBoolean(Const.KEY_REMINDER_ON_GOING, false)))
        items.add(SeekBarItem(R.string.setting_notify_time, getPrefer().getInt(Const.KEY_REMINDER_TIME, 20), 0, 90, "分钟"))
        items.add(VerticalItem(R.string.setting_add_widget, "长按桌面空白处，或者在桌面做双指捏合手势，选择桌面小工具，肯定是有的，仔细找找，实在找不到就重启手机再找。\n" +
                "P.S. 添加桌面小部件，想要确保它正常工作，最好在系统设置中，手动管理本App的后台，允许本App后台自启和后台运行。"))
        //items.add(SwitchItem("提醒同时将手机静音", PreferenceUtils.getBooleanFromSP(applicationContext, "silence_reminder", false)))
        items.add(VerticalItem(R.string.setting_blank, "\n\n\n"))
    }

    private fun onSwitchItemCheckChange(item: SwitchItem, isChecked: Boolean) {
        when (item.title) {
            R.string.setting_hide_virtual_key -> {
                getPrefer().edit {
                    putBoolean(Const.KEY_HIDE_NAV_BAR, isChecked)
                }
                mRecyclerView.longSnack("重启App后生效哦~")
                item.checked = isChecked
            }
            R.string.setting_notify_turn_on -> {
                launch {
                    val task = widgetDao.getWidgetsByTypes(0, 1)
                    if (task.isEmpty()) {
                        mRecyclerView.longSnack("好像还没有设置日视图小部件呢>_<")
                        getPrefer().edit {
                            putBoolean(Const.KEY_COURSE_REMIND, false)
                        }
                        item.checked = false
                        mAdapter.notifyDataSetChanged()
                    } else {
                        getPrefer().edit {
                            putBoolean(Const.KEY_COURSE_REMIND, isChecked)
                        }
                        AppWidgetUtils.updateWidget(applicationContext)
                        item.checked = isChecked
                    }
                }
            }
            R.string.setting_notify_on_going -> {
                getPrefer().edit {
                    putBoolean(Const.KEY_REMINDER_ON_GOING, isChecked)
                }
                item.checked = isChecked
                mRecyclerView.longSnack("对下一次提醒通知生效哦")
            }
//            "提醒同时将手机静音" -> {
//                val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !notificationManager.isNotificationPolicyAccessGranted) {
//                    val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
//                    startActivity(intent)
//                    item.checked = false
//                } else {
//                    getPrefer().edit {
//                        putBoolean(Const.KEY_SILENCE_REMINDER, isChecked)
//                    }
//                    AppWidgetUtils.updateWidget(applicationContext)
//                    item.checked = isChecked
//                }
//            }
        }
    }

    private fun onVerticalItemClick(item: VerticalItem) {
        when (item.title) {
            R.string.setting_unlock -> {
                try {
                    val intent = Intent()
                    intent.action = "android.intent.action.VIEW"
                    val qrCodeUrl = Uri.parse("alipayqr://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=HTTPS://QR.ALIPAY.COM/FKX09148M0LN2VUUZENO9B?_s=web-other")
                    intent.data = qrCodeUrl
                    intent.setClassName("com.eg.android.AlipayGphone", "com.alipay.mobile.quinox.LauncherActivity")
                    startActivity(intent)
                    Toasty.success(this, "非常感谢(*^▽^*)").show()
                } catch (e: Exception) {
                    Toasty.info(this, "没有检测到支付宝客户端o(╥﹏╥)o").show()
                }
            }
            R.string.setting_theme_color -> {
                ColorPickerFragment.newBuilder()
                        .setShowAlphaSlider(true)
                        .setColor(getPrefer().getInt(Const.KEY_THEME_COLOR, color(R.color.colorAccent)))
                        .show(this)
            }
        }
    }

    private fun onSeekBarItemClick(item: SeekBarItem, position: Int) {
        val dialog = MaterialAlertDialogBuilder(this)
                .setTitle(item.title)
                .setView(R.layout.dialog_edit_text)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, null)
                .create()
        dialog.show()
        val inputLayout = dialog.findViewById<TextInputLayout>(R.id.text_input_layout)
        val editText = dialog.findViewById<TextInputEditText>(R.id.edit_text)
        inputLayout?.helperText = "范围 ${item.min} ~ ${item.max}"
        inputLayout?.suffixText = item.unit
        editText?.inputType = InputType.TYPE_CLASS_NUMBER
        val valueStr = item.valueInt.toString()
        editText?.setText(valueStr)
        editText?.setSelection(valueStr.length)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val value = editText?.text
            if (value.isNullOrBlank()) {
                inputLayout?.error = "数值不能为空哦>_<"
                return@setOnClickListener
            }
            val valueInt = try {
                value.toString().toInt()
            } catch (e: Exception) {
                inputLayout?.error = "输入异常>_<"
                return@setOnClickListener
            }
            if (valueInt < item.min || valueInt > item.max) {
                inputLayout?.error = "注意范围 ${item.min} ~ ${item.max}"
                return@setOnClickListener
            }
            when (item.title) {
                R.string.setting_notify_time -> {
                    getPrefer().edit {
                        putInt(Const.KEY_REMINDER_TIME, valueInt)
                    }
                    AppWidgetUtils.updateWidget(applicationContext)
                }
            }
            item.valueInt = valueInt
            mAdapter.notifyItemChanged(position)
            dialog.dismiss()
        }
    }

    override fun onDestroy() {
        AppWidgetUtils.updateWidget(applicationContext)
        super.onDestroy()
    }
}
