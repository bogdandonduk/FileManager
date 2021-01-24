package pro.filemanager.core.tools.rename

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import java.io.File

object RenameTool {
    const val KEY_ARGUMENT_PATH_TO_RENAME = "pathToRename"

    var sheetShown = false

    var lastRefreshAction: (() -> Unit)? = null

    fun showRenameBottomModalSheetFragment(fm: FragmentManager, path: String, refreshAction: () -> Unit) {
        if(!sheetShown) {
            sheetShown = true

            val sheet = RenameFragment()
            sheet.arguments = bundleOf(
                    KEY_ARGUMENT_PATH_TO_RENAME to path
            )

            lastRefreshAction = refreshAction

            sheet.show(fm, null)
        }
    }

    fun rename(context: Context, path: String, newPath: String) {
        val oldFile = File(path)
        val newFile = File(newPath)

        if(newFile.exists()) {
            if(path != newPath) throw IllegalArgumentException("File already exists")
        } else {
            oldFile.renameTo(newFile)

            MediaScannerConnection.scanFile(
                    context,
                    arrayOf(newFile.absolutePath),
                    arrayOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(newPath))),
            ) { _: String, _: Uri ->
                MediaScannerConnection.scanFile(
                        context,
                        arrayOf(newFile.parent),
                        arrayOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(newPath))),
                ) { _: String, _: Uri ->
                    lastRefreshAction?.invoke()
                    lastRefreshAction = null
                }
            }
        }
    }
}