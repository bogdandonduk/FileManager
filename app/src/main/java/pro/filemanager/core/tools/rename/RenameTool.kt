package pro.filemanager.core.tools.rename

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.delay
import pro.filemanager.core.tools.info.InfoItemBottomModalSheetFragment
import pro.filemanager.core.tools.sort.SortBottomModalSheetFragment
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

                if(newFile.isDirectory) {
                    MediaScannerConnection.scanFile(
                            context,
                            arrayOf(newFile.parent),
                            null
                    ) { _: String, _: Uri ->
                        if(newFile.listFiles() != null) {
                            newFile.listFiles()?.forEach {
                                MediaScannerConnection.scanFile(
                                        context,
                                        arrayOf(),
                                        null
                                ) { _: String, _: Uri ->
                                    lastRefreshAction?.run()
                                    lastRefreshAction = null
                                }
                            }
                        } else {
                            lastRefreshAction?.run()
                            lastRefreshAction = null
                        }
                    }
                } else {
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