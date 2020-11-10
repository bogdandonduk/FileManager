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

                isFindingExternalRoot = true

                var shortestPathLength = 1000000

                while(!cursor.isAfterLast) {
                    val data = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))

                    if(!data.toLowerCase(Locale.ROOT).contains(internalRootPath.toLowerCase(Locale.ROOT))) {
                        if(!internalRootPath.toLowerCase(Locale.ROOT).contains(data)) {
                            val splitData = data.split(File.separator)

                            if(splitData.size < shortestPathLength) {
                                shortestPathLength = splitData.size
                                externalRootPath = data
                            }
                        }
                    }

                    cursor.moveToNext()
                }

                cursor.close()

                isFindingExternalRoot = false
            }

        }
    }

}