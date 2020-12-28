package pro.filemanager.core.generics

import android.graphics.Typeface
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity
import pro.filemanager.R
import pro.filemanager.core.wrappers.PermissionWrapper

open class BaseSectionFragment : BaseFragment() {

    lateinit var pinchZoomGestureDetector: ScaleGestureDetector

    var translucentStatusBar = false
    var tabsBarVisible = false
    var toolBarVisible = false

    open fun launchCore() {

    }

    open fun notifyListEmpty(adapterItemCount: Int, text: TextView) {
        if(adapterItemCount > 0)
            text.visibility = View.GONE
        else
            text.visibility = View.VISIBLE
    }

    open fun initTabsBar(
            tabsBarLayout: ViewGroup,
            highlightTitle: TextView,
            indicator: ViewGroup,
            libraryLayout: ViewGroup,
            libraryTitle: TextView,
            libraryText: String,
            libraryOnClickListener: View.OnClickListener,
            foldersLayout: ViewGroup,
            foldersTitle: TextView,
            foldersOnClickListener: View.OnClickListener,
    ) {
        tabsBarLayout.post {
            tabsBarLayout.height.let {
                libraryTitle.textSize = (it / 10).toFloat()
                libraryTitle.text = libraryText

                foldersTitle.textSize = (it / 10).toFloat()
                foldersTitle.text = resources.getText(R.string.title_folders)
            }

            highlightTitle.setTypeface(null, Typeface.BOLD)
            indicator.visibility = View.VISIBLE
        }

        libraryLayout.setOnClickListener(libraryOnClickListener)
        foldersLayout.setOnClickListener(foldersOnClickListener)
    }

    open fun initSelectionState(
            viewModel: BaseViewModel,
            activity: HomeActivity,
            adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
            toolBarLayout: ViewGroup,
            tabsBarLayout: ViewGroup,
            selectionBarLayout: ViewGroup,
            selectionCheckBox: CheckBox
    ) {
        viewModel.selectionTool.initSelectionState(
                activity,
                adapter,
                toolBarLayout,
                tabsBarLayout,
                selectionBarLayout,
                selectionCheckBox,
                viewModel.selectionTool.selectionMode,
                viewModel.selectionTool.selectedPaths.size
        )
    }

    open fun initSelectionBar(
            selectionCheckBox: CheckBox,
            selectionCheckBoxOnCheckedChangeListener: CompoundButton.OnCheckedChangeListener
    ) {
        selectionCheckBox.setOnCheckedChangeListener(selectionCheckBoxOnCheckedChangeListener)
    }

    open fun initToolBar(
            toolBarLayout: ViewGroup,
            actionButtonLayouts: MutableList<ViewGroup>,
            actionButtonOnClickListeners: MutableList<View.OnClickListener>,
            actionButtonTitles: MutableList<TextView>
    ) {
        toolBarLayout.post {
            toolBarLayout.height.let { height ->
                actionButtonTitles.forEach {
                    it.textSize = (height / 12).toFloat()
                }
            }

            toolBarLayout.visibility = View.GONE
        }

        actionButtonLayouts.forEachIndexed { i: Int, actionButtonLayout: ViewGroup ->
            actionButtonLayout.setOnClickListener(actionButtonOnClickListeners[i])
        }
    }

    fun handleUserReturnFromAppSettings(activity: HomeActivity) {
        if(ApplicationLoader.isUserSentToAppDetailsSettings && PermissionWrapper.checkExternalStoragePermissions(frContext)) {
            launchCore()
            ApplicationLoader.isUserSentToAppDetailsSettings = false
        } else if(ApplicationLoader.isUserSentToAppDetailsSettings && !PermissionWrapper.checkExternalStoragePermissions(frContext)) {
            ApplicationLoader.isUserSentToAppDetailsSettings = false
            activity.onBackPressed()
        }
    }

    open fun updateListState() {

    }
}