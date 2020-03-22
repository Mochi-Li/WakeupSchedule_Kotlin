package com.suda.yzune.wakeupschedule

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.BaseDialogFragment
import com.suda.yzune.wakeupschedule.utils.UpdateUtils
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_update.*

class UpdateFragment : BaseDialogFragment() {

    override val layoutId: Int
        get() = R.layout.fragment_update

    private var versionName = ""
    private var versionInfo = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            versionName = it.getString("versionName")!!
            versionInfo = it.getString("versionInfo")!!
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_old_version.text = "当前版本：" + UpdateUtils.getVersionName(context!!.applicationContext)
        tv_new_version.text = "最新版本：$versionName"
        tv_info.text = versionInfo
        tv_visit.setOnClickListener {
            if (BuildConfig.CHANNEL == "google") {
                try {
                    val uri = Uri.parse("market://details?id=com.suda.yzune.wakeupschedule.pro")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    activity!!.startActivity(intent)
                } catch (e: Exception) {
                    Toasty.info(context!!, "没有检测到应用商店o(╥﹏╥)o").show()
                }
            } else {
                val intent = Intent()
                intent.action = "android.intent.action.VIEW"
                val contentUrl = Uri.parse("https://www.coolapk.com/apk/com.suda.yzune.wakeupschedule")
                intent.data = contentUrl
                context!!.startActivity(intent)
            }
            dismiss()
        }
        ib_close.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(name: String, info: String) =
                UpdateFragment().apply {
                    arguments = Bundle().apply {
                        putString("versionName", name)
                        putString("versionInfo", info)
                    }
                }
    }
}
