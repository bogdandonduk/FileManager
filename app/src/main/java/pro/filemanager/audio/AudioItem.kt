package pro.filemanager.audio

import java.io.Serializable

data class AudioItem(
        val data: String,
        val displayName: String,
        val size: Int,
        val dateModified: Int,
        val dateAdded: Int
) : Serializable