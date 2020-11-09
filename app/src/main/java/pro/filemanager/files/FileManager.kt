package pro.filemanager.files

import android.content.Context
import android.database.Cursor
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import pro.filemanager.ApplicationLoader
import pro.filemanager.R
import java.io.File
import java.sql.SQLData
import java.util.*

class FileManager {

    companion object {
        var internalRootPath = Environment.getExternalStorageDirectory().toString()

        var externalRootPath = ""

        var isFindingExternalRoot = false

        fun findExternalRoot(context: Context) {
            val cursor: Cursor = context.contentResolver.query(MediaStore.Files.getContentUri("external"), arrayOf(
                    MediaStore.Files.FileColumns.DATA
            ), null, null, null, null)!!

            if(cursor.moveToFirst()) {

                externalRootPath = ""

                val rootSplit = internalRootPath.split(File.separator)
                var rootSearchComplete = false

                isFindingExternalRoot = true

                while(!cursor.isAfterLast) {
                    val dataSplit = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)).split(File.separator)

                    dataSplit.forEachIndexed { i: Int, s: String ->
                        if (!rootSearchComplete && i < rootSplit.size) {
                            if (s.toLowerCase(Locale.ROOT) != rootSplit[i].toLowerCase(Locale.ROOT)) {
                                externalRootPath = externalRootPath + s + File.separator
                                rootSearchComplete = true

                            } else {
                                externalRootPath = externalRootPath + s + File.separator

                            }
                        }
                    }

                    cursor.moveToNext()

                }

                cursor.close()

                Log.d("TAG", "findExternalRoot: " + externalRootPath)

                isFindingExternalRoot = false
            }

        }
    }

}