package pro.filemanager.images.folders

import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import kotlinx.coroutines.*
import pro.filemanager.ApplicationLoader
import pro.filemanager.core.PreferencesWrapper
import pro.filemanager.core.base.BaseViewModel
import pro.filemanager.core.tools.SearchTool
import pro.filemanager.core.tools.sort.SortTool
import pro.filemanager.images.ImageRepo

@Parcelize
class ImageFoldersViewModel(var imageRepo: @RawValue ImageRepo) : BaseViewModel(), ImageRepo.AlbumObserver {

    var IOScope = CoroutineScope(Dispatchers.IO)
    var MainScope: CoroutineScope? = CoroutineScope(Dispatchers.Main)

    private var itemsLive: MutableLiveData<MutableList<ImageFolderItem>>? = null

    var searchInProgress = false

    var mainListRvState: Parcelable? = null
    var isSearchViewEnabled = false
    var currentSearchText = ""
    var librarySortOrder: String

    init {
        imageRepo.observe(this)

        librarySortOrder = PreferencesWrapper.getString(ApplicationLoader.appContext, SortTool.KEY_SP_IMAGE_LIBRARY_SORT_ORDER, SortTool.SORT_ORDER_DATE_RECENT)
        currentSortOrder = PreferencesWrapper.getString(ApplicationLoader.appContext, SortTool.KEY_SP_IMAGE_FOLDERS_SORT_ORDER, SortTool.SORT_ORDER_DATE_RECENT)
    }

