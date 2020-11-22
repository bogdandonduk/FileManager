package pro.filemanager.files

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import kotlinx.coroutines.delay
import java.io.File
import java.util.*

object FileCore {

    const val KEY_ARGUMENT_PATH = "path"
    const val KEY_ARGUMENT_APP_BAR_TITLE = "appBarTitle"

    const val KEY_INTERNAL_STORAGE = "internal"
    const val KEY_EXTERNAL_STORAGE = "external"

    const val MESSAGE_ILLEGAL_STATE_INTERNAL = "illegalStateInternal"

    @Volatile var externalRootPaths: MutableList<String>? = null
    @Volatile var findingExternalRootsInProgress = false
    @Volatile var outerIntentInProgress = false

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

        }
    }

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

    suspend fun findExternalRoots(context: Context) : MutableList<String> {
        return if(externalRootPaths != null) {
            externalRootPaths!!
        } else {

            if(!findingExternalRootsInProgress) {
                findingExternalRootsInProgress = true

                val cursor: Cursor = context.contentResolver.query(
                        MediaStore.Files.getContentUri("external"), arrayOf(
                        MediaStore.Files.FileColumns.DATA
                ), null, null, null, null
                )!!

                if (cursor.moveToFirst()) {
                    if(getInternalRootPath() != MESSAGE_ILLEGAL_STATE_INTERNAL) {

                        findingExternalRootsInProgress = true

                        var shortestPathLength = 100000000

                        externalRootPaths = mutableListOf()

                        val internalRootFile = File(getInternalRootPath())

                        while (!cursor.isAfterLast) {
                            val data = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))

                            if (!data.contains(internalRootFile.absolutePath, true)) {
                                if(!internalRootFile.absolutePath.contains(data, true)) {
                                    val splitData = data.split(File.separator)

                                    if (splitData.size < shortestPathLength) {
                                        shortestPathLength = splitData.size

                                        File(data).apply {
                                            if (isDirectory && totalSpace != internalRootFile.totalSpace && !data.contains(internalRootFile.parent!!, true))
                                                externalRootPaths!!.add(data)
                                        }

                                    }
                                }
                            }

                            cursor.moveToNext()
                        }

                    }

                }

                cursor.close()

                findingExternalRootsInProgress = false

                externalRootPaths!!
            } else {
                val timeOut = System.currentTimeMillis() + 20000

                while(findingExternalRootsInProgress && System.currentTimeMillis() < timeOut) {
                    delay(25)
                }

                if(externalRootPaths != null) {

                    externalRootPaths!!
                } else {
                    throw IllegalStateException("Something went wrong while fetching audios from MediaStore")
                }

            }
        }


    }

}