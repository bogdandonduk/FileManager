package pro.filemanager.core.tools

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Typeface
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import pro.filemanager.HomeActivity
import pro.filemanager.R
import pro.filemanager.core.base.BaseViewModel
import java.time.format.TextStyle

object DeleteTool {

    var showingDialogInProgress = false

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
                    showingDialogInProgress = false
                }
                .create()

        dialog.setOnShowListener { dialogInterface ->
            showingDialogInProgress = true

            (dialogInterface as AlertDialog).findViewById<Button>(R.id.layoutBaseDialogPositiveButton).setOnClickListener {
                dialogInterface.dismiss()

                try {
                    paths.forEach {
                        activity.contentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                MediaStore.Images.ImageColumns.DATA + "=?", arrayOf(it))
                    }
                } catch(thr: Throwable) {

                }

                refreshAction.run()

                activity.onBackPressed()
            }

            dialogInterface.findViewById<Button>(R.id.layoutBaseDialogNegativeButton).setOnClickListener {
                dialogInterface.dismiss()
            }
        }

        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_blank)
        dialog.show()
    }
    
    fun deleteAlbumsAndRefreshMediaStore(activity: HomeActivity, paths: MutableList<String>, albumsCount: Int, refreshAction: Runnable) {
        val dialog = AlertDialog.Builder(activity)
                .setView(
                        activity.layoutInflater.inflate(R.layout.layout_base_dialog, null).apply {
                            findViewById<TextView>(R.id.layoutBaseDialogTitle).text = activity.resources.getString(R.string.title_are_you_sure)

                            findViewById<TextView>(R.id.layoutBaseDialogMessage).text =
                                    String.format(
                                            if(paths.size > 1 )
                                                activity.resources.getString(R.string.title_deletion_prompt_multiple_albums)
                                            else
                                                activity.resources.getString(R.string.title_deletion_prompt_single_album), albumsCount
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
                    showingDialogInProgress = false
                }
                .create()

        dialog.setOnShowListener { dialogInterface ->
            showingDialogInProgress = true

            (dialogInterface as AlertDialog).findViewById<Button>(R.id.layoutBaseDialogPositiveButton).setOnClickListener {
                dialogInterface.dismiss()

                try {
                    paths.forEach {
                        activity.contentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                MediaStore.Images.ImageColumns.DATA + "=?", arrayOf(it))
                    }
                } catch(thr: Throwable) {

                }

                refreshAction.run()

                activity.onBackPressed()
            }

            dialogInterface.findViewById<Button>(R.id.layoutBaseDialogNegativeButton).setOnClickListener {
                dialogInterface.dismiss()
            }
        }

        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_blank)
        dialog.show()
    }
}