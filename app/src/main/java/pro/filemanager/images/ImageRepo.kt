package pro.filemanager.images

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pro.filemanager.core.tools.sort.SortTool
import pro.filemanager.images.albums.ImageAlbumItem
import java.io.File
import java.util.*

/**

 * Image Repository Singleton that fetches MediaStore.Images tables or returns previously fetched unchanged ones.
 * It may also further split fetched images (items) into albums.
 * Fetching is intentionally performed in a coroutine during early ApplicationLoader creation - the foundation for speed of this application.
 * Any updates to fetched items, especially those coming from FileObserver, are pushed to Subscriber ViewModels. They further update UI with LiveData.

 */

class ImageRepo private constructor() {

    companion object {
        @Volatile private var instance: ImageRepo? = null

        fun getSingleton() : ImageRepo {
            if(instance == null) {
                instance = ImageRepo()
            }

            return instance!!
        }
    }

    @Volatile private var itemObservers = mutableListOf<ItemObserver>()
    @Volatile private var albumObservers = mutableListOf<AlbumObserver>()

    @Volatile private var loadedItemsSortedByDateRecent: MutableList<ImageItem>? = null
    @Volatile private var loadedItemsSortedBySizeLargest: MutableList<ImageItem>? = null
    @Volatile private var loadedItemsSortedByNameReversed: MutableList<ImageItem>? = null

    @Volatile private var loadingItemsSortedByDateRecentInProgress = false
    @Volatile private var loadingItemsSortedBySizeLargestInProgress = false
    @Volatile private var loadingItemsSortedByNameReversedInProgress = false

    @Volatile private var splitAlbumsSortedByDateRecent: MutableList<ImageAlbumItem>? = null
    @Volatile private var splitAlbumsSortedByDateRecentLoadedByDateRecent: MutableList<ImageAlbumItem>? = null
    @Volatile private var splitAlbumsSortedByDateRecentLoadedBySizeLargest: MutableList<ImageAlbumItem>? = null
    @Volatile private var splitAlbumsSortedByDateRecentLoadedByNameReversed: MutableList<ImageAlbumItem>? = null

    @Volatile private var splitAlbumsSortedByDateOldest: MutableList<ImageAlbumItem>? = null
    @Volatile private var splitAlbumsSortedByDateOldestLoadedByDateRecent: MutableList<ImageAlbumItem>? = null
    @Volatile private var splitAlbumsSortedByDateOldestLoadedBySizeLargest: MutableList<ImageAlbumItem>? = null
    @Volatile private var splitAlbumsSortedByDateOldestLoadedByNameReversed: MutableList<ImageAlbumItem>? = null

    @Volatile private var splitAlbumsSortedBySizeLargest: MutableList<ImageAlbumItem>? = null
    @Volatile private var splitAlbumsSortedBySizeLargestLoadedByDateRecent: MutableList<ImageAlbumItem>? = null
    @Volatile private var splitAlbumsSortedBySizeLargestLoadedBySizeLargest: MutableList<ImageAlbumItem>? = null
    @Volatile private var splitAlbumsSortedBySizeLargestLoadedByNameReversed: MutableList<ImageAlbumItem>? = null

    @Volatile private var splitAlbumsSortedBySizeSmallest: MutableList<ImageAlbumItem>? = null
    @Volatile private var splitAlbumsSortedBySizeSmallestLoadedByDateRecent: MutableList<ImageAlbumItem>? = null
    @Volatile private var splitAlbumsSortedBySizeSmallestLoadedBySizeLargest: MutableList<ImageAlbumItem>? = null
    @Volatile private var splitAlbumsSortedBySizeSmallestLoadedByNameReversed: MutableList<ImageAlbumItem>? = null

    @Volatile private var splitAlbumsSortedByNameReversed: MutableList<ImageAlbumItem>? = null
    @Volatile private var splitAlbumsSortedByNameReversedLoadedByDateRecent: MutableList<ImageAlbumItem>? = null
    @Volatile private var splitAlbumsSortedByNameReversedLoadedBySizeLargest: MutableList<ImageAlbumItem>? = null
    @Volatile private var splitAlbumsSortedByNameReversedLoadedByNameReversed: MutableList<ImageAlbumItem>? = null

    @Volatile private var splitAlbumsSortedByNameAlphabetic: MutableList<ImageAlbumItem>? = null
    @Volatile private var splitAlbumsSortedByNameAlphabeticLoadedByDateRecent: MutableList<ImageAlbumItem>? = null
    @Volatile private var splitAlbumsSortedByNameAlphabeticLoadedBySizeLargest: MutableList<ImageAlbumItem>? = null
    @Volatile private var splitAlbumsSortedByNameAlphabeticLoadedByNameReversed: MutableList<ImageAlbumItem>? = null

    @Volatile private var splittingAlbumsSortedByDateRecentInProgress = false
    @Volatile private var loadingSplitAlbumsSortedByDateRecentByDateRecentInProgress = false
    @Volatile private var loadingSplitAlbumsSortedByDateRecentBySizeLargestInProgress = false
    @Volatile private var loadingSplitAlbumsSortedByDateRecentByNameReversedInProgress = false

    @Volatile private var splittingAlbumsSortedByDateOldestInProgress = false
    @Volatile private var loadingSplitAlbumsSortedByDateOldestByDateRecentInProgress = false
    @Volatile private var loadingSplitAlbumsSortedByDateOldestBySizeLargestInProgress = false
    @Volatile private var loadingSplitAlbumsSortedByDateOldestByNameReversedInProgress = false

    @Volatile private var splittingAlbumsSortedBySizeLargestInProgress = false
    @Volatile private var loadingSplitAlbumsSortedBySizeLargestByDateRecentInProgress = false
    @Volatile private var loadingSplitAlbumsSortedBySizeLargestBySizeLargestInProgress = false
    @Volatile private var loadingSplitAlbumsSortedBySizeLargestByNameReversedInProgress = false

