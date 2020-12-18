package pro.filemanager.core.tools

import pro.filemanager.images.ImageItem
import pro.filemanager.images.folders.ImageFolderItem

object SearchTool {
    fun searchImageItems(text: String, dataSet: MutableList<ImageItem>) : MutableList<ImageItem> =
            mutableListOf<ImageItem>().apply {
                dataSet.forEach {
                    if(it.displayName.startsWith(text, true)) {
                        add(it)
                    }
                }

                dataSet.forEach {
                    if(it.displayName.contains(text, true)  && !contains(it)) {
                        add(it)
                    }
                }
            }

    fun searchImageAlbumItems(text: String, dataSet: MutableList<ImageFolderItem>) : MutableList<ImageFolderItem> {
        val newDataSet: MutableList<ImageFolderItem> = mutableListOf()

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

//    fun searchAudioItems(text: String, dataSet: MutableList<AudioItem>) : MutableList<AudioItem> =
//            mutableListOf<AudioItem>().apply {
//                dataSet.forEach {
//                    if(it.displayName.startsWith(text, true)) {
//                        add(it)
//                    }
//                }
//
//                dataSet.forEach {
//                    if(it.displayName.contains(text, true)  && !contains(it)) {
//                        add(it)
//                    }
//                }
//            }
//
//    fun searchAudioAlbumItems(text: String, dataSet: MutableList<AudioAlbumItem>) : MutableList<AudioAlbumItem> {
//        val newDataSet: MutableList<AudioAlbumItem> = mutableListOf()
//
//        dataSet.forEach {
//            if(it.displayName.startsWith(text, true)) {
//                newDataSet.add(it)
//            }
//        }
//
//        dataSet.forEach {
//            if(it.displayName.contains(text, true) && !newDataSet.contains(it)) {
//                newDataSet.add(it)
//            }
//        }
//
//        return newDataSet
//    }
//
//    fun searchVideoItems(text: String, dataSet: MutableList<VideoItem>) : MutableList<VideoItem> =
//            mutableListOf<VideoItem>().apply {
//                dataSet.forEach {
//                    if(it.displayName.startsWith(text, true)) {
//                        add(it)
//                    }
//                }
//
//                dataSet.forEach {
//                    if(it.displayName.contains(text, true)  && !contains(it)) {
//                        add(it)
//                    }
//                }
//            }
//
//    fun searchVideoAlbumItems(text: String, dataSet: MutableList<VideoAlbumItem>) : MutableList<VideoAlbumItem> {
//        val newDataSet: MutableList<VideoAlbumItem> = mutableListOf()
//
//        dataSet.forEach {
//            if(it.displayName.startsWith(text, true)) {
//                newDataSet.add(it)
//            }
//        }
//
//        dataSet.forEach {
//            if(it.displayName.contains(text, true) && !newDataSet.contains(it)) {
//                newDataSet.add(it)
//            }
//        }
//
//        return newDataSet
//    }

}