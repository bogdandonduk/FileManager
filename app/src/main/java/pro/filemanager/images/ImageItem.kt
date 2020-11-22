package pro.filemanager.images

import java.io.Serializable

data class ImageItem(
        val data: String,
        val displayName: String,
        val size: Int,
        val dateModified: Int,
        val dateAdded: Int
) : Serializable