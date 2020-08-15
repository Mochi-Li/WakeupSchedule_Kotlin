package com.suda.yzune.wakeupschedule.settings

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseTitleActivity
import com.suda.yzune.wakeupschedule.bean.TableBean
import es.dmoral.toasty.Toasty
import splitties.resources.color

class TimeSettingsActivity : BaseTitleActivity() {
    override val layoutId: Int
        get() = R.layout.activity_time_settings

    override fun onSetupSubButton(): AppCompatTextView? {
        tvButton = AppCompatTextView(this)
        tvButton.setText(R.string.save)
        tvButton.typeface = Typeface.DEFAULT_BOLD
        tvButton.setTextColor(color(R.color.colorAccent))
        tvButton.setOnClickListener {
            saveAndExit()
        }
        return tvButton
    }

    private val viewModel by viewModels<TimeSettingsViewModel>()
    private lateinit var navController: NavController
    private lateinit var tvButton: AppCompatTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.table = intent.extras!!.getParcelable<TableBean>("tableData")!!
        initView()
    }

    private fun initView() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_fragment) as NavHostFragment
        val navGraph = navHostFragment.navController.navInflater.inflate(R.navigation.nav_time_settings)
//        val fragDestination = navGraph.findNode(R.id.timeTableFragment)!!
//        fragDestination.addArgument("selectedId", NavArgument.Builder()
//                .setType(NavType.IntType).setIsNullable(false).setDefaultValue(intent.extras!!.getInt("selectedId")).build())
//        fragDestination.setDefaultArguments(Bundle().apply {
//            this.putInt("selectedId", intent.extras!!.getInt("selectedId"))
//        })
        navHostFragment.navController.graph = navGraph
        navController = Navigation.findNavController(this, R.id.nav_fragment)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            mainTitle.text = destination.label
            tvButton.isVisible = destination.id == R.id.timeSettingsFragment
        }
    }

    private fun saveAndExit() {
        launch {
            try {
                viewModel.saveDetailData(viewModel.entryPosition)
                navController.navigateUp()
                Toasty.success(this@TimeSettingsActivity, "保存成功").show()
            } catch (e: Exception) {
                Toasty.error(this@TimeSettingsActivity, "出现错误>_<${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onBackPressed() {
        when (navController.currentDestination?.id) {
            R.id.timeSettingsFragment -> {
                MaterialAlertDialogBuilder(this)
                        .setMessage("需要保存以使设置生效吗？")
                        .setPositiveButton(R.string.save) { _, _ ->
                            saveAndExit()
                        }
                        .setNegativeButton(R.string.exit) { _, _ ->
                            navController.navigateUp()
                        }
                        .show()
            }
            else -> {
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra("timeTable", viewModel.table.timeTable)
                })
                super.onBackPressed()
            }
        }
    }

}
