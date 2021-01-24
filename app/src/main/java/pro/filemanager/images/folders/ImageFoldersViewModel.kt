package pro.filemanager.images.folders

import android.content.Context
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import kotlinx.coroutines.*
import pro.filemanager.ApplicationLoader
import pro.filemanager.core.wrappers.PreferencesWrapper
import pro.filemanager.core.base.BaseViewModel
import pro.filemanager.core.tools.SearchTool
import pro.filemanager.core.tools.sort.SortTool
import pro.filemanager.images.ImageRepo

@Parcelize
class ImageFoldersViewModel(val context: @RawValue Context, var imageRepo: @RawValue ImageRepo) : BaseViewModel() {
    private var itemsLive: MutableLiveData<MutableList<ImageFolderItem>>? = null

    var librarySortOrder: String

    init {
        librarySortOrder = PreferencesWrapper.getString(ApplicationLoader.appContext, SortTool.KEY_SP_IMAGE_LIBRARY_SORT_ORDER, SortTool.SORT_ORDER_DATE_RECENT)
        currentSortOrder = PreferencesWrapper.getString(ApplicationLoader.appContext, SortTool.KEY_SP_IMAGE_FOLDERS_SORT_ORDER, SortTool.SORT_ORDER_DATE_RECENT)
    }

