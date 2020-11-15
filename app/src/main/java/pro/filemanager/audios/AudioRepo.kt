package pro.filemanager.audios

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
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

    @Volatile private var subscribers: MutableList<RepoSubscriber> = mutableListOf()
    @Volatile private var loadedItems: MutableList<AudioItem>? = null
    @Volatile private var loadingInProgress = false

    interface RepoSubscriber {
        fun onUpdate(items: MutableList<AudioItem>)
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

    suspend fun loadItems(context: Context = ApplicationLoader.appContext) : MutableList<AudioItem> {
        return if(loadedItems != null) {

            loadedItems!!
        } else {
            if(!loadingInProgress) {

                loadingInProgress = true

                val cursor: Cursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf(
                        MediaStore.Audio.AudioColumns.DATA,
                        MediaStore.Audio.AudioColumns.DISPLAY_NAME,
                        MediaStore.Audio.AudioColumns.SIZE
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

                loadedItems = audioItems

                loadingInProgress = false

                loadedItems!!
            } else {
                val timeOut = System.currentTimeMillis() + 20000

                while(loadingInProgress && System.currentTimeMillis() < timeOut) {
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

    suspend fun reloadItems(context: Context = ApplicationLoader.appContext) : MutableList<AudioItem>  {
        val timeOut = System.currentTimeMillis() + 20000

        while(loadingInProgress && System.currentTimeMillis() < timeOut) {
            delay(25)
        }

        loadingInProgress = true

        val cursor: Cursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf(
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.DISPLAY_NAME,
                MediaStore.Audio.AudioColumns.SIZE
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

        loadedItems = audioItems

        loadingInProgress = false

        notifySubscribers()

        return loadedItems!!
    }
}