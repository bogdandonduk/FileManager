package pro.filemanager.core.ui

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import pro.filemanager.core.wrappers.PreferencesWrapper

object UIManager {
    const val KEY_SP_SPAN_NUMBER_IMAGE_LIBRARY_PORTRAIT = "spanNumberLibraryPortrait"
    const val KEY_SP_SPAN_NUMBER_IMAGE_LIBRARY_LANDSCAPE = "spanNumberLibraryLandscape"

    const val KEY_SP_SPAN_NUMBER_IMAGE_FOLDERS_PORTRAIT = "spanNumberFoldersPortrait"
    const val KEY_SP_SPAN_NUMBER_IMAGE_FOLDERS_LANDSCAPE = "spanNumberFoldersLandscape"

    const val KEY_TRANSIENT_PARCELABLE_IMAGE_FOLDERS_MAIN_LIST_RV_STATE = "imageFoldersMainListRvState"

    const val KEY_TRANSIENT_STRINGS_IMAGE_LIBRARY_SEARCH_TEXT = "librarySearchText"
    const val KEY_TRANSIENT_STRINGS_IMAGE_FOLDERS_SEARCH_TEXT = "foldersSearchText"

    fun getImageLibraryGridSpanNumberPortrait(context: Context) : Int {
        return context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_IMAGE_LIBRARY_PORTRAIT, 4)
    }

    fun getImageLibraryGridSpanNumberLandscape(context: Context) : Int {
        return context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_IMAGE_LIBRARY_LANDSCAPE, 6)
    }

    fun setImageLibraryGridSpanNumberPortrait(context: Context, spanNumber: Int) {
        context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SP_SPAN_NUMBER_IMAGE_LIBRARY_PORTRAIT, spanNumber).apply()
    }

    fun setImageLibraryGridSpanNumberLandscape(context: Context, spanNumber: Int) {
        context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SP_SPAN_NUMBER_IMAGE_LIBRARY_LANDSCAPE, spanNumber).apply()
    }

    fun getImageFoldersGridSpanNumberPortrait(context: Context) : Int {
        return context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_IMAGE_FOLDERS_PORTRAIT, 2)
    }

    fun getImageFoldersGridSpanNumberLandscape(context: Context) : Int {
        return context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_IMAGE_FOLDERS_LANDSCAPE, 4)
    }

    fun setImageFoldersGridSpanNumberPortrait(context: Context, spanNumber: Int) {
        context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SP_SPAN_NUMBER_IMAGE_FOLDERS_PORTRAIT, spanNumber).apply()
    }

    fun setImageFoldersGridSpanNumberLandscape(context: Context, spanNumber: Int) {
        context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SP_SPAN_NUMBER_IMAGE_FOLDERS_LANDSCAPE, spanNumber).apply()
    }

    fun getImageLibraryGridSpanNumber(activity: Activity) : Int {
        return if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getImageLibraryGridSpanNumberLandscape(activity)
        } else {
            getImageLibraryGridSpanNumberPortrait(activity)
        }
    }

    fun getImageFoldersGridSpanNumber(activity: Activity) : Int {
        return if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getImageFoldersGridSpanNumberLandscape(activity)
        } else {
            getImageFoldersGridSpanNumberPortrait(activity)
        }
    }
}