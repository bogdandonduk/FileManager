package pro.filemanager.core.tools

import pro.filemanager.images.ImageItem
import pro.filemanager.images.albums.ImageAlbumItem

object SearchTool {
    fun searchImageItems(text: String, dataSet: MutableList<ImageItem>) : MutableList<ImageItem> {
        val newDataSet: MutableList<ImageItem> = mutableListOf()

        dataSet.forEach {
            if(it.displayName.startsWith(text, true)) {
                newDataSet.add(it)
            }
        }

        dataSet.forEach {
            if(it.displayName.contains(text, true)  && !newDataSet.contains(it)) {
                newDataSet.add(it)
            }
        }

        return newDataSet
    }

    fun searchImageAlbumItems(text: String, dataSet: MutableList<ImageAlbumItem>) : MutableList<ImageAlbumItem> {
        val newDataSet: MutableList<ImageAlbumItem> = mutableListOf()

        dataSet.forEach {
            if(it.displayName.startsWith(text, true)) {
                newDataSet.add(it)
            }
        }

        dataSet.forEach {
            if(it.displayName.contains(text, true) && !newDataSet.contains(it)) {
                newDataSet.add(it)
            }
        }

        return newDataSet
    }
}