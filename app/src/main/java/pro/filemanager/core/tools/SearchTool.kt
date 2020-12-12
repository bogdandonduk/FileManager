package pro.filemanager.core.tools

//import pro.filemanager.audio.albums.AudioAlbumItem
import pro.filemanager.images.ImageItem
import pro.filemanager.images.albums.ImageAlbumItem

//import pro.filemanager.audio.AudioItem

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

//    fun searchAudioItems(text: String, dataSet: MutableList<AudioItem>) : MutableList<AudioItem> {
//        val newDataSet: MutableList<AudioItem> = mutableListOf()
//
//        dataSet.forEach {
//            if(it.displayName.startsWith(text, true)) {
//                newDataSet.add(it)
//            }
//        }
//
//        dataSet.forEach {
//            if(it.displayName.contains(text, true)  && !newDataSet.contains(it)) {
//                newDataSet.add(it)
//            }
//        }
//
//        return newDataSet
//    }
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
}