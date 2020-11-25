package pro.filemanager.docs

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.database.MergeCursor
import android.provider.MediaStore
import android.util.Log
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

                val cursor: Cursor = context.contentResolver.query(MediaStore.Files.getContentUri("external"), arrayOf(
                        MediaStore.Files.FileColumns.DATA,
                        MediaStore.Files.FileColumns.DISPLAY_NAME,
                        MediaStore.Files.FileColumns.SIZE,
                        MediaStore.Files.FileColumns.DATE_ADDED,
                ), null, null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC", null)!!

                val docItems: MutableList<DocItem> = mutableListOf()

                if(cursor.moveToFirst()) {
                    while(!cursor.isAfterLast) {
                        if(
                           cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)).endsWith(".pdf", true ) ||
                           cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)).endsWith(".doc", true ) ||
                           cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)).endsWith(".docx", true ) ||
                           cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)).endsWith(".xls", true ) ||
                           cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)).endsWith(".xlsx", true ) ||
                           cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)).endsWith(".ppt", true ) ||
                           cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)).endsWith(".pptx", true ) ||
                           cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)).endsWith(".txt", true ) ||
                           cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)).endsWith(".html", true ) ||
                           cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)).endsWith(".xml", true ) ||
                           cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)).endsWith(".json", true )
                        ) {
                                docItems.add(
                                        DocItem(
                                                cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)),
                                                cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)),
                                                cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE))
                                        )
                                )
                        }

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

        val cursor: Cursor = context.contentResolver.query(MediaStore.Files.getContentUri("external"), arrayOf(
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_ADDED,
        ), null, null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC", null)!!

        val docItems: MutableList<DocItem> = mutableListOf()

        if(cursor.moveToFirst()) {
            while(!cursor.isAfterLast) {
                if(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)).endsWith(".pdf", true )) {
                    docItems.add(
                            DocItem(
                                    cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)),
                                    cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)),
                                    cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE))
                            )
                    )
                }

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