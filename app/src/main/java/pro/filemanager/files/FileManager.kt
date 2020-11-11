package pro.filemanager.files

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.lang.Exception
import java.util.*

class FileManager {

    companion object {

        const val KEY_ARGUMENT_PATH = "path"
        const val KEY_ARGUMENT_APP_BAR_TITLE = "appBarTitle"

        var internalRootPath = Environment.getExternalStorageDirectory().toString()

        var externalRootPath = ""

        var isFindingExternalRoot = false

        fun findExternalRoot(context: Context) {
            val cursor: Cursor = context.contentResolver.query(MediaStore.Files.getContentUri("external"), arrayOf(
                    MediaStore.Files.FileColumns.DATA
            ), null, null, null, null)!!

            if(cursor.moveToFirst()) {

                externalRootPath = ""

                isFindingExternalRoot = true

                var shortestPathLength = 1000000

                while(!cursor.isAfterLast) {
                    val data = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))

                    if(!data.toLowerCase(Locale.ROOT).contains(internalRootPath.toLowerCase(Locale.ROOT))) {
                        if(!internalRootPath.toLowerCase(Locale.ROOT).contains(data)) {
                            val splitData = data.split(File.separator)

                            if(splitData.size < shortestPathLength) {
                                shortestPathLength = splitData.size

                                if(File(data).isDirectory) {
                                    externalRootPath = data
                                }
                            }
                        }
                    }

                    cursor.moveToNext()
                }

                cursor.close()

                isFindingExternalRoot = false
            }

        }

        fun openFile(context: Context, path: String) {
            val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(path))

            val uri: Uri =
                    try {
                        FileProvider.getUriForFile(context, context.packageName + ".fileProvider", File(path))
                    } catch (e: Exception) {
                        Uri.parse(path)
                    }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, type)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            if(intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // take user to google play for video player
            }
        }

    }

}