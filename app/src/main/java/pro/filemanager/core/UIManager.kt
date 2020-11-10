package pro.filemanager.core

import android.app.Activity
import android.content.Context
import android.content.res.Configuration

class UIManager {

    companion object {
        const val KEY_SP_SPAN_NUMBER_PORTRAIT_IMAGE_BROWSER = "spanNumberPortraitImageBrowser"
        const val KEY_SP_SPAN_NUMBER_LANDSCAPE_IMAGE_BROWSER = "spanNumberLandscapeImageBrowser"

        const val KEY_VIEWS_CREATED : String = "viewsCreated"

        fun getImageBrowserSpanNumber(activity: Activity) : Int {
            return if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                activity.getSharedPreferences(KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_LANDSCAPE_IMAGE_BROWSER, 6)
            else
                activity.getSharedPreferences(KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_PORTRAIT_IMAGE_BROWSER, 4)
        }

    }

}