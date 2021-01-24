package pro.filemanager.core.ui

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.widget.TextView
import pro.filemanager.core.wrappers.PreferencesWrapper

object UIManager {

    const val AUTOSCROLL_SPEED_MS_PER_INCH = 500

    const val KEY_SP_SPAN_NUMBER_IMAGE_LIBRARY_PORTRAIT = "spanNumberImageLibraryPortrait"
    const val KEY_SP_SPAN_NUMBER_IMAGE_LIBRARY_LANDSCAPE = "spanNumberImageLibraryLandscape"

    const val KEY_SP_SPAN_NUMBER_IMAGE_FOLDERS_PORTRAIT = "spanNumberImageFoldersPortrait"
    const val KEY_SP_SPAN_NUMBER_IMAGE_FOLDERS_LANDSCAPE = "spanNumberImageFoldersLandscape"

    const val KEY_SP_SPAN_NUMBER_VIDEO_LIBRARY_PORTRAIT = "spanNumberVideoLibraryPortrait"
    const val KEY_SP_SPAN_NUMBER_VIDEO_LIBRARY_LANDSCAPE = "spanNumberVideoLibraryLandscape"

    const val KEY_SP_SPAN_NUMBER_VIDEO_FOLDERS_PORTRAIT = "spanNumberVideoFoldersPortrait"
    const val KEY_SP_SPAN_NUMBER_VIDEO_FOLDERS_LANDSCAPE = "spanNumberVideoFoldersLandscape"

    const val KEY_SP_SPAN_NUMBER_AUDIO_LIBRARY_PORTRAIT = "spanNumberAudioLibraryPortrait"
    const val KEY_SP_SPAN_NUMBER_AUDIO_LIBRARY_LANDSCAPE = "spanNumberAudioLibraryLandscape"

    const val KEY_SP_SPAN_NUMBER_AUDIO_FOLDERS_PORTRAIT = "spanNumberAudioFoldersPortrait"
    const val KEY_SP_SPAN_NUMBER_AUDIO_FOLDERS_LANDSCAPE = "spanNumberAudioFoldersLandscape"

    const val KEY_SP_SPAN_NUMBER_DOC_LIBRARY_PORTRAIT = "spanNumberDocLibraryPortrait"
    const val KEY_SP_SPAN_NUMBER_DOC_LIBRARY_LANDSCAPE = "spanNumberDocLibraryLandscape"

    const val KEY_SP_SPAN_NUMBER_DOC_FOLDERS_PORTRAIT = "spanNumberDocFoldersPortrait"
    const val KEY_SP_SPAN_NUMBER_DOC_FOLDERS_LANDSCAPE = "spanNumberDocFoldersLandscape"

    const val KEY_SP_SPAN_NUMBER_APK_LIBRARY_PORTRAIT = "spanNumberApkLibraryPortrait"
    const val KEY_SP_SPAN_NUMBER_APK_LIBRARY_LANDSCAPE = "spanNumberApkLibraryLandscape"

    const val KEY_SP_SPAN_NUMBER_APK_FOLDERS_PORTRAIT = "spanNumberApkFoldersPortrait"
    const val KEY_SP_SPAN_NUMBER_APK_FOLDERS_LANDSCAPE = "spanNumberApkFoldersLandscape"

    const val KEY_TRANSIENT_PARCELABLE_IMAGE_FOLDERS_MAIN_LIST_RV_STATE = "imageFoldersMainListRvState"

    const val KEY_TRANSIENT_STRINGS_IMAGE_LIBRARY_SEARCH_TEXT = "imageLibrarySearchText"
    const val KEY_TRANSIENT_STRINGS_IMAGE_FOLDERS_SEARCH_TEXT = "imageFoldersSearchText"

    const val KEY_TRANSIENT_PARCELABLE_VIDEO_FOLDERS_MAIN_LIST_RV_STATE = "videoFoldersMainListRvState"

    const val KEY_TRANSIENT_STRINGS_VIDEO_LIBRARY_SEARCH_TEXT = "videoLibrarySearchText"
    const val KEY_TRANSIENT_STRINGS_VIDEO_FOLDERS_SEARCH_TEXT = "videoFoldersSearchText"

