package com.suda.yzune.wakeupschedule.schedule_import

import android.app.Activity.RESULT_OK
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseFragment
import com.suda.yzune.wakeupschedule.schedule_import.exception.CheckCodeErrorException
import com.suda.yzune.wakeupschedule.schedule_import.exception.PasswordErrorException
import com.suda.yzune.wakeupschedule.schedule_import.exception.UserNameErrorException
import com.suda.yzune.wakeupschedule.schedule_import.login_school.hust.MobileHub
import com.suda.yzune.wakeupschedule.schedule_import.login_school.jlu.UIMS
import com.suda.yzune.wakeupschedule.schedule_import.login_school.suda.SudaXK
import com.suda.yzune.wakeupschedule.utils.Utils
import com.suda.yzune.wakeupschedule.utils.ViewUtils
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_login_web.*
import kotlinx.coroutines.delay
import splitties.resources.styledColor
import java.io.IOException
import java.util.*

class LoginWebFragment : BaseFragment() {

    private var year = ""
    private var term = ""

    private val viewModel by activityViewModels<ImportViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login_web, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_title.text = viewModel.school
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            input_id.setAutofillHints(View.AUTOFILL_HINT_USERNAME)
            input_pwd.setAutofillHints(View.AUTOFILL_HINT_PASSWORD)
        }
        if (viewModel.school != "苏州大学") {
            input_code.visibility = View.INVISIBLE
            rl_code.visibility = View.INVISIBLE
            tv_tip.visibility = View.GONE
        } else {
            viewModel.sudaXK = SudaXK()
            refreshCode()
            tv_tip.setOnClickListener {
                Utils.openUrl(requireContext(), "https://yzune.github.io/2018/08/13/%E4%BD%BF%E7%94%A8FortiClient%E8%BF%9E%E6%8E%A5%E6%A0%A1%E5%9B%AD%E7%BD%91/")
            }
        }
        if (viewModel.school == "清华大学") {
            input_id.hint = "用户名"
            tv_thanks.text = "感谢 @RikaSugisawa\n能导入贵校课程离不开他无私贡献代码"
            et_id.inputType = InputType.TYPE_CLASS_TEXT
        }
        if (viewModel.school == "吉林大学") {
            viewModel.jlu = UIMS()
            MaterialAlertDialogBuilder(requireContext())
                    .setTitle("提示")
                    .setMessage("是否在使用校园网？如果在校内建议连接校园网后再导入课表。如果没有连接校园网，需要先在这里登录 VPNS，再登录教务导入课表。")
                    .setPositiveButton("我已连接校园网") { _, _ ->
                        input_code.visibility = View.VISIBLE
                        rl_code.visibility = View.VISIBLE
                        refreshCode()
                        viewModel.isReady = true
                    }
                    .setNegativeButton("没有连接校园网，登录 VPNS") { _, _ ->
                        et_id.inputType = InputType.TYPE_CLASS_TEXT
                        input_id.hint = "吉大邮箱用户名"
                        tv_tip.visibility = View.VISIBLE
                        tv_tip.text = "账号为吉大学生邮箱的用户名\n不包含@mails.jlu.edu.cn\n密码为邮箱密码"
                    }
                    .setCancelable(false)
                    .show()
            tv_thanks.text = "感谢 @颩欥殘膤、@IceSpite\n能导入贵校课程离不开他们无私贡献代码"
        }
        if (viewModel.school == "华中科技大学") {
            et_id.inputType = InputType.TYPE_CLASS_TEXT
            tv_thanks.text = "感谢 @Lyt99\n能导入贵校课程离不开他无私贡献代码"
        }
        if (viewModel.school == "西北工业大学") {
            et_id.inputType = InputType.TYPE_CLASS_TEXT
            tv_thanks.text = "感谢 @ludoux\n能导入贵校课程离不开他无私贡献代码"
        }
        initEvent()
    }

    private fun TextInputLayout.showError(str: String, dur: Long = 3000) {
        launch {
            this@showError.error = str
            delay(dur)
            this@showError.error = null
        }
    }

    private fun initEvent() {

        fab_login.apply {
            setImageResource(R.drawable.ic_outline_done_24)
            imageTintList = ViewUtils.createColorStateList(styledColor(R.attr.colorSurface))
        }

        iv_code.setOnClickListener {
            refreshCode()
        }

        iv_error.setOnClickListener {
            refreshCode()
        }

//        sheet.setOnClickListener {
//            fab_login.isExpanded = false
//        }

        btn_to_schedule.setOnClickListener {
            when (viewModel.school) {
                "苏州大学" -> getSudaSchedule()
                "西北工业大学" -> getNWPUSchedule()
            }
        }

        btn_cancel.setOnClickListener {
            refreshCode()
            fab_login.isExpanded = false
        }

        fab_login.setOnClickListener {
            when {
                et_id.text!!.isEmpty() -> input_id.showError("学号不能为空")
                et_pwd.text!!.isEmpty() -> input_pwd.showError("密码不能为空")
                et_code.text!!.isEmpty() && (viewModel.school == "苏州大学" ||
                        (viewModel.isReady && viewModel.school == "吉林大学"))
                -> input_code.showError("验证码不能为空")
                else -> launch { login() }
            }
        }
    }

    private suspend fun login() {
        var exception: Exception? = null
        var result = 0
        when (viewModel.school) {
            "苏州大学" -> {
                pb_loading.visibility = View.VISIBLE
                ll_dialog.visibility = View.INVISIBLE
                fab_login.isExpanded = true
                viewModel.sudaXK?.id = et_id.text.toString()
                viewModel.sudaXK?.password = et_pwd.text.toString()
                viewModel.sudaXK?.code = et_code.text.toString()
                try {
                    viewModel.sudaXK?.login()
                    pb_loading.visibility = View.GONE
                    cardC2Dialog(viewModel.sudaXK?.years!!)
                } catch (e: IOException) {
                    Toasty.error(requireActivity(), "请检查是否连接校园网", Toast.LENGTH_LONG).show()
                    delay(500)
                    fab_login.isExpanded = false
                } catch (e: Exception) {
                    when (e) {
                        is UserNameErrorException -> {
                            et_id.requestFocus()
                            input_id.showError(e.message ?: "", 5000)
                            refreshCode()
                        }
                        is PasswordErrorException -> {
                            et_pwd.requestFocus()
                            input_pwd.showError(e.message ?: "", 5000)
                            refreshCode()
                        }
                        is CheckCodeErrorException -> {
                            input_code.showError(e.message ?: "", 5000)
                            refreshCode()
                        }
                        else -> Toasty.error(requireActivity(), e.message
                                ?: "再试一次看看哦", Toast.LENGTH_LONG).show()
                    }
                    delay(500)
                    fab_login.isExpanded = false
                }
            }
            "清华大学" -> {
                try {
                    result = viewModel.loginTsinghua(et_id.text.toString(),
                            et_pwd.text.toString())
                } catch (e: Exception) {
                    exception = e
                }
            }
            "吉林大学" -> {
                if (!viewModel.isReady) {
                    try {
                        viewModel.jlu?.setNeedVpns()
                        viewModel.jlu?.getVPNSCookie()
                        viewModel.jlu?.connectToVPNS(et_id.text.toString(), et_pwd.text.toString())
                        et_id.setText("")
                        et_pwd.setText("")
                        et_id.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
                        input_id.hint = "学号"
                        input_code.visibility = View.VISIBLE
                        rl_code.visibility = View.VISIBLE
                        refreshCode()
                        tv_tip.text = "登录 VPNS 成功\n现在请输入学号和教务系统的密码"
                    } catch (e: Exception) {
                        exception = e
                    }
                } else {
                    try {
                        viewModel.jlu?.login(et_id.text.toString(), et_pwd.text.toString(),
                                et_code.text.toString())
                        viewModel.jlu?.getCurrentUserInfo()
                        viewModel.jlu?.getCourseSchedule()
                        result = viewModel.convertJLU(viewModel.jlu!!.courseJSON)
                    } catch (e: Exception) {
                        exception = e
                    }
                }
            }
            "华中科技大学" -> {
                val hub = MobileHub(et_id.text.toString(), et_pwd.text.toString())
                try {
                    hub.login()
                    hub.getCourseSchedule()
                    result = viewModel.convertHUST(hub.courseHTML)
                } catch (e: Exception) {
                    exception = e
                }
            }
            "西北工业大学" -> {
                Toasty.info(requireActivity(), "年份为学年的起始年，学期[秋、春、夏]分别对应[1、2、3]\n例如[2019-2020春] 选择[2019 2]", Toast.LENGTH_LONG).show()
                pb_loading.visibility = View.INVISIBLE
                fab_login.isExpanded = true
                val year = Calendar.getInstance().get(Calendar.YEAR)
                val list = mutableListOf<String>()
                for (index in year - 7..year) {
                    list.add(index.toString())
                }
                cardC2Dialog(list, true)
            }
        }
        if (viewModel.school == "苏州大学" || viewModel.school == "西北工业大学") return
        if (viewModel.school == "吉林大学" && !viewModel.isReady && exception == null) {
            viewModel.isReady = true
            return
        }
        when (exception) {
            null -> {
                showSuccess(result)
            }
            is UserNameErrorException -> {
                et_id.requestFocus()
                input_id.showError(exception.message ?: "", 5000)
            }
            is PasswordErrorException -> {
                et_pwd.requestFocus()
                input_pwd.showError(exception.message ?: "", 5000)
            }
            else -> Toasty.error(requireActivity(), exception.message
                    ?: "再试一次看看哦", Toast.LENGTH_LONG).show()
        }
    }

    private fun getNWPUSchedule() {
        launch {
            try {
                if (term.isEmpty()) {
                    term = "1"
                }
                val result = viewModel.loginNWPU(et_id.text.toString(), et_pwd.text.toString(), year, term)
                showSuccess(result)
            } catch (e: Exception) {
                fab_login.isExpanded = false
                when (e) {
                    is UserNameErrorException -> {
                        et_id.requestFocus()
                        input_id.showError(e.message ?: "", 5000)
                        refreshCode()
                    }
                    is PasswordErrorException -> {
                        et_pwd.requestFocus()
                        input_pwd.showError(e.message ?: "", 5000)
                        refreshCode()
                    }
                    is CheckCodeErrorException -> {
                        input_code.showError(e.message ?: "", 5000)
                        refreshCode()
                    }
                    else -> Toasty.error(requireActivity(), e.message
                            ?: "再试一次看看哦", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getSudaSchedule() {
        viewModel.importType = Common.TYPE_ZF
        launch {
            try {
                val result = viewModel.importSchedule(viewModel.sudaXK?.toSchedule(year, term)!!)
                showSuccess(result)
            } catch (e: Exception) {
                Toasty.error(requireActivity(),
                        "导入失败>_<\n${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun refreshCode() {
        launch {
            et_code.setText("")
            progress_bar.visibility = View.VISIBLE
            iv_code.visibility = View.INVISIBLE
            iv_error.visibility = View.INVISIBLE
            try {
                val bitmap = when (viewModel.school) {
                    "苏州大学" -> viewModel.sudaXK?.getCheckCode()
                    "吉林大学" -> viewModel.jlu?.getCheckCode(et_code.text.toString())
                    else -> null
                }
                progress_bar.visibility = View.GONE
                iv_code.visibility = View.VISIBLE
                iv_error.visibility = View.INVISIBLE
                iv_code.setImageBitmap(bitmap)
            } catch (e: Exception) {
                progress_bar.visibility = View.GONE
                iv_code.visibility = View.INVISIBLE
                iv_error.visibility = View.VISIBLE
                Toasty.error(requireContext(), "请检查是否连接校园网", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showSuccess(result: Int) {
        Toasty.success(requireActivity(),
                "成功导入 $result 门课程(ﾟ▽ﾟ)/\n请在右侧栏切换后查看", Toast.LENGTH_LONG).show()
        requireActivity().setResult(RESULT_OK)
        requireActivity().finish()
    }

    private fun cardC2Dialog(years: List<String>, selectLastYear: Boolean = false) {
        ll_dialog.visibility = View.VISIBLE
        val terms = arrayOf("1", "2", "3")
        wp_term.displayedValues = terms
        wp_term.value = 0
        wp_term.minValue = 0
        wp_term.maxValue = terms.size - 1

        wp_years.displayedValues = years.toTypedArray()
        wp_years.minValue = 0
        wp_years.maxValue = years.size - 1
        if (!selectLastYear) {
            wp_years.value = 0
        } else {
            wp_years.value = wp_years.maxValue
        }

        wp_years.setOnValueChangedListener { _, _, newVal ->
            year = years[newVal]
            Log.d("选中", "选中学年$year")
        }
        wp_term.setOnValueChangedListener { _, _, newVal ->
            term = terms[newVal]
            Log.d("选中", "选中学期$term")
        }
    }

    override fun onDestroyView() {
        btg_ports.clearOnButtonCheckedListeners()
        super.onDestroyView()
    }

}
