package elytrondesign.lib.android.dialogwrapper

import android.app.Activity
import android.app.AlertDialog
import android.os.Build
import android.view.ViewGroup
import android.widget.TextView

object DialogWrapper {

    fun buildAlertDialog(
        activity: Activity,
        parentLayout: ViewGroup,
        title: String,
        message: String,
        cancelable: Boolean,
        positiveButtonText: String,
        positiveButtonAction: () -> Unit,
        negativeButtonText: String,
        negativeButtonAction: () -> Unit,
        onDismissAction: () -> Unit,
        onCancelAction: () -> Unit = {
            onDismissAction.invoke()
        }
    ) : AlertDialog {
        val dialog = AlertDialog.Builder(activity)
            .setView(
                activity.layoutInflater.inflate(R.layout.layout_base_dialog, parentLayout, false).apply {
                    findViewById<TextView>(R.id.layoutBaseDialogTitle).text = title

                    findViewById<TextView>(R.id.layoutBaseDialogMessage).text = message

                    findViewById<TextView>(R.id.layoutBaseDialogNegativeButton).apply {
                        text = negativeButtonText
                    }

                    findViewById<TextView>(R.id.layoutBaseDialogPositiveButton).apply {
                        text = positiveButtonText
                    }
                }
            )
            .setCancelable(cancelable)
            .create()

        if(cancelable) dialog.setOnCancelListener {
            onDismissAction.invoke()
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) dialog.setOnDismissListener {
            onCancelAction.invoke()
        }

        dialog.setOnShowListener { dialogInterface ->
            (dialogInterface as AlertDialog).findViewById<TextView>(R.id.layoutBaseDialogPositiveButton).setOnClickListener {
                dialogInterface.dismiss()
                positiveButtonAction.invoke()
            }

            dialogInterface.findViewById<TextView>(R.id.layoutBaseDialogNegativeButton).setOnClickListener {
                dialog.dismiss()
                negativeButtonAction.invoke()
            }
        }

        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_blank)

        return dialog
    }
}