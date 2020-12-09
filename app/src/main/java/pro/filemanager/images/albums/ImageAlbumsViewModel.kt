package pro.filemanager.images.albums

import android.content.Context
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import kotlinx.coroutines.*
import pro.filemanager.ApplicationLoader
import pro.filemanager.core.PreferencesWrapper
import pro.filemanager.core.base.BaseViewModel
import pro.filemanager.core.tools.SearchTool
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.core.tools.sort.SortTool
import pro.filemanager.images.ImageRepo

@Parcelize
class ImageAlbumsViewModel(var imageRepo: @RawValue ImageRepo) : BaseViewModel(), ImageRepo.AlbumObserver {

    var IOScope = CoroutineScope(Dispatchers.IO)
    var MainScope: CoroutineScope? = CoroutineScope(Dispatchers.Main)

    private var itemsLive: MutableLiveData<MutableList<ImageAlbumItem>>? = null
    var selectionTool: SelectionTool? = null

    var searchInProgress = false

    var mainListRvState: Parcelable? = null
    var isSearchViewEnabled = false
    var currentSearchText = ""
    var gallerySortOrder: String

    init {
        imageRepo.observe(this)

        gallerySortOrder = PreferencesWrapper.getString(ApplicationLoader.appContext, SortTool.KEY_SP_IMAGE_BROWSER_SORT_ORDER, SortTool.SORT_ORDER_DATE_RECENT)
        currentSortOrder = PreferencesWrapper.getString(ApplicationLoader.appContext, SortTool.KEY_SP_IMAGE_ALBUMS_SORT_ORDER, SortTool.SORT_ORDER_DATE_RECENT)
    }

