package pro.filemanager.images

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import kotlinx.coroutines.delay
import pro.filemanager.images.albums.ImageAlbumItem
import java.io.File

/**

 * Image Repository Singleton that fetches MediaStore.Images tables or returns previously fetched unchanged ones.
 * It may also further split fetched images (items) into albums.
 * Fetching is intentionally performed in a coroutine during early ApplicationLoader creation - the foundation for speed of this application.
 * Any updates to fetched items, especially those coming from FileObserver, are pushed to Subscriber ViewModels. They further update UI with LiveData.

 */

class ImageRepo private constructor() {

    companion object {
        @Volatile private var instance: ImageRepo? = null

        fun getSingleton() : ImageRepo = instance ?: ImageRepo().apply { instance = this }
    }

    @Volatile private var itemObservers = arrayOf<ItemObserver>()
    @Volatile private var albumObservers = arrayOf<AlbumObserver>()

    @Volatile private var loadedItems: Array<ImageItem>? = null
    @Volatile private var itemsSortedBySizeMax: MutableList<ImageItem>? = null
    @Volatile private var itemsSortedByNameReversed: MutableList<ImageItem>? = null

    @Volatile private var loadedAlbums: MutableList<ImageAlbumItem> ?= null

    @Volatile private var loadingItemsInProgress = false
    @Volatile private var loadingItemsSortedBySizeMaxInProgress = false
    @Volatile private var loadingItemsSortedByNameReversedInProgress = false
    @Volatile private var loadingAlbumsInProgress = false

    // interface for pushing updates to loadedItems to subscriber (basically ViewModels)
    interface ItemObserver {
        fun onUpdate(items: Array<ImageItem>)
    }

    interface AlbumObserver {
        fun onUpdate(items: Array<ImageAlbumItem>)
    }

    private fun notifyItemObservers(items: Array<ImageItem>) {
        itemObservers.forEach {
            it.onUpdate(items)
        }
    }

    private fun notifyAlbumObservers(items: Array<ImageAlbumItem>) {
        albumObservers.forEach {
            it.onUpdate(items)
        }
    }

    fun subscribe(observer: ItemObserver) {
        itemObservers[itemObservers.size] = observer
    }

    fun unsubscribe(observer: ItemObserver) {
        itemObservers.forEachIndexed { i: Int, itemObserver: ItemObserver ->
            if(itemObserver == observer) itemObservers.drop(i)
        }
    }

    fun subscribe(observer: AlbumObserver) {
        albumObservers[albumObservers.size] = observer
    }

    fun unsubscribe(observer: AlbumObserver) {
        albumObservers.forEachIndexed { i: Int, albumObserver: AlbumObserver ->
            if(albumObserver == observer) albumObservers.drop(i)
        }
    }
    //