    private suspend fun initItemsLive(context: Context) {
        if(itemsLive == null) {
            itemsLive = MutableLiveData(
                    when (librarySortOrder) {
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

    override suspend fun assignItemsLive(context: Context, forceLoad: Boolean) {
        if(itemsLive != null) {
            when(librarySortOrder) {
                SortTool.SORT_ORDER_DATE_RECENT -> {
                    if(forceLoad) {
                        imageRepo.loadItemsByDateRecent(context, true)
                        imageRepo.splitAlbumsByDateRecent(context, true)
                    }

                    when(currentSortOrder) {
                        SortTool.SORT_ORDER_DATE_RECENT -> {
                            search(imageRepo.splitAlbumsByDateRecentLoadAlbumsByDateRecent(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_DATE_OLDEST -> {
                            search(imageRepo.splitAlbumsByDateRecentLoadAlbumsByDateOldest(context))
                        }
                        SortTool.SORT_ORDER_NAME_REVERSED -> {
                            search(imageRepo.splitAlbumsByDateRecentLoadAlbumsByNameReversed(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                            search(imageRepo.splitAlbumsByDateRecentLoadAlbumsByNameAlphabetic(context))
                        }
                        SortTool.SORT_ORDER_SIZE_LARGEST -> {
                            search(imageRepo.splitAlbumsByDateRecentLoadAlbumsBySizeLargest(context, forceLoad))
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
                    if(forceLoad) {
                        imageRepo.loadItemsByDateRecent(context, true)
                        imageRepo.splitAlbumsByDateOldest(context, true)
                    }

                    when(currentSortOrder) {
                        SortTool.SORT_ORDER_DATE_RECENT -> {
                            search(imageRepo.splitAlbumsByDateOldestLoadAlbumsByDateRecent(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_DATE_OLDEST -> {
                            search(imageRepo.splitAlbumsByDateOldestLoadAlbumsByDateOldest(context))
                        }
                        SortTool.SORT_ORDER_NAME_REVERSED -> {
                            search(imageRepo.splitAlbumsByDateOldestLoadAlbumsByNameReversed(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                            search(imageRepo.splitAlbumsByDateOldestLoadAlbumsByNameAlphabetic(context))
                        }
                        SortTool.SORT_ORDER_SIZE_LARGEST -> {
                            search(imageRepo.splitAlbumsByDateOldestLoadAlbumsBySizeLargest(context, forceLoad))
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
                    if(forceLoad) {
                        imageRepo.loadItemsBySizeLargest(context, true)
                        imageRepo.splitAlbumsBySizeLargest(context, true)
                    }

                    when(currentSortOrder) {
                        SortTool.SORT_ORDER_DATE_RECENT -> {
                            search(imageRepo.splitAlbumsBySizeLargestLoadAlbumsByDateRecent(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_DATE_OLDEST -> {
                            search(imageRepo.splitAlbumsBySizeLargestLoadAlbumsByDateOldest(context))
                        }
                        SortTool.SORT_ORDER_NAME_REVERSED -> {
                            search(imageRepo.splitAlbumsBySizeLargestLoadAlbumsByNameReversed(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                            search(imageRepo.splitAlbumsBySizeLargestLoadAlbumsByNameAlphabetic(context))
                        }
                        SortTool.SORT_ORDER_SIZE_LARGEST -> {
                            search(imageRepo.splitAlbumsBySizeLargestLoadAlbumsBySizeLargest(context, forceLoad))
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
                    if(forceLoad) {
                        imageRepo.loadItemsBySizeLargest(context, true)
                        imageRepo.splitAlbumsBySizeSmallest(context, true)
                    }

                    when(currentSortOrder) {
                        SortTool.SORT_ORDER_DATE_RECENT -> {
                            search(imageRepo.splitAlbumsBySizeSmallestLoadAlbumsByDateRecent(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_DATE_OLDEST -> {
                            search(imageRepo.splitAlbumsBySizeSmallestLoadAlbumsByDateOldest(context))
                        }
                        SortTool.SORT_ORDER_NAME_REVERSED -> {
                            search(imageRepo.splitAlbumsBySizeSmallestLoadAlbumsByNameReversed(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                            search(imageRepo.splitAlbumsBySizeSmallestLoadAlbumsByNameAlphabetic(context))
                        }
                        SortTool.SORT_ORDER_SIZE_LARGEST -> {
                            search(imageRepo.splitAlbumsBySizeSmallestLoadAlbumsBySizeLargest(context, forceLoad))
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
                    if(forceLoad) {
                        imageRepo.loadItemsByNameReversed(context, true)
                        imageRepo.splitAlbumsByNameReversed(context, true)
                    }

                    when(currentSortOrder) {
                        SortTool.SORT_ORDER_DATE_RECENT -> {
                            search(imageRepo.splitAlbumsByNameReversedLoadAlbumsByDateRecent(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_DATE_OLDEST -> {
                            search(imageRepo.splitAlbumsByNameReversedLoadAlbumsByDateOldest(context))
                        }
                        SortTool.SORT_ORDER_NAME_REVERSED -> {
                            search(imageRepo.splitAlbumsByNameReversedLoadAlbumsByNameReversed(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                            search(imageRepo.splitAlbumsByNameReversedLoadAlbumsByNameAlphabetic(context))
                        }
                        SortTool.SORT_ORDER_SIZE_LARGEST -> {
                            search(imageRepo.splitAlbumsByNameReversedLoadAlbumsBySizeLargest(context, forceLoad))
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
                    if(forceLoad) {
                        imageRepo.loadItemsByNameReversed(context, true)
                        imageRepo.splitAlbumsByNameAlphabetic(context, true)
                    }

                    when(currentSortOrder) {
                        SortTool.SORT_ORDER_DATE_RECENT -> {
                            search(imageRepo.splitAlbumsByNameAlphabeticLoadAlbumsByDateRecent(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_DATE_OLDEST -> {
                            search(imageRepo.splitAlbumsByNameAlphabeticLoadAlbumsByDateOldest(context))
                        }
                        SortTool.SORT_ORDER_NAME_REVERSED -> {
                            search(imageRepo.splitAlbumsByNameAlphabeticLoadAlbumsByNameReversed(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                            search(imageRepo.splitAlbumsByNameAlphabeticLoadAlbumsByNameAlphabetic(context))
                        }
                        SortTool.SORT_ORDER_SIZE_LARGEST -> {
                            search(imageRepo.splitAlbumsByNameAlphabeticLoadAlbumsBySizeLargest(context, forceLoad))
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

    suspend fun getItemsLive() : LiveData<MutableList<ImageFolderItem>> {
        initItemsLive(ApplicationLoader.appContext)

        return itemsLive!!
    }

    override fun onUpdate() {
        if(itemsLive != null)
            IOScope.launch {
                assignItemsLive(ApplicationLoader.appContext, false)
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

        if(isPersistable) PreferencesWrapper.putString(context, SortTool.KEY_SP_IMAGE_FOLDERS_SORT_ORDER, sortOrder)
    }

    fun setSearchText(text: String?) {
        currentSearchText = text ?: ""
    }

    suspend fun search(items: MutableList<ImageFolderItem>) {
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