    private suspend fun initItemsLive(context: Context) {
        if(itemsLive == null) {
            itemsLive = MutableLiveData(
                    when (gallerySortOrder) {
                        SortTool.SORT_ORDER_DATE_RECENT -> {
                            when (currentSortOrder) {
                                SortTool.SORT_ORDER_DATE_RECENT -> {
                                    imageRepo.splitAlbumsByDateRecentLoadAlbumsByDateRecent(context, false)
                                }
                                SortTool.SORT_ORDER_DATE_OLDEST -> {
                                    imageRepo.splitAlbumsByDateRecentLoadAlbumsByDateOldest(context)
                                }
                                SortTool.SORT_ORDER_NAME_REVERSED -> {
                                    imageRepo.splitAlbumsByDateRecentLoadAlbumsByNameReversed(context, false)
                                }
                                SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                                    imageRepo.splitAlbumsByDateRecentLoadAlbumsByNameAlphabetic(context)
                                }
                                SortTool.SORT_ORDER_SIZE_LARGEST -> {
                                    imageRepo.splitAlbumsByDateRecentLoadAlbumsBySizeLargest(context, false)
                                }
                                SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                                    imageRepo.splitAlbumsByDateRecentLoadAlbumsBySizeSmallest(context)
                                }
                                else -> {
                                    throw IllegalStateException("Invalid Sort Order")
                                }
                            }
                        }
                        SortTool.SORT_ORDER_DATE_OLDEST -> {
                            when (currentSortOrder) {
                                SortTool.SORT_ORDER_DATE_RECENT -> {
                                    imageRepo.splitAlbumsByDateOldestLoadAlbumsByDateRecent(context, false)
                                }
                                SortTool.SORT_ORDER_DATE_OLDEST -> {
                                    imageRepo.splitAlbumsByDateOldestLoadAlbumsByDateOldest(context)
                                }
                                SortTool.SORT_ORDER_NAME_REVERSED -> {
                                    imageRepo.splitAlbumsByDateOldestLoadAlbumsByNameReversed(context, false)
                                }
                                SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                                    imageRepo.splitAlbumsByDateOldestLoadAlbumsByNameAlphabetic(context)
                                }
                                SortTool.SORT_ORDER_SIZE_LARGEST -> {
                                    imageRepo.splitAlbumsByDateOldestLoadAlbumsBySizeLargest(context, false)
                                }
                                SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                                    imageRepo.splitAlbumsByDateOldestLoadAlbumsBySizeSmallest(context)
                                }
                                else -> {
                                    throw IllegalStateException("Invalid Sort Order")
                                }
                            }
                        }
                        SortTool.SORT_ORDER_SIZE_LARGEST -> {
                            when (currentSortOrder) {
                                SortTool.SORT_ORDER_DATE_RECENT -> {
                                    imageRepo.splitAlbumsBySizeLargestLoadAlbumsByDateRecent(context, false)
                                }
                                SortTool.SORT_ORDER_DATE_OLDEST -> {
                                    imageRepo.splitAlbumsBySizeLargestLoadAlbumsByDateOldest(context)
                                }
                                SortTool.SORT_ORDER_NAME_REVERSED -> {
                                    imageRepo.splitAlbumsBySizeLargestLoadAlbumsByNameReversed(context, false)
                                }
                                SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                                    imageRepo.splitAlbumsBySizeLargestLoadAlbumsByNameAlphabetic(context)
                                }
                                SortTool.SORT_ORDER_SIZE_LARGEST -> {
                                    imageRepo.splitAlbumsBySizeLargestLoadAlbumsBySizeLargest(context, false)
                                }
                                SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                                    imageRepo.splitAlbumsBySizeLargestLoadAlbumsBySizeSmallest(context)
                                }
                                else -> {
                                    throw IllegalStateException("Invalid Sort Order")
                                }
                            }
                        }
                        SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                            when (currentSortOrder) {
                                SortTool.SORT_ORDER_DATE_RECENT -> {
                                    imageRepo.splitAlbumsBySizeSmallestLoadAlbumsByDateRecent(context, false)
                                }
                                SortTool.SORT_ORDER_DATE_OLDEST -> {
                                    imageRepo.splitAlbumsBySizeSmallestLoadAlbumsByDateOldest(context)
                                }
                                SortTool.SORT_ORDER_NAME_REVERSED -> {
                                    imageRepo.splitAlbumsBySizeSmallestLoadAlbumsByNameReversed(context, false)
                                }
                                SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                                    imageRepo.splitAlbumsBySizeSmallestLoadAlbumsByNameAlphabetic(context)
                                }
                                SortTool.SORT_ORDER_SIZE_LARGEST -> {
                                    imageRepo.splitAlbumsBySizeSmallestLoadAlbumsBySizeLargest(context, false)
                                }
                                SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                                    imageRepo.splitAlbumsBySizeSmallestLoadAlbumsBySizeSmallest(context)
                                }
                                else -> {
                                    throw IllegalStateException("Invalid Sort Order")
                                }
                            }
                        }
                        SortTool.SORT_ORDER_NAME_REVERSED -> {
                            when (currentSortOrder) {
                                SortTool.SORT_ORDER_DATE_RECENT -> {
                                    imageRepo.splitAlbumsByNameReversedLoadAlbumsByDateRecent(context, false)
                                }
                                SortTool.SORT_ORDER_DATE_OLDEST -> {
                                    imageRepo.splitAlbumsByNameReversedLoadAlbumsByDateOldest(context)
                                }
                                SortTool.SORT_ORDER_NAME_REVERSED -> {
                                    imageRepo.splitAlbumsByNameReversedLoadAlbumsByNameReversed(context, false)
                                }
                                SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                                    imageRepo.splitAlbumsByNameReversedLoadAlbumsByNameAlphabetic(context)
                                }
                                SortTool.SORT_ORDER_SIZE_LARGEST -> {
                                    imageRepo.splitAlbumsByNameReversedLoadAlbumsBySizeLargest(context, false)
                                }
                                SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                                    imageRepo.splitAlbumsByNameReversedLoadAlbumsBySizeSmallest(context)
                                }
                                else -> {
                                    throw IllegalStateException("Invalid Sort Order")
                                }
                            }
                        }
                        SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                            when (currentSortOrder) {
                                SortTool.SORT_ORDER_DATE_RECENT -> {
                                    imageRepo.splitAlbumsByNameAlphabeticLoadAlbumsByDateRecent(context, false)
                                }
                                SortTool.SORT_ORDER_DATE_OLDEST -> {
                                    imageRepo.splitAlbumsByNameAlphabeticLoadAlbumsByDateOldest(context)
                                }
                                SortTool.SORT_ORDER_NAME_REVERSED -> {
                                    imageRepo.splitAlbumsByNameAlphabeticLoadAlbumsByNameReversed(context, false)
                                }
                                SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                                    imageRepo.splitAlbumsByNameAlphabeticLoadAlbumsByNameAlphabetic(context)
                                }
                                SortTool.SORT_ORDER_SIZE_LARGEST -> {
                                    imageRepo.splitAlbumsByNameAlphabeticLoadAlbumsBySizeLargest(context, false)
                                }
                                SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                                    imageRepo.splitAlbumsByNameAlphabeticLoadAlbumsBySizeSmallest(context)
                                }
                                else -> {
                                    throw IllegalStateException("Invalid Sort Order")
                                }
                            }
                        }
                        else -> {
                            throw IllegalStateException("Invalid Sort Order")
                        }
                    }
            )
        }
    }

    override suspend fun assignItemsLive(context: Context) {
        if(itemsLive != null) {
            when(gallerySortOrder) {
                SortTool.SORT_ORDER_DATE_RECENT -> {
                    when(currentSortOrder) {
                        SortTool.SORT_ORDER_DATE_RECENT -> {
                            search(imageRepo.splitAlbumsByDateRecentLoadAlbumsByDateRecent(context, false))
                        }
                        SortTool.SORT_ORDER_DATE_OLDEST -> {
                            search(imageRepo.splitAlbumsByDateRecentLoadAlbumsByDateOldest(context))
                        }
                        SortTool.SORT_ORDER_NAME_REVERSED -> {
                            search(imageRepo.splitAlbumsByDateRecentLoadAlbumsByNameReversed(context, false))
                        }
                        SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                            search(imageRepo.splitAlbumsByDateRecentLoadAlbumsByNameAlphabetic(context))
                        }
                        SortTool.SORT_ORDER_SIZE_LARGEST -> {
                            search(imageRepo.splitAlbumsByDateRecentLoadAlbumsBySizeLargest(context, false))
                        }
                        SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                            search(imageRepo.splitAlbumsByDateRecentLoadAlbumsBySizeSmallest(context))
                        }
                        else -> {
                            throw IllegalStateException("Invalid Sort Order")
                        }
                    }
                }
                SortTool.SORT_ORDER_DATE_OLDEST -> {
                    when(currentSortOrder) {
                        SortTool.SORT_ORDER_DATE_RECENT -> {
                            search(imageRepo.splitAlbumsByDateOldestLoadAlbumsByDateRecent(context, false))
                        }
                        SortTool.SORT_ORDER_DATE_OLDEST -> {
                            search(imageRepo.splitAlbumsByDateOldestLoadAlbumsByDateOldest(context))
                        }
                        SortTool.SORT_ORDER_NAME_REVERSED -> {
                            search(imageRepo.splitAlbumsByDateOldestLoadAlbumsByNameReversed(context, false))
                        }
                        SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                            search(imageRepo.splitAlbumsByDateOldestLoadAlbumsByNameAlphabetic(context))
                        }
                        SortTool.SORT_ORDER_SIZE_LARGEST -> {
                            search(imageRepo.splitAlbumsByDateOldestLoadAlbumsBySizeLargest(context, false))
                        }
                        SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                            search(imageRepo.splitAlbumsByDateOldestLoadAlbumsBySizeSmallest(context))
                        }
                        else -> {
                            throw IllegalStateException("Invalid Sort Order")
                        }
                    }
                }
                SortTool.SORT_ORDER_SIZE_LARGEST -> {
                    when(currentSortOrder) {
                        SortTool.SORT_ORDER_DATE_RECENT -> {
                            search(imageRepo.splitAlbumsBySizeLargestLoadAlbumsByDateRecent(context, false))
                        }
                        SortTool.SORT_ORDER_DATE_OLDEST -> {
                            search(imageRepo.splitAlbumsBySizeLargestLoadAlbumsByDateOldest(context))
                        }
                        SortTool.SORT_ORDER_NAME_REVERSED -> {
                            search(imageRepo.splitAlbumsBySizeLargestLoadAlbumsByNameReversed(context, false))
                        }
                        SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                            search(imageRepo.splitAlbumsBySizeLargestLoadAlbumsByNameAlphabetic(context))
                        }
                        SortTool.SORT_ORDER_SIZE_LARGEST -> {
                            search(imageRepo.splitAlbumsBySizeLargestLoadAlbumsBySizeLargest(context, false))
                        }
                        SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                            search(imageRepo.splitAlbumsBySizeLargestLoadAlbumsBySizeSmallest(context))
                        }
                        else -> {
                            throw IllegalStateException("Invalid Sort Order")
                        }
                    }
                }
                SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                    when(currentSortOrder) {
                        SortTool.SORT_ORDER_DATE_RECENT -> {
                            search(imageRepo.splitAlbumsBySizeSmallestLoadAlbumsByDateRecent(context, false))
                        }
                        SortTool.SORT_ORDER_DATE_OLDEST -> {
                            search(imageRepo.splitAlbumsBySizeSmallestLoadAlbumsByDateOldest(context))
                        }
                        SortTool.SORT_ORDER_NAME_REVERSED -> {
                            search(imageRepo.splitAlbumsBySizeSmallestLoadAlbumsByNameReversed(context, false))
                        }
                        SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                            search(imageRepo.splitAlbumsBySizeSmallestLoadAlbumsByNameAlphabetic(context))
                        }
                        SortTool.SORT_ORDER_SIZE_LARGEST -> {
                            search(imageRepo.splitAlbumsBySizeSmallestLoadAlbumsBySizeLargest(context, false))
                        }
                        SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                            search(imageRepo.splitAlbumsBySizeSmallestLoadAlbumsBySizeSmallest(context))
                        }
                        else -> {
                            throw IllegalStateException("Invalid Sort Order")
                        }
                    }
                }
                SortTool.SORT_ORDER_NAME_REVERSED -> {
                    when(currentSortOrder) {
                        SortTool.SORT_ORDER_DATE_RECENT -> {
                            search(imageRepo.splitAlbumsByNameReversedLoadAlbumsByDateRecent(context, false))
                        }
                        SortTool.SORT_ORDER_DATE_OLDEST -> {
                            search(imageRepo.splitAlbumsByNameReversedLoadAlbumsByDateOldest(context))
                        }
                        SortTool.SORT_ORDER_NAME_REVERSED -> {
                            search(imageRepo.splitAlbumsByNameReversedLoadAlbumsByNameReversed(context, false))
                        }
                        SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                            search(imageRepo.splitAlbumsByNameReversedLoadAlbumsByNameAlphabetic(context))
                        }
                        SortTool.SORT_ORDER_SIZE_LARGEST -> {
                            search(imageRepo.splitAlbumsByNameReversedLoadAlbumsBySizeLargest(context, false))
                        }
                        SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                            search(imageRepo.splitAlbumsByNameReversedLoadAlbumsBySizeSmallest(context))
                        }
                        else -> {
                            throw IllegalStateException("Invalid Sort Order")
                        }
                    }
                }
                SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                    when(currentSortOrder) {
                        SortTool.SORT_ORDER_DATE_RECENT -> {
                            search(imageRepo.splitAlbumsByNameAlphabeticLoadAlbumsByDateRecent(context, false))
                        }
                        SortTool.SORT_ORDER_DATE_OLDEST -> {
                            search(imageRepo.splitAlbumsByNameAlphabeticLoadAlbumsByDateOldest(context))
                        }
                        SortTool.SORT_ORDER_NAME_REVERSED -> {
                            search(imageRepo.splitAlbumsByNameAlphabeticLoadAlbumsByNameReversed(context, false))
                        }
                        SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                            search(imageRepo.splitAlbumsByNameAlphabeticLoadAlbumsByNameAlphabetic(context))
                        }
                        SortTool.SORT_ORDER_SIZE_LARGEST -> {
                            search(imageRepo.splitAlbumsByNameAlphabeticLoadAlbumsBySizeLargest(context, false))
                        }
                        SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                            search(imageRepo.splitAlbumsByNameAlphabeticLoadAlbumsBySizeSmallest(context))
                        }
                        else -> {
                            throw IllegalStateException("Invalid Sort Order")
                        }
                    }
                }
                else -> {
                    throw IllegalStateException("Invalid Sort Order")
                }
            }
        }
    }

    suspend fun getAlbumsLive() : LiveData<MutableList<ImageAlbumItem>> {
        initItemsLive(ApplicationLoader.appContext)

        return itemsLive!!
    }

    override fun onUpdate() {
        if(itemsLive != null)
            IOScope.launch {
                assignItemsLive(ApplicationLoader.appContext)
            }
    }

    override fun onCleared() {
        super.onCleared()

        imageRepo.stopObserving(this)

        try {
            IOScope.cancel()
            MainScope?.cancel()
        } catch (thr: Throwable) {

        }
    }

    override fun setSortOrder(context: Context, sortOrder: String, isPersistable: Boolean) {
        super.setSortOrder(context, sortOrder, isPersistable)

        if(isPersistable) PreferencesWrapper.putString(context, SortTool.KEY_SP_IMAGE_ALBUMS_SORT_ORDER, sortOrder)
    }

    fun setSearchText(text: String?) {
        currentSearchText = text ?: ""
    }

    suspend fun search(items: MutableList<ImageAlbumItem>) {
        while (searchInProgress) {
            delay(25)
        }

        searchInProgress = true

        if(currentSearchText.isNotEmpty()) {
            itemsLive?.postValue(SearchTool.searchImageAlbumItems(currentSearchText, items))
        } else {
            itemsLive?.postValue(items)
        }
    }
}