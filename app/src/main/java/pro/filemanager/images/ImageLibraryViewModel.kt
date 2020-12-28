package pro.filemanager.images

import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import kotlinx.coroutines.*
import pro.filemanager.ApplicationLoader
import pro.filemanager.core.generics.BaseViewModel
import pro.filemanager.core.tools.SearchTool
import pro.filemanager.core.tools.sort.SortTool
import pro.filemanager.core.ui.UIManager
import pro.filemanager.core.wrappers.PreferencesWrapper
import pro.filemanager.images.folders.ImageFolderItem
import java.io.File
import java.lang.IllegalStateException

@Parcelize
class ImageLibraryViewModel(val context: @RawValue Context, val imageRepo: @RawValue ImageRepo, val folderItem: ImageFolderItem?) : BaseViewModel(), ImageRepo.ItemObserver {
    var searchInProgress = false

    private var itemsLive: MutableLiveData<MutableList<ImageItem>>? = null
    var mainListRvState: Parcelable? = null
    var isSearchViewEnabled = false
    var currentSearchText = ""

    init {
        imageRepo.observe(this)

        currentSortOrder = PreferencesWrapper.getString(ApplicationLoader.appContext, SortTool.KEY_SP_IMAGE_LIBRARY_SORT_ORDER, SortTool.SORT_ORDER_DATE_RECENT)

    }

