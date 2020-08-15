package com.suda.yzune.wakeupschedule.schedule_import

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseFragment
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_JZ
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_QZ
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_ZF
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.Utils
import com.suda.yzune.wakeupschedule.utils.ViewUtils
import com.suda.yzune.wakeupschedule.utils.getPrefer
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_html_import.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import splitties.dimensions.dip
import splitties.snackbar.longSnack
import java.nio.charset.Charset

class HtmlImportFragment : BaseFragment() {

    private val viewModel by activityViewModels<ImportViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_html_import, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.resizeStatusBar(requireContext().applicationContext, v_status)
        if (requireActivity().getPrefer().getBoolean(Const.KEY_HIDE_NAV_BAR, false)) {
            ViewCompat.setOnApplyWindowInsetsListener(fab_import) { v, insets ->
                v.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    bottomMargin = insets.systemWindowInsets.bottom + v.dip(16)
                }
                insets
            }
        }
        tv_way.setOnClickListener {
            Utils.openUrl(requireActivity(), "https://support.qq.com/products/97617/faqs/73528")
        }

        tv_type.setOnClickListener {
            startActivityForResult(Intent(activity, SchoolListActivity::class.java).apply {
                putExtra("fromLocal", true)
            }, Const.REQUEST_CODE_CHOOSE_SCHOOL)
        }

        cp_utf.isChecked = true

        cp_utf.setOnClickListener {
            cp_utf.isChecked = true
            cp_gbk.isChecked = false
        }

        cp_gbk.setOnClickListener {
            cp_gbk.isChecked = true
            cp_utf.isChecked = false
        }

        var qzChipId = 0
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

        var zfChipId = 0
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
                    viewModel.importType = Common.TYPE_JZ
                }
                R.id.chip_jz2 -> {
                    jzChipId = checkedId
                    viewModel.importType = Common.TYPE_JZ_1
                }
                else -> {
                    group.findViewById<Chip>(jzChipId).isChecked = true
                }
            }
        }

        tv_self.setOnClickListener {
            if (viewModel.importType.equals("html")) {
                getView()?.longSnack("请先点击第二个按钮选择类型哦")
            } else {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/*"
                }
                try {
                    startActivityForResult(intent, Const.REQUEST_CODE_IMPORT_HTML)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        ib_back.setOnClickListener {
            requireActivity().finish()
        }

        fab_import.setOnClickListener {
            if (viewModel.htmlUri == null) {
                it.longSnack("还没有选择文件呢>_<")
                return@setOnClickListener
            }
            launch {
                try {
                    val html = withContext(Dispatchers.IO) {
                        requireActivity().contentResolver.openInputStream(viewModel.htmlUri!!)!!.bufferedReader(
                                if (cp_utf.isChecked) Charsets.UTF_8 else Charset.forName("gbk")
                        ).readText()
                    }
                    val result = viewModel.importSchedule(html)
                    Toasty.success(requireActivity(),
                            "成功导入 $result 门课程(ﾟ▽ﾟ)/\n请在右侧栏切换后查看").show()
                    requireActivity().setResult(RESULT_OK)
                    requireActivity().finish()
                } catch (e: Exception) {
                    Toasty.error(requireActivity(),
                            "导入失败>_<\n${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Const.REQUEST_CODE_IMPORT_HTML && resultCode == RESULT_OK) {
            viewModel.htmlUri = data?.data
        }
        if (requestCode == Const.REQUEST_CODE_CHOOSE_SCHOOL && resultCode == RESULT_OK) {
            viewModel.importType = data!!.getStringExtra("type")
            when (viewModel.importType) {
                TYPE_ZF -> {
                    chip_zf1.isChecked = true
                    cg_qz.visibility = View.GONE
                    cg_jz.visibility = View.GONE
                    cg_zf.visibility = View.VISIBLE
                }
                TYPE_QZ -> {
                    chip_qz1.isChecked = true
                    cg_qz.visibility = View.VISIBLE
                    cg_zf.visibility = View.GONE
                    cg_jz.visibility = View.GONE
                }
                TYPE_JZ -> {
                    chip_jz1.isChecked = true
                    cg_jz.visibility = View.VISIBLE
                    cg_qz.visibility = View.GONE
                    cg_zf.visibility = View.GONE
                }
                else -> {
                    cg_jz.visibility = View.GONE
                    cg_qz.visibility = View.GONE
                    cg_zf.visibility = View.GONE
                }
            }
        }
    }

}
