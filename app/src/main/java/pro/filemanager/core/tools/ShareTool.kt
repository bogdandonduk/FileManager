package pro.filemanager.core.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pro.filemanager.ApplicationLoader
import pro.filemanager.R
import pro.filemanager.core.FileExposer
import pro.filemanager.core.wrappers.CoroutineWrapper
import java.io.File

object ShareTool {
    var sharingInProgress = false

    fun shareItems(context: Context, paths: MutableList<String>, mimeType: String, homogeneousMimeType: Boolean) {
        if(paths.size <= 0) {
            throw IllegalArgumentException("Illegal filepaths")
        } else {
            val itemsMimeType = if(homogeneousMimeType)
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(paths.first())) ?: mimeType
            else mimeType

            if(!sharingInProgress) {
                sharingInProgress = true

                if(paths.size == 1) {
                    context.startActivity(
                        Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                                    context,
                                    context.packageName + FileExposer.authority,
                                    File(paths.first())
                                ))
                                type = itemsMimeType
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            }, context.resources.getString(R.string.toolbar_share)))
                } else if(paths.size > 1) {
                    CoroutineWrapper.globalDefaultScope.launch {
                        var totalSize: Long = 0

                        paths.forEach {
                            totalSize += File(it).length()
                        }

                        withContext(Main) {
                            if(paths.size <= 500 && totalSize <= 1073741824) {
                                try {
                                    context.startActivity(
                                        Intent.createChooser(
                                            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                                putParcelableArrayListExtra(
                                                    Intent.EXTRA_STREAM,
                                                    arrayListOf<Uri>().apply {
                                                        paths.forEach {
                                                            add(
                                                                FileProvider.getUriForFile(
                                                                    context,
                                                                    context.packageName + ".fileProvider",
                                                                    File(it)
                                                                )
                                                            )
                                                        }
                                                    })
                                                type = itemsMimeType
                                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            }, context.resources.getString(R.string.toolbar_share)
                                        )
                                    )
                                } catch(thr: Throwable) {
                                    Toast.makeText(context, context.resources.getString(R.string.warning_items_size_too_large), Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, context.resources.getString(R.string.warning_items_size_too_large), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
}