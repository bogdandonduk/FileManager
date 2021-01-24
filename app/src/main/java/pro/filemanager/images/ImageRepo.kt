package pro.filemanager.images

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pro.filemanager.core.tools.sort.SortTool
import pro.filemanager.core.wrappers.CoroutineWrapper
import pro.filemanager.images.folders.ImageFolderItem
import pro.filemanager.images.library.ImageLibraryItem
import java.io.File
import java.util.*

/**

 * Image Repository Singleton that fetches MediaStore.Images tables or returns previously fetched unchanged ones.
 * It may also further split fetched images (items) into Folders.
 * Fetching is intentionally performed in a coroutine during early ApplicationLoader creation - the foundation for speed of this application.
 * Any updates to fetched items, especially those coming from FileObserver, are pushed to Subscriber ViewModels. They further update UI with LiveData.

 */

class ImageRepo private constructor() {

    companion object {
        @Volatile private var instance: ImageRepo? = null

        fun getSingleton() : ImageRepo {
            if(instance == null) instance = ImageRepo()

            return instance!!
        }
    }

    @Volatile private var loadedItemsSortedByDateRecent: MutableList<ImageLibraryItem>? = null
    @Volatile private var loadedItemsSortedBySizeLargest: MutableList<ImageLibraryItem>? = null
    @Volatile private var loadedItemsSortedByNameReversed: MutableList<ImageLibraryItem>? = null

    @Volatile private var loadingItemsSortedByDateRecentInProgress = false
    @Volatile private var loadingItemsSortedBySizeLargestInProgress = false
    @Volatile private var loadingItemsSortedByNameReversedInProgress = false

    @Volatile private var splitFoldersSortedByDateRecent: MutableList<ImageFolderItem>? = null
    @Volatile private var splitFoldersSortedByDateRecentLoadedByDateRecent: MutableList<ImageFolderItem>? = null
    @Volatile private var splitFoldersSortedByDateRecentLoadedBySizeLargest: MutableList<ImageFolderItem>? = null
    @Volatile private var splitFoldersSortedByDateRecentLoadedByNameReversed: MutableList<ImageFolderItem>? = null

    @Volatile private var splitFoldersSortedByDateOldest: MutableList<ImageFolderItem>? = null
    @Volatile private var splitFoldersSortedByDateOldestLoadedByDateRecent: MutableList<ImageFolderItem>? = null
    @Volatile private var splitFoldersSortedByDateOldestLoadedBySizeLargest: MutableList<ImageFolderItem>? = null
    @Volatile private var splitFoldersSortedByDateOldestLoadedByNameReversed: MutableList<ImageFolderItem>? = null

    @Volatile private var splitFoldersSortedBySizeLargest: MutableList<ImageFolderItem>? = null
    @Volatile private var splitFoldersSortedBySizeLargestLoadedByDateRecent: MutableList<ImageFolderItem>? = null
    @Volatile private var splitFoldersSortedBySizeLargestLoadedBySizeLargest: MutableList<ImageFolderItem>? = null
    @Volatile private var splitFoldersSortedBySizeLargestLoadedByNameReversed: MutableList<ImageFolderItem>? = null

    @Volatile private var splitFoldersSortedBySizeSmallest: MutableList<ImageFolderItem>? = null
    @Volatile private var splitFoldersSortedBySizeSmallestLoadedByDateRecent: MutableList<ImageFolderItem>? = null
    @Volatile private var splitFoldersSortedBySizeSmallestLoadedBySizeLargest: MutableList<ImageFolderItem>? = null
    @Volatile private var splitFoldersSortedBySizeSmallestLoadedByNameReversed: MutableList<ImageFolderItem>? = null

    @Volatile private var splitFoldersSortedByNameReversed: MutableList<ImageFolderItem>? = null
    @Volatile private var splitFoldersSortedByNameReversedLoadedByDateRecent: MutableList<ImageFolderItem>? = null
    @Volatile private var splitFoldersSortedByNameReversedLoadedBySizeLargest: MutableList<ImageFolderItem>? = null
    @Volatile private var splitFoldersSortedByNameReversedLoadedByNameReversed: MutableList<ImageFolderItem>? = null

    @Volatile private var splitFoldersSortedByNameAlphabetic: MutableList<ImageFolderItem>? = null
    @Volatile private var splitFoldersSortedByNameAlphabeticLoadedByDateRecent: MutableList<ImageFolderItem>? = null
    @Volatile private var splitFoldersSortedByNameAlphabeticLoadedBySizeLargest: MutableList<ImageFolderItem>? = null
    @Volatile private var splitFoldersSortedByNameAlphabeticLoadedByNameReversed: MutableList<ImageFolderItem>? = null

    @Volatile private var splittingFoldersSortedByDateRecentInProgress = false
    @Volatile private var loadingSplitFoldersSortedByDateRecentByDateRecentInProgress = false
    @Volatile private var loadingSplitFoldersSortedByDateRecentBySizeLargestInProgress = false
    @Volatile private var loadingSplitFoldersSortedByDateRecentByNameReversedInProgress = false

    @Volatile private var splittingFoldersSortedByDateOldestInProgress = false
    @Volatile private var loadingSplitFoldersSortedByDateOldestByDateRecentInProgress = false
    @Volatile private var loadingSplitFoldersSortedByDateOldestBySizeLargestInProgress = false
    @Volatile private var loadingSplitFoldersSortedByDateOldestByNameReversedInProgress = false

