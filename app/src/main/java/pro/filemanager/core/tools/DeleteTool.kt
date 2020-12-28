package pro.filemanager.core.tools

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.TextView
import pro.filemanager.HomeActivity
import pro.filemanager.R
import pro.filemanager.core.generics.BaseFolderItem
import pro.filemanager.core.generics.BaseViewModel
import java.io.File
import java.time.format.TextStyle

object DeleteTool {

    var lastDialogShown = false

    @SuppressLint("InflateParams")
    fun deleteItemsAndRefreshMediaStore(activity: HomeActivity, paths: MutableList<String>, refreshAction: Runnable) {
        val dialog = AlertDialog.Builder(activity)
                .setView(
                        activity.layoutInflater.inflate(R.layout.layout_base_dialog, null).apply {
                            findViewById<TextView>(R.id.layoutBaseDialogTitle).text = activity.resources.getString(R.string.title_are_you_sure)

                            findViewById<TextView>(R.id.layoutBaseDialogMessage).text =
                                String.format(
                                    if(paths.size > 1 )
                                        activity.resources.getString(R.string.title_deletion_prompt_multiple_items)
                                    else
                                        activity.resources.getString(R.string.title_deletion_prompt_single_item), paths.size
                                )

                            findViewById<TextView>(R.id.layoutBaseDialogNegativeButton).apply {
                                text = activity.resources.getString(R.string.title_go_back)
                            }

                            findViewById<TextView>(R.id.layoutBaseDialogPositiveButton).apply {
                                text = activity.resources.getString(R.string.title_confirm)
                            }
                        }
                )
                .setOnCancelListener {
                    it.dismiss()

                }
                .setOnDismissListener {
                    lastDialogShown = false
                }
                .create()

        dialog.setOnShowListener { dialogInterface ->
            lastDialogShown = true

            (dialogInterface as AlertDialog).findViewById<TextView>(R.id.layoutBaseDialogPositiveButton).setOnClickListener {
                dialogInterface.dismiss()

                try {
                    paths.forEach {
                        Log.d("TAG", "deleteItemsAndRefreshMediaStore: $it")

                        activity.contentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                MediaStore.Images.ImageColumns.DATA + "=?", arrayOf(it))
                    }
                } catch(thr: Throwable) {

                }

                refreshAction.run()

                activity.onBackPressed()
            }

            dialogInterface.findViewById<TextView>(R.id.layoutBaseDialogNegativeButton).setOnClickListener {
                dialogInterface.dismiss()
            }
        }

        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_blank)
        dialog.show()
    }
}