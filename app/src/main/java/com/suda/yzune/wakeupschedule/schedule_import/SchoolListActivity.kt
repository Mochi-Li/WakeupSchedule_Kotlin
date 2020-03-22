package com.suda.yzune.wakeupschedule.schedule_import

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bigkoo.quicksidebar.listener.OnQuickSideBarTouchListener
import com.google.gson.Gson
import com.suda.yzune.wakeupschedule.AppDatabase
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseTitleActivity
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_BNUZ
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_CF
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_ECJTU
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_HELP
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_HNIU
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_HNUST
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_HUNNU
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_JNU
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_JZ
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_LOGIN
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_MAINTAIN
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_PKU
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_QZ
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_QZ_2017
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_QZ_BR
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_QZ_CRAZY
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_QZ_OLD
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_QZ_WITH_NODE
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_SHU
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_UMOOC
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_URP
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_URP_NEW
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_URP_NEW_AJAX
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_WHU
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_ZF
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_ZF_1
import com.suda.yzune.wakeupschedule.schedule_import.Common.TYPE_ZF_NEW
import com.suda.yzune.wakeupschedule.schedule_import.bean.SchoolInfo
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.Utils
import com.suda.yzune.wakeupschedule.utils.getPrefer
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_school_list.*
import splitties.activities.start
import splitties.dimensions.dip
import splitties.resources.color
import splitties.resources.styledColor
import splitties.snackbar.action
import splitties.snackbar.longSnack

class SchoolListActivity : BaseTitleActivity(), OnQuickSideBarTouchListener {

    private val letters = HashMap<String, Int>()
    private val showList = arrayListOf<SchoolInfo>()
    private val schools = arrayListOf<SchoolInfo>()
    private lateinit var searchView: AppCompatEditText
    private var fromLocal = false

    override val layoutId: Int
        get() = R.layout.activity_school_list

    override fun onSetupSubButton(tvButton: AppCompatTextView): AppCompatTextView? {
        tvButton.text = "申请适配"
        tvButton.setOnClickListener {
            start<LoginWebActivity> {
                putExtra("import_type", "apply")
            }
            finish()
        }
        return tvButton
    }