    @Volatile private var splittingFoldersSortedBySizeLargestInProgress = false
    @Volatile private var loadingSplitFoldersSortedBySizeLargestByDateRecentInProgress = false
    @Volatile private var loadingSplitFoldersSortedBySizeLargestBySizeLargestInProgress = false
    @Volatile private var loadingSplitFoldersSortedBySizeLargestByNameReversedInProgress = false

    @Volatile private var splittingFoldersSortedBySizeSmallestInProgress = false
    @Volatile private var loadingSplitFoldersSortedBySizeSmallestByDateRecentInProgress = false
    @Volatile private var loadingSplitFoldersSortedBySizeSmallestBySizeLargestInProgress = false
    @Volatile private var loadingSplitFoldersSortedBySizeSmallestByNameReversedInProgress = false

    @Volatile private var splittingFoldersSortedByNameReversedInProgress = false
    @Volatile private var loadingSplitFoldersSortedByNameReversedByDateRecentInProgress = false
    @Volatile private var loadingSplitFoldersSortedByNameReversedBySizeLargestInProgress = false
    @Volatile private var loadingSplitFoldersSortedByNameReversedByNameReversedInProgress = false

    @Volatile private var splittingFoldersSortedByNameAlphabeticInProgress = false
    @Volatile private var loadingSplitFoldersSortedByNameAlphabeticByDateRecentInProgress = false
    @Volatile private var loadingSplitFoldersSortedByNameAlphabeticBySizeLargestInProgress = false
    @Volatile private var loadingSplitFoldersSortedByNameAlphabeticByNameReversedInProgress = false

