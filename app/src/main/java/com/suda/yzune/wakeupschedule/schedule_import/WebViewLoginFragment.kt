package com.suda.yzune.wakeupschedule.schedule_import

import android.app.Activity.RESULT_OK
import android.graphics.Color
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.suda.yzune.wakeupschedule.BuildConfig
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseFragment
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_JZ
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_JZ_1
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_QZ
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_QZ_2017
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_QZ_BR
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_QZ_CRAZY
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_QZ_WITH_NODE
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_ZF
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_ZF_1
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.Utils
import com.suda.yzune.wakeupschedule.utils.ViewUtils
import com.suda.yzune.wakeupschedule.utils.getPrefer
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_web_view_login.*
import splitties.snackbar.longSnack

class WebViewLoginFragment : BaseFragment() {

    private lateinit var url: String
    private val viewModel by activityViewModels<ImportViewModel>()
    private var isRefer = false
    private val hostRegex = Regex("""(http|https)://.*?/""")
    private fun foregroundColorSpan() = ForegroundColorSpan(Color.RED)
    private var tips = SpannableStringBuilder()
            .append("1. 在上方输入教务网址，部分学校需要连接校园网。\n")
            .append("2. 登录后点击到个人课表的页面，注意选择自己需要导入的学期，一般首页的课表都是不可导入的！另外不会导入调课、停课的信息，请导入后自行修改！\n")
            .append("3. 点击右下角的按钮完成导入。\n")
            .append("4. 如果遇到网页错位等问题，可以尝试取消底栏的「电脑模式」或者调节字体缩放。")
            .apply {
                val text1 = "个人课表"
                val index1 = this.indexOf(text1)
                val text2 = "一般首页的课表都是不可导入的！"
                val index2 = this.indexOf(text2)
                val text3 = "不会导入调课、停课的信息"
                val index3 = this.indexOf(text3)
                setSpan(foregroundColorSpan(), index1, index1 + text1.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                setSpan(foregroundColorSpan(), index2, index2 + text2.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                setSpan(foregroundColorSpan(), index3, index3 + text3.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            }
    private var zoom = 100
    private var countClick = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            url = it.getString("url")!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_web_view_login, container, false)
    }

    @JavascriptInterface
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.resizeStatusBar(requireContext(), view.findViewById(R.id.v_status))
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            v.updatePadding(bottom = insets.systemWindowInsets.bottom)
            insets
        }
        if (url != "") {
            et_url.setText(url)
            et_url.setSelection(url.length)
            startVisit()
        } else {
            val url = requireContext().getPrefer().getString(Const.KEY_SCHOOL_URL, "")
            if (url != "") {
                et_url.setText(url)
                et_url.setSelection(url!!.length)
                startVisit()
            } else {
                wv_course.visibility = View.VISIBLE
                ll_error.visibility = View.GONE
                wv_course.loadUrl("file:///android_asset/empty.html")
            }
        }

        if (viewModel.importType == "apply") {
            tips = SpannableStringBuilder()
                    .append("1. 在上方输入教务网址，部分学校需要连接校园网。\n")
                    .append("2. 登录后点击到个人课表或者相关的页面。\n")
                    .append("3. 点击右下角的按钮抓取源码，并上传到服务器。\n")
                    .append("4. 如果遇到网页错位等问题，可以尝试取消底栏的「电脑模式」或者调节字体缩放。")
        }

