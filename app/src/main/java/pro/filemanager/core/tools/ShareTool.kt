package pro.filemanager.core.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import pro.filemanager.R
import pro.filemanager.files.FileCore
import java.io.File
import java.util.*

object ShareTool {

    var outerSharingIntentInProgress = false

    fun shareFiles(context: Context, paths: MutableList<String>) {
        if(!outerSharingIntentInProgress) {
            outerSharingIntentInProgress = true

            if(paths.size == 1) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_STREAM, Uri.parse(paths[0]))
                    type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(paths[0]).toLowerCase(Locale.ROOT))
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }

                context.startActivity(Intent.createChooser(intent, context.resources.getString(R.string.toolbar_share)))
            } else if(paths.size > 1) {

            } else {
                throw IllegalArgumentException("Illegal filepaths")
            }
        }
    }
}