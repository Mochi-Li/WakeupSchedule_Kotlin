package com.suda.yzune.wakeupschedule.suda_life

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseFragment
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_empty_room.*
import splitties.dimensions.dip

class EmptyRoomFragment : BaseFragment() {

    private val viewModel by activityViewModels<SudaLifeViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_empty_room, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spinner_campus.dropDownVerticalOffset = view.dip(48)
        spinner_building.dropDownVerticalOffset = view.dip(48)
        spinner_date.dropDownVerticalOffset = view.dip(48)

        initData()
        initEvent()

        rv_room.adapter = RoomAdapter(R.layout.item_suda_room, viewModel.roomData).apply {
            addFooterView(View(activity).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip(240))
            })
        }
        rv_room.layoutManager = LinearLayoutManager(requireActivity())
    }

    private fun initData() {
        launch {
            try {
                viewModel.getBuildingData()
                spinner_campus.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, viewModel.buildingData.keys.toList())
                        .apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
            } catch (e: Exception) {
                Toasty.error(requireActivity(), "发生异常>_<${e.message}").show()
            }
        }

        launch {
            spinner_date.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, viewModel.getDateList())
                    .apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
        }
    }

    private fun initEvent() {
        spinner_campus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                spinner_building.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item,
                        viewModel.buildingData[(v as TextView).text]!!)
                        .apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
            }

        }

        spinner_building.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                queryRoomData()
            }

        }

        spinner_date.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                queryRoomData()
            }

        }

    }

    private fun queryRoomData() {
        val building = spinner_building.selectedItem as String?
        val date = spinner_date.selectedItem as String?
        if (building != null && date != null) {
            launch {
                try {
                    viewModel.getRoomData(building, date)
                    rv_room.adapter?.notifyDataSetChanged()
                } catch (e: Exception) {
                    Toasty.error(requireActivity(), "发生异常>_<${e.message}").show()
                }
            }
        }
    }

}