    @Volatile private var splittingAlbumsSortedBySizeSmallestInProgress = false
    @Volatile private var loadingSplitAlbumsSortedBySizeSmallestByDateRecentInProgress = false
    @Volatile private var loadingSplitAlbumsSortedBySizeSmallestBySizeLargestInProgress = false
    @Volatile private var loadingSplitAlbumsSortedBySizeSmallestByNameReversedInProgress = false

    @Volatile private var splittingAlbumsSortedByNameReversedInProgress = false
    @Volatile private var loadingSplitAlbumsSortedByNameReversedByDateRecentInProgress = false
    @Volatile private var loadingSplitAlbumsSortedByNameReversedBySizeLargestInProgress = false
    @Volatile private var loadingSplitAlbumsSortedByNameReversedByNameReversedInProgress = false

    @Volatile private var splittingAlbumsSortedByNameAlphabeticInProgress = false
    @Volatile private var loadingSplitAlbumsSortedByNameAlphabeticByDateRecentInProgress = false
    @Volatile private var loadingSplitAlbumsSortedByNameAlphabeticBySizeLargestInProgress = false
    @Volatile private var loadingSplitAlbumsSortedByNameAlphabeticByNameReversedInProgress = false

    // interface for pushing updates to loadedItems to subscriber (basically ViewModels)
    interface ItemObserver {
        fun onUpdate()
    }

    private fun notifyItemObservers() {
        itemObservers.forEach {
            it.onUpdate()
        }
    }

    fun observe(observer: ItemObserver) {
        itemObservers.add(observer)
    }

    fun stopObserving(observer: ItemObserver) {
        itemObservers.remove(observer)
    }

    ///////

    interface AlbumObserver {
        fun onUpdate()
    }

    private fun notifyAlbumObservers() {
        albumObservers.forEach {
            it.onUpdate()
        }
    }

    fun observe(observer: AlbumObserver) {
        albumObservers.add(observer)
    }

    fun stopObserving(observer: AlbumObserver) {
        albumObservers.remove(observer)
    }

    //////////

