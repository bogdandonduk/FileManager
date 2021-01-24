package pro.filemanager.core.tools

import android.content.Context
import android.graphics.Color
import android.os.Vibrator
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.annotation.IntDef
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import elytrondesign.lib.android.dialogwrapper.DialogWrapper
import pro.filemanager.R
import pro.filemanager.home.HomeActivity

class SelectionTool {
    companion object {
        const val CLICK_SHORT = 1
        const val CLICK_LONG = 2

        @IntDef(CLICK_SHORT, CLICK_LONG)
        @Retention(AnnotationRetention.SOURCE)
        annotation class ClickType

        var clearWithConfirmation = true
    }

    var selectionMode = false
    val selectedPaths = mutableListOf<String>()

    var selectionCheckBoxSticky = false

    fun handleClickInViewHolder(
            @ClickType clickType: Int,
            context: Context,
            position: Int,
            path: String,
            adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
            refreshAction: (() -> Unit)? = null,
            offAction: (() -> Unit)? = null
    ) {
        if(clickType == CLICK_SHORT) {
            if(!selectionMode)
                offAction?.invoke()
            else {
                if(!selectedPaths.contains(path)) {
                    selectedPaths.add(path)
                } else {
                    selectedPaths.remove(path)
                }

                adapter.notifyItemChanged(position)
            }
        } else if(clickType == CLICK_LONG) {
            if(!selectionMode) {
                (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(50)

                selectionMode = true
                selectedPaths.add(path)

                updateAll(adapter)
            } else {
                if(!selectedPaths.contains(path)) {
                    selectedPaths.add(path)
                } else {
                    selectedPaths.remove(path)
                }

                adapter.notifyItemChanged(position)
            }
        }

        refreshAction?.invoke()
    }

    fun updateAll(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        for(i in 0 until adapter.itemCount) {
            adapter.notifyItemChanged(i)
        }
    }

    fun selectAll(items: MutableList<String>, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        selectedPaths.clear()
        selectedPaths.addAll(items)

        updateAll(adapter)
    }

    fun unselectAll(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        selectedPaths.clear()

        updateAll(adapter)
    }

    fun initOnBackCallback(
            activity: HomeActivity,
            parentLayout: ViewGroup,
            adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
            selectionCheckBox: CheckBox,
            selectionCheckBoxLayout: ViewGroup,
            toolbarLayout: ViewGroup,
            toolbar: Toolbar,
            tabsBarLayout: ViewGroup
    ) {
         activity.currentOnBackBehavior = if(selectionMode)
             {
                 {
                     if(clearWithConfirmation) {
                         DialogWrapper.buildAlertDialog(
                                 activity,
                                 parentLayout,
                                 activity.resources.getString(R.string.title_are_you_sure),
                                 activity.resources.getString(R.string.this_will_clear_your_selection),
                                 true,
                                 activity.resources.getString(R.string.title_confirm),
                                 {
                                     selectionMode = false

                                     unselectAll(adapter)

                                     if(selectionCheckBox.isChecked)
                                         selectionCheckBox.toggle()

                                     selectionCheckBoxLayout.visibility = View.GONE

                                     toolbarLayout.visibility = View.GONE
                                     tabsBarLayout.visibility = View.VISIBLE

                                     toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
                                     toolbar.setNavigationContentDescription(R.string.go_back)

                                     activity.currentOnBackBehavior = null
                                 },
                                 activity.resources.getString(R.string.go_back),
                                 {


                                 },
                                {

                                }
                         ).show()
                     } else {
                         selectionMode = false

                         unselectAll(adapter)

                         if(selectionCheckBox.isChecked)
                             selectionCheckBox.toggle()

                         selectionCheckBoxLayout.visibility = View.GONE

                         toolbarLayout.visibility = View.GONE
                         tabsBarLayout.visibility = View.VISIBLE

                         toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
                         toolbar.setNavigationContentDescription(R.string.go_back)

                         activity.currentOnBackBehavior = null

                         clearWithConfirmation = true
                     }
                 }
             }
         else null
    }

    fun differentiateItem(
            context: Context,
            path: String,
            thumbnailLayout: ViewGroup,
            checkMark: ImageView,
            uncheckedMark: ImageView,
    ) {
        if(selectionMode) {
            if(selectedPaths.contains(path)) {
                checkMark.visibility = View.VISIBLE
                uncheckedMark.visibility = View.INVISIBLE

                thumbnailLayout.setBackgroundColor(context.resources.getColor(R.color.dark_transparent_faint))

                checkMark.scaleX = 0f
                checkMark.scaleY = 0f

                checkMark.visibility = View.VISIBLE
                checkMark.animate().scaleX(1f).setDuration(150).start()
                checkMark.animate().scaleY(1f).setDuration(150).start()
            } else {
                thumbnailLayout.setBackgroundColor(Color.TRANSPARENT)

                checkMark.visibility = View.INVISIBLE
                uncheckedMark.visibility = View.VISIBLE
            }
        } else {
            thumbnailLayout.setBackgroundColor(Color.TRANSPARENT)

            checkMark.visibility = View.INVISIBLE
            uncheckedMark.visibility = View.INVISIBLE
        }
    }

    fun initSelectionState(
            activity: HomeActivity,
            parentLayout: ViewGroup,
            adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
            toolbarLayout: ViewGroup,
            toolbar: Toolbar,
            tabsBarLayout: ViewGroup,
            selectionCheckBoxLayout: ViewGroup,
            selectionCheckBox: CheckBox,
            selectionMode: Boolean,
            selectedItemCount: Int
    ) {
        if(selectionMode) {
            initOnBackCallback(activity, parentLayout, adapter, selectionCheckBox, selectionCheckBoxLayout, toolbarLayout, toolbar, tabsBarLayout)

            selectionCheckBoxLayout.visibility = View.VISIBLE

            selectionCheckBox.text = selectedItemCount.toString()

            tabsBarLayout.visibility = View.GONE
            toolbarLayout.visibility = View.VISIBLE

            toolbar.setNavigationIcon(R.drawable.ic_baseline_clear_24)
            toolbar.setNavigationContentDescription(R.string.clear_selection)
        } else {
            selectionCheckBoxLayout.visibility = View.GONE

            tabsBarLayout.visibility = View.VISIBLE
            toolbarLayout.visibility = View.GONE

            toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
            toolbar.setNavigationContentDescription(R.string.go_back)
        }
    }

    fun initSelectionCheckBox(selectionCheckBox: CheckBox, totalItemCount: Int) {
        if(totalItemCount > 0) {
            if(selectedPaths.size == totalItemCount && !selectionCheckBox.isChecked) {
                selectionCheckBoxSticky = true

                selectionCheckBox.toggle()

                selectionCheckBoxSticky = false
            } else if(selectedPaths.size != totalItemCount && selectionCheckBox.isChecked) {
                selectionCheckBoxSticky = true

                selectionCheckBox.toggle()

                selectionCheckBoxSticky = false
            }
        }
    }
}