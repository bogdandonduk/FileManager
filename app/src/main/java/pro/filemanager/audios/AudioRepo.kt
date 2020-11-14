package pro.filemanager.audios

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.delay
import pro.filemanager.ApplicationLoader
import java.lang.IllegalStateException

class AudioRepo private constructor() {

    companion object {
        @Volatile private var instance: AudioRepo? = null

        fun getInstance() : AudioRepo {
            return if(instance != null) {
                instance!!
            } else {
                instance = AudioRepo()
                instance!!
            }
        }

    }

    @Volatile var loadedItemsLive: MutableLiveData<MutableList<AudioItem>>? = null
    @Volatile private var loadingInProgress = false


    suspend fun loadLive(context: Context = ApplicationLoader.appContext) : MutableLiveData<MutableList<AudioItem>> {
        return if(loadedItemsLive != null) {

            loadedItemsLive!!
        } else {
            if(!loadingInProgress) {

                loadingInProgress = true

                val cursor: Cursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf(
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.SIZE
                ), null, null, null, null)!!

                val audioItems: MutableList<AudioItem> = mutableListOf()

                if(cursor.moveToFirst()) {
                    while(!cursor.isAfterLast) {
                        audioItems.add(AudioItem(
                                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)),
                                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME)),
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.SIZE))
                        ))

                        cursor.moveToNext()
                    }

                }

                cursor.close()

                loadedItemsLive = MutableLiveData(audioItems)

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