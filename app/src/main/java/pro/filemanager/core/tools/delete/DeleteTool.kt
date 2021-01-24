package pro.filemanager.core.tools.delete

import android.app.Dialog
import android.provider.MediaStore
import android.view.ViewGroup
import elytrondesign.lib.android.dialogwrapper.DialogWrapper
import pro.filemanager.home.HomeActivity
import pro.filemanager.R
import pro.filemanager.core.base.BaseLibraryItem
import pro.filemanager.core.tools.SelectionTool

object DeleteTool {
    const val dialogId = "delete"

    val lastLibraryItems = mutableListOf<BaseLibraryItem>()

    var dialogShown = false

    fun delete(activity: HomeActivity, parentLayout: ViewGroup, paths: MutableList<String>, dialogCollection: MutableMap<String, Dialog>, refreshAction: () -> Unit) {
        if(!dialogShown) {
            dialogShown = true

            DialogWrapper.buildAlertDialog(
                activity,
                parentLayout,
                activity.resources.getString(R.string.title_are_you_sure),
                String.format(
                    if (paths.size > 1)
                        activity.resources.getString(R.string.title_deletion_prompt_multiple_items)
                    else
                        activity.resources.getString(R.string.title_deletion_prompt_single_item),
                    paths.size
                ),
                true,
                activity.resources.getString(R.string.title_confirm),
                {
                    try {
                        paths.forEach {
                            activity.contentResolver.delete(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                MediaStore.Images.ImageColumns.DATA + "=?", arrayOf(it)
                            )
                        }
                    } catch (thr: Throwable) {

                    }

                    dialogShown = false
                    SelectionTool.clearWithConfirmation = false

                    refreshAction.invoke()

                    activity.onBackPressed()
                },
                activity.resources.getString(R.string.go_back),
                {
                    dialogShown = false
                },
                {
                    dialogShown = false
                }
            ) {
                
            }.apply {
                dialogCollection[dialogId] = this
            }.show()
        }
    }
}