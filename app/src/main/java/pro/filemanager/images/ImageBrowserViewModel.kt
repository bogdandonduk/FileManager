package pro.filemanager.images

import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import pro.filemanager.ApplicationLoader
import pro.filemanager.core.*
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

    private var itemsLive: MutableLiveData<MutableList<ImageItem>>? = null
    var mainListRvState: Parcelable? = null
    var isSearchViewEnabled = false
    var currentSearchText = ""

    var selectionTool: SelectionTool? = null

    init {
        imageRepo.observe(this)

        currentSortOrder = PreferencesWrapper.getString(ApplicationLoader.appContext, SortTool.KEY_SP_IMAGE_BROWSER_SORT_ORDER, SortTool.SORT_ORDER_DATE_RECENT)
    }

    private suspend fun initItemsLive(context: Context) {
        if(itemsLive == null) {
           itemsLive = MutableLiveData(
                   when (currentSortOrder) {
                       SortTool.SORT_ORDER_DATE_RECENT -> {
                           if (albumItem != null) {
                               imageRepo.splitAlbumsByDateRecent(context, false).forEach {
                                   if (it.data == albumItem.data) {
                                       albumItem.containedImages = it.containedImages
                                   }
                               }

                               albumItem.containedImages
                           } else {
                               imageRepo.loadItemsByDateRecent(context, false)
                           }
                       }
                       SortTool.SORT_ORDER_DATE_OLDEST -> {
                           if (albumItem != null) {
                               imageRepo.splitAlbumsByDateOldest(context, false).forEach {
                                   if (it.data == albumItem.data) {
                                       albumItem.containedImages = it.containedImages
                                   }
                               }

                               albumItem.containedImages
                           } else {
                               imageRepo.loadItemsByDateOldest(context)
                           }
                       }
                       SortTool.SORT_ORDER_NAME_REVERSED -> {
                           if (albumItem != null) {
                               imageRepo.splitAlbumsByNameReversed(context, false).forEach {
                                   if (it.data == albumItem.data) {
                                       albumItem.containedImages = it.containedImages
                                   }
                               }

                               albumItem.containedImages
                           } else {
                               imageRepo.loadItemsByNameReversed(context, false)
                           }
                       }
                       SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                           if (albumItem != null) {
                               imageRepo.splitAlbumsByNameAlphabetic(context, false).forEach {
                                   if (it.data == albumItem.data) {
                                       albumItem.containedImages = it.containedImages
                                   }
                               }

                               albumItem.containedImages
                           } else {
                               imageRepo.loadItemsByNameAlphabetic(context)
                           }
                       }
                       SortTool.SORT_ORDER_SIZE_LARGEST -> {
                           if (albumItem != null) {
                               imageRepo.splitAlbumsBySizeLargest(context, false).forEach {
                                   if (it.data == albumItem.data) {
                                       albumItem.containedImages = it.containedImages
                                   }
                               }

                               albumItem.containedImages
                           } else {
                               imageRepo.loadItemsBySizeLargest(context, false)
                           }
                       }
                       SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                           if (albumItem != null) {
                               imageRepo.splitAlbumsBySizeSmallest(context, false).forEach {
                                   if (it.data == albumItem.data) {
                                       albumItem.containedImages = it.containedImages
                                   }
                               }

                               albumItem.containedImages
                           } else {
                               imageRepo.loadItemsBySizeSmallest(context)
                           }
                       }
                       else -> {
                           throw IllegalStateException("Invalid Sort Order")
                       }
                   }
           )
        }
    }

    override fun setSortOrder(context: Context, sortOrder: String, isPersistable: Boolean) {
        super.setSortOrder(context, sortOrder, isPersistable)

        if(isPersistable) {
            PreferencesWrapper.putString(context, SortTool.KEY_SP_IMAGE_BROWSER_SORT_ORDER, sortOrder)
        }

        ApplicationLoader.transientParcelables.remove(UIManager.KEY_TRANSIENT_PARCELABLE_ALBUMS_MAIN_LIST_RV_STATE)
    }

    override suspend fun assignItemsLive(context: Context, forceLoad: Boolean) {
        if(itemsLive != null) {
            when(currentSortOrder) {
                SortTool.SORT_ORDER_DATE_RECENT -> {
                    search(
                            if(albumItem != null) {
                                val imageItems = mutableListOf<ImageItem>()
                                imageRepo.loadItemsByDateRecent(context, true).forEach {
                                    if(File(it.data).parent == albumItem.data) {
                                        imageItems.add(it)
                                    }
                                }
                                albumItem.containedImages = imageItems
                                albumItem.containedImages
                            } else {
                                imageRepo.loadItemsByDateRecent(context, true)
                            }
                    )
                }
                SortTool.SORT_ORDER_DATE_OLDEST -> {
                    search(
                            if(albumItem != null) {
                                val imageItems = mutableListOf<ImageItem>()
                                imageRepo.loadItemsByDateRecent(context, forceLoad)
                                imageRepo.loadItemsByDateOldest(context).forEach {
                                    if(File(it.data).parent == albumItem.data) {
                                        imageItems.add(it)
                                    }
                                }
                                albumItem.containedImages = imageItems
                                albumItem.containedImages
                            } else {
                                imageRepo.loadItemsByDateRecent(context, forceLoad)
                                imageRepo.loadItemsByDateOldest(context)
                            }
                    )
                }
                SortTool.SORT_ORDER_NAME_REVERSED -> {
                    search(
                            if(albumItem != null) {
                                val imageItems = mutableListOf<ImageItem>()
                                imageRepo.loadItemsByNameReversed(context, forceLoad).forEach {
                                    if(File(it.data).parent == albumItem.data) {
                                        imageItems.add(it)
                                    }
                                }
                                albumItem.containedImages = imageItems
                                albumItem.containedImages
                            } else {
                                imageRepo.loadItemsByNameReversed(context, forceLoad)
                            }
                    )
                }
                SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                    search(
                            if(albumItem != null) {
                                val imageItems = mutableListOf<ImageItem>()
                                imageRepo.loadItemsByNameReversed(context, forceLoad)
                                imageRepo.loadItemsByNameAlphabetic(context).forEach {
                                    if(File(it.data).parent == albumItem.data) {
                                        imageItems.add(it)
                                    }
                                }
                                albumItem.containedImages = imageItems
                                albumItem.containedImages
                            } else {
                                imageRepo.loadItemsByNameReversed(context, forceLoad)
                                imageRepo.loadItemsByNameAlphabetic(context)
                            }
                    )
                }
                SortTool.SORT_ORDER_SIZE_LARGEST -> {
                    search(
                            if(albumItem != null) {
                                val imageItems = mutableListOf<ImageItem>()
                                imageRepo.loadItemsBySizeLargest(context, forceLoad).forEach {
                                    if(File(it.data).parent == albumItem.data) {
                                        imageItems.add(it)
                                    }
                                }
                                albumItem.containedImages = imageItems
                                albumItem.containedImages
                            } else {
                                imageRepo.loadItemsBySizeLargest(context, forceLoad)
                            }
                    )
                }
                SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                    search(
                            if(albumItem != null) {
                                val imageItems = mutableListOf<ImageItem>()
                                imageRepo.loadItemsBySizeLargest(context, forceLoad)
                                imageRepo.loadItemsBySizeSmallest(context).forEach {
                                    if(File(it.data).parent == albumItem.data) {
                                        imageItems.add(it)
                                    }
                                }
                                albumItem.containedImages = imageItems
                                albumItem.containedImages
                            } else {
                                imageRepo.loadItemsBySizeLargest(context, forceLoad)
                                imageRepo.loadItemsBySizeSmallest(context)
                            }
                    )
                }
                else -> {
                    throw IllegalStateException("Invalid Sort Order")
                }
            }
        }
    }

    suspend fun getItemsLive(context: Context) : LiveData<MutableList<ImageItem>> {
        initItemsLive(context)

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

        if(albumItem == null) {
            ApplicationLoader.transientParcelables.remove(UIManager.KEY_TRANSIENT_PARCELABLE_ALBUMS_MAIN_LIST_RV_STATE)
            ApplicationLoader.transientStrings.remove(UIManager.KEY_TRANSIENT_STRINGS_ALBUMS_SEARCH_TEXT)
        }

        try {
            IOScope.cancel()
            MainScope?.cancel()
        } catch (thr: Throwable) {  }
    }

    fun setSearchText(text: String?) {
        currentSearchText = text ?: ""
    }

    suspend fun search(items: MutableList<ImageItem>) {
        while (searchInProgress) {
            delay(25)
        }

        searchInProgress = true

        if(currentSearchText.isNotEmpty()) {
            itemsLive?.postValue(SearchTool.searchImageItems(currentSearchText, items))
        } else {
            itemsLive?.postValue(items)
        }
    }

}