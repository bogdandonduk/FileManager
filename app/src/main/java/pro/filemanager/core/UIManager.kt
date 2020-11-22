package pro.filemanager.core

import android.app.Activity
import android.content.Context
import android.content.res.Configuration

object UIManager {
    const val KEY_SP_SPAN_NUMBER_ITEM_PORTRAIT = "spanNumberItemPortrait"
    const val KEY_SP_SPAN_NUMBER_ITEM_LANDSCAPE = "spanNumberItemLandscape"

    const val KEY_SP_SPAN_NUMBER_ALBUM_PORTRAIT = "spanNumberItemPortrait"
    const val KEY_SP_SPAN_NUMBER_ALBUM_LANDSCAPE = "spanNumberItemLandscape"

    fun getItemGridSpanNumber(activity: Activity) : Int {
        return if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            activity.getSharedPreferences(KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_ITEM_LANDSCAPE, 6)
        else
            activity.getSharedPreferences(KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_ITEM_PORTRAIT, 4)
    }

    fun getAlbumGridSpanNumber(activity: Activity) : Int {
        return if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            activity.getSharedPreferences(KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_ALBUM_LANDSCAPE, 4)
        else
            activity.getSharedPreferences(KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_ALBUM_PORTRAIT, 2)
    }

}