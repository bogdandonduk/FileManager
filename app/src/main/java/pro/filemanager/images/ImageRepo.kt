package pro.filemanager.images

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.MergeCursor
import android.os.Build
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.delay
import pro.filemanager.ApplicationLoader
import pro.filemanager.R
import pro.filemanager.docs.DocItem

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

    @Volatile private var loadedItemsLive: MutableLiveData<MutableList<ImageItem>>? = null
    @Volatile private var loadingInProgress = false

    @SuppressLint("Recycle")
    suspend fun loadLive(context: Context = ApplicationLoader.appContext) : MutableLiveData<MutableList<ImageItem>> {
        return if(loadedItemsLive != null) {
            loadedItemsLive!!
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

                loadedItemsLive = MutableLiveData(imageItems)

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