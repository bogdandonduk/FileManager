package pro.filemanager.images

import android.content.Context
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import pro.filemanager.ApplicationLoader
import pro.filemanager.core.KEY_SP_NAME
import pro.filemanager.core.KEY_TRANSIENT_PARCELABLE_ALBUMS_MAIN_LIST_RV_STATE
import pro.filemanager.core.KEY_TRANSIENT_STRINGS_ALBUMS_SEARCH_TEXT
import pro.filemanager.core.base.BaseViewModel
import pro.filemanager.core.tools.SearchTool
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.core.tools.sort.SortTool
import pro.filemanager.images.albums.ImageAlbumItem
import java.io.File
import java.lang.IllegalStateException

@Parcelize
class ImageBrowserViewModel(val imageRepo: @RawValue ImageRepo, val albumItem: ImageAlbumItem?) : BaseViewModel(), ImageRepo.ItemObserver {

    var IOScope = CoroutineScope(IO)
    var MainScope: CoroutineScope? = CoroutineScope(Main)

    var searchInProgress = false

    private var itemsLive = MutableLiveData<MutableList<ImageItem>>()
    var mainListRvState: Parcelable? = null
    var isSearchViewEnabled = false
    var currentSearchText = ""

    var selectionTool: SelectionTool? = null

    var currentSortOrder = SortTool.SORT_ORDER_DATE_RECENT

    init {
        imageRepo.subscribe(this)

        currentSortOrder = ApplicationLoader.appContext.getSharedPreferences(KEY_SP_NAME, Context.MODE_PRIVATE).getString(SortTool.KEY_SP_IMAGE_BROWSER_SORT_ORDER, SortTool.SORT_ORDER_DATE_RECENT)!!
    }

    private suspend fun initItemsLive(context: Context) : LiveData<MutableList<ImageItem>> =
            itemsLive.apply { postValue(
                    if(albumItem != null) {
                        when(currentSortOrder) {
                            SortTool.SORT_ORDER_DATE_RECENT -> albumItem.containedImages
                            SortTool.SORT_ORDER_DATE_OLDEST ->
                            SortTool.SORT_ORDER_SIZE_LARGEST -> imageRepo.loadItemsBySizeMax(context, false)
                            SortTool.SORT_ORDER_SIZE_SMALLEST -> imageRepo.loadItemsBySizeMin(context, false)
                            SortTool.SORT_ORDER_NAME_ALPHABET -> imageRepo.loadItemsByNameAlphabetic(context, false)
                            SortTool.SORT_ORDER_NAME_REVERSED -> imageRepo.loadItemsByNameReversed(context, false)

                            else -> throw IllegalStateException()
                        }
                    } else {
                        when(currentSortOrder) {
                            SortTool.SORT_ORDER_DATE_RECENT -> imageRepo.loadItems(context, false)
                            SortTool.SORT_ORDER_DATE_OLDEST -> imageRepo.loadItemsByDateOldest(context, false)
                            SortTool.SORT_ORDER_SIZE_LARGEST -> imageRepo.loadItemsBySizeMax(context, false)
                            SortTool.SORT_ORDER_SIZE_SMALLEST -> imageRepo.loadItemsBySizeMin(context, false)
                            SortTool.SORT_ORDER_NAME_ALPHABET -> imageRepo.loadItemsByNameAlphabetic(context, false)
                            SortTool.SORT_ORDER_NAME_REVERSED -> imageRepo.loadItemsByNameReversed(context, false)

                            else -> throw IllegalStateException()
                        }
                    }
            ) }

