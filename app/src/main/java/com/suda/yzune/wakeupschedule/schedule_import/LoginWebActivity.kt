package com.suda.yzune.wakeupschedule.schedule_import

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.SplashActivity
import com.suda.yzune.wakeupschedule.base_view.BaseActivity
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.getPrefer
import kotlinx.android.synthetic.main.fragment_login_web.*
import java.io.FileNotFoundException

class LoginWebActivity : BaseActivity() {

    private val viewModel by viewModels<ImportViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!getPrefer().getBoolean(Const.KEY_HAS_INTRO, false)) {
            showResultDialog("提示", "首次安装后请先打开App一次再导入。", true)
        }

        intent.extras?.getString("import_type")?.let {
            viewModel.importType = it
        }
        intent.extras?.getString("school_name")?.let {
            viewModel.school = it
        }

        val fragment = when (viewModel.importType) {
            "login" -> {
                LoginWebFragment()
            }
            "apply" -> {
                SchoolInfoFragment()
            }
            "file" -> {
                FileImportFragment()
            }
            "excel" -> {
                ExcelImportFragment()
            }
            "html" -> {
                HtmlImportFragment()
            }
            "code" -> {
                CodeImportFragment()
            }
            else -> {
                if (viewModel.importType.isNullOrEmpty() || viewModel.school.isNullOrEmpty()) {
                    null
                } else {
                    WebViewLoginFragment.newInstance(intent.getStringExtra("url")!!)
                }
            }
        }
        fragment?.let { frag ->
            val transaction = supportFragmentManager.beginTransaction()
            transaction.add(android.R.id.content, frag, viewModel.school)
            transaction.commit()
            if (viewModel.importType != "apply" && viewModel.importType != "file" && viewModel.importType != "code") {
                showImportSettingDialog()
            }
        }

        if (fragment == null && intent.action == Intent.ACTION_VIEW) {
            launch {
                viewModel.importId = viewModel.getNewId()
                viewModel.newFlag = true
                val uri = intent.data
                val path = uri?.path ?: ""
                val type = when {
                    path.contains("wakeup_schedule") || path.endsWith("WAKEUP_SCHEDULE") -> "file"
                    path.endsWith("csv") || path.endsWith("CSV") -> "csv"
                    path.endsWith("html") || path.endsWith("HTML") -> "html"
                    else -> ""
                }
                if (type.isEmpty()) {
                    showResultDialog("导入失败>_<",
                            "文件扩展名必须是csv、html或wakeup_schedule。其中csv文件一定是要按照模板的要求填写的，不是说随便一个Excel文件转成的都可以。",
                            true)
                    return@launch
                }
                val transaction = supportFragmentManager.beginTransaction()
                when (type) {
                    "file" -> transaction.add(android.R.id.content, FileImportFragment(), null)
                    "csv" -> transaction.add(android.R.id.content, ExcelImportFragment(), null)
                    "html" -> transaction.add(android.R.id.content, HtmlImportFragment(), null)
                }
                transaction.commit()
                if (type == "html") {
                    viewModel.htmlUri = uri
                } else {
                    try {
                        when (type) {
                            "file" -> viewModel.importFromFile(uri)
                            "csv" -> viewModel.importFromExcel(uri)
                        }
                        showResultDialog("导入成功(ﾟ▽ﾟ)/",
                                "请记得要打开多课表面板来查看哦~",
                                true)
                    } catch (e: Exception) {
                        showResultDialog("发生异常>_<",
                                if (e is IllegalStateException || e is FileNotFoundException) {
                                    "读取文件失败。建议分享到QQ，然后在QQ的界面点击文件，选择「导入到课程表」。具体错误信息：${e.message}"
                                } else {
                                    if (type == "csv") {
                                        "导入失败，请严格按照模板的格式进行填写，如「周数」使用 中文顿号 分隔而不是逗号。具体错误信息：${e.message}"
                                    } else {
                                        "导入失败。具体错误信息：${e.message}"
                                    }
                                },
                                true)
                    }
                }
            }
        }
    }

    fun showResultDialog(title: CharSequence, msg: CharSequence, needStartActivity: Boolean, otherAction: () -> Unit = {}) {
        MaterialAlertDialogBuilder(this@LoginWebActivity)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(R.string.sure) { _, _ ->
                    if (needStartActivity) {
                        val intent = Intent(this@LoginWebActivity, SplashActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        otherAction.invoke()
                    }
                }
                .show()
    }

    private fun showImportSettingDialog() {
        ImportSettingFragment().apply {
            isCancelable = false
        }.show(supportFragmentManager, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            Const.REQUEST_CODE_IMPORT_FILE -> {
                launch {
                    try {
                        viewModel.importFromFile(data?.data)
                        showResultDialog("导入成功(ﾟ▽ﾟ)/", "请记得要打开多课表面板来查看哦~", false) {
                            setResult(RESULT_OK)
                            finish()
                        }
                    } catch (e: Exception) {
                        showResultDialog("发生异常>_<", "导入失败。建议分享到QQ，然后在QQ的界面点击文件，选择「导入到课程表」。具体错误信息：${e.message}", false)
                    }
                }
            }
            Const.REQUEST_CODE_IMPORT_CSV -> {
                launch {
                    try {
                        viewModel.importFromExcel(data?.data)
                        showResultDialog("导入成功(ﾟ▽ﾟ)/", "请记得要打开多课表面板来查看哦~", false) {
                            setResult(RESULT_OK)
                            finish()
                        }
                    } catch (e: Exception) {
                        showResultDialog("发生异常>_<",
                                if (e is IllegalStateException || e is FileNotFoundException) {
                                    "读取文件失败。建议分享到QQ，然后在QQ的界面点击文件，选择「导入到课程表」。具体错误信息：${e.message}"
                                } else {
                                    "导入失败，请严格按照模板的格式进行填写，如「周数」使用 中文顿号 分隔而不是逗号。具体错误信息：${e.message}"
                                },
                                false)
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        val suda = supportFragmentManager.findFragmentByTag("苏州大学")
        if (suda != null && fab_login.isExpanded) {
            fab_login.isExpanded = false
        } else {
            super.onBackPressed()
        }
    }

}
