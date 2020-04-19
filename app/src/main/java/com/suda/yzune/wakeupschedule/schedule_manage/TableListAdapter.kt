package com.suda.yzune.wakeupschedule.schedule_manage

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.bean.TableSelectBean

class TableListAdapter(layoutResId: Int, data: MutableList<TableSelectBean>) :
        BaseQuickAdapter<TableSelectBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: TableSelectBean) {
        if (item.type == 1) {
            holder.getView<View>(R.id.ib_delete).visibility = View.GONE
        } else {
            holder.getView<View>(R.id.ib_delete).visibility = View.VISIBLE
        }

        if (item.tableName != "") {
            holder.setText(R.id.tv_table_name, item.tableName)
        } else {
            holder.setText(R.id.tv_table_name, "我的课表")
        }
        val imageView = holder.getView<AppCompatImageView>(R.id.iv_pic)
        if (item.background != "") {
            if (item.background.startsWith("#")) {
                val bitmap = try {
                    Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888).apply {
                        eraseColor(item.background.removePrefix("#").toInt())
                    }
                } catch (e: Exception) {
                    Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
                        eraseColor(Color.GRAY)
                    }
                }
                Glide.with(context)
                        .load(bitmap)
                        .into(imageView)
            } else {
                Glide.with(context)
                        .load(item.background)
                        .override(400, 600)
                        .error(BitmapDrawable(context.resources, Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
                            eraseColor(Color.GRAY)
                        }))
                        .into(imageView)
            }
        } else {
            Glide.with(context)
                    .load(R.drawable.main_background_2020_1)
                    .override(400, 600)
                    .into(imageView)
        }
    }
}