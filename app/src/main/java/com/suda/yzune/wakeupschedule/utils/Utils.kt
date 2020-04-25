package com.suda.yzune.wakeupschedule.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.InputType
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.settings.items.SeekBarItem
import com.suda.yzune.wakeupschedule.widget.colorpicker.ColorPickerFragment
import java.security.MessageDigest


object Utils {

    fun openUrl(context: Context, url: String) {
        val intent = Intent()
        intent.action = "android.intent.action.VIEW"
        val contentUrl = Uri.parse(url)
        intent.data = contentUrl
        context.startActivity(intent)
    }

    fun getMD5Str(str: String): String {
        val md5StrBuff = StringBuffer()
        try {
            val messageDigest = MessageDigest.getInstance("MD5")
            messageDigest.reset()
            messageDigest.update(str.toByteArray(charset("UTF-8")))
            val byteArray = messageDigest.digest()
            for (i in byteArray.indices) {
                if (Integer.toHexString(0xFF and byteArray[i].toInt()).length == 1)
                    md5StrBuff.append("0").append(
                            Integer.toHexString(0xFF and byteArray[i].toInt()))
                else
                    md5StrBuff.append(Integer.toHexString(0xFF and byteArray[i].toInt()))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return md5StrBuff.toString()
    }

    fun buildColorPickerDialogBuilder(activity: FragmentActivity, color: Int, id: Int, showAlphaSlider: Boolean = true) {
        ColorPickerFragment.newBuilder()
                .setShowAlphaSlider(showAlphaSlider)
                .setColor(color)
                .setDialogId(id)
                .show(activity)
    }

    fun showSeekBarItemDialog(context: Context, item: SeekBarItem, action: (alertDialog: AlertDialog, value: Int) -> Unit) {
        val dialog = MaterialAlertDialogBuilder(context)
                .setTitle(item.title)
                .setView(R.layout.dialog_edit_text)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.sure, null)
                .setCancelable(false)
                .create()
        dialog.show()
        val inputLayout = dialog.findViewById<TextInputLayout>(R.id.text_input_layout)
        val editText = dialog.findViewById<TextInputEditText>(R.id.edit_text)
        inputLayout?.helperText = "范围 ${item.min} ~ ${item.max}"
        if (item.prefix.isNotEmpty()) {
            inputLayout?.prefixText = item.prefix
        }
        inputLayout?.suffixText = item.unit
        editText?.inputType = InputType.TYPE_CLASS_NUMBER
        if (item.valueInt < item.min) {
            item.valueInt = item.min
        }
        if (item.valueInt > item.max) {
            item.valueInt = item.max
        }
        val valueStr = item.valueInt.toString()
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
            if (valueInt < item.min || valueInt > item.max) {
                inputLayout?.error = "注意范围 ${item.min} ~ ${item.max}"
                return@setOnClickListener
            }
            action.invoke(dialog, valueInt)
        }
    }

}