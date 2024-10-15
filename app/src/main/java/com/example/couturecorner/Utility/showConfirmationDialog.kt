package com.example.couturecorner.Utility

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.example.couturecorner.R

private var confirmationDialog: Dialog? = null

fun showConfirmationDialog(context: Context, message: String, onOkClick: () -> Unit) {
    if (confirmationDialog?.isShowing == true) {
        return
    }

    val dialog = Dialog(context)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(true)

    val view = LayoutInflater.from(context).inflate(R.layout.dialog_confirmation, null)
    dialog.setContentView(view)
    val textViewMessage = view.findViewById<TextView>(R.id.textViewMessage)
    val buttonOk = view.findViewById<Button>(R.id.buttonOk)
    textViewMessage.text = message

    buttonOk.setOnClickListener {
        dialog.dismiss()
        onOkClick()
    }

    dialog.setOnDismissListener {
        confirmationDialog = null
    }

    confirmationDialog = dialog
    dialog.show()
}