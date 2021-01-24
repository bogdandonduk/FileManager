package pro.filemanager.images.library

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import kotlinx.coroutines.*
import pro.filemanager.core.VolatileValueHolder
import pro.filemanager.core.base.BaseViewModel
import pro.filemanager.core.tools.SearchTool
import pro.filemanager.core.tools.sort.SortTool
import pro.filemanager.core.ui.UIManager
import pro.filemanager.core.wrappers.PreferencesWrapper
import pro.filemanager.images.ImageRepo
import pro.filemanager.images.folders.ImageFolderItem
import java.io.File
import java.lang.IllegalStateException

@Parcelize
@SuppressLint("UNCHECKED_CAST")
class ImageLibraryViewModel(val context: @RawValue Context, var imageRepo: @RawValue ImageRepo, var folderItem: ImageFolderItem?) : BaseViewModel() {

    private var itemsLive: MutableLiveData<MutableList<ImageLibraryItem>>? = null

    init {
        currentSortOrder = PreferencesWrapper.getString(context, SortTool.KEY_SP_IMAGE_LIBRARY_SORT_ORDER, SortTool.SORT_ORDER_DATE_RECENT)
    }

    override suspend fun initItemsLive(context: Context) {
        if(itemsLive == null) {
           itemsLive = MutableLiveData(
                   when (currentSortOrder) {
                       SortTool.SORT_ORDER_DATE_RECENT -> {
                           if (folderItem != null) {
                               imageRepo.splitFoldersByDateRecent(context, false).forEach {
                                   if (it.data == folderItem!!.data) {
                                       folderItem!!.containedImages = it.containedImages
                                   }
                               }

                               folderItem!!.containedImages
                           } else {
                               imageRepo.loadItemsByDateRecent(context, false)
                           }
                       }
                       SortTool.SORT_ORDER_DATE_OLDEST -> {
                           if (folderItem != null) {
                               imageRepo.splitFoldersByDateOldest(context, false).forEach {
                                   if (it.data == folderItem!!.data) {
                                       folderItem!!.containedImages = it.containedImages
                                   }
                               }

                               folderItem!!.containedImages
                           } else {
                               imageRepo.loadItemsByDateOldest(context)
                           }
                       }
                       SortTool.SORT_ORDER_NAME_REVERSED -> {
                           if (folderItem != null) {
                               imageRepo.splitFoldersByNameReversed(context, false).forEach {
                                   if (it.data == folderItem!!.data) {
                                       folderItem!!.containedImages = it.containedImages
                                   }
                               }

                               folderItem!!.containedImages
                           } else {
                               imageRepo.loadItemsByNameReversed(context, false)
                           }
                       }
                       SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                           if (folderItem != null) {
                               imageRepo.splitFoldersByNameAlphabetic(context, false).forEach {
                                   if (it.data == folderItem!!.data) {
                                       folderItem!!.containedImages = it.containedImages
                                   }
                               }

                               folderItem!!.containedImages
                           } else {
                               imageRepo.loadItemsByNameAlphabetic(context)
                           }
                       }
                       SortTool.SORT_ORDER_SIZE_LARGEST -> {
                           if (folderItem != null) {
                               imageRepo.splitFoldersBySizeLargest(context, false).forEach {
                                   if (it.data == folderItem!!.data) {
                                       folderItem!!.containedImages = it.containedImages
                                   }
                               }

                               folderItem!!.containedImages
                           } else {
                               imageRepo.loadItemsBySizeLargest(context, false)
                           }
                       }
                       SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                           if (folderItem != null) {
                               imageRepo.splitFoldersBySizeSmallest(context, false).forEach {
                                   if (it.data == folderItem!!.data) {
                                       folderItem!!.containedImages = it.containedImages
                                   }
                               }

                               folderItem!!.containedImages
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

    override suspend fun assignItemsLive(context: Context, forceLoad: Boolean) {
        if(itemsLive != null) {
            when(currentSortOrder) {
                SortTool.SORT_ORDER_DATE_RECENT -> {
                    search(
                            if(folderItem != null) {
                                val imageItems = mutableListOf<ImageLibraryItem>()
                                imageRepo.loadItemsByDateRecent(context, forceLoad).forEach {
                                    if(File(it.data).parent == folderItem!!.data) {
                                        imageItems.add(it)
                                    }
                                }
                                folderItem!!.containedImages = imageItems
                                folderItem!!.containedImages
                            } else {
                                imageRepo.loadItemsByDateRecent(context, forceLoad)
                            }
                    )
                }
                SortTool.SORT_ORDER_DATE_OLDEST -> {
                    search(
                            if(folderItem != null) {
                                val imageItems = mutableListOf<ImageLibraryItem>()
                                imageRepo.loadItemsByDateRecent(context, forceLoad)
                                imageRepo.loadItemsByDateOldest(context).forEach {
                                    if(File(it.data).parent == folderItem!!.data) {
                                        imageItems.add(it)
                                    }
                                }
                                folderItem!!.containedImages = imageItems
                                folderItem!!.containedImages
                            } else {
                                imageRepo.loadItemsByDateRecent(context, forceLoad)
                                imageRepo.loadItemsByDateOldest(context)
                            }
                    )
                }
                SortTool.SORT_ORDER_NAME_REVERSED -> {
                    search(
                            if(folderItem != null) {
                                val imageItems = mutableListOf<ImageLibraryItem>()
                                imageRepo.loadItemsByNameReversed(context, forceLoad).forEach {
                                    if(File(it.data).parent == folderItem!!.data) {
                                        imageItems.add(it)
                                    }
                                }
                                folderItem!!.containedImages = imageItems
                                folderItem!!.containedImages
                            } else {
                                imageRepo.loadItemsByNameReversed(context, forceLoad)
                            }
                    )
                }
                SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                    search(
                            if(folderItem != null) {
                                val imageItems = mutableListOf<ImageLibraryItem>()
                                imageRepo.loadItemsByNameReversed(context, forceLoad)
                                imageRepo.loadItemsByNameAlphabetic(context).forEach {
                                    if(File(it.data).parent == folderItem!!.data) {
                                        imageItems.add(it)
                                    }
                                }
                                folderItem!!.containedImages = imageItems
                                folderItem!!.containedImages
                            } else {
                                imageRepo.loadItemsByNameReversed(context, forceLoad)
                                imageRepo.loadItemsByNameAlphabetic(context)
                            }
                    )
                }
                SortTool.SORT_ORDER_SIZE_LARGEST -> {
                    search(
                            if(folderItem != null) {
                                val imageItems = mutableListOf<ImageLibraryItem>()
                                imageRepo.loadItemsBySizeLargest(context, forceLoad).forEach {
                                    if(File(it.data).parent == folderItem!!.data) {
                                        imageItems.add(it)
                                    }
                                }
                                folderItem!!.containedImages = imageItems
                                folderItem!!.containedImages
                            } else {
                                imageRepo.loadItemsBySizeLargest(context, forceLoad)
                            }
                    )
                }
                SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                    search(
                            if(folderItem != null) {
                                val imageItems = mutableListOf<ImageLibraryItem>()
                                imageRepo.loadItemsBySizeLargest(context, forceLoad)
                                imageRepo.loadItemsBySizeSmallest(context).forEach {
                                    if(File(it.data).parent == folderItem!!.data) {
                                        imageItems.add(it)
                                    }
                                }
                                folderItem!!.containedImages = imageItems
                                folderItem!!.containedImages
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

    override fun setSortOrder(context: Context, sortOrder: String, isPersistable: Boolean) {
        super.setSortOrder(context, sortOrder, isPersistable)

        if(isPersistable) PreferencesWrapper.putString(context, SortTool.KEY_SP_IMAGE_LIBRARY_SORT_ORDER, sortOrder)
    }

    override fun onCleared() {
        super.onCleared()

        try {
            iOScope.cancel()
            mainScope.cancel()
            mainImmediateScope.cancel()
        } catch (thr: Throwable) {

        }

        if(folderItem == null) {
            VolatileValueHolder.strings.remove(UIManager.KEY_TRANSIENT_STRINGS_IMAGE_FOLDERS_SEARCH_TEXT)
            VolatileValueHolder.parcelables.remove(UIManager.KEY_TRANSIENT_PARCELABLE_IMAGE_FOLDERS_MAIN_LIST_RV_STATE)
        }
    }

    private suspend fun search(items: MutableList<ImageLibraryItem>) {
        while (searchInProgress) {
            delay(25)
        }

        searchInProgress = true

        if(currentSearchQuery.isNotEmpty()) {
            itemsLive?.postValue(SearchTool.search(currentSearchQuery, items))
        } else {
            itemsLive?.postValue(items)
        }
    }

    suspend fun getItemsLive(context: Context): LiveData<MutableList<ImageLibraryItem>> {
        initItemsLive(context)

        return itemsLive!!
    }

}