    @SuppressLint("Recycle")
    suspend fun loadItems(context: Context, forceLoad: Boolean) : Array<ImageItem> {
        return if(loadedItems != null && !forceLoad) {
            loadedItems!! // return items if they are already fetched and forceLoad flag is off
        } else {
            if(!loadingItemsInProgress) { // if "loading already in progress" indicator is off

                loadingItemsInProgress = true // turn "loading already in progress" indicator on

                // Block of MediaStore fetching to ImageItem objects
                val cursor: Cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.SIZE,
                    MediaStore.Images.ImageColumns.DATE_MODIFIED,
                    MediaStore.Images.ImageColumns.DATE_ADDED
                ), null, null, MediaStore.Images.ImageColumns.DATE_ADDED + " DESC", null)!!

                if(cursor.moveToFirst()) {
                    val imageItems = arrayOf<ImageItem>()

                    while(!cursor.isAfterLast) {
                        imageItems[imageItems.size] = ImageItem(
                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)),
                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)),
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.SIZE)).toLong(),
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED)),
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_ADDED))
                        )

                        cursor.moveToNext()
                    }

                    loadedItems = imageItems
                }

                cursor.close()

                //

                loadingItemsInProgress = false // turn "loading already in progress" indicator off

                notifyItemObservers(loadedItems!!)

                loadedItems!!
            } else {
                // this runs if "loading already in progress" indicator is on.
                // this condition is very rare to occur because this indicator is usually switched on and back off just in matter of some milliseconds (refer to block above)

                val timeout = System.currentTimeMillis() + 20000 // setting timeout of 20 seconds for possible slowest device (rare to occur)

                while(loadingItemsInProgress && System.currentTimeMillis() < timeout) {
                    delay(25) // delaying the containing coroutine, waiting out "loading already in progress" indicator or timeout going off. 40 cycles in a second
                }

                if(loadedItems != null) {
                    notifyItemObservers(loadedItems!!)

                    loadedItems!! // return items when they are ready from previous fetching that we were waiting out
                } else {
                    throw IllegalStateException("Something went wrong while fetching audios from MediaStore") // No idea what happened. Most likely, error from MediaStore
                }

            }
        }

    }

    // refer to loadItems(context: Context, forceLoad: Boolean): MutableList<ImageItem> method for similar comments
    @SuppressLint("Recycle")
    suspend fun loadItemsBySizeMax(context: Context, forceLoad: Boolean) : MutableList<ImageItem> {
        return if(itemsSortedBySizeMax != null && !forceLoad) {
            itemsSortedBySizeMax!!
        } else {
            if(!loadingItemsSortedBySizeMaxInProgress) {

                loadingItemsSortedBySizeMaxInProgress = true

                val cursor: Cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.SIZE,
                    MediaStore.Images.ImageColumns.DATE_MODIFIED,
                    MediaStore.Images.ImageColumns.DATE_ADDED
                ), null, null, MediaStore.Images.ImageColumns.SIZE + " DESC", null)!!

                val imageItems: MutableList<ImageItem> = mutableListOf()

                if(cursor.moveToFirst()) {
                    while(!cursor.isAfterLast) {
                        imageItems.add(
                            ImageItem(
                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)),
                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)),
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.SIZE)).toLong(),
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED)),
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_ADDED))
                            )
                        )

                        cursor.moveToNext()
                    }

                }

                cursor.close()

                itemsSortedBySizeMax = imageItems
                //

                loadingItemsSortedBySizeMaxInProgress = false

                itemsSortedBySizeMax!!
            } else {

                val timeout = System.currentTimeMillis() + 20000

                while(loadingItemsSortedBySizeMaxInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                if(itemsSortedBySizeMax != null) {
                    itemsSortedBySizeMax!!
                } else {
                    throw IllegalStateException("Something went wrong while fetching audios from MediaStore")
                }

            }
        }
    }

    // refer to loadItems(context: Context, forceLoad: Boolean): MutableList<ImageItem> method for similar comments
    @SuppressLint("Recycle")
    suspend fun loadItemsByNameReversed(context: Context, forceLoad: Boolean) : MutableList<ImageItem> {
        return if(itemsSortedByNameReversed!= null && !forceLoad) {
            itemsSortedByNameReversed!!
        } else {
            if(!loadingItemsSortedByNameReversedInProgress) {
                loadingItemsSortedByNameReversedInProgress= true

                val cursor: Cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.SIZE,
                    MediaStore.Images.ImageColumns.DATE_MODIFIED,
                    MediaStore.Images.ImageColumns.DATE_ADDED
                ), null, null, MediaStore.Images.ImageColumns.DISPLAY_NAME + " DESC", null)!!

                val imageItems: MutableList<ImageItem> = mutableListOf()

                if(cursor.moveToFirst()) {
                    while(!cursor.isAfterLast) {
                        imageItems.add(
                            ImageItem(
                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)),
                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)),
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.SIZE)).toLong(),
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED)),
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_ADDED))
                            )
                        )

                        cursor.moveToNext()
                    }

                }

                cursor.close()

                itemsSortedByNameReversed = imageItems
                //

                loadingItemsSortedByNameReversedInProgress = false

                itemsSortedByNameReversed!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingItemsSortedByNameReversedInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                if(itemsSortedByNameReversed != null) {
                    itemsSortedByNameReversed!!
                } else {
                    throw IllegalStateException("Something went wrong while fetching audios from MediaStore")
                }

            }
        }

    }

    suspend fun loadItemsByNameAlphabetic(context: Context, forceLoad: Boolean) : MutableList<ImageItem> {
        return loadItemsByNameReversed(context, forceLoad).reversed() as MutableList<ImageItem>
    }

    suspend fun loadItemsBySizeMin(context: Context, forceLoad: Boolean) : MutableList<ImageItem> {
        return loadItemsBySizeMax(context, forceLoad).reversed() as MutableList<ImageItem>
    }

    suspend fun loadItemsByDateOldest(context: Context, forceLoad: Boolean) : MutableList<ImageItem> {
        return loadItems(context, forceLoad).reversed() as MutableList<ImageItem>
    }

    // refer to similar loadItems(context: Context, forceLoad: Boolean) : MutableList<ImageItems> method's similar comments
    // quite heavy operation running for about 475 ms on a new young CPU
    suspend fun loadAlbums(items: MutableList<ImageItem>, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(loadedAlbums != null && !forceLoad) {
            loadedAlbums!!
        } else {
            if(!loadingAlbumsInProgress) {
                loadingAlbumsInProgress = true

                loadedAlbums = splitIntoAlbums(items)

                loadingAlbumsInProgress = false

                notifyAlbumObservers(loadedAlbums!!)

                loadedAlbums!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingAlbumsInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                if(loadedAlbums != null) {
                    notifyAlbumObservers(loadedAlbums!!)
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
    private fun splitIntoAlbums(imageItems: MutableList<ImageItem>) : MutableList<ImageAlbumItem> {

        //  mediator procedure for finding paths of folders containing ImageItems
        val parentPaths: MutableList<String> = mutableListOf()

        imageItems.forEach { imageItem ->
            val file = File(imageItem.data)

            if(!parentPaths.contains(file.parent!!)) {
                parentPaths.add(file.parent!!)
            }

        }

        // initial collecting of album items
        val audioAlbums: MutableList<ImageAlbumItem> = mutableListOf()

        parentPaths.forEach {
            audioAlbums.add(ImageAlbumItem(it))
        }

        // loop for every AlbumItem to populate its contained ImageItems
        audioAlbums.forEach { albumItem ->
            imageItems.forEach {
                if(File(it.data).parent == albumItem.data) {
                    albumItem.containedImages.add(it)
                }
            }
        }

        return audioAlbums
    }
}