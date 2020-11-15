package pro.filemanager.images

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import kotlinx.coroutines.delay
import pro.filemanager.ApplicationLoader

class ImageRepo private constructor() {

    companion object {
        @Volatile private var instance: ImageRepo? = null

        fun getInstance() : ImageRepo {
            return if(instance != null) {
                instance!!
            } else {
                instance = ImageRepo()
                instance!!
            }
        }
    }

    @Volatile private var subscribers: MutableList<RepoSubscriber> = mutableListOf()
    @Volatile private var loadedItems: MutableList<ImageItem>? = null
    @Volatile private var loadingInProgress = false

    interface RepoSubscriber {
        fun onUpdate(items: MutableList<ImageItem>)
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
    suspend fun loadItems(context: Context = ApplicationLoader.appContext) : MutableList<ImageItem> {
        return if(loadedItems != null) {
            loadedItems!!
        } else {
            if(!loadingInProgress) {
                loadingInProgress = true

                val cursor: Cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.SIZE,
                    MediaStore.Images.ImageColumns.DATE_MODIFIED,
                ), null, null, null, null)!!

                val imageItems: MutableList<ImageItem> = mutableListOf()

                if(cursor.moveToFirst()) {
                    while(!cursor.isAfterLast) {
                        imageItems.add(
                            ImageItem(
                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)),
                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)),
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.SIZE)),
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED))
                            )
                        )

                        cursor.moveToNext()
                    }

                }

                cursor.close()

                loadedItems = imageItems

                loadingInProgress = false

                loadedItems!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingInProgress && System.currentTimeMillis() < timeout) {
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

    suspend fun reloadItems(context: Context = ApplicationLoader.appContext) : MutableList<ImageItem>  {
        val timeOut = System.currentTimeMillis() + 20000

        while(loadingInProgress && System.currentTimeMillis() < timeOut) {
            delay(25)
        }

        loadingInProgress = true

        val cursor: Cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.DISPLAY_NAME,
                MediaStore.Images.ImageColumns.SIZE,
                MediaStore.Images.ImageColumns.DATE_MODIFIED
        ), null, null, null, null)!!

        val imageItems: MutableList<ImageItem> = mutableListOf()

        if(cursor.moveToFirst()) {
            while(!cursor.isAfterLast) {
                imageItems.add(ImageItem(
                        cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.SIZE)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED))
                ))

                cursor.moveToNext()
            }

        }

        cursor.close()

        loadedItems = imageItems

        loadingInProgress = false

        notifySubscribers()

        return loadedItems!!
    }

}