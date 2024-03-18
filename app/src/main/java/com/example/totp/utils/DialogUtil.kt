package com.example.totp.utils

import android.app.AlertDialog
import android.content.Context

object DialogUtil {
    fun Context.showAlertDialog(
        msg: String,
        onCancel: () -> Unit = {},
        onConfirm: () -> Unit
    ) {
        AlertDialog.Builder(this)
            .setMessage(msg)
            .setPositiveButton("确认") { _, _ -> onConfirm() }
            .setNegativeButton("取消") { _, _ -> onCancel() }
            .show()
    }
}