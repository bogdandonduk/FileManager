package pro.filemanager.videos

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import pro.filemanager.ApplicationLoader
import pro.filemanager.R
import java.io.File
import java.lang.Exception

class VideoManager() {
    private var cursor: Cursor = ApplicationLoader.context.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, arrayOf(
            MediaStore.Video.VideoColumns.DATA,
            MediaStore.Video.VideoColumns.DISPLAY_NAME,
            MediaStore.Video.VideoColumns.SIZE,
            MediaStore.Video.VideoColumns.DATE_MODIFIED
    ), null, null, null)!!

    fun fetch() : MutableList<VideoItem> {
        val items: MutableList<VideoItem> = mutableListOf()

        if(cursor.moveToFirst()) {
            while(!cursor.isAfterLast) {
                items.add(VideoItem(
                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns.SIZE)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_MODIFIED))
                ))

                cursor.moveToNext()
            }
        }

        return items
    }

    fun closeCursor() {
        cursor.close()
    }

    companion object {
        const val MIME_TYPE = "video/*"

        val glideVideoRequestBuilder = Glide.with(ApplicationLoader.context)
                .asBitmap()
                .load(R.drawable.placeholder_image_video_item_empty)
                .placeholder(R.drawable.placeholder_image_video_item)
                .thumbnail(0.5f)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()

        var preloadedVideos: MutableList<VideoItem>? = null
        var preloadingVideosInProgress = false

        @SuppressLint("NewApi")
        fun preloadVideos(context: Context) {
            if((Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) || (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                preloadingVideosInProgress = true

                val videoManager = VideoManager()

                preloadedVideos = videoManager.fetch()

                videoManager.closeCursor()

                preloadingVideosInProgress = false

            }
        }

        fun openInOtherVideoPlayer(context: Context, path: String) {

            val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(path))

            val uri: Uri =
                    try {
                        FileProvider.getUriForFile(context, context.packageName + ".fileProvider", File(path))
                    } catch (e: Exception) {
                        Uri.parse(path)
                    }

            val intent = Intent(Intent.ACTION_VIEW)

            intent.setDataAndType(uri, type)
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

            if(intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // take user to google play for video player
            }
        }
    }
}