    @SuppressLint("Recycle")
    suspend fun loadItemsByDateRecent(context: Context, forceLoad: Boolean) : MutableList<ImageLibraryItem> {
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
                        MediaStore.Images.ImageColumns.WIDTH,
                        MediaStore.Images.ImageColumns.HEIGHT
                ), null, null, MediaStore.Images.ImageColumns.DATE_MODIFIED + " DESC", null)!!

                val imageItems = mutableListOf<ImageLibraryItem>()

                if(cursor.moveToFirst()) {
                    while(!cursor.isAfterLast) {
                        File(cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA))).let {
                            if(it.exists() && !it.isDirectory) {
                                imageItems.add(
                                        ImageLibraryItem(
                                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)),
                                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)),
                                                cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.SIZE)),
                                                cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED)),
                                                cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH)),
                                                cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT))
                                        )
                                )
                            }
                        }

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
                loadedItemsSortedByDateRecent ?: throw IllegalStateException("Something went wrong while fetching images from MediaStore") // No idea what happened. Most likely, error from MediaStore

            }
        }

    }

    // refer to loadItems(context: Context, forceLoad: Boolean): MutableList<ImageItem> method for similar comments
    @SuppressLint("Recycle")
    suspend fun loadItemsBySizeLargest(context: Context, forceLoad: Boolean) : MutableList<ImageLibraryItem> {
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
                        MediaStore.Images.ImageColumns.WIDTH,
                        MediaStore.Images.ImageColumns.HEIGHT
                ), null, null, MediaStore.Images.ImageColumns.SIZE + " DESC", null)!!

                val imageItems = mutableListOf<ImageLibraryItem>()

                if(cursor.moveToFirst()) {
                    while(!cursor.isAfterLast) {
                        File(cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA))).let {
                            if(it.exists() && !it.isDirectory) {
                                imageItems.add(
                                        ImageLibraryItem(
                                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)),
                                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)),
                                                cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.SIZE)),
                                                cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED)),
                                                cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH)),
                                                cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT))
                                        )
                                )
                            }
                        }

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
    suspend fun loadItemsByNameReversed(context: Context, forceLoad: Boolean) : MutableList<ImageLibraryItem> {
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
                        MediaStore.Images.ImageColumns.WIDTH,
                        MediaStore.Images.ImageColumns.HEIGHT
                ), null, null, MediaStore.Images.ImageColumns.DISPLAY_NAME + " DESC", null)!!

                val imageItems = mutableListOf<ImageLibraryItem>()

                if(cursor.moveToFirst()) {

                    while(!cursor.isAfterLast) {
                        File(cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA))).let {
                            if(it.exists() && !it.isDirectory) {
                                imageItems.add(
                                        ImageLibraryItem(
                                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)),
                                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)),
                                                cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.SIZE)),
                                                cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED)),
                                                cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH)),
                                                cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT))
                                        )
                                )
                            }
                        }

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

    suspend fun loadItemsByNameAlphabetic(context: Context) : MutableList<ImageLibraryItem> {
        val items = loadItemsByNameReversed(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageLibraryItem> else mutableListOf()
    }

    suspend fun loadItemsBySizeSmallest(context: Context) : MutableList<ImageLibraryItem> {
        val items = loadItemsBySizeLargest(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageLibraryItem> else mutableListOf()
    }

    suspend fun loadItemsByDateOldest(context: Context) : MutableList<ImageLibraryItem> {
        val items = loadItemsByDateRecent(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageLibraryItem> else mutableListOf()
    }

    // refer to similar loadItems(context: Context, forceLoad: Boolean) : MutableList<ImageItems> method's similar comments
    // quite heavy operation running for about 475 ms on a new young CPU
    suspend fun splitFoldersByDateRecent(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedByDateRecent.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedByDateRecent!!
        } else {
            if(!splittingFoldersSortedByDateRecentInProgress) {
                splittingFoldersSortedByDateRecentInProgress = true

                splitFoldersSortedByDateRecent = splitIntoFolders(loadItemsByDateRecent(context, false))

                splittingFoldersSortedByDateRecentInProgress = false

                splitFoldersSortedByDateRecent!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(splittingFoldersSortedByDateRecentInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedByDateRecent ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersByDateRecentLoadFoldersByDateRecent(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedByDateRecentLoadedByDateRecent.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedByDateRecentLoadedByDateRecent!!
        } else {
            if(!loadingSplitFoldersSortedByDateRecentByDateRecentInProgress) {
                loadingSplitFoldersSortedByDateRecentByDateRecentInProgress = true

                splitFoldersSortedByDateRecentLoadedByDateRecent = splitFoldersByDateRecent(context, false)

                loadingSplitFoldersSortedByDateRecentByDateRecentInProgress = false

                splitFoldersSortedByDateRecentLoadedByDateRecent!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitFoldersSortedByDateRecentByDateRecentInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedByDateRecentLoadedByDateRecent ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersByDateRecentLoadFoldersBySizeLargest(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedByDateRecentLoadedBySizeLargest.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedByDateRecentLoadedBySizeLargest!!
        } else {
            if(!loadingSplitFoldersSortedByDateRecentBySizeLargestInProgress) {
                loadingSplitFoldersSortedByDateRecentBySizeLargestInProgress = true

                val sizeLongs = mutableListOf<Long>()
                val finalItems = mutableListOf<ImageFolderItem>()

                val rawItems = splitFoldersByDateRecent(context, false).onEach { FolderItem ->
                    FolderItem.containedImages.forEach {
                        FolderItem.totalSize += it.size
                    }
                }

                rawItems.forEach {
                    sizeLongs.add(it.totalSize)
                }

                Collections.sort(sizeLongs, Collections.reverseOrder())

                sizeLongs.forEach { size ->
                    rawItems.forEach {
                        if(it.totalSize == size && !finalItems.contains(it)) {
                            finalItems.add(it)
                        }
                    }
                }

                splitFoldersSortedByDateRecentLoadedBySizeLargest = finalItems

                loadingSplitFoldersSortedByDateRecentBySizeLargestInProgress = false

                splitFoldersSortedByDateRecentLoadedBySizeLargest!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitFoldersSortedByDateRecentBySizeLargestInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedByDateRecentLoadedBySizeLargest ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersByDateRecentLoadFoldersByNameReversed(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedByDateRecentLoadedByNameReversed.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedByDateRecentLoadedByNameReversed!!
        } else {
            if(!loadingSplitFoldersSortedByDateRecentByNameReversedInProgress) {
                loadingSplitFoldersSortedByDateRecentByNameReversedInProgress = true

                val titleFiles = mutableListOf<File>()
                val finalItems = mutableListOf<ImageFolderItem>()

                val rawItems = splitFoldersByDateRecent(context, false)

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

                splitFoldersSortedByDateRecentLoadedByNameReversed = finalItems

                loadingSplitFoldersSortedByDateRecentByNameReversedInProgress = false

                splitFoldersSortedByDateRecentLoadedByNameReversed!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitFoldersSortedByDateRecentByNameReversedInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedByDateRecentLoadedByNameReversed ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersByDateRecentLoadFoldersByDateOldest(context: Context) : MutableList<ImageFolderItem> {
        val items = splitFoldersByDateRecentLoadFoldersByDateRecent(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageFolderItem> else mutableListOf()
    }

    suspend fun splitFoldersByDateRecentLoadFoldersBySizeSmallest(context: Context) : MutableList<ImageFolderItem> {
        val items = splitFoldersByDateRecentLoadFoldersBySizeLargest(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageFolderItem> else mutableListOf()
    }

    suspend fun splitFoldersByDateRecentLoadFoldersByNameAlphabetic(context: Context) : MutableList<ImageFolderItem> {
        val items = splitFoldersByDateRecentLoadFoldersByNameReversed(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageFolderItem> else mutableListOf()
    }

    suspend fun splitFoldersByDateOldest(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedByDateOldest.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedByDateOldest!!
        } else {
            if(!splittingFoldersSortedByDateOldestInProgress) {
                splittingFoldersSortedByDateOldestInProgress = true

                splitFoldersSortedByDateOldest = splitIntoFolders(loadItemsByDateOldest(context))

                splittingFoldersSortedByDateOldestInProgress = false

                splitFoldersSortedByDateOldest!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(splittingFoldersSortedByDateOldestInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedByDateOldest ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersByDateOldestLoadFoldersByDateRecent(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedByDateOldestLoadedByDateRecent.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedByDateOldestLoadedByDateRecent!!
        } else {
            if(!loadingSplitFoldersSortedByDateOldestByDateRecentInProgress) {
                loadingSplitFoldersSortedByDateOldestByDateRecentInProgress = true

                val finalItems = mutableListOf<ImageFolderItem>()

                val rawItems = splitFoldersByDateOldest(context, false)

                splitFoldersByDateRecent(context, false).forEach { FolderItem ->
                    rawItems.forEach {
                        if(it.data == FolderItem.data) {
                            finalItems.add(it)
                        }
                    }
                }

                splitFoldersSortedByDateOldestLoadedByDateRecent = finalItems

                loadingSplitFoldersSortedByDateOldestByDateRecentInProgress = false

                splitFoldersSortedByDateOldestLoadedByDateRecent!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitFoldersSortedByDateOldestByDateRecentInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedByDateOldestLoadedByDateRecent ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersByDateOldestLoadFoldersBySizeLargest(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedByDateOldestLoadedBySizeLargest.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedByDateOldestLoadedBySizeLargest!!
        } else {
            if(!loadingSplitFoldersSortedByDateOldestBySizeLargestInProgress) {
                loadingSplitFoldersSortedByDateOldestBySizeLargestInProgress = true

                val sizeLongs = mutableListOf<Long>()
                val finalItems = mutableListOf<ImageFolderItem>()

                val rawItems = splitFoldersByDateOldest(context, false).onEach { FolderItem ->
                    FolderItem.containedImages.forEach {
                        FolderItem.totalSize += it.size
                    }
                }

                rawItems.forEach {
                    sizeLongs.add(it.totalSize)
                }

                Collections.sort(sizeLongs, Collections.reverseOrder())

                sizeLongs.forEach { size ->
                    rawItems.forEach {
                        if(it.totalSize == size && !finalItems.contains(it)) {
                            finalItems.add(it)
                        }
                    }
                }

                splitFoldersSortedByDateOldestLoadedBySizeLargest = finalItems

                loadingSplitFoldersSortedByDateOldestBySizeLargestInProgress = false

                splitFoldersSortedByDateOldestLoadedBySizeLargest!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitFoldersSortedByDateOldestBySizeLargestInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedByDateOldestLoadedBySizeLargest ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersByDateOldestLoadFoldersByNameReversed(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedByDateOldestLoadedByNameReversed.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedByDateOldestLoadedByNameReversed!!
        } else {
            if(!loadingSplitFoldersSortedByDateOldestByNameReversedInProgress) {
                loadingSplitFoldersSortedByDateOldestByNameReversedInProgress = true

                val titleFiles = mutableListOf<File>()
                val finalItems = mutableListOf<ImageFolderItem>()

                val rawItems = splitFoldersByDateOldest(context, false)

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

                splitFoldersSortedByDateOldestLoadedByNameReversed = finalItems

                loadingSplitFoldersSortedByDateOldestByNameReversedInProgress = false

                splitFoldersSortedByDateOldestLoadedByNameReversed!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitFoldersSortedByDateOldestByNameReversedInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedByDateOldestLoadedByNameReversed ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersByDateOldestLoadFoldersByDateOldest(context: Context) : MutableList<ImageFolderItem> {
        val items = splitFoldersByDateOldestLoadFoldersByDateRecent(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageFolderItem> else mutableListOf()
    }

    suspend fun splitFoldersByDateOldestLoadFoldersBySizeSmallest(context: Context) : MutableList<ImageFolderItem> {
        val items = splitFoldersByDateOldestLoadFoldersBySizeLargest(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageFolderItem> else mutableListOf()
    }

    suspend fun splitFoldersByDateOldestLoadFoldersByNameAlphabetic(context: Context) : MutableList<ImageFolderItem> {
        val items = splitFoldersByDateOldestLoadFoldersByNameReversed(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageFolderItem> else mutableListOf()
    }

    suspend fun splitFoldersBySizeLargest(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedBySizeLargest.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedBySizeLargest!!
        } else {
            if(!splittingFoldersSortedBySizeLargestInProgress) {
                splittingFoldersSortedBySizeLargestInProgress = true

                splitFoldersSortedBySizeLargest = splitIntoFolders(loadItemsBySizeLargest(context, false))

                splittingFoldersSortedBySizeLargestInProgress = false

                splitFoldersSortedBySizeLargest!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(splittingFoldersSortedBySizeLargestInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedBySizeLargest ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersBySizeLargestLoadFoldersByDateRecent(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedBySizeLargestLoadedByDateRecent.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedBySizeLargestLoadedByDateRecent!!
        } else {
            if(!loadingSplitFoldersSortedBySizeLargestByDateRecentInProgress) {
                loadingSplitFoldersSortedBySizeLargestByDateRecentInProgress = true

                val rawItems = splitFoldersBySizeLargest(context, false)

                val finalItems = mutableListOf<ImageFolderItem>()

                splitFoldersByDateRecent(context, false).forEach { FolderItem ->
                    rawItems.forEach {
                        if(it.data == FolderItem.data) {
                            finalItems.add(it)
                        }
                    }
                }

                splitFoldersSortedBySizeLargestLoadedByDateRecent = finalItems

                loadingSplitFoldersSortedBySizeLargestByDateRecentInProgress = false

                splitFoldersSortedBySizeLargestLoadedByDateRecent!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitFoldersSortedBySizeLargestByDateRecentInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedBySizeLargestLoadedByDateRecent ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersBySizeLargestLoadFoldersBySizeLargest(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedBySizeLargestLoadedBySizeLargest.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedBySizeLargestLoadedBySizeLargest!!
        } else {
            if(!loadingSplitFoldersSortedBySizeLargestBySizeLargestInProgress) {
                loadingSplitFoldersSortedBySizeLargestBySizeLargestInProgress = true

                val sizeLongs = mutableListOf<Long>()
                val finalItems = mutableListOf<ImageFolderItem>()

                val rawItems = splitFoldersBySizeLargest(context, false).onEach { FolderItem ->
                    FolderItem.containedImages.forEach {
                        FolderItem.totalSize += it.size
                    }
                }

                rawItems.forEach {
                    sizeLongs.add(it.totalSize)
                }

                Collections.sort(sizeLongs, Collections.reverseOrder())

                sizeLongs.forEach { size ->
                    rawItems.forEach {
                        if(it.totalSize == size && !finalItems.contains(it)) {
                            finalItems.add(it)
                        }
                    }
                }

                splitFoldersSortedBySizeLargestLoadedBySizeLargest = finalItems

                loadingSplitFoldersSortedBySizeLargestBySizeLargestInProgress = false

                splitFoldersSortedBySizeLargestLoadedBySizeLargest!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitFoldersSortedBySizeLargestBySizeLargestInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedBySizeLargestLoadedBySizeLargest ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersBySizeLargestLoadFoldersByNameReversed(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedBySizeLargestLoadedByNameReversed.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedBySizeLargestLoadedByNameReversed!!
        } else {
            if(!loadingSplitFoldersSortedBySizeLargestByNameReversedInProgress) {
                loadingSplitFoldersSortedBySizeLargestByNameReversedInProgress = true

                val titleFiles = mutableListOf<File>()
                val finalItems = mutableListOf<ImageFolderItem>()

                val rawItems = splitFoldersBySizeLargest(context, false)

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

                splitFoldersSortedBySizeLargestLoadedByNameReversed = finalItems

                loadingSplitFoldersSortedBySizeLargestByNameReversedInProgress = false

                splitFoldersSortedBySizeLargestLoadedByNameReversed!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitFoldersSortedBySizeLargestByNameReversedInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedBySizeLargestLoadedByNameReversed ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersBySizeLargestLoadFoldersByDateOldest(context: Context) : MutableList<ImageFolderItem> {
        val items = splitFoldersBySizeLargestLoadFoldersByDateRecent(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageFolderItem> else mutableListOf()
    }

    suspend fun splitFoldersBySizeLargestLoadFoldersBySizeSmallest(context: Context) : MutableList<ImageFolderItem> {
        val items = splitFoldersBySizeLargestLoadFoldersBySizeLargest(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageFolderItem> else mutableListOf()
    }

    suspend fun splitFoldersBySizeLargestLoadFoldersByNameAlphabetic(context: Context) : MutableList<ImageFolderItem> {
        val items = splitFoldersBySizeLargestLoadFoldersByNameReversed(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageFolderItem> else mutableListOf()
    }

    suspend fun splitFoldersBySizeSmallest(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedBySizeSmallest.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedBySizeSmallest!!
        } else {
            if(!splittingFoldersSortedBySizeSmallestInProgress) {
                splittingFoldersSortedBySizeSmallestInProgress = true

                splitFoldersSortedBySizeSmallest = splitIntoFolders(loadItemsBySizeSmallest(context))

                splittingFoldersSortedBySizeSmallestInProgress = false

                splitFoldersSortedBySizeSmallest!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(splittingFoldersSortedBySizeSmallestInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedBySizeSmallest ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersBySizeSmallestLoadFoldersByDateRecent(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedBySizeSmallestLoadedByDateRecent.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedBySizeSmallestLoadedByDateRecent!!
        } else {
            if(!loadingSplitFoldersSortedBySizeSmallestByDateRecentInProgress) {
                loadingSplitFoldersSortedBySizeSmallestByDateRecentInProgress = true

                val rawItems = splitFoldersBySizeSmallest(context, false)

                val finalItems = mutableListOf<ImageFolderItem>()

                splitFoldersByDateRecent(context, false).forEach { FolderItem ->
                    rawItems.forEach {
                        if(it.data == FolderItem.data) {
                            finalItems.add(it)
                        }
                    }
                }

                splitFoldersSortedBySizeSmallestLoadedByDateRecent = finalItems

                loadingSplitFoldersSortedBySizeSmallestByDateRecentInProgress = false

                splitFoldersSortedBySizeSmallestLoadedByDateRecent!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitFoldersSortedBySizeSmallestByDateRecentInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedBySizeSmallestLoadedByDateRecent ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersBySizeSmallestLoadFoldersBySizeLargest(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedBySizeSmallestLoadedBySizeLargest.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedBySizeSmallestLoadedBySizeLargest!!
        } else {
            if(!loadingSplitFoldersSortedBySizeSmallestBySizeLargestInProgress) {
                loadingSplitFoldersSortedBySizeSmallestBySizeLargestInProgress = true

                val sizeLongs = mutableListOf<Long>()
                val finalItems = mutableListOf<ImageFolderItem>()

                val rawItems = splitFoldersBySizeSmallest(context, false).onEach { FolderItem ->
                    FolderItem.containedImages.forEach {
                        FolderItem.totalSize += it.size
                    }
                }

                rawItems.forEach {
                    sizeLongs.add(it.totalSize)
                }

                Collections.sort(sizeLongs, Collections.reverseOrder())

                sizeLongs.forEach { size ->
                    rawItems.forEach {
                        if(it.totalSize == size && !finalItems.contains(it)) {
                            finalItems.add(it)
                        }
                    }
                }

                splitFoldersSortedBySizeSmallestLoadedBySizeLargest = finalItems

                loadingSplitFoldersSortedBySizeSmallestBySizeLargestInProgress = false

                splitFoldersSortedBySizeSmallestLoadedBySizeLargest!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitFoldersSortedBySizeSmallestBySizeLargestInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedBySizeSmallestLoadedBySizeLargest ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersBySizeSmallestLoadFoldersByNameReversed(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedBySizeSmallestLoadedByNameReversed.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedBySizeSmallestLoadedByNameReversed!!
        } else {
            if(!loadingSplitFoldersSortedBySizeSmallestByNameReversedInProgress) {
                loadingSplitFoldersSortedBySizeSmallestByNameReversedInProgress = true

                val titleFiles = mutableListOf<File>()
                val finalItems = mutableListOf<ImageFolderItem>()

                val rawItems = splitFoldersBySizeSmallest(context, false)

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

                splitFoldersSortedBySizeSmallestLoadedByNameReversed = finalItems

                loadingSplitFoldersSortedBySizeSmallestByNameReversedInProgress = false

                splitFoldersSortedBySizeSmallestLoadedByNameReversed!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitFoldersSortedBySizeSmallestByNameReversedInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedBySizeSmallestLoadedByNameReversed ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersBySizeSmallestLoadFoldersByDateOldest(context: Context) : MutableList<ImageFolderItem> {
        val items = splitFoldersBySizeSmallestLoadFoldersByDateRecent(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageFolderItem> else mutableListOf()
    }

    suspend fun splitFoldersBySizeSmallestLoadFoldersBySizeSmallest(context: Context) : MutableList<ImageFolderItem> {
        val items = splitFoldersBySizeSmallestLoadFoldersBySizeLargest(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageFolderItem> else mutableListOf()
    }

    suspend fun splitFoldersBySizeSmallestLoadFoldersByNameAlphabetic(context: Context) : MutableList<ImageFolderItem> {
        val items = splitFoldersBySizeSmallestLoadFoldersByNameReversed(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageFolderItem> else mutableListOf()
    }

    suspend fun splitFoldersByNameReversed(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedByNameReversed.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedByNameReversed!!
        } else {
            if(!splittingFoldersSortedByNameReversedInProgress) {
                splittingFoldersSortedByNameReversedInProgress = true

                splitFoldersSortedByNameReversed = splitIntoFolders(loadItemsByNameReversed(context, false))

                splittingFoldersSortedByNameReversedInProgress = false

                splitFoldersSortedByNameReversed!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(splittingFoldersSortedByNameReversedInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedByNameReversed ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersByNameReversedLoadFoldersByDateRecent(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedByNameReversedLoadedByDateRecent.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedByNameReversedLoadedByDateRecent!!
        } else {
            if(!loadingSplitFoldersSortedByNameReversedByDateRecentInProgress) {
                loadingSplitFoldersSortedByNameReversedByDateRecentInProgress = true

                val rawItems = splitFoldersByNameReversed(context, false)

                val finalItems = mutableListOf<ImageFolderItem>()

                splitFoldersByDateRecent(context, false).forEach { FolderItem ->
                    rawItems.forEach {
                        if(it.data == FolderItem.data) {
                            finalItems.add(it)
                        }
                    }
                }

                splitFoldersSortedByNameReversedLoadedByDateRecent = finalItems

                loadingSplitFoldersSortedByNameReversedByDateRecentInProgress = false

                splitFoldersSortedByNameReversedLoadedByDateRecent!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitFoldersSortedByNameReversedByDateRecentInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedByNameReversedLoadedByDateRecent ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersByNameReversedLoadFoldersBySizeLargest(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedByNameReversedLoadedBySizeLargest.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedByNameReversedLoadedBySizeLargest!!
        } else {
            if(!loadingSplitFoldersSortedByNameReversedBySizeLargestInProgress) {
                loadingSplitFoldersSortedByNameReversedBySizeLargestInProgress = true

                val sizeLongs = mutableListOf<Long>()
                val finalItems = mutableListOf<ImageFolderItem>()

                val rawItems = splitFoldersByNameReversed(context, false).onEach { FolderItem ->
                    FolderItem.containedImages.forEach {
                        FolderItem.totalSize += it.size
                    }
                }

                rawItems.forEach {
                    sizeLongs.add(it.totalSize)
                }

                Collections.sort(sizeLongs, Collections.reverseOrder())

                sizeLongs.forEach { size ->
                    rawItems.forEach {
                        if(it.totalSize == size && !finalItems.contains(it)) {
                            finalItems.add(it)
                        }
                    }
                }

                splitFoldersSortedByNameReversedLoadedBySizeLargest = finalItems

                loadingSplitFoldersSortedByNameReversedBySizeLargestInProgress = false

                splitFoldersSortedByNameReversedLoadedBySizeLargest!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitFoldersSortedByNameReversedBySizeLargestInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedByNameReversedLoadedBySizeLargest ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersByNameReversedLoadFoldersByNameReversed(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedByNameReversedLoadedByNameReversed.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedByNameReversedLoadedByNameReversed!!
        } else {
            if(!loadingSplitFoldersSortedByNameReversedByNameReversedInProgress) {
                loadingSplitFoldersSortedByNameReversedByNameReversedInProgress = true

                val titleFiles = mutableListOf<File>()
                val finalItems = mutableListOf<ImageFolderItem>()

                val rawItems = splitFoldersByNameReversed(context, false)

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

                splitFoldersSortedByNameReversedLoadedByNameReversed = finalItems

                loadingSplitFoldersSortedByNameReversedByNameReversedInProgress = false

                splitFoldersSortedByNameReversedLoadedByNameReversed!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitFoldersSortedByNameReversedByNameReversedInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedByNameReversedLoadedByNameReversed ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersByNameReversedLoadFoldersByDateOldest(context: Context) : MutableList<ImageFolderItem> {
        val items = splitFoldersByNameReversedLoadFoldersByDateRecent(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageFolderItem> else mutableListOf()
    }

    suspend fun splitFoldersByNameReversedLoadFoldersBySizeSmallest(context: Context) : MutableList<ImageFolderItem> {
        val items = splitFoldersByNameReversedLoadFoldersBySizeLargest(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageFolderItem> else mutableListOf()
    }

    suspend fun splitFoldersByNameReversedLoadFoldersByNameAlphabetic(context: Context) : MutableList<ImageFolderItem> {
        val items = splitFoldersByNameReversedLoadFoldersByNameReversed(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageFolderItem> else mutableListOf()
    }

    suspend fun splitFoldersByNameAlphabetic(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedByNameAlphabetic.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedByNameAlphabetic!!
        } else {
            if(!splittingFoldersSortedByNameAlphabeticInProgress) {
                splittingFoldersSortedByNameAlphabeticInProgress = true

                splitFoldersSortedByNameAlphabetic = splitIntoFolders(loadItemsByNameAlphabetic(context))

                splittingFoldersSortedByNameReversedInProgress = false

                splitFoldersSortedByNameAlphabetic!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(splittingFoldersSortedByNameAlphabeticInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedByNameAlphabetic ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersByNameAlphabeticLoadFoldersByDateRecent(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedByNameAlphabeticLoadedByDateRecent.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedByNameAlphabeticLoadedByDateRecent!!
        } else {
            if(!loadingSplitFoldersSortedByNameAlphabeticByDateRecentInProgress) {
                loadingSplitFoldersSortedByNameAlphabeticByDateRecentInProgress = true

                val rawItems = splitFoldersByNameAlphabetic(context, false)

                val finalItems = mutableListOf<ImageFolderItem>()

                splitFoldersByDateRecent(context, false).forEach { FolderItem ->
                    rawItems.forEach {
                        if(it.data == FolderItem.data) {
                            finalItems.add(it)
                        }
                    }
                }

                splitFoldersSortedByNameAlphabeticLoadedByDateRecent = finalItems

                loadingSplitFoldersSortedByNameAlphabeticByDateRecentInProgress = false

                splitFoldersSortedByNameAlphabeticLoadedByDateRecent!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitFoldersSortedByNameAlphabeticByDateRecentInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedByNameAlphabeticLoadedByDateRecent ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersByNameAlphabeticLoadFoldersBySizeLargest(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedByNameAlphabeticLoadedBySizeLargest.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedByNameAlphabeticLoadedBySizeLargest!!
        } else {
            if(!loadingSplitFoldersSortedByNameAlphabeticBySizeLargestInProgress) {
                loadingSplitFoldersSortedByNameAlphabeticBySizeLargestInProgress = true

                val sizeLongs = mutableListOf<Long>()
                val finalItems = mutableListOf<ImageFolderItem>()

                val rawItems = splitFoldersByNameAlphabetic(context, false).onEach { FolderItem ->
                    FolderItem.containedImages.forEach {
                        FolderItem.totalSize += it.size
                    }
                }

                rawItems.forEach {
                    sizeLongs.add(it.totalSize)
                }

                Collections.sort(sizeLongs, Collections.reverseOrder())

                sizeLongs.forEach { size ->
                    rawItems.forEach {
                        if(it.totalSize == size && !finalItems.contains(it)) {
                            finalItems.add(it)
                        }
                    }
                }

                splitFoldersSortedByNameAlphabeticLoadedBySizeLargest = finalItems

                loadingSplitFoldersSortedByNameAlphabeticBySizeLargestInProgress = false

                splitFoldersSortedByNameAlphabeticLoadedBySizeLargest!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitFoldersSortedByNameAlphabeticBySizeLargestInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedByNameAlphabeticLoadedBySizeLargest ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersByNameAlphabeticLoadFoldersByNameReversed(context: Context, forceLoad: Boolean) : MutableList<ImageFolderItem> {
        return if(!splitFoldersSortedByNameAlphabeticLoadedByNameReversed.isNullOrEmpty() && !forceLoad) {
            splitFoldersSortedByNameAlphabeticLoadedByNameReversed!!
        } else {
            if(!loadingSplitFoldersSortedByNameAlphabeticByNameReversedInProgress) {
                loadingSplitFoldersSortedByNameAlphabeticByNameReversedInProgress = true

                val titleFiles = mutableListOf<File>()
                val finalItems = mutableListOf<ImageFolderItem>()

                val rawItems = splitFoldersByNameAlphabetic(context, false)

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

                splitFoldersSortedByNameAlphabeticLoadedByNameReversed = finalItems

                loadingSplitFoldersSortedByNameAlphabeticByNameReversedInProgress = false

                splitFoldersSortedByNameAlphabeticLoadedByNameReversed!!
            } else {
                val timeout = System.currentTimeMillis() + 20000

                while(loadingSplitFoldersSortedByNameAlphabeticByNameReversedInProgress && System.currentTimeMillis() < timeout) {
                    delay(25)
                }

                splitFoldersSortedByNameAlphabeticLoadedByNameReversed ?: throw IllegalStateException("Something went wrong while splitting image items into Folders")
            }
        }
    }

    suspend fun splitFoldersByNameAlphabeticLoadFoldersByDateOldest(context: Context) : MutableList<ImageFolderItem> {
        val items = splitFoldersByNameAlphabeticLoadFoldersByDateRecent(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageFolderItem> else mutableListOf()
    }

    suspend fun splitFoldersByNameAlphabeticLoadFoldersBySizeSmallest(context: Context) : MutableList<ImageFolderItem> {
        val items = splitFoldersByNameAlphabeticLoadFoldersBySizeLargest(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageFolderItem> else mutableListOf()
    }

    suspend fun splitFoldersByNameAlphabeticLoadFoldersByNameAlphabetic(context: Context) : MutableList<ImageFolderItem> {
        val items = splitFoldersByNameAlphabeticLoadFoldersByNameReversed(context, false)

        return if(!items.isNullOrEmpty()) items.reversed() as MutableList<ImageFolderItem> else mutableListOf()
    }

    // that very preloading magic, executes for 2-5 seconds
    suspend fun loadAll(context: Context, forceLoad: Boolean) {

        loadItemsByDateRecent(context, forceLoad)
        loadItemsBySizeLargest(context, forceLoad)
        loadItemsByNameReversed(context, forceLoad)

        CoroutineWrapper.globalIOScope.launch {
            splitFoldersByDateRecent(context, forceLoad)
            splitFoldersByDateRecentLoadFoldersByDateRecent(context, forceLoad)
            splitFoldersByDateRecentLoadFoldersBySizeLargest(context, forceLoad)
            splitFoldersByDateRecentLoadFoldersByNameReversed(context, forceLoad)

        }

        CoroutineWrapper.globalIOScope.launch {
            splitFoldersByDateOldest(context, forceLoad)
            splitFoldersByDateOldestLoadFoldersByDateRecent(context, forceLoad)
            splitFoldersByDateOldestLoadFoldersBySizeLargest(context, forceLoad)
            splitFoldersByDateOldestLoadFoldersByNameReversed(context, forceLoad)

        }

        CoroutineWrapper.globalIOScope.launch {
            splitFoldersBySizeLargest(context, forceLoad)
            splitFoldersBySizeLargestLoadFoldersByDateRecent(context, forceLoad)
            splitFoldersBySizeLargestLoadFoldersBySizeLargest(context, forceLoad)
            splitFoldersBySizeLargestLoadFoldersByNameReversed(context, forceLoad)
        }

        CoroutineWrapper.globalIOScope.launch {
            splitFoldersBySizeSmallest(context, forceLoad)
            splitFoldersBySizeSmallestLoadFoldersByDateRecent(context, forceLoad)
            splitFoldersBySizeSmallestLoadFoldersBySizeLargest(context, forceLoad)
            splitFoldersBySizeSmallestLoadFoldersByNameReversed(context, forceLoad)

        }

        CoroutineWrapper.globalIOScope.launch {
            splitFoldersByNameReversed(context, forceLoad)
            splitFoldersByNameReversedLoadFoldersByDateRecent(context, forceLoad)
            splitFoldersByNameReversedLoadFoldersBySizeLargest(context, forceLoad)
            splitFoldersByNameReversedLoadFoldersByNameReversed(context, forceLoad)

        }

        CoroutineWrapper.globalIOScope.launch {
            splitFoldersByNameAlphabetic(context, forceLoad)
            splitFoldersByNameAlphabeticLoadFoldersByDateRecent(context, forceLoad)
            splitFoldersByNameAlphabeticLoadFoldersBySizeLargest(context, forceLoad)
            splitFoldersByNameAlphabeticLoadFoldersByNameReversed(context, forceLoad)

        }
    }

    // algo for grouping images into Folders that are just their parent folders in essence
    private fun splitIntoFolders(imageItems: MutableList<ImageLibraryItem>) : MutableList<ImageFolderItem> {

        //  mediator procedure for finding paths of folders containing ImageItems
        val parentPaths = mutableListOf<String>()

        imageItems.forEach { imageItem ->
            val file = File(imageItem.data)

            if(!parentPaths.contains(file.parent!!)) {
                parentPaths.add(file.parent!!)
            }
        }

        // initial collecting of folder items
        val folderItems = MutableList(parentPaths.size) {
            ImageFolderItem(parentPaths[it])
        }

        // loop for every FolderItem to populate its contained ImageItems
        folderItems.forEach { folderItem ->
            imageItems.forEach {
                if(File(it.data).parent == folderItem.data) {
                    folderItem.containedImages.add(it)
                }
            }
        }

        return folderItems
    }
}