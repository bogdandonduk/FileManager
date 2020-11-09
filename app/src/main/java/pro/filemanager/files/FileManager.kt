package pro.filemanager.files

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import pro.filemanager.ApplicationLoader

class FileManager() {
    private var cursor: Cursor = ApplicationLoader.context.contentResolver.query(MediaStore.Files.getContentUri("external"), arrayOf(
            MediaStore.Files.FileColumns.DATA
    ), null, null, null)!!

    fun fetch() : MutableList<FileItem> {
        val items: MutableList<FileItem> = mutableListOf()

        if(cursor.moveToFirst()) {

            while(!cursor.isAfterLast) {

                items.add(
                        FileItem(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)))
                )

                cursor.moveToNext()
            }

        }

//        findRoots(items)

        return items
    }

    fun findRoots(fileItems: MutableList<FileItem>) {

        if(!fileItems.isNullOrEmpty()) {

            val possibleRoots: MutableList<String> = mutableListOf()

            possibleRoots.add(fileItems[0].data)

            fileItems.forEach {
                if(it.data.split('/').size > possibleRoots[0].split('/').size) {
                    possibleRoots[0] = it.data
                }
            }
        }
    }

    fun closeCursor() {
        cursor.close()
    }

    companion object {

        var preloadedFiles: MutableList<FileItem>? = null
        var preloadingInProgress = false

        @SuppressLint("NewApi")
        fun preloadFiles(context: Context) {
            if((Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) || (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                preloadingInProgress = true

                val fileManager = FileManager()

                preloadedFiles = fileManager.fetch()

                fileManager.closeCursor()

                preloadingInProgress = false

            }
        }

    }

}