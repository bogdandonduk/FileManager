package pro.filemanager.videos

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import kotlinx.coroutines.delay
import pro.filemanager.ApplicationLoader

class VideoRepo private constructor() {

    companion object {
        @Volatile private var instance: VideoRepo? = null

        fun getInstance() : VideoRepo {
            return if(instance != null) {
                instance!!
            } else {
                instance = VideoRepo()
                instance!!
            }
        }
    }

    @Volatile private var subscribers: MutableList<RepoSubscriber> = mutableListOf()
    @Volatile private var loadedItems: MutableList<VideoItem>? = null
    @Volatile private var loadingInProgress = false

    interface RepoSubscriber {
        fun onUpdate(items: MutableList<VideoItem>)
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
    suspend fun loadItems(context: Context = ApplicationLoader.appContext) : MutableList<VideoItem> {
        return if(loadedItems != null) {
            loadedItems!!
        } else {
            if(!loadingInProgress) {
                loadingInProgress = true

                val cursor: Cursor = context.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, arrayOf(
                        MediaStore.Video.VideoColumns.DATA,
                        MediaStore.Video.VideoColumns.DISPLAY_NAME,
                        MediaStore.Video.VideoColumns.SIZE,
                        MediaStore.Video.VideoColumns.DATE_MODIFIED,
                ), null, null, null, null)!!

                val videoItems: MutableList<VideoItem> = mutableListOf()

                if(cursor.moveToFirst()) {
                    while(!cursor.isAfterLast) {
                        videoItems.add(
                                VideoItem(
                                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA)),
                                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME)),
                                        cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns.SIZE)),
                                        cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_MODIFIED))
                                )
                        )

                        cursor.moveToNext()
                    }

                }

                cursor.close()

                loadedItems = videoItems

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

    suspend fun reloadItems(context: Context = ApplicationLoader.appContext) : MutableList<VideoItem>  {
        val timeOut = System.currentTimeMillis() + 20000

        while(loadingInProgress && System.currentTimeMillis() < timeOut) {
            delay(25)
        }

        loadingInProgress = true

        val cursor: Cursor = context.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, arrayOf(
                MediaStore.Video.VideoColumns.DATA,
                MediaStore.Video.VideoColumns.DISPLAY_NAME,
                MediaStore.Video.VideoColumns.SIZE,
                MediaStore.Video.VideoColumns.DATE_MODIFIED
        ), null, null, null, null)!!

        val videoItems: MutableList<VideoItem> = mutableListOf()

        if(cursor.moveToFirst()) {
            while(!cursor.isAfterLast) {
                videoItems.add(VideoItem(
                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns.SIZE)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_MODIFIED))
                ))

                cursor.moveToNext()
            }

        }

        cursor.close()

        loadedItems = videoItems

        loadingInProgress = false

        notifySubscribers()

        return loadedItems!!
    }
}