    const val KEY_TRANSIENT_PARCELABLE_AUDIO_FOLDERS_MAIN_LIST_RV_STATE = "audioFoldersMainListRvState"

    const val KEY_TRANSIENT_STRINGS_AUDIO_LIBRARY_SEARCH_TEXT = "audioLibrarySearchText"
    const val KEY_TRANSIENT_STRINGS_AUDIO_FOLDERS_SEARCH_TEXT = "audioFoldersSearchText"

    const val KEY_TRANSIENT_PARCELABLE_DOC_FOLDERS_MAIN_LIST_RV_STATE = "docFoldersMainListRvState"

    const val KEY_TRANSIENT_STRINGS_DOC_LIBRARY_SEARCH_TEXT = "docLibrarySearchText"
    const val KEY_TRANSIENT_STRINGS_DOC_FOLDERS_SEARCH_TEXT = "docFoldersSearchText"

    const val KEY_TRANSIENT_PARCELABLE_APK_FOLDERS_MAIN_LIST_RV_STATE = "apkFoldersMainListRvState"

    const val KEY_TRANSIENT_STRINGS_APK_LIBRARY_SEARCH_TEXT = "apkLibrarySearchText"
    const val KEY_TRANSIENT_STRINGS_APK_FOLDERS_SEARCH_TEXT = "apkFoldersSearchText"

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

