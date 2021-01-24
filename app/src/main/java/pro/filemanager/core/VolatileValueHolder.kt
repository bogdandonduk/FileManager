package pro.filemanager.core

import android.os.Parcelable

object VolatileValueHolder {

    val parcelables: MutableMap<String, Parcelable?> = mutableMapOf()
    val strings: MutableMap<String, String?> = mutableMapOf()
}