        if (viewModel.school == "强智教务" || viewModel.importType in arrayOf(TYPE_QZ, TYPE_QZ_BR, TYPE_QZ_CRAZY, TYPE_QZ_WITH_NODE, TYPE_QZ_2017)) {
            if (viewModel.importType == TYPE_QZ) {
                cg_qz.visibility = View.VISIBLE
                chip_qz1.isChecked = true
            } else {
                cg_qz.visibility = View.GONE
            }
            tips = SpannableStringBuilder()
                    .append("1. 在上方输入教务网址，部分学校需要连接校园网。\n")
                    .append("2. 登录后点击到「学期理论课表」的页面，注意不是「首页的课表」！注意选择自己需要导入的学期。\n")
                    .append("3. 点击右下角的按钮完成导入。\n")
                    .append("4. 如果遇到网页错位等问题，可以尝试取消底栏的「电脑模式」或者调节字体缩放。")
                    .apply {
                        val text1 = "「学期理论课表」"
                        val index1 = this.indexOf(text1)
                        val text2 = "不是「首页的课表」"
                        val index2 = this.indexOf(text2)
                        setSpan(foregroundColorSpan(), index1, index1 + text1.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        setSpan(foregroundColorSpan(), index2, index2 + text2.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                    }
        } else {
            cg_qz.visibility = View.GONE
        }

        if (viewModel.school == "正方教务" || viewModel.importType in arrayOf(TYPE_ZF, TYPE_ZF_1)) {
            cg_zf.visibility = View.VISIBLE
            if (viewModel.importType == TYPE_ZF) {
                chip_zf1.isChecked = true
            } else {
                chip_zf2.isChecked = true
            }
        } else {
            cg_zf.visibility = View.GONE
        }

        if (viewModel.school == "金智教务") {
            cg_jz.visibility = View.VISIBLE
            chip_jz1.isChecked = true
        } else {
            cg_jz.visibility = View.GONE
        }

        if (viewModel.importType in arrayOf(TYPE_ZF, TYPE_ZF_1, TYPE_JZ)) {
            tips = SpannableStringBuilder()
                    .append("1. 在上方输入教务网址，部分学校需要连接校园网。\n")
                    .append("2. 登录后点击到「个人课表」的页面，注意不是「班级课表」！注意选择自己需要导入的学期。正方教务目前仅支持个人课表的导入。另外不会导入调课、停课的信息，请导入后自行修改！\n")
                    .append("3. 点击右下角的按钮完成导入。\n")
                    .append("4. 如果遇到网页错位等问题，可以尝试取消底栏的「电脑模式」或者调节字体缩放。")
                    .apply {
                        val text1 = "「个人课表」"
                        val index1 = this.indexOf(text1)
                        val text2 = "不是「班级课表」"
                        val index2 = this.indexOf(text2)
                        val text3 = "不会导入调课、停课的信息"
                        val index3 = this.indexOf(text3)
                        setSpan(foregroundColorSpan(), index1, index1 + text1.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        setSpan(foregroundColorSpan(), index2, index2 + text2.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        setSpan(foregroundColorSpan(), index3, index3 + text3.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                    }
        }

        if (viewModel.importType == Common.TYPE_JNU) {
            tips = SpannableStringBuilder()
                    .append("1. 在上方输入教务网址，部分学校需要连接校园网。\n")
                    .append("2. 登录教务后操作：左边导航栏->选课管理系统->课程表及考试表。\n")
                    .append("3. 点击右下角的按钮完成导入，要进行多次操作，请耐心等待网页加载。\n")
                    .append("4. 如果遇到网页错位等问题，可以尝试取消底栏的「电脑模式」或者调节字体缩放。")
                    .apply {
                        val text1 = "左边导航栏->选课管理系统->课程表及考试表"
                        val index1 = this.indexOf(text1)
                        val text2 = "多次操作"
                        val index2 = this.indexOf(text2)
                        setSpan(foregroundColorSpan(), index1, index1 + text1.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        setSpan(foregroundColorSpan(), index2, index2 + text2.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                    }
        }

        if (viewModel.importType == Common.TYPE_UMOOC) {
            tips = SpannableStringBuilder()
                    .append("1. 在上方输入教务网址，部分学校需要连接校园网。\n")
                    .append("2. 登录教务后，要选择小节课表，也就是「第1节」「第2节」分开显示那种。不支持导入大节课表。\n")
                    .append("3. 点击右下角的按钮完成导入。部分学校有中午的课时，导入后会当成一节来处理。\n")
                    .append("4. 如果遇到网页错位等问题，可以尝试取消底栏的「电脑模式」或者调节字体缩放。")
                    .apply {
                        val text1 = "小节课表"
                        val index1 = this.indexOf(text1)
                        val text2 = "不支持"
                        val index2 = this.indexOf(text2)
                        val text3 = "当成一节"
                        val index3 = this.indexOf(text3)
                        setSpan(foregroundColorSpan(), index1, index1 + text1.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        setSpan(foregroundColorSpan(), index2, index2 + text2.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        setSpan(foregroundColorSpan(), index3, index3 + text3.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                    }
        }

        if (viewModel.importType == Common.TYPE_WHU) {
            tips = SpannableStringBuilder()
                    .append("1. 在上方输入教务网址。\n")
                    .append("2. 登录教务后，最终能够导入到第几周的课程似乎跟页面周数选择有关，请多次尝试，请导入后仔细检查。\n")
                    .append("3. 点击右下角的按钮完成导入。\n")
                    .append("4. 如果遇到网页错位等问题，可以尝试取消底栏的「电脑模式」或者调节字体缩放。")
                    .apply {
                        val text1 = "似乎跟页面周数选择有关"
                        val index1 = this.indexOf(text1)
                        val text2 = "导入后仔细检查"
                        val index2 = this.indexOf(text2)
                        setSpan(foregroundColorSpan(), index1, index1 + text1.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        setSpan(foregroundColorSpan(), index2, index2 + text2.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                    }
        }

        if (viewModel.importType == Common.TYPE_URP_NEW) {
            cg_new_urp.visibility = View.VISIBLE
            chip_new_urp.isChecked = true
        } else {
            cg_new_urp.visibility = View.GONE
        }

        MaterialAlertDialogBuilder(requireContext())
                .setTitle("注意事项")
                .setMessage(tips)
                .setPositiveButton("我知道啦", null)
                .setNeutralButton("如何正确选择教务？") { _, _ ->
                    Utils.openUrl(requireActivity(), "https://support.qq.com/embed/97617/faqs/59901")
                }
                .setCancelable(false)
                .show()

        wv_course.settings.javaScriptEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            wv_course.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        wv_course.addJavascriptInterface(InJavaScriptLocalObj(), "local_obj")
        wv_course.webViewClient = object : WebViewClient() {

            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                if (BuildConfig.CHANNEL != "google") {
                    handler.proceed() //接受所有网站的证书
                    return
                }
                MaterialAlertDialogBuilder(requireContext())
                        .setMessage("SSL证书验证失败")
                        .setPositiveButton("继续浏览") { _, _ ->
                            handler.proceed()
                        }
                        .setNegativeButton("取消") { _, _ ->
                            handler.cancel()
                        }
                        .setCancelable(false)
                        .show()
            }

        }
        wv_course.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress == 100) {
                    pb_load.progress = newProgress
                    pb_load.visibility = View.GONE
                    // Toasty.info(activity!!, wv_course.url, Toast.LENGTH_LONG).show()
                } else {
                    pb_load.progress = newProgress * 5
                    pb_load.visibility = View.VISIBLE
                }
            }
        }
        // 设置自适应屏幕，两者合用
        wv_course.settings.useWideViewPort = true //将图片调整到适合WebView的大小
        wv_course.settings.loadWithOverviewMode = true // 缩放至屏幕的大小
        // 缩放操作
        wv_course.settings.setSupportZoom(true) //支持缩放，默认为true。是下面那个的前提。
        wv_course.settings.builtInZoomControls = true //设置内置的缩放控件。若为false，则该WebView不可缩放
        wv_course.settings.displayZoomControls = false //隐藏原生的缩放控件wvCourse.settings
        wv_course.settings.javaScriptCanOpenWindowsAutomatically = true
        wv_course.settings.domStorageEnabled = true
        wv_course.settings.userAgentString = wv_course.settings.userAgentString.replace("Mobile", "eliboM").replace("Android", "diordnA")
        wv_course.settings.textZoom = 100
        // We accept third party cookies
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(wv_course, true)
        }
        initEvent()
    }

    private fun initEvent() {

        chip_mode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                wv_course.settings.userAgentString = wv_course.settings.userAgentString.replace("Mobile", "eliboM").replace("Android", "diordnA")
            } else {
                wv_course.settings.userAgentString = wv_course.settings.userAgentString.replace("eliboM", "Mobile").replace("diordnA", "Android")
            }
            wv_course.reload()
        }

        chip_zoom.setOnClickListener {
            val dialog = MaterialAlertDialogBuilder(requireContext())
                    .setTitle("设置缩放")
                    .setView(R.layout.dialog_edit_text)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.sure, null)
                    .create()
            dialog.show()
            val inputLayout = dialog.findViewById<TextInputLayout>(R.id.text_input_layout)
            val editText = dialog.findViewById<TextInputEditText>(R.id.edit_text)
            inputLayout?.helperText = "范围 10 ~ 200"
            inputLayout?.suffixText = "%"
            editText?.inputType = InputType.TYPE_CLASS_NUMBER
            val valueStr = zoom.toString()
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
                if (valueInt < 10 || valueInt > 200) {
                    inputLayout?.error = "注意范围 10 ~ 200"
                    return@setOnClickListener
                }
                zoom = valueInt
                wv_course.settings.textZoom = zoom
                chip_zoom.text = "文字缩放 $zoom%"
                wv_course.reload()
                dialog.dismiss()
            }
        }

        var qzChipId = R.id.chip_qz1
        cg_qz.setOnCheckedChangeListener { chipGroup, id ->
            when (id) {
                R.id.chip_qz1 -> {
                    qzChipId = id
                    viewModel.qzType = 0
                }
                R.id.chip_qz2 -> {
                    qzChipId = id
                    viewModel.qzType = 1
                }
                R.id.chip_qz3 -> {
                    qzChipId = id
                    viewModel.qzType = 2
                }
                R.id.chip_qz4 -> {
                    qzChipId = id
                    viewModel.qzType = 3
                }
                R.id.chip_qz5 -> {
                    qzChipId = id
                    viewModel.qzType = 4
                }
                else -> {
                    chipGroup.findViewById<Chip>(qzChipId).isChecked = true
                }
            }
        }

        var zfChipId = R.id.chip_zf1
        cg_zf.setOnCheckedChangeListener { chipGroup, id ->
            when (id) {
                R.id.chip_zf1 -> {
                    zfChipId = id
                    viewModel.zfType = 0
                }
                R.id.chip_zf2 -> {
                    zfChipId = id
                    viewModel.zfType = 1
                }
                else -> {
                    chipGroup.findViewById<Chip>(zfChipId).isChecked = true
                }
            }
        }

        var jzChipId = R.id.chip_jz1
        cg_jz.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chip_jz1 -> {
                    jzChipId = checkedId
                    viewModel.importType = TYPE_JZ
                }
                R.id.chip_jz2 -> {
                    jzChipId = checkedId
                    viewModel.importType = TYPE_JZ_1
                }
                else -> {
                    group.findViewById<Chip>(jzChipId).isChecked = true
                }
            }
        }

        var newUrpChipId = R.id.chip_new_urp
        cg_new_urp.setOnCheckedChangeListener { chipGroup, id ->
            if (cg_new_urp.visibility != View.VISIBLE) return@setOnCheckedChangeListener
            when (id) {
                R.id.chip_new_urp -> {
                    newUrpChipId = id
                    viewModel.importType = Common.TYPE_URP_NEW
                    isRefer = false
                }
                R.id.chip_new_urp_ajax -> {
                    newUrpChipId = id
                    viewModel.importType = Common.TYPE_URP_NEW_AJAX
                    isRefer = false
                }
                else -> {
                    chipGroup.findViewById<Chip>(newUrpChipId).isChecked = true
                }
            }
        }

        tv_go.setOnClickListener {
            startVisit()
        }

        et_url.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                startVisit()
            }
            return@setOnEditorActionListener false
        }

        val js = "javascript:var ifrs=document.getElementsByTagName(\"iframe\");" +
                "var iframeContent=\"\";" +
                "for(var i=0;i<ifrs.length;i++){" +
                "iframeContent=iframeContent+ifrs[i].contentDocument.body.parentElement.outerHTML;" +
                "}\n" +
                "var frs=document.getElementsByTagName(\"frame\");" +
                "var frameContent=\"\";" +
                "for(var i=0;i<frs.length;i++){" +
                "frameContent=frameContent+frs[i].contentDocument.body.parentElement.outerHTML;" +
                "}\n" +
                "window.local_obj.showSource(document.getElementsByTagName('html')[0].innerHTML + iframeContent + frameContent);"

        fab_import.setOnClickListener {
            if (viewModel.importType == Common.TYPE_HNUST) {
                if (!isRefer) {
                    val referUrl = when (viewModel.school) {
                        "湖南科技大学" -> "http://kdjw.hnust.cn:8080/kdjw/tkglAction.do?method=goListKbByXs&istsxx=no"
                        "湖南科技大学潇湘学院" -> "http://xxjw.hnust.cn:8080/xxjw/tkglAction.do?method=goListKbByXs&istsxx=no"
                        else -> getHostUrl() + "tkglAction.do?method=goListKbByXs&istsxx=no"
                    }
                    wv_course.loadUrl(referUrl)
                    it.longSnack("请在看到网页加载完成后，再点一次右下角按钮")
                    isRefer = true
                } else {
                    wv_course.loadUrl(js)
                }
            } else if (viewModel.importType == Common.TYPE_CF) {
                if (!isRefer) {
                    val referUrl = getHostUrl() + "xsgrkbcx!getXsgrbkList.action"
                    wv_course.loadUrl(referUrl)
                    it.longSnack("请重新选择一下学期再点按钮导入，要记得选择全部周，记得点查询按钮")
                    isRefer = true
                } else {
                    wv_course.loadUrl(js)
                }
            } else if (viewModel.importType == Common.TYPE_URP || viewModel.isUrp) {
                if (!isRefer) {
                    val referUrl = getHostUrl() + "xkAction.do?actionType=6"
                    wv_course.loadUrl(referUrl)
                    it.longSnack("请在看到网页加载完成后，再点一次右下角按钮")
                    isRefer = true
                } else {
                    wv_course.loadUrl(js)
                }
            } else if (viewModel.importType == Common.TYPE_URP_NEW) {
                if (!isRefer) {
                    var referUrl = getHostUrl() + "student/courseSelect/thisSemesterCurriculum/callback"
                    if (viewModel.school == "烟台大学") {
                        referUrl = getHostUrl() + "student/courseSelect/thisSemesterCurriculum/ajaxStudentSchedule/curr/callback"
                    }
                    wv_course.loadUrl(referUrl)
                    it.longSnack("请在看到网页加载完成后，再点一次右下角按钮")
                    isRefer = true
                } else {
                    wv_course.loadUrl("javascript:window.local_obj.showSource(document.getElementsByTagName('html')[0].innerText);")
                }
            } else if (viewModel.importType == Common.TYPE_URP_NEW_AJAX) {
                if (!isRefer) {
                    val referUrl = getHostUrl() + "student/courseSelect/thisSemesterCurriculum/ajaxStudentSchedule/callback"
                    wv_course.loadUrl(referUrl)
                    it.longSnack("请在看到网页加载完成后，再点一次右下角按钮")
                    isRefer = true
                } else {
                    wv_course.loadUrl("javascript:window.local_obj.showSource(document.getElementsByTagName('html')[0].innerText);")
                }
            } else if (viewModel.importType == Common.TYPE_SHU) {
                if (!isRefer) {
                    val referUrl = getHostUrl() + "StudentQuery/CtrlViewQueryCourseTable"
                    wv_course.loadUrl(referUrl)
                    it.longSnack("请在看到网页加载完成后，再点一次右下角按钮")
                    isRefer = true
                } else {
                    wv_course.loadUrl(js)
                    isRefer = false
                }
            } else if (viewModel.importType == Common.TYPE_JNU) {
                if (countClick == 0) {
                    val referUrl = getHostUrl() + "Secure/TeachingPlan/wfrm_Prt_Report.aspx"
                    wv_course.loadUrl(referUrl)
                    it.longSnack("请在看到网页加载完成后，再点一次右下角按钮")
                    countClick++
                } else if (countClick == 1) {
//                    val jnujs = "javascript:window.local_obj.jump2DespairingUrl(document.getElementById(\"ReportFrameReportViewer1\").src);"
                    val jnujs = "javascript:window.location.href = document.getElementById(\"ReportFrameReportViewer1\").src;"
                    wv_course.loadUrl(jnujs)
//                    wv_course.loadUrl(despairingUrl)
                    it.longSnack("请再点一次右下角按钮")
                    countClick++
                } else {
                    wv_course.loadUrl(js)
                    countClick = 0
                }
            } else {
                wv_course.loadUrl(js)
            }
        }

        btn_back.setOnClickListener {
            if (wv_course.canGoBack()) {
                wv_course.goBack()
            }
        }
    }

    private fun getHostUrl(): String {
        var url = wv_course.url
        if (!url.endsWith('/')) {
            url += "/"
        }
        return hostRegex.find(wv_course.url)?.value ?: wv_course.url
    }

    private fun startVisit() {
        wv_course.visibility = View.VISIBLE
        ll_error.visibility = View.GONE
        val url = if (et_url.text.toString().startsWith("http://") || et_url.text.toString().startsWith("https://"))
            et_url.text.toString() else "http://" + et_url.text.toString()
        if (URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url)) {
            wv_course.loadUrl(url)
            requireContext().getPrefer().edit {
                putString(Const.KEY_SCHOOL_URL, url)
            }
        } else {
            Toasty.error(requireContext(), "请输入正确的网址╭(╯^╰)╮").show()
        }
    }

    internal inner class InJavaScriptLocalObj {
        @JavascriptInterface
        fun showSource(html: String) {
            // Log.d("源码", html)
            if (viewModel.importType != "apply") {
                launch {
                    try {
                        val result = viewModel.importSchedule(html)
                        Toasty.success(activity!!,
                                "成功导入 $result 门课程(ﾟ▽ﾟ)/\n请在右侧栏切换后查看").show()
                        activity!!.setResult(RESULT_OK)
                        activity!!.finish()
                    } catch (e: Exception) {
                        isRefer = false
                        countClick = 0
                        Toasty.error(activity!!,
                                "导入失败>_<\n${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                launch {
                    try {
                        viewModel.postHtml(
                                school = viewModel.schoolInfo[0],
                                type = if (viewModel.isUrp) "URP" else viewModel.schoolInfo[1],
                                qq = viewModel.schoolInfo[2],
                                html = html)
                        Toasty.success(activity!!, "上传源码成功~请等待适配哦", Toast.LENGTH_LONG).show()
                        //activity!!.start<ApplyInfoActivity>()
                        activity!!.finish()
                    } catch (e: Exception) {
                        Toasty.error(activity!!, "上传失败>_<\n" + e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        wv_course?.webViewClient = null
        wv_course?.webChromeClient = null
        wv_course?.clearCache(true)
        wv_course?.clearHistory()
        wv_course?.removeAllViews()
        wv_course?.destroy()
        super.onDestroyView()
    }

    companion object {
        @JvmStatic
        fun newInstance(url: String = "") =
                WebViewLoginFragment().apply {
                    arguments = Bundle().apply {
                        putString("url", url)
                    }
                }
    }
}
