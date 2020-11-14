package pro.filemanager.docs

import android.annotation.SuppressLint
import android.content.Context
import android.database.MergeCursor
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.delay
import pro.filemanager.ApplicationLoader

class DocRepo private constructor() {

    companion object {
        @Volatile private var instance: DocRepo? = null

        fun getInstance() : DocRepo {
            return if(instance != null) {
                instance!!
            } else {
                instance = DocRepo()
                instance!!
            }
        }

    }

    @Volatile private var loadedItemsLive: MutableLiveData<MutableList<DocItem>>? = null
    @Volatile private var loadingInProgress = false

    @SuppressLint("Recycle")
    suspend fun loadLive(context: Context = ApplicationLoader.appContext) : MutableLiveData<MutableList<DocItem>> {
        return if(loadedItemsLive != null) {
            loadedItemsLive!!
        } else {
            if(!loadingInProgress) {
                loadingInProgress = true

                val cursor = MergeCursor(arrayOf(
                    context.contentResolver.query(MediaStore.Files.getContentUri("external"), arrayOf(
                        MediaStore.Files.FileColumns.DATA,
                        MediaStore.Files.FileColumns.DISPLAY_NAME,
                        MediaStore.Files.FileColumns.SIZE
                    ), MediaStore.Files.FileColumns.MIME_TYPE + "=?",
                        arrayOf(
                            MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
                        ), null, null)!!,
                    context.contentResolver.query(MediaStore.Files.getContentUri("external"), arrayOf(
                        MediaStore.Files.FileColumns.DATA,
                        MediaStore.Files.FileColumns.DISPLAY_NAME,
                        MediaStore.Files.FileColumns.SIZE
                    ), MediaStore.Files.FileColumns.MIME_TYPE + "=?",
                        arrayOf(
                            MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc")
                        ), null, null)!!,
                    context.contentResolver.query(MediaStore.Files.getContentUri("external"), arrayOf(
                        MediaStore.Files.FileColumns.DATA,
                        MediaStore.Files.FileColumns.DISPLAY_NAME,
                        MediaStore.Files.FileColumns.SIZE
                    ), MediaStore.Files.FileColumns.MIME_TYPE + "=?",
                        arrayOf(
                            MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx")
                        ), null, null)!!,
                    context.contentResolver.query(MediaStore.Files.getContentUri("external"), arrayOf(
                        MediaStore.Files.FileColumns.DATA,
                        MediaStore.Files.FileColumns.DISPLAY_NAME,
                        MediaStore.Files.FileColumns.SIZE
                    ), MediaStore.Files.FileColumns.MIME_TYPE + "=?",
                        arrayOf(
                            MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt")
                        ), null, null)!!,
                    context.contentResolver.query(MediaStore.Files.getContentUri("external"), arrayOf(
                        MediaStore.Files.FileColumns.DATA,
                        MediaStore.Files.FileColumns.DISPLAY_NAME,
                        MediaStore.Files.FileColumns.SIZE
                    ), MediaStore.Files.FileColumns.MIME_TYPE + "=?",
                        arrayOf(
                            MimeTypeMap.getSingleton().getMimeTypeFromExtension("xml")
                        ), null, null)!!
                ))

                val docItems: MutableList<DocItem> = mutableListOf()

                if(cursor.moveToFirst()) {
                    while(!cursor.isAfterLast) {
                        docItems.add(
                            DocItem(
                                cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)),
                                cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)),
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE))
                            )
                        )

                        cursor.moveToNext()
                    }

                }

                cursor.close()

                loadedItemsLive = MutableLiveData(docItems)

                loadingInProgress = false

                loadedItemsLive!!
            } else {
                while(loadingInProgress) {
                    delay(25)
                }

                if(loadedItemsLive != null) {
                    loadedItemsLive!!
                } else {
                    throw IllegalStateException("Something went wrong while fetching audios from MediaStore")
                }

            }
        }

    }

}