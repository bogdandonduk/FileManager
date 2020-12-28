package pro.filemanager.core.tools.rename

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import pro.filemanager.images.ImageCore
import java.io.File

object RenameTool {
    const val KEY_ARGUMENT_PATH_TO_RENAME = "pathToRename"

    var showingDialogInProgress = false

    var lastRefreshAction: Runnable? = null

    fun showRenameBottomModalSheetFragment(fm: FragmentManager, path: String, refreshAction: Runnable) {
        val sheet = RenameBottomModalSheetFragment()
            sheet.arguments = bundleOf(
                    KEY_ARGUMENT_PATH_TO_RENAME to path
            )

        lastRefreshAction = refreshAction

        sheet.show(fm, null)
    }

    fun rename(context: Context, path: String, newPath: String) {
        if(!showingDialogInProgress) {
            showingDialogInProgress = true

            val oldFile = File(path)
            val newFile = File(newPath)

            if(newFile.exists()) {
                if(path != newPath) {
                    showingDialogInProgress = false

                    throw IllegalArgumentException("File already exists")
                }
            } else {
                oldFile.renameTo(newFile)

                MediaScannerConnection.scanFile(
                        context,
                        arrayOf(newFile.absolutePath),
                        arrayOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(newPath)) ?: ImageCore.MIME_TYPE),
                ) { _: String, _: Uri ->
                    MediaScannerConnection.scanFile(
                            context,
                            arrayOf(newFile.parent),
                            arrayOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(newPath)) ?: ImageCore.MIME_TYPE),
                    ) { _: String, _: Uri ->
                        lastRefreshAction?.run()
                        lastRefreshAction = null
                    }
                }
            }
        }
    }
}