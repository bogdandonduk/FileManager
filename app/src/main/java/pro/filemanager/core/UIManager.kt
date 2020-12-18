package pro.filemanager.core

import android.app.Activity
import android.content.Context
import android.content.res.Configuration

object UIManager {
    const val KEY_SP_SPAN_NUMBER_LIBRARY_PORTRAIT = "spanNumberLibraryPortrait"
    const val KEY_SP_SPAN_NUMBER_LIBRARY_LANDSCAPE = "spanNumberLibraryLandscape"

    const val KEY_SP_SPAN_NUMBER_FOLDERS_PORTRAIT = "spanNumberFoldersPortrait"
    const val KEY_SP_SPAN_NUMBER_FOLDERS_LANDSCAPE = "spanNumberFoldersLandscape"

    const val KEY_TRANSIENT_PARCELABLE_FOLDERS_MAIN_LIST_RV_STATE = "foldersMainListRvState"

    const val KEY_TRANSIENT_STRINGS_LIBRARY_SEARCH_TEXT = "librarySearchText"
    const val KEY_TRANSIENT_STRINGS_FOLDERS_SEARCH_TEXT = "foldersSearchText"

    const val KEY_LAYOUT_RECREATED = "layoutRecreated"

    fun getItemGridSpanNumber(activity: Activity) : Int {
        return if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            activity.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_LIBRARY_LANDSCAPE, 6)
        else
            activity.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_LIBRARY_PORTRAIT, 4)
    }

    fun getAlbumGridSpanNumber(activity: Activity) : Int {
        return if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            activity.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_FOLDERS_LANDSCAPE, 4)
        else
            activity.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_FOLDERS_PORTRAIT, 2)
    }

}