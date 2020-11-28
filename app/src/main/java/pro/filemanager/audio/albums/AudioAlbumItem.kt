package pro.filemanager.audio.albums

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import pro.filemanager.audio.AudioItem
import java.io.File
import java.io.Serializable

@Parcelize
data class AudioAlbumItem(
        val data: String,
        val displayName: String = File(data).name,
        val containedImages: @RawValue MutableList<AudioItem> = mutableListOf()
    ) : Parcelable, Serializable