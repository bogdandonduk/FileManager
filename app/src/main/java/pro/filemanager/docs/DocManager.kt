package pro.filemanager.docs

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.MergeCursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Job
import pro.filemanager.ApplicationLoader
import pro.filemanager.audios.AudioItem
import java.io.File
import java.lang.Exception

class DocManager() {
    private var cursor: MergeCursor = MergeCursor(arrayOf(
            ApplicationLoader.context.contentResolver.query(MediaStore.Files.getContentUri("external"), arrayOf(
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.SIZE
            ), MediaStore.Files.FileColumns.MIME_TYPE + "=?",
                    arrayOf(
                            MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
                    ), null, null)!!,
            ApplicationLoader.context.contentResolver.query(MediaStore.Files.getContentUri("external"), arrayOf(
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.SIZE
            ), MediaStore.Files.FileColumns.MIME_TYPE + "=?",
                    arrayOf(
                            MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc")
                    ), null, null)!!,
            ApplicationLoader.context.contentResolver.query(MediaStore.Files.getContentUri("external"), arrayOf(
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.SIZE
            ), MediaStore.Files.FileColumns.MIME_TYPE + "=?",
                    arrayOf(
                            MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx")
                    ), null, null)!!,
            ApplicationLoader.context.contentResolver.query(MediaStore.Files.getContentUri("external"), arrayOf(
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.SIZE
            ), MediaStore.Files.FileColumns.MIME_TYPE + "=?",
                    arrayOf(
                            MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt")
                    ), null, null)!!,
            ApplicationLoader.context.contentResolver.query(MediaStore.Files.getContentUri("external"), arrayOf(
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.SIZE
            ), MediaStore.Files.FileColumns.MIME_TYPE + "=?",
                    arrayOf(
                            MimeTypeMap.getSingleton().getMimeTypeFromExtension("xml")
                    ), null, null)!!
    ))

    fun fetch() : MutableList<DocItem> {
        val items: MutableList<DocItem> = mutableListOf()

        if(cursor.moveToFirst()) {
            while(!cursor.isAfterLast) {
                items.add(DocItem(
                        cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE))
                ))

                cursor.moveToNext()
            }
        }

        return items
    }

    fun closeCursor() {
        cursor.close()
    }

    companion object {

        var preloadedDocs: MutableList<DocItem>? = null
        var preloadingInProgress = false

        @SuppressLint("NewApi")
        fun loadDocs(context: Context) {
            if((Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) || (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                preloadingInProgress = true

                val docManager = DocManager()

                preloadedDocs = docManager.fetch()

                docManager.closeCursor()

                preloadingInProgress = false

            }
        }

    }

}