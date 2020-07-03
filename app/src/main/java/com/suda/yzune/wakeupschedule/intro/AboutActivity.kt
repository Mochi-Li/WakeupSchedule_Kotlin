package com.suda.yzune.wakeupschedule.intro

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.suda.yzune.wakeupschedule.BuildConfig
import com.suda.yzune.wakeupschedule.DonateActivity
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseBlurTitleActivity
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.UpdateUtils
import com.suda.yzune.wakeupschedule.utils.Utils
import com.suda.yzune.wakeupschedule.utils.getPrefer
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_about.*
import splitties.activities.start
import splitties.dimensions.dip
import splitties.resources.color
import splitties.resources.styledDimenPxSize


class AboutActivity : BaseBlurTitleActivity() {
    override val layoutId: Int
        get() = R.layout.activity_about

    private val clipboardManager by lazy(LazyThreadSafetyMode.NONE) {
        (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
    }

    override fun onSetupSubButton(tvButton: AppCompatTextView): AppCompatTextView? {
        return if (BuildConfig.CHANNEL == "google" || (BuildConfig.CHANNEL == "huawei" && !getPrefer().getBoolean(Const.KEY_SHOW_DONATE, false))) {
            null
        } else {
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

        try {
            tv_version.text = "版本号：${UpdateUtils.getVersionName(this)}"
        } catch (e: Exception) {
            e.printStackTrace()
        }

        ll_wakeup.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                    .setTitle("不辜负每一个清晨")
                    .setView(AppCompatTextView(this).apply {
                        text = "「 苏州大学WakeUp俱乐部 」是苏州大学的一个社团，由13级马惠荣学姐创办，旨在鼓励大学生早睡早起，不辜负每一个清晨，时刻做一个醒着的人。" +
                                "社团招新，欢迎加入！关注我们的微信公众号或加群了解更多。"
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        val space = styledDimenPxSize(R.attr.dialogPreferredPadding)
                        setPadding(space, dip(8), space, 0)
                        lineHeight += dip(2)
                    })
                    .setPositiveButton("欢迎苏大的小伙伴加群看看") { _, _ ->
                        val clipData = ClipData.newPlainText("", "658600211")
                        clipboardManager.setPrimaryClip(clipData)
                        Toasty.success(this, "群号已复制到剪贴板中", Toasty.LENGTH_LONG).show()
                    }
                    .setNeutralButton("复制微信公众号") { _, _ ->
                        val clipData = ClipData.newPlainText("", "szdxcx")
                        clipboardManager.setPrimaryClip(clipData)
                        Toasty.success(this, "微信公众号已复制到剪贴板中", Toasty.LENGTH_LONG).show()
                    }
                    .show()
        }

        ll_code.setOnClickListener {
            Utils.openUrl(this, "https://github.com/YZune/WakeupSchedule_Kotlin")
            Toasty.success(this, "开发不易，给个Star呗XD", Toasty.LENGTH_LONG).show()
        }

        ll_question.setOnClickListener {
            Utils.openUrl(this, "https://support.qq.com/embed/97617/faqs-more")
        }

        ll_group.setOnClickListener {
            val intent = Intent()
            intent.data = Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3DxC88hljh6en0zP4rtqt5s86JzBXDtt13")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                startActivity(intent)
            } catch (e: Exception) {
                val clipData = ClipData.newPlainText("", "921826443")
                (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clipData)
                Toasty.error(this, "调起QQ失败>_<群号已复制到剪贴板中", Toasty.LENGTH_LONG).show()
            }
        }

        ll_weibo.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse("sinaweibo://userinfo?uid=6970231444")
                startActivity(intent)
            } catch (e: Exception) {
                Toasty.info(this, "没有检测到微博客户端o(╥﹏╥)o", Toasty.LENGTH_LONG).show()
            }
        }

        ll_mark.setOnClickListener {
            try {
                val uri = Uri.parse("market://details?id=com.suda.yzune.wakeupschedule")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e: Exception) {
                Toasty.info(this, "没有检测到应用商店o(╥﹏╥)o", Toasty.LENGTH_LONG).show()
            }
        }

    }
}