    override fun createTitleBar() = LinearLayoutCompat(this).apply {
        orientation = LinearLayoutCompat.VERTICAL
        setBackgroundColor(styledColor(R.attr.colorSurface))
        addView(LinearLayoutCompat(context).apply {
            setPadding(0, getStatusBarHeight(), 0, 0)
            setBackgroundColor(styledColor(R.attr.colorSurface))
            val outValue = TypedValue()
            context.theme.resolveAttribute(R.attr.selectableItemBackgroundBorderless, outValue, true)

            addView(AppCompatImageButton(context).apply {
                setImageResource(R.drawable.ic_back)
                setBackgroundResource(outValue.resourceId)
                setPadding(dip(8))
                setColorFilter(styledColor(R.attr.colorOnBackground))
                setOnClickListener {
                    onBackPressed()
                }
            }, LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, dip(48)))

            mainTitle = AppCompatTextView(context).apply {
                text = title
                gravity = Gravity.CENTER_VERTICAL
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
            }

            addView(mainTitle, LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, dip(48)).apply {
                weight = 1f
            })

            searchView = AppCompatEditText(context).apply {
                hint = "请输入……"
                textSize = 16f
                background = null
                gravity = Gravity.CENTER_VERTICAL
                visibility = View.GONE
                setLines(1)
                setSingleLine()
                imeOptions = EditorInfo.IME_ACTION_SEARCH
                addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {}

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        showList.clear()
                        if (s.isNullOrBlank() || s.isEmpty()) {
                            showList.addAll(schools)
                        } else {
                            showList.addAll(schools.filter {
                                it.name.contains(s.toString())
                            })
                        }
                        recyclerView.adapter?.notifyDataSetChanged()
                        if (showList.isEmpty()) {
                            longSnack("没有找到你的学校哦") {
                                action("申请适配") {
                                    start<LoginWebActivity> {
                                        putExtra("import_type", "apply")
                                    }
                                }
                            }
                        }
                    }

                })
            }

            addView(searchView, LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, dip(48)).apply {
                weight = 1f
            })

            val iconFont = ResourcesCompat.getFont(context, R.font.iconfont)
            addView(AppCompatTextView(context).apply {
                textSize = 20f
                typeface = iconFont
                text = "\uE6D4"
                gravity = Gravity.CENTER
                setBackgroundResource(outValue.resourceId)
                setOnClickListener {
                    when (searchView.visibility) {
                        View.GONE -> {
                            mainTitle.visibility = View.GONE
                            searchView.visibility = View.VISIBLE
                            setTextColor(color(R.color.colorAccent))
                            searchView.isFocusable = true
                            searchView.isFocusableInTouchMode = true
                            searchView.requestFocus()
                            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            inputMethodManager.showSoftInput(searchView, 0)
                        }
                    }
                }
            }, LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, dip(48)).apply {
                marginEnd = dip(24)
            })
        }, LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fromLocal = intent.getBooleanExtra("fromLocal", false)
        quickSideBarView.setOnQuickSideBarTouchListener(this)
        initSchoolList()
    }

    private fun initSchoolList() {
        val dataBase = AppDatabase.getDatabase(application)
        val tableDao = dataBase.tableDao()
        val gson = Gson()
        schools.apply {
            add(SchoolInfo("A", "安徽信息工程学院", "http://teach.aiit.edu.cn/xtgl/login_slogin.html", TYPE_ZF_NEW))
            add(SchoolInfo("A", "安徽农业大学", "http://newjwxt.ahau.edu.cn/jwglxt", TYPE_ZF_NEW))
            add(SchoolInfo("A", "安徽大学", "http://xk2.ahu.cn/default2.aspx", TYPE_ZF))
            add(SchoolInfo("A", "安徽工业大学", "http://jwxt.ahut.edu.cn/jsxsd/", TYPE_QZ))
            add(SchoolInfo("A", "安徽建筑大学", "http://219.231.0.156/", TYPE_ZF_NEW))
            add(SchoolInfo("A", "安徽财经大学", "", TYPE_URP_NEW_AJAX))
            add(SchoolInfo("B", "保定学院", "http://jwgl.bdu.edu.cn/xtgl/login_slogin.html", TYPE_ZF_NEW))
            add(SchoolInfo("B", "北京信息科技大学", "http://jwgl.bistu.edu.cn/", TYPE_ZF))
            add(SchoolInfo("B", "北京化工大学", "http://jwglxt.buct.edu.cn/", TYPE_ZF_NEW))
            add(SchoolInfo("B", "北京大学", "http://elective.pku.edu.cn", TYPE_PKU))
            add(SchoolInfo("B", "北京工业大学", "http://gdjwgl.bjut.edu.cn/", TYPE_ZF))
            add(SchoolInfo("B", "北京师范大学珠海分校", "http://es.bnuz.edu.cn/", TYPE_BNUZ))
            add(SchoolInfo("B", "北京林业大学", "http://newjwxt.bjfu.edu.cn/", TYPE_QZ_BR))
            add(SchoolInfo("B", "北京理工大学", "http://jwms.bit.edu.cn/", TYPE_QZ_WITH_NODE))
            add(SchoolInfo("B", "北京理工大学珠海学院", "http://e.zhbit.com/jsxsd/", TYPE_QZ_WITH_NODE))
            add(SchoolInfo("B", "北京联合大学", "", TYPE_ZF))
            add(SchoolInfo("B", "北京邮电大学", "http://jwgl.bupt.edu.cn/jsxsd", TYPE_QZ_WITH_NODE))
            add(SchoolInfo("B", "渤海大学", "http://jw.bhu.edu.cn/", TYPE_URP))
            add(SchoolInfo("B", "滨州医学院", "http://jwgl.bzmc.edu.cn/jwglxt/xtgl/login_slogin.html", TYPE_ZF_NEW))
            add(SchoolInfo("C", "常州机电职业技术学院", "http://jwc.czmec.cn/", TYPE_ZF_NEW))
            add(SchoolInfo("C", "成都理工大学工程技术学院", "http://110.189.108.15/", TYPE_ZF))
            add(SchoolInfo("C", "重庆三峡学院", "http://jwgl.sanxiau.edu.cn/", TYPE_ZF))
            add(SchoolInfo("C", "重庆交通大学", "http://jwgl.cqjtu.edu.cn/jsxsd/", TYPE_QZ))
            add(SchoolInfo("C", "重庆交通职业学院", "", TYPE_ZF_1))
            add(SchoolInfo("C", "重庆大学城市科技学院", "", TYPE_QZ_WITH_NODE))
            add(SchoolInfo("C", "重庆邮电大学移通学院", "http://222.179.134.225:81/", TYPE_ZF))
            add(SchoolInfo("C", "长春大学", "http://cdjwc.ccu.edu.cn/jsxsd/", TYPE_QZ_BR))
            add(SchoolInfo("C", "长沙医学院", "http://jiaowu.csmu.edu.cn:8099/jsxsd/", TYPE_QZ_WITH_NODE))
            add(SchoolInfo("C", "长沙理工大学", "http://xk.csust.edu.cn/", TYPE_QZ_BR))
            add(SchoolInfo("D", "东北林业大学", "http://jwcnew.nefu.edu.cn/dblydx_jsxsd/", TYPE_QZ))
            add(SchoolInfo("D", "东北石油大学", "http://jwgl.nepu.edu.cn/", TYPE_HNUST))
            add(SchoolInfo("D", "大庆师范学院", "", TYPE_QZ))
            add(SchoolInfo("D", "大连外国语大学", "http://cas.dlufl.edu.cn/cas/", TYPE_QZ))
            add(SchoolInfo("D", "大连大学", "http://202.199.155.33/default2.aspx", TYPE_ZF))
            add(SchoolInfo("D", "大连工业大学艺术与信息工程学院", "http://www.caie.org/page_556.shtml", TYPE_ZF))
            add(SchoolInfo("D", "德州学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("D", "电子科技大学中山学院", "http://jwgln.zsc.edu.cn/jsxsd/", TYPE_QZ))
            add(SchoolInfo("F", "佛山科学技术学院", "http://100.fosu.edu.cn/", TYPE_QZ_CRAZY))
            add(SchoolInfo("F", "福建农林大学", "http://jwgl.fafu.edu.cn", TYPE_ZF_1))
            add(SchoolInfo("F", "福建农林大学金山学院", "http://jsxyjwgl.fafu.edu.cn/", TYPE_ZF))
            add(SchoolInfo("F", "福建工程学院", "https://jwxtwx.fjut.edu.cn/jwglxt/", TYPE_ZF_NEW))
            add(SchoolInfo("F", "福建师范大学", "http://jwglxt.fjnu.edu.cn/xtgl/login_slogin.html", TYPE_ZF_NEW))
            add(SchoolInfo("G", "广东外语外贸大学", "http://jxgl.gdufs.edu.cn/jsxsd/", TYPE_QZ_WITH_NODE))
            add(SchoolInfo("G", "广东工业大学", "http://jxfw.gdut.edu.cn/", TYPE_CF))
            add(SchoolInfo("G", "广东海洋大学", "http://210.38.137.126:8016/default2.aspx", TYPE_ZF))
            add(SchoolInfo("G", "广东环境保护工程职业学院", "http://113.107.254.7/", TYPE_ZF))
            add(SchoolInfo("G", "广东科学技术职业学院", "", TYPE_ZF_1))
            add(SchoolInfo("G", "广东财经大学", "http://jwxt.gdufe.edu.cn/", TYPE_QZ))
            add(SchoolInfo("G", "广东金融学院", "http://jwxt.gduf.edu.cn/", TYPE_QZ_BR))
            add(SchoolInfo("G", "广州医科大学", "", TYPE_QZ_WITH_NODE))
            add(SchoolInfo("G", "广州大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("G", "广西大学", "http://jwxt2018.gxu.edu.cn/jwglxt/xtgl/", TYPE_ZF_NEW))
            add(SchoolInfo("G", "广西大学行健文理学院", "http://210.36.24.21:9017/jwglxt/xtgl", TYPE_ZF_NEW))
            add(SchoolInfo("G", "广西师范学院", "http://210.36.80.160/jsxsd/", TYPE_QZ))
            add(SchoolInfo("G", "硅湖职业技术学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("G", "贵州财经大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("H", "华东理工大学", "https://inquiry.ecust.edu.cn/jsxsd/", TYPE_QZ))
            add(SchoolInfo("H", "华中农业大学", "http://jwgl.hzau.edu.cn/xtgl/login_slogin.html", TYPE_ZF_NEW))
            add(SchoolInfo("H", "华中师范大学", "http://one.ccnu.edu.cn/", TYPE_ZF_NEW))
            add(SchoolInfo("H", "华中科技大学", "", TYPE_LOGIN))
            add(SchoolInfo("H", "华北电力大学科技学校", "http://202.204.74.178/", TYPE_ZF))
            add(SchoolInfo("H", "华南农业大学", "http://jwxt.scau.edu.cn/", TYPE_QZ_2017))
            add(SchoolInfo("H", "华南理工大学", "http://xsjw2018.jw.scut.edu.cn/", TYPE_ZF_NEW))
            add(SchoolInfo("H", "哈尔滨商业大学", "http://jwxsd.hrbcu.edu.cn/", TYPE_QZ))
            add(SchoolInfo("H", "哈尔滨工程大学", "", TYPE_QZ_WITH_NODE))
            add(SchoolInfo("H", "海南大学", "http://jxgl.hainu.edu.cn/", TYPE_QZ_WITH_NODE))
            add(SchoolInfo("H", "海南师范大学", "http://210.37.0.16/", TYPE_ZF))
            add(SchoolInfo("H", "杭州医学院", "http://edu.hmc.edu.cn/", TYPE_ZF))
            add(SchoolInfo("H", "杭州电子科技大学", "http://jxgl.hdu.edu.cn/", TYPE_ZF))
            add(SchoolInfo("H", "河北大学", "http://zhjw.hbu.edu.cn/", TYPE_URP))
            add(SchoolInfo("H", "河北工程大学", "http://219.148.85.172:9111/login", TYPE_URP_NEW_AJAX))
            add(SchoolInfo("H", "河北农业大学", "urp.hebau.edu.cn:9002", TYPE_URP))
            add(SchoolInfo("H", "河北师范大学", "http://jwgl.hebtu.edu.cn/xtgl/", TYPE_ZF_NEW))
            add(SchoolInfo("H", "河北政法职业学院", "http://jwxt.helc.edu.cn/xtgl/login_slogin.html", TYPE_ZF_NEW))
            add(SchoolInfo("H", "河北环境工程学院", "http://jw.hebuee.edu.cn/xtgl/login_slogin.html", TYPE_ZF_NEW))
            add(SchoolInfo("H", "河北科技师范学院", "http://121.22.25.47/", TYPE_ZF))
            add(SchoolInfo("H", "河北经贸大学", "http://222.30.218.44/default2.aspx", TYPE_ZF))
            add(SchoolInfo("H", "河北金融学院", "", TYPE_QZ_CRAZY))
            add(SchoolInfo("H", "河南工程学院", "http://125.219.48.18/", TYPE_ZF))
            add(SchoolInfo("H", "河南理工大学", "", TYPE_URP))
            add(SchoolInfo("H", "河南财经政法大学", "http://xk.huel.edu.cn/jwglxt/xtgl/login_slogin.html", TYPE_ZF_NEW))
            add(SchoolInfo("H", "河海大学", "http://202.119.113.135/", TYPE_URP))
            add(SchoolInfo("H", "黑龙江科技大学", "http://xsurp.usth.edu.cn/index.jsp/", TYPE_URP_NEW_AJAX))
            add(SchoolInfo("H", "黑龙江外国语学院", "", TYPE_ZF))
            add(SchoolInfo("H", "淮南师范学院", "http://211.70.176.173/jwglxt/xtgl/", TYPE_ZF_NEW))
            add(SchoolInfo("H", "湖北中医药大学", "http://jwxt.hbtcm.edu.cn/jwglxt/xtgl", TYPE_ZF_NEW))
            add(SchoolInfo("H", "湖北医药学院", "http://jw.hbmu.edu.cn", TYPE_CF))
            add(SchoolInfo("H", "湖北工程学院新技术学院", "http://jwglxt.hbeutc.cn:20000/jwglxt/xtgl", TYPE_ZF_NEW))
            add(SchoolInfo("H", "湖北师范大学", "http://jwxt.hbnu.edu.cn/xtgl/login_slogin.html", TYPE_ZF_NEW))
            add(SchoolInfo("H", "湖北经济学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("H", "湖南信息职业技术学院", "http://my.hniu.cn/jwweb/ZNPK/KBFB_ClassSel.aspx", TYPE_HNIU))
            add(SchoolInfo("H", "湖南农业大学", "http://jwc.hunau.edu.cn/xsxk/", TYPE_ZF))
            add(SchoolInfo("H", "湖南商学院", "http://jwgl.hnuc.edu.cn/", TYPE_QZ))
            add(SchoolInfo("H", "湖南城市学院", "http://58.47.143.9:2045/zfca/login", TYPE_ZF))
            add(SchoolInfo("H", "湖南工业大学", "http://218.75.197.123:83/", TYPE_QZ))
            add(SchoolInfo("H", "湖南工商大学", "http://jwgl.hnuc.edu.cn/", TYPE_QZ))
            add(SchoolInfo("H", "湖南工学院", "http://jwgl.hnit.edu.cn/", TYPE_QZ_OLD))
            add(SchoolInfo("H", "湖南理工学院", "http://bkjw.hnist.cn/login", TYPE_URP_NEW))
            add(SchoolInfo("H", "湖南科技大学", "http://kdjw.hnust.cn:8080/kdjw", TYPE_HNUST))
            add(SchoolInfo("H", "湖南科技大学潇湘学院", "http://xxjw.hnust.cn:8080/xxjw/", TYPE_HNUST))
            add(SchoolInfo("H", "贺州学院", "http://jwglxt.hzu.gx.cn/jwglxt/xtgl/login_slogin.html", TYPE_ZF_NEW))
            add(SchoolInfo("H", "黄冈师范学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("J", "吉林大学", "", TYPE_LOGIN))
            add(SchoolInfo("J", "吉林师范大学", "http://jwxt.jlnu.edu.cn/", TYPE_QZ))
            add(SchoolInfo("J", "吉林建筑大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("J", "吉首大学", "http://jwxt.jsu.edu.cn/", TYPE_QZ))
            add(SchoolInfo("J", "嘉兴学院", "http://jwzx.zjxu.edu.cn/jwglxt/xtgl/login_slogin.html", TYPE_ZF_NEW))
            add(SchoolInfo("J", "嘉兴学院南湖学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("J", "江苏工程职业技术学院", "http://tyjw.tmu.edu.cn/", TYPE_ZF_NEW))
            add(SchoolInfo("J", "江苏师范大学", "http://sdjw.jsnu.edu.cn/", TYPE_QZ_WITH_NODE))
            add(SchoolInfo("J", "江苏建筑职业技术学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("J", "江苏科技大学", "http://jwgl.just.edu.cn:8080/jsxsd/", TYPE_QZ))
            add(SchoolInfo("J", "江西中医药大学", "http://jwxt.jxutcm.edu.cn/jwglxt/xtgl/", TYPE_ZF_NEW))
            add(SchoolInfo("J", "江西农业大学南昌商学院", "http://223.82.35.198:8888/jsxsd/", TYPE_QZ_BR))
            add(SchoolInfo("J", "暨南大学", "https://jwxt.jnu.edu.cn/", TYPE_JNU))
            add(SchoolInfo("J", "济南大学", "http://jwgl4.ujn.edu.cn/jwglxt", TYPE_ZF_NEW))
            add(SchoolInfo("J", "济南工程职业技术学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("J", "锦州医科大学", "http://jwgl.jzmu.edu.cn/jsxsd/", TYPE_QZ))
            add(SchoolInfo("L", "临沂大学", "http://jwxt.lyu.edu.cn/jxd/", TYPE_QZ))
            add(SchoolInfo("L", "辽宁机电职业技术学院", "http://jwgl.lnjdp.com/", TYPE_ZF_NEW))
            add(SchoolInfo("M", "茂名职业技术学院", "http://jwc.mmvtc.cn/", TYPE_ZF_1))
            add(SchoolInfo("M", "闽南师范大学", "http://222.205.160.107/jwglxt/xtgl/login_slogin.html", TYPE_ZF_NEW))
            add(SchoolInfo("N", "内蒙古大学", "http://jwxt.imu.edu.cn/login", TYPE_URP_NEW_AJAX))
            add(SchoolInfo("N", "内蒙古民族大学", "http://219.225.128.30/login", TYPE_URP_NEW_AJAX))
            add(SchoolInfo("N", "内蒙古师范大学", "", TYPE_QZ))
            add(SchoolInfo("N", "内蒙古科技大学", "http://stuzhjw.imust.edu.cn/login", TYPE_URP_NEW_AJAX))
            add(SchoolInfo("N", "内蒙古科技大学包头师范学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("N", "南京城市职业学院", "http://jw.ncc.edu.cn/jwglxt/xtgl/", TYPE_ZF_NEW))
            add(SchoolInfo("N", "南京工业大学", "https://jwgl.njtech.edu.cn/", TYPE_ZF_NEW))
            add(SchoolInfo("N", "南京师范大学中北学院", "http://222.192.5.246/", TYPE_ZF_NEW))
            add(SchoolInfo("N", "南京特殊教育师范学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("N", "南京理工大学", "http://202.119.81.112:8080/", TYPE_QZ))
            add(SchoolInfo("N", "南宁师范大学", "http://210.36.80.160/jsxsd/", TYPE_QZ))
            add(SchoolInfo("N", "南宁职业技术学院", "http://jwxt.ncvt.net:8088/jwglxt/", TYPE_ZF_NEW))
            add(SchoolInfo("N", "南方医科大学", "http://zhjw.smu.edu.cn/", TYPE_CF))
            add(SchoolInfo("N", "南方科技大学", "http://jwxt.sustc.edu.cn/jsxsd", TYPE_QZ))
            add(SchoolInfo("N", "南昌大学", "http://jwc104.ncu.edu.cn:8081/jsxsd/", TYPE_QZ))
            add(SchoolInfo("N", "南昌航空大学", "", TYPE_QZ))
            add(SchoolInfo("N", "宁波工程学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("Q", "清华大学", "", TYPE_LOGIN))
            add(SchoolInfo("Q", "青岛农业大学", "", TYPE_QZ_BR))
            add(SchoolInfo("Q", "青岛滨海学院", "http://jwgl.qdbhu.edu.cn/jwglxt/xtgl/login_slogin.html", TYPE_ZF_NEW))
            add(SchoolInfo("Q", "青岛科技大学", "https://jw.qust.edu.cn/jwglxt.htm", TYPE_ZF_NEW))
            add(SchoolInfo("Q", "齐鲁工业大学", "http://jwxt.qlu.edu.cn/", TYPE_QZ))
            add(SchoolInfo("Q", "齐鲁师范学院", "", TYPE_URP_NEW))
            add(SchoolInfo("Q", "齐齐哈尔大学", "", TYPE_URP))
            add(SchoolInfo("S", "三江学院", "http://jw.sju.edu.cn/jwglxt/xtgl/login_slogin.html", TYPE_ZF_NEW))
            add(SchoolInfo("S", "上海大学", "http://www.xk.shu.edu.cn/", TYPE_SHU))
            add(SchoolInfo("S", "上海海洋大学", "https://urp.shou.edu.cn/login", TYPE_URP_NEW_AJAX))
            add(SchoolInfo("S", "四川大学", "http://zhjw.scu.edu.cn/login", TYPE_URP_NEW_AJAX))
            add(SchoolInfo("S", "四川大学锦城学院", "http://jwweb.scujcc.cn/", TYPE_ZF))
            add(SchoolInfo("S", "四川美术学院", "", TYPE_QZ))
            add(SchoolInfo("S", "四川轻化工大学", "http://61.139.105.138/xtgl/", TYPE_ZF_NEW))
            add(SchoolInfo("S", "山东农业大学", "http://xjw.sdau.edu.cn/jwglxt/", TYPE_ZF_NEW))
            add(SchoolInfo("S", "山东大学威海校区", "https://portal.wh.sdu.edu.cn/", TYPE_QZ))
            add(SchoolInfo("S", "山东大学（威海）", "", TYPE_QZ))
            add(SchoolInfo("S", "山东师范大学", "http://www.bkjw.sdnu.edu.cn", TYPE_ZF))
            add(SchoolInfo("S", "山东政法大学", "http://114.214.79.176/jwglxt/", TYPE_ZF_NEW))
            add(SchoolInfo("S", "山东理工大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("S", "山东科技大学", "http://jwgl.sdust.edu.cn/", TYPE_QZ))
            add(SchoolInfo("S", "山东财经大学", "", TYPE_QZ))
            add(SchoolInfo("S", "山东青年政治学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("S", "山西农业大学", "http://xsjwxt.sxau.edu.cn:7873/login", TYPE_URP_NEW_AJAX))
            add(SchoolInfo("S", "山西农业大学信息学院", "", TYPE_URP_NEW_AJAX))
            add(SchoolInfo("S", "山西工程技术学院", "http://211.82.48.36/login", TYPE_URP_NEW_AJAX))
            add(SchoolInfo("S", "沈阳工程学院", "http://awcwea.com/jwgl.sie.edu.cn/jwgl/", TYPE_QZ))
            add(SchoolInfo("S", "沈阳师范大学", "http://210.30.208.140/", TYPE_ZF))
            add(SchoolInfo("S", "石家庄学院", "http://jwgl.sjzc.edu.cn/jwglxt/", TYPE_ZF_NEW))
            add(SchoolInfo("S", "绍兴文理学院", "http://jw.usx.edu.cn/", TYPE_ZF))
            add(SchoolInfo("S", "绍兴文理学院元培学院", "http://www.ypc.edu.cn/jwgl.htm", TYPE_ZF))
            add(SchoolInfo("S", "苏州农业职业技术学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("S", "苏州大学", "", TYPE_LOGIN))
            add(SchoolInfo("S", "苏州大学（备用）", "http://xk.suda.edu.cn", TYPE_ZF))
            add(SchoolInfo("S", "苏州科技大学", "http://jw.usts.edu.cn/default2.aspx", TYPE_ZF))
            add(SchoolInfo("S", "苏州科技大学天平学院", "http://tpjw.usts.edu.cn/default2.aspx", TYPE_ZF))
            add(SchoolInfo("S", "韶关学院", "http://jwc.sgu.edu.cn/", TYPE_QZ))
            add(SchoolInfo("T", "天津科技大学", "", TYPE_URP_NEW_AJAX))
            add(SchoolInfo("T", "天津中医药大学", "http://jiaowu.tjutcm.edu.cn/jsxsd/", TYPE_QZ))
            add(SchoolInfo("T", "天津体育学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("T", "天津医科大学", "http://tyjw.tmu.edu.cn/", TYPE_QZ))
            add(SchoolInfo("T", "天津工业大学", "http://jwpt.tjpu.edu.cn/", TYPE_URP_NEW_AJAX))
            add(SchoolInfo("T", "天津商业大学", "http://xk.tjcu.edu.cn/", TYPE_URP))
            add(SchoolInfo("T", "天津职业技术师范大学", "", TYPE_URP_NEW_AJAX))
            add(SchoolInfo("W", "五邑大学", "http://jxgl.wyu.edu.cn/", TYPE_CF))
            add(SchoolInfo("W", "威海职业学院", "", TYPE_QZ))
            add(SchoolInfo("W", "无锡太湖学院", "http://jwcnew.thxy.org/jwglxt/xtgl/login_slogin.html", TYPE_ZF_NEW))
            add(SchoolInfo("W", "武昌首义学院", "http://syjw.wsyu.edu.cn/xtgl/", TYPE_ZF_NEW))
            add(SchoolInfo("W", "武汉东湖学院", "http://221.232.159.27/", TYPE_ZF))
            add(SchoolInfo("W", "武汉纺织大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("W", "武汉轻工大学", "http://jwglxt.whpu.edu.cn/xtgl/", TYPE_ZF_NEW))
            add(SchoolInfo("W", "温州医科大学", "http://jwxt.wmu.edu.cn", TYPE_ZF_NEW))
            add(SchoolInfo("W", "渭南师范学院", "http://218.195.46.49/jwglxt/", TYPE_ZF_NEW))
            add(SchoolInfo("W", "潍坊学院", "http://210.44.64.154/", TYPE_ZF))
            add(SchoolInfo("W", "潍坊职业学院", "http://jwgl.sdwfvc.cn/", TYPE_ZF_NEW))
            add(SchoolInfo("W", "皖西学院", "", TYPE_QZ))
            add(SchoolInfo("X", "信阳师范学院", "http://jwc.xynu.edu.cn/jxzhxxfwpt.htm", TYPE_ZF_NEW))
            add(SchoolInfo("X", "厦门工学院", "http://jwxt.xit.edu.cn/default2.aspx", TYPE_ZF))
            add(SchoolInfo("X", "厦门理工学院", "http://jw.xmut.edu.cn/", TYPE_ZF_NEW))
            add(SchoolInfo("X", "徐州医科大学", "http://222.193.95.102/", TYPE_ZF_NEW))
            add(SchoolInfo("X", "徐州幼儿师范高等专科学校", "http://222.187.124.16/", TYPE_ZF))
            add(SchoolInfo("X", "湘潭大学", "http://jwxt.xtu.edu.cn/jsxsd/", TYPE_QZ))
            add(SchoolInfo("X", "西北工业大学", "", TYPE_LOGIN))
            add(SchoolInfo("X", "西华大学", "http://jwc.xhu.edu.cn/xtgl/login_slogin.html", TYPE_ZF_NEW))
            add(SchoolInfo("X", "西南大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("X", "西南政法大学", "http://njwxt.swupl.edu.cn/jwglxt/xtgl", TYPE_ZF_NEW))
            add(SchoolInfo("X", "西南民族大学", "http://jwxt.swun.edu.cn/", TYPE_ZF_NEW))
            add(SchoolInfo("X", "西南石油大学", "http://jwxt.swpu.edu.cn/", TYPE_URP))
            add(SchoolInfo("X", "西安外事学院", "http://jwxt.xaiu.edu.cn/xtgl/login_slogin.html", TYPE_ZF_NEW))
            add(SchoolInfo("X", "西安建筑科技大学", "http://xk.xauat.edu.cn/default2.aspx#a", TYPE_ZF))
            add(SchoolInfo("X", "西安理工大学", "http://202.200.112.200/", TYPE_ZF))
            add(SchoolInfo("X", "西安科技大学", "http://jwportal.xust.edu.cn/", TYPE_ZF_NEW))
            add(SchoolInfo("X", "西安邮电大学", "http://www.zfjw.xupt.edu.cn/jwglxt/", TYPE_ZF_NEW))
            add(SchoolInfo("X", "西昌学院", "https://jwxt.xcc.edu.cn/xtgl/login_slogin.html", TYPE_ZF_NEW))
            add(SchoolInfo("Y", "云南财经大学", "http://202.203.194.2/", TYPE_ZF))
            add(SchoolInfo("Y", "延安大学", "http://jwglxt.yau.edu.cn/jwglxt/xtgl/login_slogin.html", TYPE_ZF_NEW))
            add(SchoolInfo("Y", "烟台大学", "http://xk.jwc.ytu.edu.cn/", TYPE_URP_NEW))
            add(SchoolInfo("Z", "中南大学", "https://csujwc.its.csu.edu.cn/", TYPE_QZ))
            add(SchoolInfo("Z", "中南林业科技大学", "http://jwgl.csuft.edu.cn/", TYPE_QZ))
            add(SchoolInfo("Z", "中南财经政法大学", "", TYPE_QZ))
            add(SchoolInfo("Z", "中国农业大学", "http://urpjw.cau.edu.cn/login", TYPE_URP_NEW_AJAX))
            add(SchoolInfo("Z", "中国医科大学", "http://jw.cmu.edu.cn/jwglxt/xtgl/login_slogin.html", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "中国地质大学（武汉）", "", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "中国石油大学（北京）", "http://urp.cup.edu.cn/login", TYPE_URP_NEW_AJAX))
            add(SchoolInfo("Z", "中国石油大学（华东）", "", TYPE_QZ))
            add(SchoolInfo("Z", "中国矿业大学", "http://jwxt.cumt.edu.cn/jwglxt/", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "中国矿业大学徐海学院", "http://xhjw.cumt.edu.cn:8080/jwglxt/xtgl/", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "中国药科大学", "http://jwgl.cpu.edu.cn/", TYPE_QZ))
            add(SchoolInfo("Z", "浙江万里学院", "http://jwxt.zwu.edu.cn/", TYPE_ZF_1))
            add(SchoolInfo("Z", "浙江农林大学", "http://115.236.84.158/xtgl", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "浙江工业大学", "http://www.gdjw.zjut.edu.cn/", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "浙江工业大学之江学院", "http://jwgl.zzjc.edu.cn/default2.aspx", TYPE_ZF))
            add(SchoolInfo("Z", "浙江工商大学", "http://124.160.64.163/jwglxt/xtgl/", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "浙江师范大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "浙江师范大学行知学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "浙江财经大学", "http://fzjh.zufe.edu.cn/jwglxt", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "郑州大学西亚斯国际学院", "http://218.198.176.111/default2.aspx", TYPE_MAINTAIN))
            add(SchoolInfo("Z", "郑州航空工业管理学院", "http://202.196.166.138/", TYPE_ZF))

            add(SchoolInfo("B", "北京电子科技职业学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("J", "济南大学舜耕校区", "", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "浙江纺织服装技术学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("J", "吉林大学珠海学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("X", "西安培华学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("C", "常熟理工学院（苏州常熟）", "", TYPE_ZF_NEW))
            add(SchoolInfo("B", "北京农学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("J", "嘉应学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("S", "山东科技职业学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("C", "常州工学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "枣庄学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "中国民航大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "中国民用航空飞行学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("S", "上海交通大学", "https://i.sjtu.edu.cn/xtgl/login_slogin.html", TYPE_ZF_NEW))
            add(SchoolInfo("H", "淮阴师范学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("T", "唐山职业技术学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "浙江理工大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("X", "徐州工业职业技术学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("Q", "青岛科技大学高密校区", "", TYPE_ZF_NEW))
            add(SchoolInfo("S", "石家庄邮电职业技术学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("N", "内蒙古化工职业学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "中央财经大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("J", "荆楚理工学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("H", "河北传媒学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("H", "华北电力大学北京校区", "", TYPE_ZF_NEW))
            add(SchoolInfo("X", "西藏农牧学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("G", "广西科技大学鹿山学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("H", "华北电力大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("L", "岭南师范学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("B", "北京第二外国语学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "浙江中医药大学滨江学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("H", "河北北方学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("C", "重庆工商大学融智学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "昭通学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("N", "南昌工学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("C", "重庆电力高等专科学校", "", TYPE_ZF_NEW))
            add(SchoolInfo("S", "四川传媒学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("H", "湖南涉外经济学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("Q", "青岛理工大学临沂校区", "", TYPE_ZF_NEW))
            add(SchoolInfo("S", "四川理工学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("S", "沈阳航空航天大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("L", "辽宁经济职业技术学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("B", "滨州医学院烟台校区", "", TYPE_ZF_NEW))
            add(SchoolInfo("A", "安徽医科大学临床医学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("Y", "玉林师范学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("N", "南通大学杏林学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("B", "包头师范学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("D", "东北大学秦皇岛分校", "", TYPE_ZF_NEW))
            add(SchoolInfo("H", "黑龙江东方学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "浙江中医药大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("S", "沈阳工学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("J", "江西财经职业学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("S", "山西能源学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("S", "四川电影电视学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("H", "汉江师范学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "浙江中医药大学富春校区", "", TYPE_ZF_NEW))
            add(SchoolInfo("W", "无锡商业职业技术学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("S", "深圳信息职业技术学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("H", "湖南机电职业技术学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("X", "徐州工业职业", "", TYPE_ZF_NEW))
            add(SchoolInfo("S", "山东政法学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("Y", "云南大学旅游文化学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("W", "武汉传媒学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("T", "太湖学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("H", "湖北经济学院法商学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("X", "西南大学荣昌校区", "", TYPE_ZF_NEW))
            add(SchoolInfo("J", "江苏医药职业学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("W", "无锡科技职业学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "浙江纺织服装职业技术学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("G", "贵州工程应用技术学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("N", "宁波职业技术学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("S", "山东职业学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("H", "淮北师范大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("S", "上海电机学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("S", "沙洲职业工学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("N", "内蒙古电子信息职业技术学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("Y", "盐城工学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("H", "河南财政金融学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("T", "唐山师范学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("J", "江苏城乡建设学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("L", "聊城大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("P", "普洱学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("J", "济南大学西校区", "", TYPE_ZF_NEW))
            add(SchoolInfo("X", "西北大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("G", "广东药科大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("W", "武汉体育学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("C", "长春职业技术学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("N", "南京信息工程大学滨江学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("J", "江苏城乡建设职业学院 ", "", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "中国矿业大学南湖校区", "", TYPE_ZF_NEW))
            add(SchoolInfo("J", "江苏卫生健康职业学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("N", "宁夏师范学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("G", "桂林旅游学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("L", "乐山师范学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("F", "福建江夏学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("H", "杭州师范大学钱江学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("J", "济南大学东校区", "", TYPE_ZF_NEW))
            add(SchoolInfo("J", "济南职业学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("H", "湖南医药学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("H", "河北工业职业技术学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("W", "温州医科大学仁济学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("L", "廊坊师范学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("S", "宿州职业技术学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("S", "山西财经大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("Q", "青岛黄海学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("T", "台州学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("J", "江苏城乡建设职业学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "浙江中医药", "", TYPE_ZF_NEW))
            add(SchoolInfo("G", "广东技术师范大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("H", "华东交通大学理工学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "中山职业技术学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("C", "常州信息职业技术学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("Q", "青岛理工大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("H", "华南师范大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("J", "江苏城乡建设职业", "", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "中国地质大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("S", "上海理工大学", "", TYPE_ZF_NEW))
            add(SchoolInfo("C", "常熟理工学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("H", "淮南师范学院泉山校区", "", TYPE_ZF_NEW))
            add(SchoolInfo("Z", "张家口学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("B", "北华航天工业学院", "", TYPE_ZF_NEW))
            add(SchoolInfo("J", "济宁学院", "", TYPE_ZF_NEW))

            add(SchoolInfo("H", "河北工业大学", "", TYPE_URP_NEW_AJAX))
            add(SchoolInfo("B", "滨州学院", "", TYPE_URP_NEW_AJAX))
            add(SchoolInfo("Q", "齐鲁理工学院", "", TYPE_URP_NEW_AJAX))
            add(SchoolInfo("Z", "浙江外国语学院", "", TYPE_URP_NEW_AJAX))
            add(SchoolInfo("D", "东北财经大学", "http://202.199.165.159/login", TYPE_URP_NEW))

            add(SchoolInfo("N", "南京师范大学", "http://ehall.nnu.edu.cn/new/index.html", TYPE_JZ))
            add(SchoolInfo("A", "安徽医学高等专科学校", "", TYPE_JZ))
            add(SchoolInfo("A", "安徽中澳科技职业学院", "http://ehall.acac.cn/new/index.html", TYPE_JZ))
            add(SchoolInfo("S", "深圳大学", "http://ehall.szu.edu.cn/jwapp/sys/kcbcx/*default/index.do", TYPE_JZ))
            add(SchoolInfo("X", "西安交通大学", "http://ehall.xjtu.edu.cn/new/index.html", TYPE_JZ))
            add(SchoolInfo("H", "湖南工业职业技术学院", "http://jwc.hunangy.com/SSJWC/index.asp", TYPE_JZ))
            add(SchoolInfo("Z", "中国传媒大学", "", TYPE_JZ))
            add(SchoolInfo("Y", "宜宾学院", "", TYPE_JZ))
            add(SchoolInfo("N", "宁波大学", "http://jwgl.nbu.edu.cn:7777/nbdx/", TYPE_JZ))
            add(SchoolInfo("Z", "中原工学院", "http://xsxk.zut.edu.cn/", TYPE_JZ))
            add(SchoolInfo("X", "西安电子科技大学", "http://ehall.xidian.edu.cn/new/index.html", TYPE_JZ))
            add(SchoolInfo("D", "东南大学", "https://newids.seu.edu.cn/authserver/login", TYPE_JZ))
            add(SchoolInfo("Y", "云南大学", "", TYPE_JZ))

            add(SchoolInfo("W", "武汉大学", "http://bkjw.whu.edu.cn/", TYPE_WHU))
            add(SchoolInfo("H", "湖南师范大学", "", TYPE_HUNNU))

            add(SchoolInfo("N", "内蒙古工业大学", "http://jwch.imut.edu.cn/jwzx/index.do", TYPE_UMOOC))
            add(SchoolInfo("S", "山东第一医科大学", "http://jwc.sdfmu.edu.cn/academic/common/security/login.jsp", TYPE_UMOOC))
            add(SchoolInfo("H", "哈尔滨理工大学", "http://jwzx.hrbust.edu.cn/homepage/index.do", TYPE_UMOOC))
            add(SchoolInfo("B", "北京石油化工学院", "http://222.31.135.24/academic/common/security/login.jsp", TYPE_UMOOC))
            add(SchoolInfo("H", "呼和浩特民族学院", "", TYPE_UMOOC))
            add(SchoolInfo("N", "内蒙古农业大学", "http://jwxt.imau.edu.cn/academic/common/security/login.jsp", TYPE_UMOOC))
            add(SchoolInfo("Z", "中国地质大学（北京）", "", TYPE_UMOOC))
            add(SchoolInfo("Q", "青岛大学", "http://jw.qdu.edu.cn/academic/common/security/login.jsp", TYPE_UMOOC))
            add(SchoolInfo("X", "西北农林科技大学", "http://jwgl.nwsuaf.edu.cn/academic/login/nwsuaf/loginIds6.jsp", TYPE_UMOOC))
            add(SchoolInfo("B", "北京外国语大学", "https://curricula.bfsu.edu.cn/academic/common/security/login.jsp", TYPE_UMOOC))
            add(SchoolInfo("Z", "中国劳动关系学院", "", TYPE_UMOOC))
            add(SchoolInfo("G", "桂林理工大学", "http://jw.glut.edu.cn/academic/common/security/login.jsp", TYPE_UMOOC))
            add(SchoolInfo("G", "桂林理工大学南宁分校", "http://jw.glutnn.cn/academic/common/security/login.jsp", TYPE_UMOOC))
            add(SchoolInfo("Z", "中央司法警官学院", "http://jwgl.cicp.edu.cn/academic/common/security/login.jsp", TYPE_UMOOC))
            add(SchoolInfo("S", "沈阳建筑大学", "http://202.199.64.11/academic/common/security/login.jsp", TYPE_UMOOC))
            add(SchoolInfo("D", "大连交通大学", "http://jw.djtu.edu.cn/academic/common/security/login.jsp", TYPE_UMOOC))
            add(SchoolInfo("Q", "泉州师范学院", "", TYPE_UMOOC))
            add(SchoolInfo("Z", "中华女子学院", "http://jw.cwu.edu.cn/academic/common/security/login.jsp", TYPE_UMOOC))
            add(SchoolInfo("L", "兰州大学", "http://jwk.lzu.edu.cn/academic/common/security/login.jsp", TYPE_UMOOC))

            add(SchoolInfo("H", "湖南大学", "http://hdjw.hnu.edu.cn/Njw2017/login.html", TYPE_QZ_2017))
            add(SchoolInfo("W", "武汉工程大学", "http://jwxt.wit.edu.cn/jsxsd/", TYPE_QZ_WITH_NODE))
            add(SchoolInfo("H", "黑龙江工程学院", "http://jw.hljit.edu.cn/default2.aspx", TYPE_ZF))
            add(SchoolInfo("H", "华东交通大学", "https://jwxt.ecjtu.edu.cn/", TYPE_ECJTU))
        }

        schools.sortWith(compareBy({ it.sortKey }, { it.name }))

        schools.add(0, SchoolInfo("通", "优慕课在线", "", TYPE_UMOOC))
        schools.add(0, SchoolInfo("通", "旧强智（需要 IE 的那种）", "", TYPE_QZ_OLD))
        schools.add(0, SchoolInfo("通", "强智教务", "", TYPE_QZ))
        schools.add(0, SchoolInfo("通", "金智教务", "", TYPE_JZ))
        schools.add(0, SchoolInfo("通", "正方教务", "", TYPE_ZF))
        schools.add(0, SchoolInfo("通", "新正方教务", "", TYPE_ZF_NEW))
        schools.add(0, SchoolInfo("通", "URP 系统", "", TYPE_URP))
        schools.add(0, SchoolInfo("通", "新 URP 系统", "", TYPE_URP_NEW))

        getImportSchoolBean()?.let {
            val newer = schools.find { item ->
                item.name == it.name
            }
            if (newer != null) {
                schools.add(0, newer.copy(sortKey = "★"))
            }
        }
        schools.add(0, SchoolInfo("★", "如何正确选择教务类型？", "https://support.qq.com/embed/97617/faqs/59901", TYPE_HELP))

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.layoutManager = layoutManager

        showList.addAll(schools)
        val adapter = SchoolImportListAdapter(showList)
        adapter.setOnItemClickListener { _, _, position ->
            if (showList[position].type == TYPE_HELP) {
                Utils.openUrl(this, showList[position].url)
                return@setOnItemClickListener
            }
            if (fromLocal) {
                setResult(Activity.RESULT_OK, Intent().apply { putExtra("type", showList[position].type) })
                finish()
            } else {
                launch {
                    if (showList[position].type == TYPE_HUNNU) {
                        Toasty.info(this@SchoolListActivity, "暂时只能通过「从HTML文件导入」方式使用哦", Toasty.LENGTH_LONG).show()
                        return@launch
                    }
                    if (showList[position].type == TYPE_MAINTAIN) {
                        Toasty.info(this@SchoolListActivity, "处于维护中哦", Toasty.LENGTH_LONG).show()
                        return@launch
                    }
                    getPrefer().edit {
                        putString(Const.KEY_IMPORT_SCHOOL, gson.toJson(showList[position]))
                    }
                    val tableId = tableDao.getDefaultTableId()
                    startActivityForResult(Intent(this@SchoolListActivity, LoginWebActivity::class.java).apply {
                        putExtra("school_name", showList[position].name)
                        putExtra("import_type", showList[position].type)
                        putExtra("tableId", tableId)
                        putExtra("url", showList[position].url)
                    }, Const.REQUEST_CODE_IMPORT)
                }
            }
        }

        val customLetters = arrayListOf<String>()

        for ((position, school) in schools.withIndex()) {
            val letter = school.sortKey
            //如果没有这个key则加入并把位置也加入
            if (!letters.containsKey(letter)) {
                letters[letter] = position
                customLetters.add(letter)
            }
        }

        quickSideBarView.letters = customLetters
        recyclerView.adapter = adapter

        val headersDecor = StickyRecyclerHeadersDecoration(adapter)
        recyclerView.addItemDecoration(headersDecor)
    }

    private fun getImportSchoolBean(): SchoolInfo? {
        val json = getPrefer().getString(Const.KEY_IMPORT_SCHOOL, null)
                ?: return null
        val gson = Gson()
        val res = gson.fromJson<SchoolInfo>(json, SchoolInfo::class.java)
        if (!res.type.isNullOrEmpty()) {
            return gson.fromJson<SchoolInfo>(json, SchoolInfo::class.java)
        }
        return null
    }

    override fun onLetterTouching(touching: Boolean) {
        quickSideBarTipsView.visibility = if (touching) View.VISIBLE else View.INVISIBLE
    }

    override fun onLetterChanged(letter: String, position: Int, y: Float) {
        quickSideBarTipsView.setText(letter, position, y)
        if (letters.containsKey(letter)) {
            (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(letters[letter]!!, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Const.REQUEST_CODE_IMPORT) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}
