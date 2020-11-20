package pro.filemanager.docs

import android.annotation.SuppressLint
import android.content.Context
import android.database.MergeCursor
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.delay
import pro.filemanager.ApplicationLoader
import pro.filemanager.images.ImageItem

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

    @Volatile private var subscribers: MutableList<RepoSubscriber> = mutableListOf()
    @Volatile private var loadedItems: MutableList<DocItem>? = null
    @Volatile private var loadingInProgress = false

    interface RepoSubscriber {
        fun onUpdate(items: MutableList<DocItem>)
    }

    private fun notifySubscribers() {
        subscribers.forEach {
            it.onUpdate(loadedItems!!)
        }
    }

    fun subscribe(subscriber: RepoSubscriber) {
        subscribers.add(subscriber)
    }

    fun unsubscribe(subscriber: RepoSubscriber) {
        subscribers.remove(subscriber)
    }

    @SuppressLint("Recycle")
    suspend fun loadItems(context: Context = ApplicationLoader.appContext) : MutableList<DocItem> {
        return if(loadedItems != null) {
            loadedItems!!
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

                loadedItems = docItems

                loadingInProgress = false

                loadedItems!!
            } else {
                while(loadingInProgress) {
                    delay(25)
                }

                if(loadedItems != null) {
                    loadedItems!!
                } else {
                    throw IllegalStateException("Something went wrong while fetching audios from MediaStore")
                }

            }
        }

    }

    @SuppressLint("Recycle")
    suspend fun reloadItems(context: Context = ApplicationLoader.appContext) : MutableList<DocItem> {
        val timeOut = System.currentTimeMillis() + 20000

        while(loadingInProgress && System.currentTimeMillis() < timeOut) {
            delay(25)
        }

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

        loadedItems = docItems

        loadingInProgress = false

        notifySubscribers()

        return loadedItems!!
    }

}