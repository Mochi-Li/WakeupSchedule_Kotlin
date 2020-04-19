package com.suda.yzune.wakeupschedule.schedule

import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.bean.TableSelectBean
import splitties.dimensions.dip

class TableNameAdapter(layoutResId: Int, data: MutableList<TableSelectBean>) :
        BaseQuickAdapter<TableSelectBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: TableSelectBean) {
        holder.setGone(R.id.menu_setting, item.type != 1)

        if (item.tableName != "") {
            holder.setText(R.id.tv_table_name, item.tableName)
        } else {
            holder.setText(R.id.tv_table_name, "我的课表")
        }
        val imageView = holder.getView<AppCompatImageView>(R.id.iv_table_bg)
        if (item.type == 1) {
            imageView.setColorFilter(ContextCompat.getColor(context, R.color.deep_grey))
        } else {
            imageView.clearColorFilter()
        }
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
                        .transform(CenterCrop(), RoundedCorners(context.dip(4)))
                        .into(imageView)
            } else {
                Glide.with(context)
                        .load(item.background)
                        .override(200, 200)
                        .transform(CenterCrop(), RoundedCorners(context.dip(4)))
                        .error(Glide.with(context)
                                .load(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
                                    eraseColor(Color.GRAY)
                                })
                                .transform(CenterCrop(), RoundedCorners(context.dip(4))))
                        .into(imageView)
            }
        } else {
            Glide.with(context)
                    .load(R.drawable.main_background_2020_1)
                    .override(200, 200)
                    .transform(CenterCrop(), RoundedCorners(context.dip(4)))
                    .into(imageView)
        }
    }

}