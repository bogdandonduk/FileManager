package pro.filemanager.files

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.util.*

class FileManager {

    companion object {

        const val KEY_ARGUMENT_PATH = "path"
        const val KEY_ARGUMENT_APP_BAR_TITLE = "appBarTitle"

        const val KEY_INTERNAL_STORAGE = "internal"
        const val KEY_EXTERNAL_STORAGE = "external"

        const val MESSAGE_ILLEGAL_STATE_INTERNAL = "illegalStateInternal"

        var externalRootPath: String? = null

        var findingExternalRootInProgress = false

        fun getInternalRootPath(): String {
            return if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED)
                Environment.getExternalStorageDirectory().absolutePath
            else MESSAGE_ILLEGAL_STATE_INTERNAL
        }

        fun getInternalDownMostRootPath(): String {

            var downMostRootPath = ""

            val internalRootPath = getInternalRootPath()

            if(internalRootPath != MESSAGE_ILLEGAL_STATE_INTERNAL) {
                internalRootPath.split(File.separator).forEach {
                    if (downMostRootPath.isEmpty()) {
                        if (it.isNotEmpty())
                            downMostRootPath = File.separator + it
                    }
                }
            }

            return downMostRootPath
        }

        @SuppressLint("NewApi")
        fun findExternalRoot(context: Context) {
            if ((Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) || (context.checkSelfPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED)
            ) {

                val cursor: Cursor = context.contentResolver.query(
                    MediaStore.Files.getContentUri("external"), arrayOf(
                        MediaStore.Files.FileColumns.DATA
                    ), null, null, null, null
                )!!

                if (cursor.moveToFirst()) {

                    if(getInternalRootPath() != MESSAGE_ILLEGAL_STATE_INTERNAL) {

                        findingExternalRootInProgress = true

                        var shortestPathLength = 100000000

                        while (!cursor.isAfterLast) {
                            val data =
                                cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))

                            if (!data.toLowerCase(Locale.ROOT).contains(getInternalRootPath().toLowerCase(Locale.ROOT))) {
                                if (!getInternalRootPath().toLowerCase(Locale.ROOT).contains(data)) {
                                    val splitData = data.split(File.separator)

                                    if (splitData.size < shortestPathLength) {
                                        shortestPathLength = splitData.size

                                        if (File(data).isDirectory) {
                                            externalRootPath = data
                                        }
                                    }
                                } else {
                                    externalRootPath = ""
                                }
                            } else {
                                externalRootPath = ""
                            }

                            cursor.moveToNext()

                        }

                        cursor.close()

                        findingExternalRootInProgress = false
                    }

                }

            }
        }

        fun openFile(context: Context, path: String) {
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
            }
        }

    }
}