    fun setImageLibraryGridSpanNumber(activity: Activity, portraitSpanNumber: Int, landscapeSpanNumber: Int) {
        if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setImageLibraryGridSpanNumberLandscape(activity, landscapeSpanNumber)
        } else {
            setImageLibraryGridSpanNumberLandscape(activity, portraitSpanNumber)
        }
    }

    fun getImageFoldersGridSpanNumber(activity: Activity) : Int {
        return if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getImageFoldersGridSpanNumberLandscape(activity)
        } else {
            getImageFoldersGridSpanNumberPortrait(activity)
        }
    }

    fun setImageFoldersGridSpanNumber(activity: Activity, portraitSpanNumber: Int, landscapeSpanNumber: Int) {
        if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setImageFoldersGridSpanNumberLandscape(activity, landscapeSpanNumber)
        } else {
            setImageFoldersGridSpanNumberLandscape(activity, portraitSpanNumber)
        }
    }

    fun resizeImageLibraryListGrid(activity: Activity) : Int {
        return if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            when(getImageLibraryGridSpanNumberLandscape(activity)) {
                4 -> {
                    setImageLibraryGridSpanNumberLandscape(activity, 6)
                    6
                }
                6 -> {
                    setImageLibraryGridSpanNumberLandscape(activity, 8)
                    8
                }
                8 -> {
                    setImageLibraryGridSpanNumberLandscape(activity, 4)
                    4
                }
                else -> {
                    setImageLibraryGridSpanNumberLandscape(activity, 6)
                    6
                }
            }
        } else {
            when(getImageLibraryGridSpanNumberPortrait(activity)) {
                3 -> {
                    setImageLibraryGridSpanNumberPortrait(activity, 4)
                    4
                }
                4 -> {
                    setImageLibraryGridSpanNumberPortrait(activity, 5)
                    5
                }
                5 -> {
                    setImageLibraryGridSpanNumberPortrait(activity, 3)
                    3
                }
                else -> {
                    setImageLibraryGridSpanNumberPortrait(activity, 4)
                    4
                }
            }
        }
    }

    fun resizeImageFoldersListGrid(activity: Activity) : Int {
        return if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            when(getImageFoldersGridSpanNumberLandscape(activity)) {
                3 -> {
                    setImageFoldersGridSpanNumberLandscape(activity, 4)
                    4
                }
                4 -> {
                    setImageFoldersGridSpanNumberLandscape(activity, 5)
                    5
                }
                5 -> {
                    setImageFoldersGridSpanNumberLandscape(activity, 3)
                    3
                }
                else -> {
                    setImageFoldersGridSpanNumberLandscape(activity, 3)
                    3
                }
            }
        } else {
            when(getImageFoldersGridSpanNumberPortrait(activity)) {
                2 -> {
                    setImageFoldersGridSpanNumberPortrait(activity, 3)
                    3
                }
                3 -> {
                    setImageFoldersGridSpanNumberPortrait(activity, 4)
                    4
                }
                4 -> {
                    setImageFoldersGridSpanNumberPortrait(activity, 2)
                    2
                }
                else -> {
                    setImageFoldersGridSpanNumberPortrait(activity, 2)
                    2
                }
            }
        }
    }

    fun getVideoLibraryGridSpanNumberPortrait(context: Context) : Int {
        return context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_VIDEO_LIBRARY_PORTRAIT, 4)
    }

    fun getVideoLibraryGridSpanNumberLandscape(context: Context) : Int {
        return context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_VIDEO_LIBRARY_LANDSCAPE, 6)
    }

    fun setVideoLibraryGridSpanNumberPortrait(context: Context, spanNumber: Int) {
        context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SP_SPAN_NUMBER_VIDEO_LIBRARY_PORTRAIT, spanNumber).apply()
    }

    fun setVideoLibraryGridSpanNumberLandscape(context: Context, spanNumber: Int) {
        context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SP_SPAN_NUMBER_VIDEO_LIBRARY_LANDSCAPE, spanNumber).apply()
    }

    fun getVideoFoldersGridSpanNumberPortrait(context: Context) : Int {
        return context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_VIDEO_FOLDERS_PORTRAIT, 2)
    }

    fun getVideoFoldersGridSpanNumberLandscape(context: Context) : Int {
        return context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_VIDEO_FOLDERS_LANDSCAPE, 4)
    }

    fun setVideoFoldersGridSpanNumberPortrait(context: Context, spanNumber: Int) {
        context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SP_SPAN_NUMBER_VIDEO_FOLDERS_PORTRAIT, spanNumber).apply()
    }

    fun setVideoFoldersGridSpanNumberLandscape(context: Context, spanNumber: Int) {
        context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SP_SPAN_NUMBER_VIDEO_FOLDERS_LANDSCAPE, spanNumber).apply()
    }

    fun getVideoLibraryGridSpanNumber(activity: Activity) : Int {
        return if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getVideoLibraryGridSpanNumberLandscape(activity)
        } else {
            getVideoLibraryGridSpanNumberPortrait(activity)
        }
    }

    fun getVideoFoldersGridSpanNumber(activity: Activity) : Int {
        return if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getVideoFoldersGridSpanNumberLandscape(activity)
        } else {
            getVideoFoldersGridSpanNumberPortrait(activity)
        }
    }

    fun getAudioLibraryGridSpanNumberPortrait(context: Context) : Int {
        return context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_AUDIO_LIBRARY_PORTRAIT, 4)
    }

    fun getAudioLibraryGridSpanNumberLandscape(context: Context) : Int {
        return context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_AUDIO_LIBRARY_LANDSCAPE, 6)
    }

    fun setAudioLibraryGridSpanNumberPortrait(context: Context, spanNumber: Int) {
        context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SP_SPAN_NUMBER_AUDIO_LIBRARY_PORTRAIT, spanNumber).apply()
    }

    fun setAudioLibraryGridSpanNumberLandscape(context: Context, spanNumber: Int) {
        context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SP_SPAN_NUMBER_AUDIO_LIBRARY_LANDSCAPE, spanNumber).apply()
    }

    fun getAudioFoldersGridSpanNumberPortrait(context: Context) : Int {
        return context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_AUDIO_FOLDERS_PORTRAIT, 2)
    }

    fun getAudioFoldersGridSpanNumberLandscape(context: Context) : Int {
        return context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_AUDIO_FOLDERS_LANDSCAPE, 4)
    }

    fun setAudioFoldersGridSpanNumberPortrait(context: Context, spanNumber: Int) {
        context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SP_SPAN_NUMBER_AUDIO_FOLDERS_PORTRAIT, spanNumber).apply()
    }

    fun setAudioFoldersGridSpanNumberLandscape(context: Context, spanNumber: Int) {
        context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SP_SPAN_NUMBER_AUDIO_FOLDERS_LANDSCAPE, spanNumber).apply()
    }

    fun getAudioLibraryGridSpanNumber(activity: Activity) : Int {
        return if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getAudioLibraryGridSpanNumberLandscape(activity)
        } else {
            getAudioLibraryGridSpanNumberPortrait(activity)
        }
    }

    fun getAudioFoldersGridSpanNumber(activity: Activity) : Int {
        return if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getAudioFoldersGridSpanNumberLandscape(activity)
        } else {
            getAudioFoldersGridSpanNumberPortrait(activity)
        }
    }

    fun getDocLibraryGridSpanNumberPortrait(context: Context) : Int {
        return context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_DOC_LIBRARY_PORTRAIT, 4)
    }

    fun getDocLibraryGridSpanNumberLandscape(context: Context) : Int {
        return context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_DOC_LIBRARY_LANDSCAPE, 6)
    }

    fun setDocLibraryGridSpanNumberPortrait(context: Context, spanNumber: Int) {
        context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SP_SPAN_NUMBER_DOC_LIBRARY_PORTRAIT, spanNumber).apply()
    }

    fun setDocLibraryGridSpanNumberLandscape(context: Context, spanNumber: Int) {
        context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SP_SPAN_NUMBER_DOC_LIBRARY_LANDSCAPE, spanNumber).apply()
    }

    fun getDocFoldersGridSpanNumberPortrait(context: Context) : Int {
        return context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_DOC_FOLDERS_PORTRAIT, 2)
    }

    fun getDocFoldersGridSpanNumberLandscape(context: Context) : Int {
        return context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_DOC_FOLDERS_LANDSCAPE, 4)
    }

    fun setDocFoldersGridSpanNumberPortrait(context: Context, spanNumber: Int) {
        context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SP_SPAN_NUMBER_DOC_FOLDERS_PORTRAIT, spanNumber).apply()
    }

    fun setDocFoldersGridSpanNumberLandscape(context: Context, spanNumber: Int) {
        context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SP_SPAN_NUMBER_DOC_FOLDERS_LANDSCAPE, spanNumber).apply()
    }

    fun getDocLibraryGridSpanNumber(activity: Activity) : Int {
        return if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getDocLibraryGridSpanNumberLandscape(activity)
        } else {
            getDocLibraryGridSpanNumberPortrait(activity)
        }
    }

    fun getDocFoldersGridSpanNumber(activity: Activity) : Int {
        return if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getDocFoldersGridSpanNumberLandscape(activity)
        } else {
            getDocFoldersGridSpanNumberPortrait(activity)
        }
    }

    fun getApkLibraryGridSpanNumberPortrait(context: Context) : Int {
        return context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_APK_LIBRARY_PORTRAIT, 4)
    }

    fun getApkLibraryGridSpanNumberLandscape(context: Context) : Int {
        return context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_APK_LIBRARY_LANDSCAPE, 6)
    }

    fun setApkLibraryGridSpanNumberPortrait(context: Context, spanNumber: Int) {
        context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SP_SPAN_NUMBER_APK_LIBRARY_PORTRAIT, spanNumber).apply()
    }

    fun setApkLibraryGridSpanNumberLandscape(context: Context, spanNumber: Int) {
        context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SP_SPAN_NUMBER_APK_LIBRARY_LANDSCAPE, spanNumber).apply()
    }

    fun getApkFoldersGridSpanNumberPortrait(context: Context) : Int {
        return context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_APK_FOLDERS_PORTRAIT, 2)
    }

    fun getApkFoldersGridSpanNumberLandscape(context: Context) : Int {
        return context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).getInt(KEY_SP_SPAN_NUMBER_APK_FOLDERS_LANDSCAPE, 4)
    }

    fun setApkFoldersGridSpanNumberPortrait(context: Context, spanNumber: Int) {
        context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SP_SPAN_NUMBER_APK_FOLDERS_PORTRAIT, spanNumber).apply()
    }

    fun setApkFoldersGridSpanNumberLandscape(context: Context, spanNumber: Int) {
        context.getSharedPreferences(PreferencesWrapper.KEY_SP_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_SP_SPAN_NUMBER_APK_FOLDERS_LANDSCAPE, spanNumber).apply()
    }

    fun getApkLibraryGridSpanNumber(activity: Activity) : Int {
        return if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getApkLibraryGridSpanNumberLandscape(activity)
        } else {
            getApkLibraryGridSpanNumberPortrait(activity)
        }
    }

    fun getApkFoldersGridSpanNumber(activity: Activity) : Int {
        return if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getApkFoldersGridSpanNumberLandscape(activity)
        } else {
            getApkFoldersGridSpanNumberPortrait(activity)
        }
    }
}