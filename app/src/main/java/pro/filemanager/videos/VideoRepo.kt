package pro.filemanager.videos

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.delay
import pro.filemanager.ApplicationLoader
import pro.filemanager.R

class VideoRepo private constructor() {

    companion object {
        const val MIME_TYPE = "video/*"

        val glideRequestBuilder = Glide.with(ApplicationLoader.appContext)
            .asBitmap()
            .placeholder(R.drawable.placeholder_image_video_item)
            .thumbnail(0.5f)
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()

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

    @Volatile private var loadedItemsLive: MutableLiveData<MutableList<VideoItem>>? = null
    @Volatile private var loadingInProgress = false

    @SuppressLint("Recycle")
    suspend fun loadLive(context: Context = ApplicationLoader.appContext) : MutableLiveData<MutableList<VideoItem>> {
        return if(loadedItemsLive != null) {
            loadedItemsLive!!
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

                loadedItemsLive = MutableLiveData(videoItems)

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