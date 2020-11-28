package pro.filemanager.audio

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import kotlinx.coroutines.delay
import pro.filemanager.audio.albums.AudioAlbumItem
import java.io.File

/**

 * Audio Repository Singleton that fetches MediaStore.Audios tables or returns previously fetched unchanged ones.
 * It may also further split fetched images (items) into albums.
 * Fetching is intentionally performed in a coroutine during early ApplicationLoader creation - the foundation for speed of this application.
 * Any updates to fetched items, especially those coming from FileObserver, are pushed to Subscriber ViewModels. They further update UI with LiveData.

 */

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

    @Volatile private var itemSubscribers: MutableList<ItemSubscriber> = mutableListOf()
    @Volatile private var albumSubscribers: MutableList<AlbumSubscriber> = mutableListOf()
    @Volatile private var loadedItems: MutableList<AudioItem>? = null
    @Volatile private var loadedAlbums: MutableList<AudioAlbumItem> ?= null
    @Volatile private var loadingItemsInProgress = false
    @Volatile private var loadingAlbumsInProgress = false

    // interface for pushing updates to loadedItems to subscriber (basically ViewModels)
    interface ItemSubscriber {
        fun onUpdate(items: MutableList<AudioItem>)
    }

    // interface for pushing updates to loadedAlbums to subscriber (basically ViewModels)
    interface AlbumSubscriber {
        fun onUpdate(items: MutableList<AudioAlbumItem>)
    }

    private fun notifyItemSubscribers(items: MutableList<AudioItem>) {
        itemSubscribers.forEach {
            it.onUpdate(items)
        }
    }

    private fun notifyAlbumSubscribers(items: MutableList<AudioAlbumItem>) {
        albumSubscribers.forEach {
            it.onUpdate(items)
        }
    }

    fun subscribe(subscriber: ItemSubscriber) {
        itemSubscribers.add(subscriber)
    }

    fun unsubscribe(subscriber: ItemSubscriber) {
        itemSubscribers.remove(subscriber)
    }

    fun subscribe(subscriber: AlbumSubscriber) {
        albumSubscribers.add(subscriber)
    }

    fun unsubscribe(subscriber: AlbumSubscriber) {
        albumSubscribers.remove(subscriber)
    }
    //

    @SuppressLint("Recycle")
    suspend fun loadItems(context: Context, forceLoad: Boolean) : MutableList<AudioItem> {
        return if(loadedItems != null && !forceLoad) {
            loadedItems!! // return items if they are already fetched and forceLoad flag is off
        } else {
            if(!loadingItemsInProgress) { // if "loading already in progress" indicator is off

                loadingItemsInProgress = true // turn "loading already in progress" indicator on

                // Block of MediaStore fetching to AudioItem objects
                val cursor: Cursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf(
                    MediaStore.Audio.AudioColumns.DATA,
                    MediaStore.Audio.AudioColumns.DISPLAY_NAME,
                    MediaStore.Audio.AudioColumns.SIZE,
                    MediaStore.Audio.AudioColumns.DATE_MODIFIED,
                    MediaStore.Audio.AudioColumns.DATE_ADDED
                ), null, null, MediaStore.Audio.AudioColumns.DATE_ADDED + " DESC", null)!!

                val audioItems: MutableList<AudioItem> = mutableListOf()

                if(cursor.moveToFirst()) {
                    while(!cursor.isAfterLast) {
                        audioItems.add(
                            AudioItem(
                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)),
                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)),
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.SIZE)),
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED)),
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_ADDED))
                            )
                        )

                        cursor.moveToNext()
                    }

                }

                cursor.close()

                loadedItems = audioItems
                //

                loadingItemsInProgress = false // turn "loading already in progress" indicator off

                notifyItemSubscribers(loadedItems!!)

                loadedItems!!
            } else {
                // this runs if "loading already in progress" indicator is on.
                // this condition is very rare to occur because this indicator is usually switched on and back off just in matter of some milliseconds (refer to block above)

                val timeout = System.currentTimeMillis() + 20000 // setting timeout of 20 seconds for possible slowest device (rare to occur)

                while(loadingItemsInProgress && System.currentTimeMillis() < timeout) {
                    delay(25) // delaying the containing coroutine, waiting out "loading already in progress" indicator or timeout going off. 40 cycles in a second
                }

                if(loadedItems != null) {
                    notifyItemSubscribers(loadedItems!!)

                    loadedItems!! // return items when they are ready from previous fetching that we were waiting out
                } else {
                    throw IllegalStateException("Something went wrong while fetching audios from MediaStore") // No idea what happened. Most likely, error from MediaStore
                }

            }
        }

    }

    // refer to similar loadItems(context: Context, forceload: Boolean) method's comments for documentation
    // quite heavy operation running for about 475 ms on a new young CPU
    suspend fun loadAlbums(items: MutableList<AudioItem>, forceLoad: Boolean) : MutableList<AudioAlbumItem> {
        return if(loadedAlbums != null && !forceLoad) {
            loadedAlbums!!
        } else {
            if(!loadingAlbumsInProgress) {
                loadingAlbumsInProgress = true

                loadedAlbums = splitIntoAlbums(items)

                loadingAlbumsInProgress = false

                notifyAlbumSubscribers(loadedAlbums!!)

                loadedAlbums!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingAlbumsInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                if(loadedAlbums != null) {
                    notifyAlbumSubscribers(loadedAlbums!!)
                    loadedAlbums!!
                } else {
                    throw IllegalStateException("Something went wrong while splitting image items into albums")
                }

            }
        }
    }

    // that very thing to run when FileObserver signal comes
    suspend fun runUpdatedPipeline(context: Context) {
        loadAlbums(loadItems(context, true),true)
    }

    // algo for grouping images into albums that are just their parent folders in essence
    private fun splitIntoAlbums(items: MutableList<AudioItem>) : MutableList<AudioAlbumItem> {

        //  mediator procedure for finding paths of folders containing ImageItems
        val parentPaths: MutableList<String> = mutableListOf()

        items.forEach { imageItem ->
            val file = File(imageItem.data)

            if(!parentPaths.contains(file.parent!!)) {
                parentPaths.add(file.parent!!)
            }

        }

        // initial collecting of album items
        val audioAlbums: MutableList<AudioAlbumItem> = mutableListOf()

        parentPaths.forEach {
            audioAlbums.add(AudioAlbumItem(it))
        }

        // loop for every AlbumItem to populate its contained ImageItems
        audioAlbums.forEach { albumItem ->
            items.forEach {
                if(File(it.data).parent == albumItem.data) {
                    albumItem.containedImages.add(it)
                }
            }
        }

        return audioAlbums
    }
}