    @SuppressLint("Recycle")
    suspend fun loadItemsByDateRecent(context: Context, forceLoad: Boolean) : MutableList<ImageItem> {
        return if(!loadedItemsSortedByDateRecent.isNullOrEmpty() && !forceLoad) {
            loadedItemsSortedByDateRecent!! // return items if they are already fetched and forceLoad flag is off
        } else {
            if(!loadingItemsSortedByDateRecentInProgress) { // if "loading already in progress" indicator is off
                loadingItemsSortedByDateRecentInProgress = true // turn "loading already in progress" indicator on

                // Block of MediaStore fetching to ImageItem objects
                val cursor: Cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.SIZE,
                    MediaStore.Images.ImageColumns.DATE_MODIFIED,
                    MediaStore.Images.ImageColumns.DATE_ADDED
                ), null, null, MediaStore.Images.ImageColumns.DATE_ADDED + " DESC", null)!!

                val imageItems = mutableListOf<ImageItem>()

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

                //

                loadingItemsSortedByDateRecentInProgress = false // turn "loading already in progress" indicator off

                loadedItemsSortedByDateRecent = imageItems

                loadedItemsSortedByDateRecent!!
            } else {
                // this runs if "loading already in progress" indicator is on.
                // this condition is very rare to occur because this indicator is usually switched on and back off just in matter of some milliseconds (refer to block above)

                val timeout = System.currentTimeMillis() + 20000 // setting timeout of 20 seconds for possible slowest device (rare to occur)

                while(loadingItemsSortedByDateRecentInProgress && System.currentTimeMillis() < timeout) {
                    delay(25) // delaying the containing coroutine, waiting out "loading already in progress" indicator or timeout going off. 40 cycles in a second
                }

                // return items when they are ready from previous fetching that we were waiting out
                loadedItemsSortedByDateRecent ?: throw IllegalStateException("Something went wrong while fetching audios from MediaStore") // No idea what happened. Most likely, error from MediaStore

            }
        }

    }

    // refer to loadItems(context: Context, forceLoad: Boolean): MutableList<ImageItem> method for similar comments
    @SuppressLint("Recycle")
    suspend fun loadItemsBySizeLargest(context: Context, forceLoad: Boolean) : MutableList<ImageItem> {
        return if(!loadedItemsSortedBySizeLargest.isNullOrEmpty() && !forceLoad) {
            loadedItemsSortedBySizeLargest!!
        } else {
            if(!loadingItemsSortedBySizeLargestInProgress) {
                loadingItemsSortedBySizeLargestInProgress = true

                val cursor: Cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.SIZE,
                    MediaStore.Images.ImageColumns.DATE_MODIFIED,
                    MediaStore.Images.ImageColumns.DATE_ADDED
                ), null, null, MediaStore.Images.ImageColumns.SIZE + " DESC", null)!!

                val imageItems = mutableListOf<ImageItem>()

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

                //

                loadingItemsSortedBySizeLargestInProgress = false

                loadedItemsSortedBySizeLargest = imageItems

                loadedItemsSortedBySizeLargest!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingItemsSortedBySizeLargestInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                loadedItemsSortedBySizeLargest ?: throw IllegalStateException("Something went wrong while fetching audios from MediaStore")

            }
        }
    }

    // refer to loadItems(context: Context, forceLoad: Boolean): MutableList<ImageItem> method for similar comments
    @SuppressLint("Recycle")
    suspend fun loadItemsByNameReversed(context: Context, forceLoad: Boolean) : MutableList<ImageItem> {
        return if(!loadedItemsSortedByNameReversed.isNullOrEmpty() && !forceLoad) {
            loadedItemsSortedByNameReversed!!
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

                val imageItems = mutableListOf<ImageItem>()

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
                //

                loadingItemsSortedByNameReversedInProgress = false

                loadedItemsSortedByNameReversed = imageItems

                loadedItemsSortedByNameReversed!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingItemsSortedByNameReversedInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                loadedItemsSortedByNameReversed ?: throw IllegalStateException("Something went wrong while fetching audios from MediaStore")
            }
        }

    }

    suspend fun loadItemsByNameAlphabetic(context: Context) : MutableList<ImageItem> {
        val items = loadItemsByNameReversed(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageItem> else mutableListOf()
    }

    suspend fun loadItemsBySizeSmallest(context: Context) : MutableList<ImageItem> {
        val items = loadItemsBySizeLargest(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageItem> else mutableListOf()
    }

    suspend fun loadItemsByDateOldest(context: Context) : MutableList<ImageItem> {
        val items = loadItemsByDateRecent(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageItem> else mutableListOf()
    }

    // refer to similar loadItems(context: Context, forceLoad: Boolean) : MutableList<ImageItems> method's similar comments
    // quite heavy operation running for about 475 ms on a new young CPU
    suspend fun splitAlbumsByDateRecent(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedByDateRecent.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedByDateRecent!!
        } else {
            if(!splittingAlbumsSortedByDateRecentInProgress) {
                splittingAlbumsSortedByDateRecentInProgress = true

                splitAlbumsSortedByDateRecent = splitIntoAlbums(loadItemsByDateRecent(context, false))

                splittingAlbumsSortedByDateRecentInProgress = false

                splitAlbumsSortedByDateRecent!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(splittingAlbumsSortedByDateRecentInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedByDateRecent ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsByDateRecentLoadAlbumsByDateRecent(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedByDateRecentLoadedByDateRecent.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedByDateRecentLoadedByDateRecent!!
        } else {
            if(!loadingSplitAlbumsSortedByDateRecentByDateRecentInProgress) {
                loadingSplitAlbumsSortedByDateRecentByDateRecentInProgress = true

                splitAlbumsSortedByDateRecentLoadedByDateRecent = splitAlbumsByDateRecent(context, false)

                loadingSplitAlbumsSortedByDateRecentByDateRecentInProgress = false

                splitAlbumsSortedByDateRecentLoadedByDateRecent!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitAlbumsSortedByDateRecentByDateRecentInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedByDateRecentLoadedByDateRecent ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsByDateRecentLoadAlbumsBySizeLargest(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedByDateRecentLoadedBySizeLargest.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedByDateRecentLoadedBySizeLargest!!
        } else {
            if(!loadingSplitAlbumsSortedByDateRecentBySizeLargestInProgress) {
                loadingSplitAlbumsSortedByDateRecentBySizeLargestInProgress = true

                val sizeLongs = mutableListOf<Long>()
                val finalItems = mutableListOf<ImageAlbumItem>()

                val rawItems = splitAlbumsByDateRecent(context, false).onEach { albumItem ->
                    albumItem.containedImages.forEach {
                        albumItem.totalSize += it.size
                    }
                }

                rawItems.forEach {
                    sizeLongs.add(it.totalSize)
                }

                Collections.sort(sizeLongs, Collections.reverseOrder())

                sizeLongs.forEach { size ->
                    rawItems.forEach {
                        if(it.totalSize == size) {
                            finalItems.add(it)
                        }
                    }
                }

                splitAlbumsSortedByDateRecentLoadedBySizeLargest = finalItems

                loadingSplitAlbumsSortedByDateRecentBySizeLargestInProgress = false

                splitAlbumsSortedByDateRecentLoadedBySizeLargest!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitAlbumsSortedByDateRecentBySizeLargestInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedByDateRecentLoadedBySizeLargest ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsByDateRecentLoadAlbumsByNameReversed(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedByDateRecentLoadedByNameReversed.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedByDateRecentLoadedByNameReversed!!
        } else {
            if(!loadingSplitAlbumsSortedByDateRecentByNameReversedInProgress) {
                loadingSplitAlbumsSortedByDateRecentByNameReversedInProgress = true

                val titleFiles = mutableListOf<File>()
                val finalItems = mutableListOf<ImageAlbumItem>()

                val rawItems = splitAlbumsByDateRecent(context, false)

                rawItems.forEach {
                    titleFiles.add(File(it.data.toLowerCase(Locale.ROOT)))
                }

                SortTool.sortFilesByNameReversed(titleFiles).forEach { file ->
                    rawItems.forEach {
                        if(it.data.equals(file.absolutePath, true)) {
                            finalItems.add(it)
                        }
                    }
                }

                splitAlbumsSortedByDateRecentLoadedByNameReversed = finalItems

                loadingSplitAlbumsSortedByDateRecentByNameReversedInProgress = false

                splitAlbumsSortedByDateRecentLoadedByNameReversed!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitAlbumsSortedByDateRecentByNameReversedInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedByDateRecentLoadedByNameReversed ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsByDateRecentLoadAlbumsByDateOldest(context: Context) : MutableList<ImageAlbumItem> {
        val items = splitAlbumsByDateRecentLoadAlbumsByDateRecent(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageAlbumItem> else mutableListOf()
    }

    suspend fun splitAlbumsByDateRecentLoadAlbumsBySizeSmallest(context: Context) : MutableList<ImageAlbumItem> {
        val items = splitAlbumsByDateRecentLoadAlbumsBySizeLargest(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageAlbumItem> else mutableListOf()
    }

    suspend fun splitAlbumsByDateRecentLoadAlbumsByNameAlphabetic(context: Context) : MutableList<ImageAlbumItem> {
        val items = splitAlbumsByDateRecentLoadAlbumsByNameReversed(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageAlbumItem> else mutableListOf()
    }

    suspend fun splitAlbumsByDateOldest(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedByDateOldest.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedByDateOldest!!
        } else {
            if(!splittingAlbumsSortedByDateOldestInProgress) {
                splittingAlbumsSortedByDateOldestInProgress = true

                splitAlbumsSortedByDateOldest = splitIntoAlbums(loadItemsByDateOldest(context))

                splittingAlbumsSortedByDateOldestInProgress = false

                splitAlbumsSortedByDateOldest!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(splittingAlbumsSortedByDateOldestInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedByDateOldest ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsByDateOldestLoadAlbumsByDateRecent(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedByDateOldestLoadedByDateRecent.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedByDateOldestLoadedByDateRecent!!
        } else {
            if(!loadingSplitAlbumsSortedByDateOldestByDateRecentInProgress) {
                loadingSplitAlbumsSortedByDateOldestByDateRecentInProgress = true

                val finalItems = mutableListOf<ImageAlbumItem>()

                val rawItems = splitAlbumsByDateOldest(context, false)

                splitAlbumsByDateRecent(context, false).forEach { albumItem ->
                    rawItems.forEach {
                        if(it.data == albumItem.data) {
                            finalItems.add(it)
                        }
                    }
                }

                splitAlbumsSortedByDateOldestLoadedByDateRecent = finalItems

                loadingSplitAlbumsSortedByDateOldestByDateRecentInProgress = false

                splitAlbumsSortedByDateOldestLoadedByDateRecent!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitAlbumsSortedByDateOldestByDateRecentInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedByDateOldestLoadedByDateRecent ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsByDateOldestLoadAlbumsBySizeLargest(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedByDateOldestLoadedBySizeLargest.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedByDateOldestLoadedBySizeLargest!!
        } else {
            if(!loadingSplitAlbumsSortedByDateOldestBySizeLargestInProgress) {
                loadingSplitAlbumsSortedByDateOldestBySizeLargestInProgress = true

                val sizeLongs = mutableListOf<Long>()
                val finalItems = mutableListOf<ImageAlbumItem>()

                val rawItems = splitAlbumsByDateOldest(context, false).onEach { albumItem ->
                    albumItem.containedImages.forEach {
                        albumItem.totalSize += it.size
                    }
                }

                rawItems.forEach {
                    sizeLongs.add(it.totalSize)
                }

                Collections.sort(sizeLongs, Collections.reverseOrder())

                sizeLongs.forEach { size ->
                    rawItems.forEach {
                        if(it.totalSize == size) {
                            finalItems.add(it)
                        }
                    }
                }

                splitAlbumsSortedByDateOldestLoadedBySizeLargest = finalItems

                loadingSplitAlbumsSortedByDateOldestBySizeLargestInProgress = false

                splitAlbumsSortedByDateOldestLoadedBySizeLargest!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitAlbumsSortedByDateOldestBySizeLargestInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedByDateOldestLoadedBySizeLargest ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsByDateOldestLoadAlbumsByNameReversed(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedByDateOldestLoadedByNameReversed.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedByDateOldestLoadedByNameReversed!!
        } else {
            if(!loadingSplitAlbumsSortedByDateOldestByNameReversedInProgress) {
                loadingSplitAlbumsSortedByDateOldestByNameReversedInProgress = true

                val titleFiles = mutableListOf<File>()
                val finalItems = mutableListOf<ImageAlbumItem>()

                val rawItems = splitAlbumsByDateOldest(context, false)

                rawItems.forEach {
                    titleFiles.add(File(it.data.toLowerCase(Locale.ROOT)))
                }

                SortTool.sortFilesByNameReversed(titleFiles).forEach { file ->
                    rawItems.forEach {
                        if(it.data.equals(file.absolutePath, true)) {
                            finalItems.add(it)
                        }
                    }
                }

                splitAlbumsSortedByDateOldestLoadedByNameReversed = finalItems

                loadingSplitAlbumsSortedByDateOldestByNameReversedInProgress = false

                splitAlbumsSortedByDateOldestLoadedByNameReversed!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitAlbumsSortedByDateOldestByNameReversedInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedByDateOldestLoadedByNameReversed ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsByDateOldestLoadAlbumsByDateOldest(context: Context) : MutableList<ImageAlbumItem> {
        val items = splitAlbumsByDateOldestLoadAlbumsByDateRecent(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageAlbumItem> else mutableListOf()
    }

    suspend fun splitAlbumsByDateOldestLoadAlbumsBySizeSmallest(context: Context) : MutableList<ImageAlbumItem> {
        val items = splitAlbumsByDateOldestLoadAlbumsBySizeLargest(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageAlbumItem> else mutableListOf()
    }

    suspend fun splitAlbumsByDateOldestLoadAlbumsByNameAlphabetic(context: Context) : MutableList<ImageAlbumItem> {
        val items = splitAlbumsByDateOldestLoadAlbumsByNameReversed(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageAlbumItem> else mutableListOf()
    }

    suspend fun splitAlbumsBySizeLargest(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedBySizeLargest.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedBySizeLargest!!
        } else {
            if(!splittingAlbumsSortedBySizeLargestInProgress) {
                splittingAlbumsSortedBySizeLargestInProgress = true

                splitAlbumsSortedBySizeLargest = splitIntoAlbums(loadItemsBySizeLargest(context, false))

                splittingAlbumsSortedBySizeLargestInProgress = false

                splitAlbumsSortedBySizeLargest!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(splittingAlbumsSortedBySizeLargestInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedBySizeLargest ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsBySizeLargestLoadAlbumsByDateRecent(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedBySizeLargestLoadedByDateRecent.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedBySizeLargestLoadedByDateRecent!!
        } else {
            if(!loadingSplitAlbumsSortedBySizeLargestByDateRecentInProgress) {
                loadingSplitAlbumsSortedBySizeLargestByDateRecentInProgress = true

                val rawItems = splitAlbumsBySizeLargest(context, false)

                val finalItems = mutableListOf<ImageAlbumItem>()

                splitAlbumsByDateRecent(context, false).forEach { albumItem ->
                    rawItems.forEach {
                        if(it.data == albumItem.data) {
                            finalItems.add(it)
                        }
                    }
                }

                splitAlbumsSortedBySizeLargestLoadedByDateRecent = finalItems

                loadingSplitAlbumsSortedBySizeLargestByDateRecentInProgress = false

                splitAlbumsSortedBySizeLargestLoadedByDateRecent!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitAlbumsSortedBySizeLargestByDateRecentInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedBySizeLargestLoadedByDateRecent ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsBySizeLargestLoadAlbumsBySizeLargest(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedBySizeLargestLoadedBySizeLargest.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedBySizeLargestLoadedBySizeLargest!!
        } else {
            if(!loadingSplitAlbumsSortedBySizeLargestBySizeLargestInProgress) {
                loadingSplitAlbumsSortedBySizeLargestBySizeLargestInProgress = true

                val sizeLongs = mutableListOf<Long>()
                val finalItems = mutableListOf<ImageAlbumItem>()

                val rawItems = splitAlbumsBySizeLargest(context, false).onEach { albumItem ->
                    albumItem.containedImages.forEach {
                        albumItem.totalSize += it.size
                    }
                }

                rawItems.forEach {
                    sizeLongs.add(it.totalSize)
                }

                Collections.sort(sizeLongs, Collections.reverseOrder())

                sizeLongs.forEach { size ->
                    rawItems.forEach {
                        if(it.totalSize == size) {
                            finalItems.add(it)
                        }
                    }
                }

                splitAlbumsSortedBySizeLargestLoadedBySizeLargest = finalItems

                loadingSplitAlbumsSortedBySizeLargestBySizeLargestInProgress = false

                splitAlbumsSortedBySizeLargestLoadedBySizeLargest!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitAlbumsSortedBySizeLargestBySizeLargestInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedBySizeLargestLoadedBySizeLargest ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsBySizeLargestLoadAlbumsByNameReversed(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedBySizeLargestLoadedByNameReversed.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedBySizeLargestLoadedByNameReversed!!
        } else {
            if(!loadingSplitAlbumsSortedBySizeLargestByNameReversedInProgress) {
                loadingSplitAlbumsSortedBySizeLargestByNameReversedInProgress = true

                val titleFiles = mutableListOf<File>()
                val finalItems = mutableListOf<ImageAlbumItem>()

                val rawItems = splitAlbumsBySizeLargest(context, false)

                rawItems.forEach {
                    titleFiles.add(File(it.data.toLowerCase(Locale.ROOT)))
                }

                SortTool.sortFilesByNameReversed(titleFiles).forEach { file ->
                    rawItems.forEach {
                        if(it.data.equals(file.absolutePath, true)) {
                            finalItems.add(it)
                        }
                    }
                }

                splitAlbumsSortedBySizeLargestLoadedByNameReversed = finalItems

                loadingSplitAlbumsSortedBySizeLargestByNameReversedInProgress = false

                splitAlbumsSortedBySizeLargestLoadedByNameReversed!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitAlbumsSortedBySizeLargestByNameReversedInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedBySizeLargestLoadedByNameReversed ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsBySizeLargestLoadAlbumsByDateOldest(context: Context) : MutableList<ImageAlbumItem> {
        val items = splitAlbumsBySizeLargestLoadAlbumsByDateRecent(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageAlbumItem> else mutableListOf()
    }

    suspend fun splitAlbumsBySizeLargestLoadAlbumsBySizeSmallest(context: Context) : MutableList<ImageAlbumItem> {
        val items = splitAlbumsBySizeLargestLoadAlbumsBySizeLargest(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageAlbumItem> else mutableListOf()
    }

    suspend fun splitAlbumsBySizeLargestLoadAlbumsByNameAlphabetic(context: Context) : MutableList<ImageAlbumItem> {
        val items = splitAlbumsBySizeLargestLoadAlbumsByNameReversed(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageAlbumItem> else mutableListOf()
    }

    suspend fun splitAlbumsBySizeSmallest(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedBySizeSmallest.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedBySizeSmallest!!
        } else {
            if(!splittingAlbumsSortedBySizeSmallestInProgress) {
                splittingAlbumsSortedBySizeSmallestInProgress = true

                splitAlbumsSortedBySizeSmallest = splitIntoAlbums(loadItemsBySizeSmallest(context))

                splittingAlbumsSortedBySizeSmallestInProgress = false

                splitAlbumsSortedBySizeSmallest!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(splittingAlbumsSortedBySizeSmallestInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedBySizeSmallest ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsBySizeSmallestLoadAlbumsByDateRecent(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedBySizeSmallestLoadedByDateRecent.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedBySizeSmallestLoadedByDateRecent!!
        } else {
            if(!loadingSplitAlbumsSortedBySizeSmallestByDateRecentInProgress) {
                loadingSplitAlbumsSortedBySizeSmallestByDateRecentInProgress = true

                val rawItems = splitAlbumsBySizeSmallest(context, false)

                val finalItems = mutableListOf<ImageAlbumItem>()

                splitAlbumsByDateRecent(context, false).forEach { albumItem ->
                    rawItems.forEach {
                        if(it.data == albumItem.data) {
                            finalItems.add(it)
                        }
                    }
                }

                splitAlbumsSortedBySizeSmallestLoadedByDateRecent = finalItems

                loadingSplitAlbumsSortedBySizeSmallestByDateRecentInProgress = false

                splitAlbumsSortedBySizeSmallestLoadedByDateRecent!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitAlbumsSortedBySizeSmallestByDateRecentInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedBySizeSmallestLoadedByDateRecent ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsBySizeSmallestLoadAlbumsBySizeLargest(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedBySizeSmallestLoadedBySizeLargest.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedBySizeSmallestLoadedBySizeLargest!!
        } else {
            if(!loadingSplitAlbumsSortedBySizeSmallestBySizeLargestInProgress) {
                loadingSplitAlbumsSortedBySizeSmallestBySizeLargestInProgress = true

                val sizeLongs = mutableListOf<Long>()
                val finalItems = mutableListOf<ImageAlbumItem>()

                val rawItems = splitAlbumsBySizeSmallest(context, false).onEach { albumItem ->
                    albumItem.containedImages.forEach {
                        albumItem.totalSize += it.size
                    }
                }

                rawItems.forEach {
                    sizeLongs.add(it.totalSize)
                }

                Collections.sort(sizeLongs, Collections.reverseOrder())

                sizeLongs.forEach { size ->
                    rawItems.forEach {
                        if(it.totalSize == size) {
                            finalItems.add(it)
                        }
                    }
                }

                splitAlbumsSortedBySizeSmallestLoadedBySizeLargest = finalItems

                loadingSplitAlbumsSortedBySizeSmallestBySizeLargestInProgress = false

                splitAlbumsSortedBySizeSmallestLoadedBySizeLargest!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitAlbumsSortedBySizeSmallestBySizeLargestInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedBySizeSmallestLoadedBySizeLargest ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsBySizeSmallestLoadAlbumsByNameReversed(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedBySizeSmallestLoadedByNameReversed.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedBySizeSmallestLoadedByNameReversed!!
        } else {
            if(!loadingSplitAlbumsSortedBySizeSmallestByNameReversedInProgress) {
                loadingSplitAlbumsSortedBySizeSmallestByNameReversedInProgress = true

                val titleFiles = mutableListOf<File>()
                val finalItems = mutableListOf<ImageAlbumItem>()

                val rawItems = splitAlbumsBySizeSmallest(context, false)

                rawItems.forEach {
                    titleFiles.add(File(it.data.toLowerCase(Locale.ROOT)))
                }

                SortTool.sortFilesByNameReversed(titleFiles).forEach { file ->
                    rawItems.forEach {
                        if(it.data.equals(file.absolutePath, true)) {
                            finalItems.add(it)
                        }
                    }
                }

                splitAlbumsSortedBySizeSmallestLoadedByNameReversed = finalItems

                loadingSplitAlbumsSortedBySizeSmallestByNameReversedInProgress = false

                splitAlbumsSortedBySizeSmallestLoadedByNameReversed!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitAlbumsSortedBySizeSmallestByNameReversedInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedBySizeSmallestLoadedByNameReversed ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsBySizeSmallestLoadAlbumsByDateOldest(context: Context) : MutableList<ImageAlbumItem> {
        val items = splitAlbumsBySizeSmallestLoadAlbumsByDateRecent(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageAlbumItem> else mutableListOf()
    }

    suspend fun splitAlbumsBySizeSmallestLoadAlbumsBySizeSmallest(context: Context) : MutableList<ImageAlbumItem> {
        val items = splitAlbumsBySizeSmallestLoadAlbumsBySizeLargest(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageAlbumItem> else mutableListOf()
    }

    suspend fun splitAlbumsBySizeSmallestLoadAlbumsByNameAlphabetic(context: Context) : MutableList<ImageAlbumItem> {
        val items = splitAlbumsBySizeSmallestLoadAlbumsByNameReversed(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageAlbumItem> else mutableListOf()
    }

    suspend fun splitAlbumsByNameReversed(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedByNameReversed.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedByNameReversed!!
        } else {
            if(!splittingAlbumsSortedByNameReversedInProgress) {
                splittingAlbumsSortedByNameReversedInProgress = true

                splitAlbumsSortedByNameReversed = splitIntoAlbums(loadItemsByNameReversed(context, false))

                splittingAlbumsSortedByNameReversedInProgress = false

                splitAlbumsSortedByNameReversed!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(splittingAlbumsSortedByNameReversedInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedByNameReversed ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsByNameReversedLoadAlbumsByDateRecent(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedByNameReversedLoadedByDateRecent.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedByNameReversedLoadedByDateRecent!!
        } else {
            if(!loadingSplitAlbumsSortedByNameReversedByDateRecentInProgress) {
                loadingSplitAlbumsSortedByNameReversedByDateRecentInProgress = true

                val rawItems = splitAlbumsByNameReversed(context, false)

                val finalItems = mutableListOf<ImageAlbumItem>()

                splitAlbumsByDateRecent(context, false).forEach { albumItem ->
                    rawItems.forEach {
                        if(it.data == albumItem.data) {
                            finalItems.add(it)
                        }
                    }
                }

                splitAlbumsSortedByNameReversedLoadedByDateRecent = finalItems

                loadingSplitAlbumsSortedByNameReversedByDateRecentInProgress = false

                splitAlbumsSortedByNameReversedLoadedByDateRecent!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitAlbumsSortedByNameReversedByDateRecentInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedByNameReversedLoadedByDateRecent ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsByNameReversedLoadAlbumsBySizeLargest(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedByNameReversedLoadedBySizeLargest.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedByNameReversedLoadedBySizeLargest!!
        } else {
            if(!loadingSplitAlbumsSortedByNameReversedBySizeLargestInProgress) {
                loadingSplitAlbumsSortedByNameReversedBySizeLargestInProgress = true

                val sizeLongs = mutableListOf<Long>()
                val finalItems = mutableListOf<ImageAlbumItem>()

                val rawItems = splitAlbumsByNameReversed(context, false).onEach { albumItem ->
                    albumItem.containedImages.forEach {
                        albumItem.totalSize += it.size
                    }
                }

                rawItems.forEach {
                    sizeLongs.add(it.totalSize)
                }

                Collections.sort(sizeLongs, Collections.reverseOrder())

                sizeLongs.forEach { size ->
                    rawItems.forEach {
                        if(it.totalSize == size) {
                            finalItems.add(it)
                        }
                    }
                }

                splitAlbumsSortedByNameReversedLoadedBySizeLargest = finalItems

                loadingSplitAlbumsSortedByNameReversedBySizeLargestInProgress = false

                splitAlbumsSortedByNameReversedLoadedBySizeLargest!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitAlbumsSortedByNameReversedBySizeLargestInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedByNameReversedLoadedBySizeLargest ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsByNameReversedLoadAlbumsByNameReversed(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedByNameReversedLoadedByNameReversed.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedByNameReversedLoadedByNameReversed!!
        } else {
            if(!loadingSplitAlbumsSortedByNameReversedByNameReversedInProgress) {
                loadingSplitAlbumsSortedByNameReversedByNameReversedInProgress = true

                val titleFiles = mutableListOf<File>()
                val finalItems = mutableListOf<ImageAlbumItem>()

                val rawItems = splitAlbumsByNameReversed(context, false)

                rawItems.forEach {
                    titleFiles.add(File(it.data.toLowerCase(Locale.ROOT)))
                }

                SortTool.sortFilesByNameReversed(titleFiles).forEach { file ->
                    rawItems.forEach {
                        if(it.data.equals(file.absolutePath, true)) {
                            finalItems.add(it)
                        }
                    }
                }

                splitAlbumsSortedByNameReversedLoadedByNameReversed = finalItems

                loadingSplitAlbumsSortedByNameReversedByNameReversedInProgress = false

                splitAlbumsSortedByNameReversedLoadedByNameReversed!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitAlbumsSortedByNameReversedByNameReversedInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedByNameReversedLoadedByNameReversed ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsByNameReversedLoadAlbumsByDateOldest(context: Context) : MutableList<ImageAlbumItem> {
        val items = splitAlbumsByNameReversedLoadAlbumsByDateRecent(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageAlbumItem> else mutableListOf()
    }

    suspend fun splitAlbumsByNameReversedLoadAlbumsBySizeSmallest(context: Context) : MutableList<ImageAlbumItem> {
        val items = splitAlbumsByNameReversedLoadAlbumsBySizeLargest(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageAlbumItem> else mutableListOf()
    }

    suspend fun splitAlbumsByNameReversedLoadAlbumsByNameAlphabetic(context: Context) : MutableList<ImageAlbumItem> {
        val items = splitAlbumsByNameReversedLoadAlbumsByNameReversed(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageAlbumItem> else mutableListOf()
    }

    suspend fun splitAlbumsByNameAlphabetic(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedByNameAlphabetic.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedByNameAlphabetic!!
        } else {
            if(!splittingAlbumsSortedByNameAlphabeticInProgress) {
                splittingAlbumsSortedByNameAlphabeticInProgress = true

                splitAlbumsSortedByNameAlphabetic = splitIntoAlbums(loadItemsByNameAlphabetic(context))

                splittingAlbumsSortedByNameReversedInProgress = false

                splitAlbumsSortedByNameAlphabetic!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(splittingAlbumsSortedByNameAlphabeticInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedByNameAlphabetic ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsByNameAlphabeticLoadAlbumsByDateRecent(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedByNameAlphabeticLoadedByDateRecent.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedByNameAlphabeticLoadedByDateRecent!!
        } else {
            if(!loadingSplitAlbumsSortedByNameAlphabeticByDateRecentInProgress) {
                loadingSplitAlbumsSortedByNameAlphabeticByDateRecentInProgress = true

                val rawItems = splitAlbumsByNameAlphabetic(context, false)

                val finalItems = mutableListOf<ImageAlbumItem>()

                splitAlbumsByDateRecent(context, false).forEach { albumItem ->
                    rawItems.forEach {
                        if(it.data == albumItem.data) {
                            finalItems.add(it)
                        }
                    }
                }

                splitAlbumsSortedByNameAlphabeticLoadedByDateRecent = finalItems

                loadingSplitAlbumsSortedByNameAlphabeticByDateRecentInProgress = false

                splitAlbumsSortedByNameAlphabeticLoadedByDateRecent!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitAlbumsSortedByNameAlphabeticByDateRecentInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedByNameAlphabeticLoadedByDateRecent ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsByNameAlphabeticLoadAlbumsBySizeLargest(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedByNameAlphabeticLoadedBySizeLargest.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedByNameAlphabeticLoadedBySizeLargest!!
        } else {
            if(!loadingSplitAlbumsSortedByNameAlphabeticBySizeLargestInProgress) {
                loadingSplitAlbumsSortedByNameAlphabeticBySizeLargestInProgress = true

                val sizeLongs = mutableListOf<Long>()
                val finalItems = mutableListOf<ImageAlbumItem>()

                val rawItems = splitAlbumsByNameAlphabetic(context, false).onEach { albumItem ->
                    albumItem.containedImages.forEach {
                        albumItem.totalSize += it.size
                    }
                }

                rawItems.forEach {
                    sizeLongs.add(it.totalSize)
                }

                Collections.sort(sizeLongs, Collections.reverseOrder())

                sizeLongs.forEach { size ->
                    rawItems.forEach {
                        if(it.totalSize == size) {
                            finalItems.add(it)
                        }
                    }
                }

                splitAlbumsSortedByNameAlphabeticLoadedBySizeLargest = finalItems

                loadingSplitAlbumsSortedByNameAlphabeticBySizeLargestInProgress = false

                splitAlbumsSortedByNameAlphabeticLoadedBySizeLargest!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitAlbumsSortedByNameAlphabeticBySizeLargestInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedByNameAlphabeticLoadedBySizeLargest ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsByNameAlphabeticLoadAlbumsByNameReversed(context: Context, forceLoad: Boolean) : MutableList<ImageAlbumItem> {
        return if(!splitAlbumsSortedByNameAlphabeticLoadedByNameReversed.isNullOrEmpty() && !forceLoad) {
            splitAlbumsSortedByNameAlphabeticLoadedByNameReversed!!
        } else {
            if(!loadingSplitAlbumsSortedByNameAlphabeticByNameReversedInProgress) {
                loadingSplitAlbumsSortedByNameAlphabeticByNameReversedInProgress = true

                val titleFiles = mutableListOf<File>()
                val finalItems = mutableListOf<ImageAlbumItem>()

                val rawItems = splitAlbumsByNameAlphabetic(context, false)

                rawItems.forEach {
                    titleFiles.add(File(it.data.toLowerCase(Locale.ROOT)))
                }

                SortTool.sortFilesByNameReversed(titleFiles).forEach { file ->
                    rawItems.forEach {
                        if(it.data.equals(file.absolutePath, true)) {
                            finalItems.add(it)
                        }
                    }
                }

                splitAlbumsSortedByNameAlphabeticLoadedByNameReversed = finalItems

                loadingSplitAlbumsSortedByNameAlphabeticByNameReversedInProgress = false

                splitAlbumsSortedByNameAlphabeticLoadedByNameReversed!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitAlbumsSortedByNameAlphabeticByNameReversedInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitAlbumsSortedByNameAlphabeticLoadedByNameReversed ?: throw IllegalStateException("Something went wrong while splitting image items into albums")
            }
        }
    }

    suspend fun splitAlbumsByNameAlphabeticLoadAlbumsByDateOldest(context: Context) : MutableList<ImageAlbumItem> {
        val items = splitAlbumsByNameAlphabeticLoadAlbumsByDateRecent(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageAlbumItem> else mutableListOf()
    }

    suspend fun splitAlbumsByNameAlphabeticLoadAlbumsBySizeSmallest(context: Context) : MutableList<ImageAlbumItem> {
        val items = splitAlbumsByNameAlphabeticLoadAlbumsBySizeLargest(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageAlbumItem> else mutableListOf()
    }

    suspend fun splitAlbumsByNameAlphabeticLoadAlbumsByNameAlphabetic(context: Context) : MutableList<ImageAlbumItem> {
        val items = splitAlbumsByNameAlphabeticLoadAlbumsByNameReversed(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageAlbumItem> else mutableListOf()
    }

    // that very preloading magic, executes for 2-5 seconds
    suspend fun loadAll(context: Context, forceLoad: Boolean) {

        loadItemsByDateRecent(context, forceLoad)
        loadItemsBySizeLargest(context, forceLoad)
        loadItemsByNameReversed(context, forceLoad)

        splitAlbumsByDateRecent(context, forceLoad)
        splitAlbumsByDateRecentLoadAlbumsByDateRecent(context, forceLoad)
        splitAlbumsByDateRecentLoadAlbumsBySizeLargest(context, forceLoad)
        splitAlbumsByDateRecentLoadAlbumsByNameReversed(context, forceLoad)

        splitAlbumsByDateOldest(context, forceLoad)
        splitAlbumsByDateOldestLoadAlbumsByDateRecent(context, forceLoad)
        splitAlbumsByDateOldestLoadAlbumsBySizeLargest(context, forceLoad)
        splitAlbumsByDateOldestLoadAlbumsByNameReversed(context, forceLoad)

        splitAlbumsBySizeLargest(context, forceLoad)
        splitAlbumsBySizeLargestLoadAlbumsByDateRecent(context, forceLoad)
        splitAlbumsBySizeLargestLoadAlbumsBySizeLargest(context, forceLoad)
        splitAlbumsBySizeLargestLoadAlbumsByNameReversed(context, forceLoad)

        splitAlbumsBySizeSmallest(context, forceLoad)
        splitAlbumsBySizeSmallestLoadAlbumsByDateRecent(context, forceLoad)
        splitAlbumsBySizeSmallestLoadAlbumsBySizeLargest(context, forceLoad)
        splitAlbumsBySizeSmallestLoadAlbumsByNameReversed(context, forceLoad)

        splitAlbumsByNameReversed(context, forceLoad)
        splitAlbumsByNameReversedLoadAlbumsByDateRecent(context, forceLoad)
        splitAlbumsByNameReversedLoadAlbumsBySizeLargest(context, forceLoad)
        splitAlbumsByNameReversedLoadAlbumsByNameReversed(context, forceLoad)

        splitAlbumsByNameAlphabetic(context, forceLoad)
        splitAlbumsByNameAlphabeticLoadAlbumsByDateRecent(context, forceLoad)
        splitAlbumsByNameAlphabeticLoadAlbumsBySizeLargest(context, forceLoad)
        splitAlbumsByNameAlphabeticLoadAlbumsByNameReversed(context, forceLoad)

    }

    // algo for grouping images into albums that are just their parent folders in essence
    private fun splitIntoAlbums(imageItems: MutableList<ImageItem>) : MutableList<ImageAlbumItem> {

        //  mediator procedure for finding paths of folders containing ImageItems
        val parentPaths = mutableListOf<String>()

        imageItems.forEach { imageItem ->
            val file = File(imageItem.data)

            if(!parentPaths.contains(file.parent!!)) {
                parentPaths.add(file.parent!!)
            }
        }

        // initial collecting of album items
        val albumItems = MutableList(parentPaths.size) {
            ImageAlbumItem(parentPaths[it])
        }

        // loop for every AlbumItem to populate its contained ImageItems
        albumItems.forEach { albumItem ->
            imageItems.forEach {
                if(File(it.data).parent == albumItem.data) {
                    albumItem.containedImages.add(it)
                }
            }
        }

        return albumItems
    }
}