    private suspend fun initItemsLive(context: Context) {
        if(itemsLive == null) {
           itemsLive = MutableLiveData(
                   when (currentSortOrder) {
                       SortTool.SORT_ORDER_DATE_RECENT -> {
                           if (folderItem != null) {
                               imageRepo.splitAlbumsByDateRecent(context, false).forEach {
                                   if (it.data == folderItem.data) {
                                       folderItem.containedImages = it.containedImages
                                   }
                               }

                               folderItem.containedImages
                           } else {
                               imageRepo.loadItemsByDateRecent(context, false)
                           }
                       }
                       SortTool.SORT_ORDER_DATE_OLDEST -> {
                           if (folderItem != null) {
                               imageRepo.splitAlbumsByDateOldest(context, false).forEach {
                                   if (it.data == folderItem.data) {
                                       folderItem.containedImages = it.containedImages
                                   }
                               }

                               folderItem.containedImages
                           } else {
                               imageRepo.loadItemsByDateOldest(context)
                           }
                       }
                       SortTool.SORT_ORDER_NAME_REVERSED -> {
                           if (folderItem != null) {
                               imageRepo.splitAlbumsByNameReversed(context, false).forEach {
                                   if (it.data == folderItem.data) {
                                       folderItem.containedImages = it.containedImages
                                   }
                               }

                               folderItem.containedImages
                           } else {
                               imageRepo.loadItemsByNameReversed(context, false)
                           }
                       }
                       SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                           if (folderItem != null) {
                               imageRepo.splitAlbumsByNameAlphabetic(context, false).forEach {
                                   if (it.data == folderItem.data) {
                                       folderItem.containedImages = it.containedImages
                                   }
                               }

                               folderItem.containedImages
                           } else {
                               imageRepo.loadItemsByNameAlphabetic(context)
                           }
                       }
                       SortTool.SORT_ORDER_SIZE_LARGEST -> {
                           if (folderItem != null) {
                               imageRepo.splitAlbumsBySizeLargest(context, false).forEach {
                                   if (it.data == folderItem.data) {
                                       folderItem.containedImages = it.containedImages
                                   }
                               }

                               folderItem.containedImages
                           } else {
                               imageRepo.loadItemsBySizeLargest(context, false)
                           }
                       }
                       SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                           if (folderItem != null) {
                               imageRepo.splitAlbumsBySizeSmallest(context, false).forEach {
                                   if (it.data == folderItem.data) {
                                       folderItem.containedImages = it.containedImages
                                   }
                               }

                               folderItem.containedImages
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
            PreferencesWrapper.putString(context, SortTool.KEY_SP_IMAGE_LIBRARY_SORT_ORDER, sortOrder)
        }
    }

    override suspend fun assignItemsLive(context: Context, forceLoad: Boolean) {
        if(itemsLive != null) {
            when(currentSortOrder) {
                SortTool.SORT_ORDER_DATE_RECENT -> {
                    search(
                            if(folderItem != null) {
                                val imageItems = mutableListOf<ImageItem>()
                                imageRepo.loadItemsByDateRecent(context, forceLoad).forEach {
                                    if(File(it.data).parent == folderItem.data) {
                                        imageItems.add(it)
                                    }
                                }
                                folderItem.containedImages = imageItems
                                folderItem.containedImages
                            } else {
                                imageRepo.loadItemsByDateRecent(context, forceLoad)
                            }
                    )
                }
                SortTool.SORT_ORDER_DATE_OLDEST -> {
                    search(
                            if(folderItem != null) {
                                val imageItems = mutableListOf<ImageItem>()
                                imageRepo.loadItemsByDateRecent(context, forceLoad)
                                imageRepo.loadItemsByDateOldest(context).forEach {
                                    if(File(it.data).parent == folderItem.data) {
                                        imageItems.add(it)
                                    }
                                }
                                folderItem.containedImages = imageItems
                                folderItem.containedImages
                            } else {
                                imageRepo.loadItemsByDateRecent(context, forceLoad)
                                imageRepo.loadItemsByDateOldest(context)
                            }
                    )
                }
                SortTool.SORT_ORDER_NAME_REVERSED -> {
                    search(
                            if(folderItem != null) {
                                val imageItems = mutableListOf<ImageItem>()
                                imageRepo.loadItemsByNameReversed(context, forceLoad).forEach {
                                    if(File(it.data).parent == folderItem.data) {
                                        imageItems.add(it)
                                    }
                                }
                                folderItem.containedImages = imageItems
                                folderItem.containedImages
                            } else {
                                imageRepo.loadItemsByNameReversed(context, forceLoad)
                            }
                    )
                }
                SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                    search(
                            if(folderItem != null) {
                                val imageItems = mutableListOf<ImageItem>()
                                imageRepo.loadItemsByNameReversed(context, forceLoad)
                                imageRepo.loadItemsByNameAlphabetic(context).forEach {
                                    if(File(it.data).parent == folderItem.data) {
                                        imageItems.add(it)
                                    }
                                }
                                folderItem.containedImages = imageItems
                                folderItem.containedImages
                            } else {
                                imageRepo.loadItemsByNameReversed(context, forceLoad)
                                imageRepo.loadItemsByNameAlphabetic(context)
                            }
                    )
                }
                SortTool.SORT_ORDER_SIZE_LARGEST -> {
                    search(
                            if(folderItem != null) {
                                val imageItems = mutableListOf<ImageItem>()
                                imageRepo.loadItemsBySizeLargest(context, forceLoad).forEach {
                                    if(File(it.data).parent == folderItem.data) {
                                        imageItems.add(it)
                                    }
                                }
                                folderItem.containedImages = imageItems
                                folderItem.containedImages
                            } else {
                                imageRepo.loadItemsBySizeLargest(context, forceLoad)
                            }
                    )
                }
                SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                    search(
                            if(folderItem != null) {
                                val imageItems = mutableListOf<ImageItem>()
                                imageRepo.loadItemsBySizeLargest(context, forceLoad)
                                imageRepo.loadItemsBySizeSmallest(context).forEach {
                                    if(File(it.data).parent == folderItem.data) {
                                        imageItems.add(it)
                                    }
                                }
                                folderItem.containedImages = imageItems
                                folderItem.containedImages
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

        try {
            IOScope.cancel()
            MainScope?.cancel()
        } catch (thr: Throwable) {

        }

        if(folderItem == null) {
            ApplicationLoader.transientStrings.remove(UIManager.KEY_TRANSIENT_STRINGS_IMAGE_LIBRARY_SEARCH_TEXT)
            ApplicationLoader.transientStrings.remove(UIManager.KEY_TRANSIENT_STRINGS_IMAGE_FOLDERS_SEARCH_TEXT)
            ApplicationLoader.transientParcelables.remove(UIManager.KEY_TRANSIENT_PARCELABLE_IMAGE_FOLDERS_MAIN_LIST_RV_STATE)
        }
    }

    fun setSearchText(text: String?) {
        currentSearchText = text ?: ""
    }

    private suspend fun search(items: MutableList<ImageItem>) {
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