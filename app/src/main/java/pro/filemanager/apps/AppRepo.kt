package pro.filemanager.apps

import pro.filemanager.images.ImageRepo

class AppRepo {
    companion object {
        @Volatile private var instance: AppRepo? = null

        fun getSingleton() : AppRepo {
            if(instance == null) instance = AppRepo()

            return instance!!
        }
    }

    @Volatile private var loadedItemsSortedByDateRecent: MutableList<AppItem>? = null
    @Volatile private var loadedItemsSortedBySizeLargest: MutableList<AppItem>? = null
    @Volatile private var loadedItemsSortedByNameReversed: MutableList<AppItem>? = null
}