    override suspend fun initItemsLive(context: Context) {
        if(itemsLive == null) {
            itemsLive = MutableLiveData(
                    when(librarySortOrder) {
                        SortTool.SORT_ORDER_DATE_RECENT -> {
                            when (currentSortOrder) {
                                SortTool.SORT_ORDER_DATE_RECENT -> {
                                    imageRepo.splitFoldersByDateRecentLoadFoldersByDateRecent(context, false)
                                }
                                SortTool.SORT_ORDER_DATE_OLDEST -> {
                                    imageRepo.splitFoldersByDateRecentLoadFoldersByDateOldest(context)
                                }
                                SortTool.SORT_ORDER_NAME_REVERSED -> {
                                    imageRepo.splitFoldersByDateRecentLoadFoldersByNameReversed(context, false)
                                }
                                SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                                    imageRepo.splitFoldersByDateRecentLoadFoldersByNameAlphabetic(context)
                                }
                                SortTool.SORT_ORDER_SIZE_LARGEST -> {
                                    imageRepo.splitFoldersByDateRecentLoadFoldersBySizeLargest(context, false)
                                }
                                SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                                    imageRepo.splitFoldersByDateRecentLoadFoldersBySizeSmallest(context)
                                }
                                else -> {
                                    throw IllegalStateException("Invalid Sort Order")
                                }
                            }
                        }
                        SortTool.SORT_ORDER_DATE_OLDEST -> {
                            when (currentSortOrder) {
                                SortTool.SORT_ORDER_DATE_RECENT -> {
                                    imageRepo.splitFoldersByDateOldestLoadFoldersByDateRecent(context, false)
                                }
                                SortTool.SORT_ORDER_DATE_OLDEST -> {
                                    imageRepo.splitFoldersByDateOldestLoadFoldersByDateOldest(context)
                                }
                                SortTool.SORT_ORDER_NAME_REVERSED -> {
                                    imageRepo.splitFoldersByDateOldestLoadFoldersByNameReversed(context, false)
                                }
                                SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                                    imageRepo.splitFoldersByDateOldestLoadFoldersByNameAlphabetic(context)
                                }
                                SortTool.SORT_ORDER_SIZE_LARGEST -> {
                                    imageRepo.splitFoldersByDateOldestLoadFoldersBySizeLargest(context, false)
                                }
                                SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                                    imageRepo.splitFoldersByDateOldestLoadFoldersBySizeSmallest(context)
                                }
                                else -> {
                                    throw IllegalStateException("Invalid Sort Order")
                                }
                            }
                        }
                        SortTool.SORT_ORDER_SIZE_LARGEST -> {
                            when (currentSortOrder) {
                                SortTool.SORT_ORDER_DATE_RECENT -> {
                                    imageRepo.splitFoldersBySizeLargestLoadFoldersByDateRecent(context, false)
                                }
                                SortTool.SORT_ORDER_DATE_OLDEST -> {
                                    imageRepo.splitFoldersBySizeLargestLoadFoldersByDateOldest(context)
                                }
                                SortTool.SORT_ORDER_NAME_REVERSED -> {
                                    imageRepo.splitFoldersBySizeLargestLoadFoldersByNameReversed(context, false)
                                }
                                SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                                    imageRepo.splitFoldersBySizeLargestLoadFoldersByNameAlphabetic(context)
                                }
                                SortTool.SORT_ORDER_SIZE_LARGEST -> {
                                    imageRepo.splitFoldersBySizeLargestLoadFoldersBySizeLargest(context, false)
                                }
                                SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                                    imageRepo.splitFoldersBySizeLargestLoadFoldersBySizeSmallest(context)
                                }
                                else -> {
                                    throw IllegalStateException("Invalid Sort Order")
                                }
                            }
                        }
                        SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                            when (currentSortOrder) {
                                SortTool.SORT_ORDER_DATE_RECENT -> {
                                    imageRepo.splitFoldersBySizeSmallestLoadFoldersByDateRecent(context, false)
                                }
                                SortTool.SORT_ORDER_DATE_OLDEST -> {
                                    imageRepo.splitFoldersBySizeSmallestLoadFoldersByDateOldest(context)
                                }
                                SortTool.SORT_ORDER_NAME_REVERSED -> {
                                    imageRepo.splitFoldersBySizeSmallestLoadFoldersByNameReversed(context, false)
                                }
                                SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                                    imageRepo.splitFoldersBySizeSmallestLoadFoldersByNameAlphabetic(context)
                                }
                                SortTool.SORT_ORDER_SIZE_LARGEST -> {
                                    imageRepo.splitFoldersBySizeSmallestLoadFoldersBySizeLargest(context, false)
                                }
                                SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                                    imageRepo.splitFoldersBySizeSmallestLoadFoldersBySizeSmallest(context)
                                }
                                else -> {
                                    throw IllegalStateException("Invalid Sort Order")
                                }
                            }
                        }
                        SortTool.SORT_ORDER_NAME_REVERSED -> {
                            when (currentSortOrder) {
                                SortTool.SORT_ORDER_DATE_RECENT -> {
                                    imageRepo.splitFoldersByNameReversedLoadFoldersByDateRecent(context, false)
                                }
                                SortTool.SORT_ORDER_DATE_OLDEST -> {
                                    imageRepo.splitFoldersByNameReversedLoadFoldersByDateOldest(context)
                                }
                                SortTool.SORT_ORDER_NAME_REVERSED -> {
                                    imageRepo.splitFoldersByNameReversedLoadFoldersByNameReversed(context, false)
                                }
                                SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                                    imageRepo.splitFoldersByNameReversedLoadFoldersByNameAlphabetic(context)
                                }
                                SortTool.SORT_ORDER_SIZE_LARGEST -> {
                                    imageRepo.splitFoldersByNameReversedLoadFoldersBySizeLargest(context, false)
                                }
                                SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                                    imageRepo.splitFoldersByNameReversedLoadFoldersBySizeSmallest(context)
                                }
                                else -> {
                                    throw IllegalStateException("Invalid Sort Order")
                                }
                            }
                        }
                        SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                            when (currentSortOrder) {
                                SortTool.SORT_ORDER_DATE_RECENT -> {
                                    imageRepo.splitFoldersByNameAlphabeticLoadFoldersByDateRecent(context, false)
                                }
                                SortTool.SORT_ORDER_DATE_OLDEST -> {
                                    imageRepo.splitFoldersByNameAlphabeticLoadFoldersByDateOldest(context)
                                }
                                SortTool.SORT_ORDER_NAME_REVERSED -> {
                                    imageRepo.splitFoldersByNameAlphabeticLoadFoldersByNameReversed(context, false)
                                }
                                SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                                    imageRepo.splitFoldersByNameAlphabeticLoadFoldersByNameAlphabetic(context)
                                }
                                SortTool.SORT_ORDER_SIZE_LARGEST -> {
                                    imageRepo.splitFoldersByNameAlphabeticLoadFoldersBySizeLargest(context, false)
                                }
                                SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                                    imageRepo.splitFoldersByNameAlphabeticLoadFoldersBySizeSmallest(context)
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
        when(librarySortOrder) {
                SortTool.SORT_ORDER_DATE_RECENT -> {
                    if(forceLoad) {
                        imageRepo.loadItemsByDateRecent(context, true)
                        imageRepo.splitFoldersByDateRecent(context, true)
                    }

                    when(currentSortOrder) {
                        SortTool.SORT_ORDER_DATE_RECENT -> {
                            search(imageRepo.splitFoldersByDateRecentLoadFoldersByDateRecent(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_DATE_OLDEST -> {
                            search(imageRepo.splitFoldersByDateRecentLoadFoldersByDateOldest(context))
                        }
                        SortTool.SORT_ORDER_NAME_REVERSED -> {
                            search(imageRepo.splitFoldersByDateRecentLoadFoldersByNameReversed(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                            search(imageRepo.splitFoldersByDateRecentLoadFoldersByNameAlphabetic(context))
                        }
                        SortTool.SORT_ORDER_SIZE_LARGEST -> {
                            search(imageRepo.splitFoldersByDateRecentLoadFoldersBySizeLargest(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                            search(imageRepo.splitFoldersByDateRecentLoadFoldersBySizeSmallest(context))
                        }
                        else -> {
                            throw IllegalStateException("Invalid Sort Order")
                        }
                    }
                }
                SortTool.SORT_ORDER_DATE_OLDEST -> {
                    if(forceLoad) {
                        imageRepo.loadItemsByDateRecent(context, true)
                        imageRepo.splitFoldersByDateOldest(context, true)
                    }

                    when(currentSortOrder) {
                        SortTool.SORT_ORDER_DATE_RECENT -> {
                            search(imageRepo.splitFoldersByDateOldestLoadFoldersByDateRecent(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_DATE_OLDEST -> {
                            search(imageRepo.splitFoldersByDateOldestLoadFoldersByDateOldest(context))
                        }
                        SortTool.SORT_ORDER_NAME_REVERSED -> {
                            search(imageRepo.splitFoldersByDateOldestLoadFoldersByNameReversed(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                            search(imageRepo.splitFoldersByDateOldestLoadFoldersByNameAlphabetic(context))
                        }
                        SortTool.SORT_ORDER_SIZE_LARGEST -> {
                            search(imageRepo.splitFoldersByDateOldestLoadFoldersBySizeLargest(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                            search(imageRepo.splitFoldersByDateOldestLoadFoldersBySizeSmallest(context))
                        }
                        else -> {
                            throw IllegalStateException("Invalid Sort Order")
                        }
                    }
                }
                SortTool.SORT_ORDER_SIZE_LARGEST -> {
                    if(forceLoad) {
                        imageRepo.loadItemsBySizeLargest(context, true)
                        imageRepo.splitFoldersBySizeLargest(context, true)
                    }

                    when(currentSortOrder) {
                        SortTool.SORT_ORDER_DATE_RECENT -> {
                            search(imageRepo.splitFoldersBySizeLargestLoadFoldersByDateRecent(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_DATE_OLDEST -> {
                            search(imageRepo.splitFoldersBySizeLargestLoadFoldersByDateOldest(context))
                        }
                        SortTool.SORT_ORDER_NAME_REVERSED -> {
                            search(imageRepo.splitFoldersBySizeLargestLoadFoldersByNameReversed(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                            search(imageRepo.splitFoldersBySizeLargestLoadFoldersByNameAlphabetic(context))
                        }
                        SortTool.SORT_ORDER_SIZE_LARGEST -> {
                            search(imageRepo.splitFoldersBySizeLargestLoadFoldersBySizeLargest(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                            search(imageRepo.splitFoldersBySizeLargestLoadFoldersBySizeSmallest(context))
                        }
                        else -> {
                            throw IllegalStateException("Invalid Sort Order")
                        }
                    }
                }
                SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                    if(forceLoad) {
                        imageRepo.loadItemsBySizeLargest(context, true)
                        imageRepo.splitFoldersBySizeSmallest(context, true)
                    }

                    when(currentSortOrder) {
                        SortTool.SORT_ORDER_DATE_RECENT -> {
                            search(imageRepo.splitFoldersBySizeSmallestLoadFoldersByDateRecent(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_DATE_OLDEST -> {
                            search(imageRepo.splitFoldersBySizeSmallestLoadFoldersByDateOldest(context))
                        }
                        SortTool.SORT_ORDER_NAME_REVERSED -> {
                            search(imageRepo.splitFoldersBySizeSmallestLoadFoldersByNameReversed(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                            search(imageRepo.splitFoldersBySizeSmallestLoadFoldersByNameAlphabetic(context))
                        }
                        SortTool.SORT_ORDER_SIZE_LARGEST -> {
                            search(imageRepo.splitFoldersBySizeSmallestLoadFoldersBySizeLargest(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                            search(imageRepo.splitFoldersBySizeSmallestLoadFoldersBySizeSmallest(context))
                        }
                        else -> {
                            throw IllegalStateException("Invalid Sort Order")
                        }
                    }
                }
                SortTool.SORT_ORDER_NAME_REVERSED -> {
                    if (forceLoad) {
                        imageRepo.loadItemsByNameReversed(context, true)
                        imageRepo.splitFoldersByNameReversed(context, true)
                    }

                    when(currentSortOrder) {
                        SortTool.SORT_ORDER_DATE_RECENT -> {
                            search(imageRepo.splitFoldersByNameReversedLoadFoldersByDateRecent(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_DATE_OLDEST -> {
                            search(imageRepo.splitFoldersByNameReversedLoadFoldersByDateOldest(context))
                        }
                        SortTool.SORT_ORDER_NAME_REVERSED -> {
                            search(imageRepo.splitFoldersByNameReversedLoadFoldersByNameReversed(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                            search(imageRepo.splitFoldersByNameReversedLoadFoldersByNameAlphabetic(context))
                        }
                        SortTool.SORT_ORDER_SIZE_LARGEST -> {
                            search(imageRepo.splitFoldersByNameReversedLoadFoldersBySizeLargest(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                            search(imageRepo.splitFoldersByNameReversedLoadFoldersBySizeSmallest(context))
                        }
                        else -> {
                            throw IllegalStateException("Invalid Sort Order")
                        }
                    }
                }
                SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                    if(forceLoad) {
                        imageRepo.loadItemsByNameReversed(context, true)
                        imageRepo.splitFoldersByNameAlphabetic(context, true)
                    }

                    when(currentSortOrder) {
                        SortTool.SORT_ORDER_DATE_RECENT -> {
                            search(imageRepo.splitFoldersByNameAlphabeticLoadFoldersByDateRecent(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_DATE_OLDEST -> {
                            search(imageRepo.splitFoldersByNameAlphabeticLoadFoldersByDateOldest(context))
                        }
                        SortTool.SORT_ORDER_NAME_REVERSED -> {
                            search(imageRepo.splitFoldersByNameAlphabeticLoadFoldersByNameReversed(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_NAME_ALPHABETIC -> {
                            search(imageRepo.splitFoldersByNameAlphabeticLoadFoldersByNameAlphabetic(context))
                        }
                        SortTool.SORT_ORDER_SIZE_LARGEST -> {
                            search(imageRepo.splitFoldersByNameAlphabeticLoadFoldersBySizeLargest(context, forceLoad))
                        }
                        SortTool.SORT_ORDER_SIZE_SMALLEST -> {
                            search(imageRepo.splitFoldersByNameAlphabeticLoadFoldersBySizeSmallest(context))
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

    suspend fun getItemsLive(context: Context) : LiveData<MutableList<ImageFolderItem>> {
        initItemsLive(context)

        return itemsLive!!
    }

    override fun onCleared() {
        super.onCleared()

        try {
            iOScope.cancel()
            mainScope.cancel()
            mainImmediateScope.cancel()
        } catch (thr: Throwable) {

        }
    }

    override fun setSortOrder(context: Context, sortOrder: String, isPersistable: Boolean) {
        super.setSortOrder(context, sortOrder, isPersistable)

        if(isPersistable) PreferencesWrapper.putString(context, SortTool.KEY_SP_IMAGE_FOLDERS_SORT_ORDER, sortOrder)
    }

    fun setSearchText(text: String?) {
        currentSearchQuery = text ?: ""
    }

    suspend fun search(items: MutableList<ImageFolderItem>) {
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
}