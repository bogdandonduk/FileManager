package pro.filemanager.files

import android.content.Context
import android.content.Intent
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.util.*
import android.os.Handler

object FileCore {

    private var outerIntentInProgress = false

    fun openFileOut(context: Context, path: String) {
        if(!outerIntentInProgress) {
            outerIntentInProgress = true

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                        FileProvider.getUriForFile(
                                context,
                                context.packageName + ".fileProvider",
                                File(path)
                        ),
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                                MimeTypeMap.getFileExtensionFromUrl(path).toLowerCase(Locale.ROOT)
                        )
                )
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)

            } else {

            }

            // TODO: Crutch! Fix!

            Handler().postDelayed({
                outerIntentInProgress = false
            }, 1000)
        }
    }
}