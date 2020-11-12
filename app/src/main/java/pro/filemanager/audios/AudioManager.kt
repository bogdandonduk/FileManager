package pro.filemanager.audios

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import pro.filemanager.ApplicationLoader

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

        var loadedAudios: MutableList<AudioItem>? = null
        var loadingInProgress = false

        @SuppressLint("NewApi")
        fun loadAudios(context: Context) {
            if((Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) || (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                loadingInProgress = true

                val audioManager = AudioManager()

                loadedAudios = audioManager.fetch()

                audioManager.closeCursor()

                loadingInProgress = false
            }
        }

    }

}