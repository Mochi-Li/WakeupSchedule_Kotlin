package com.suda.yzune.wakeupschedule.schedule

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.BaseDialogFragment
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.bean.MyResponse
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.MyRetrofitUtils
import com.suda.yzune.wakeupschedule.utils.UpdateUtils
import com.suda.yzune.wakeupschedule.utils.Utils
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_export_settings.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.UnknownHostException

class ExportSettingsFragment : BaseDialogFragment() {

    override val layoutId: Int
        get() = R.layout.fragment_export_settings

    private val viewModel by activityViewModels<ScheduleViewModel>()

    val tableName by lazy(LazyThreadSafetyMode.NONE) {
        if (viewModel.tableConfig.tableName == "") {
            "我的课表"
        } else {
            viewModel.tableConfig.tableName
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false

        tv_export.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_TITLE, "$tableName.wakeup_schedule")
            }
            Toasty.info(requireActivity(), "请自行选择导出的地方\n不要修改文件的扩展名哦", Toasty.LENGTH_LONG).show()
            activity?.startActivityForResult(intent, Const.REQUEST_CODE_EXPORT)
            dismiss()
        }

        tv_export_ics.setOnLongClickListener {
            Utils.openUrl(requireActivity(), "https://www.jianshu.com/p/de3524cbe8aa")
            return@setOnLongClickListener true
        }

        tv_export_ics.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/calendar"
                putExtra(Intent.EXTRA_TITLE, "日历-$tableName")
            }
            Toasty.info(requireActivity(), "请自行选择导出的地方\n不要修改文件的扩展名哦", Toasty.LENGTH_LONG).show()
            activity?.startActivityForResult(intent, Const.REQUEST_CODE_EXPORT_ICS)
            dismiss()
        }

        tv_share.setOnClickListener {
            tv_share.text = "上传中……请稍后"
            val gson = Gson()
            launch {
                try {
                    val content = viewModel.exportData()
                    val versionCode = UpdateUtils.getVersionCode(requireActivity())
                    val response = withContext(Dispatchers.IO) {
                        MyRetrofitUtils.instance.getService()
                                .shareSchedule(versionCode, content)
                                .execute()
                    }
                    if (response.isSuccessful) {
                        val body = withContext(Dispatchers.IO) {
                            gson.fromJson<MyResponse<String>>(response.body()!!.string(), object : TypeToken<MyResponse<String>>() {}.type)
                        }
                        if (body.data.isBlank()) throw Exception("分享码为空")
                        if (body.status != "1" || body.message != "success") {
                            Toasty.info(requireActivity(), body.message, Toasty.LENGTH_LONG).show()
                            dismiss()
                            return@launch
                        }
                        if (activity is ScheduleActivity) {
                            (activity as ScheduleActivity).showShareOnlineDialog(body.data)
                        }
                        dismiss()
                    } else {
                        Toasty.error(requireActivity(), "服务器似乎在开小差呢>_<请稍后再试", Toasty.LENGTH_LONG).show()
                        dismiss()
                    }
                } catch (e: Exception) {
                    val msg = if (e is UnknownHostException) {
                        "发生异常>_<请检查网络连接\n${e.message}"
                    } else {
                        "发生异常>_<${e.message}"
                    }
                    Toasty.error(requireActivity(), msg, Toasty.LENGTH_LONG).show()
                    dismiss()
                }
            }
        }

        tv_cancel.setOnClickListener {
            dismiss()
        }
    }
}
