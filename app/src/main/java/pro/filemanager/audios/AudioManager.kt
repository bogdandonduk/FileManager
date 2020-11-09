package pro.filemanager.audios

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
import pro.filemanager.ApplicationLoader
import java.io.File
import java.lang.Exception

class AudioManager() {
    private var cursor: Cursor = ApplicationLoader.context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf(
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.SIZE
    ), null, null, null)!!

    fun fetch() : MutableList<AudioItem> {
        val items: MutableList<AudioItem> = mutableListOf()

        if(cursor.moveToFirst()) {
            while(!cursor.isAfterLast) {
                items.add(AudioItem(
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.SIZE))
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

        var preloadedAudios: MutableList<AudioItem>? = null
        var preloadingInProgress = false

        @SuppressLint("NewApi")
        fun preloadAudios(context: Context) {
            if((Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) || (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                preloadingInProgress = true

                val audioManager = AudioManager()

                preloadedAudios = audioManager.fetch()

                audioManager.closeCursor()

                preloadingInProgress = false
            }
        }

        fun openInOtherMusicPlayer(context: Context, path: String) {
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