    override fun onUpdate(items: MutableList<ImageItem>) {
        if(albumItem != null) {

        } else {
            if(currentSearchText.isNotEmpty()) {
                search(ApplicationLoader.appContext, currentSearchText)
            } else {
                itemsLive.postValue(items)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        imageRepo.unsubscribe(this)

        if(albumItem == null) {
            ApplicationLoader.transientParcelables.remove(KEY_TRANSIENT_PARCELABLE_ALBUMS_MAIN_LIST_RV_STATE)
            ApplicationLoader.transientStrings.remove(KEY_TRANSIENT_STRINGS_ALBUMS_SEARCH_TEXT)
        }

        try {
            IOScope.cancel()
            MainScope?.cancel()
        } catch (thr: Throwable) {

        }

    }

    fun search(context: Context, text: String?, items: MutableList<ImageItem>? = null) {
        IOScope.launch {
            while (searchInProgress) {
                delay(25)
            }

            searchInProgress = true

            currentSearchText = text ?: ""

            if (!text.isNullOrEmpty()) {
                if(albumItem != null) {
                    itemsLive.postValue(SearchTool.searchImageItems(text, albumItem.containedImages))
                } else {
                    if(items == null) {
                        itemsLive.postValue(SearchTool.searchImageItems(text, imageRepo.loadItems(context, false)))
                    } else {
                        itemsLive.postValue(SearchTool.searchImageItems(text, items))
                    }
                }
            } else {
                if(items == null) {

                    when(currentSortOrder) {
                        SortTool.SORT_ORDER_DATE_RECENT -> {
                            Log.d("TAG", "search: RECENT")
                            itemsLive.postValue(imageRepo.loadItems(context, false))
                        }
                        SortTool.SORT_ORDER_DATE_OLDEST -> {
                            Log.d("TAG", "search: OLDEST")
                            itemsLive?.postValue(imageRepo.loadItemsByDateOldest(context, false))
                        }
                    }
                } else {
                    itemsLive.postValue(items)
                }
            }
        }
    }

    fun getAlbumChildren(items: MutableList<ImageItem>) : MutableList<ImageItem> {
        val albumItems = mutableListOf<ImageItem>()

        items.forEach {
            if(File(it.data).parent == albumItem!!.data) {
                albumItems.add(it)
            }
        }

        return albumItems
    }

    override fun sortBySizeMax(context: Context) {
        IOScope.launch {
            if(currentSearchText.isNotEmpty()) {
                if(albumItem != null) {
                    search(context, currentSearchText, getAlbumChildren(imageRepo.loadItemsBySizeMax(context, false)))
                } else {
                    search(context, currentSearchText, imageRepo.loadItemsBySizeMax(context, false))
                }
            } else {
                if(albumItem != null) {
                    itemsLive.postValue(getAlbumChildren(imageRepo.loadItemsBySizeMax(context, false)))
                } else {
                    itemsLive.postValue(imageRepo.loadItemsBySizeMax(context, false))
                }
            }
        }
    }

    override fun sortBySizeMin(context: Context) {
        IOScope.launch {
            if(currentSearchText.isNotEmpty()) {
                search(context, currentSearchText, imageRepo.loadItemsBySizeMin(context, false))
            } else {
                itemsLive.postValue(imageRepo.loadItemsBySizeMin(context, false))
            }
        }
    }

    override fun sortByNameReversed(context: Context) {
        IOScope.launch {
            if(currentSearchText.isNotEmpty()) {
                search(context, currentSearchText, imageRepo.loadItemsByNameReversed(context, false))
            } else {
                itemsLive?.postValue(imageRepo.loadItemsByNameReversed(context, false))
            }
        }
    }

    override fun sortByNameAlphabetic(context: Context) {
        IOScope.launch {
            if(currentSearchText.isNotEmpty()) {
                search(context, currentSearchText, imageRepo.loadItemsByNameAlphabetic(context, false))
            } else {
                itemsLive?.postValue(imageRepo.loadItemsByNameAlphabetic(context, false))
            }
        }
    }

    override fun sortByDateRecent(context: Context) {
        IOScope.launch {
            currentSortOrder = SortTool.SORT_ORDER_DATE_OLDEST
            if(currentSearchText.isNotEmpty()) {
                search(context, currentSearchText, imageRepo.loadItems(context, false))
            } else {
                itemsLive?.postValue(imageRepo.loadItems(context, false))
            }
        }
    }

    override fun sortByDateOldest(context: Context) {
        IOScope.launch {
            currentSortOrder = SortTool.SORT_ORDER_DATE_OLDEST
            if(currentSearchText.isNotEmpty()) {
                search(context, currentSearchText, imageRepo.loadItemsByDateOldest(context, false))
            } else {
                itemsLive?.postValue(imageRepo.loadItemsByDateOldest(context, false))
            }
        }
    }

}