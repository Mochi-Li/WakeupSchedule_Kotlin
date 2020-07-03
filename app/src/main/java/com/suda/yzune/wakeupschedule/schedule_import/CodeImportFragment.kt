package com.suda.yzune.wakeupschedule.schedule_import

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseFragment
import com.suda.yzune.wakeupschedule.bean.MyResponse
import com.suda.yzune.wakeupschedule.utils.MyRetrofitUtils
import com.suda.yzune.wakeupschedule.utils.UpdateUtils
import com.suda.yzune.wakeupschedule.utils.ViewUtils
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_code_import.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.UnknownHostException

class CodeImportFragment : BaseFragment() {

    private val gson by lazy(LazyThreadSafetyMode.NONE) {
        Gson()
    }

    private val viewModel by activityViewModels<ImportViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_code_import, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.resizeStatusBar(requireContext().applicationContext, v_status)

        tv_import.setOnClickListener {
            val shareText = edit_text.text.toString()
            if (shareText.isBlank()) {
                inputLayoutShowError("请在此粘贴分享码>_<")
                return@setOnClickListener
            }
            if (!shareText.startsWith("这是来自「WakeUp课程表」的课表分享") || !shareText.contains("分享口令为「")) {
                inputLayoutShowError("请将分享口令复制完整哦>_<")
                return@setOnClickListener
            }
            val code = shareText.substringAfterLast("分享口令为「").substringBefore("」")
            launch {
                tv_import.text = "导入中…请稍后"
                val versionCode = UpdateUtils.getVersionCode(requireActivity())
                try {
                    val response = withContext(Dispatchers.IO) {
                        MyRetrofitUtils.instance.getService()
                                .getShareSchedule(versionCode, code)
                                .execute()
                    }
                    if (response.isSuccessful) {
                        val body = withContext(Dispatchers.IO) {
                            gson.fromJson<MyResponse<String>>(response.body()!!.string(), object : TypeToken<MyResponse<String>>() {}.type)
                        }
                        if (body.data.isBlank()) {
                            Toasty.error(requireActivity(), "数据读取失败>_<可能是分享口令已经过期了哦", Toasty.LENGTH_LONG).show()
                            tv_import.text = "点击导入"
                            return@launch
                        }
                        if (body.status != "1" || body.message != "success") {
                            Toasty.info(requireActivity(), body.message, Toasty.LENGTH_LONG).show()
                            tv_import.text = "点击导入"
                            return@launch
                        }
                        viewModel.importFromExport(body.data.lines())
                        if (activity is LoginWebActivity) {
                            (activity as LoginWebActivity).apply {
                                showResultDialog("导入成功(ﾟ▽ﾟ)/", "请记得要打开多课表面板来查看哦~", false) {
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }
                            }
                        } else {
                            requireActivity().setResult(Activity.RESULT_OK)
                            requireActivity().finish()
                        }
                    } else {
                        tv_import.text = "点击导入"
                        Toasty.error(requireActivity(), "服务器似乎在开小差呢>_<请稍后再试", Toasty.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    tv_import.text = "点击导入"
                    val msg = if (e is UnknownHostException) {
                        "发生异常>_<请检查网络连接\n${e.message}"
                    } else {
                        "发生异常>_<${e.message}"
                    }
                    Toasty.error(requireActivity(), msg, Toasty.LENGTH_LONG).show()
                }
            }
        }

        ib_back.setOnClickListener {
            requireActivity().finish()
        }
    }

    private fun inputLayoutShowError(msg: String) {
        launch {
            text_input_layout.error = msg
            delay(3000)
            text_input_layout.error = null
        }
    }
}