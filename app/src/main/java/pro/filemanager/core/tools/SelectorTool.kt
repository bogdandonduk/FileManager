package pro.filemanager.core.tools

import androidx.annotation.IntDef
import androidx.recyclerview.widget.RecyclerView
import pro.filemanager.HomeActivity

class SelectorTool(private var activity: HomeActivity, private var adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>){
    companion object {
        const val CLICK_SHORT = 1
        const val CLICK_LONG = 2

        @IntDef(CLICK_SHORT, CLICK_LONG)
        @Retention(AnnotationRetention.RUNTIME)
        annotation class ClickType
    }

    private var selectionMode = false
    val selectedPositions = mutableListOf<Int>()

    private var onBackBehavior = Runnable {
        val copy = mutableListOf<Int>()

        copy.addAll(selectedPositions)

        selectedPositions.clear()
        selectionMode = false

        copy.forEach {
            adapter.notifyItemChanged(it)
        }
    }

    fun handleClickInViewHolder(@ClickType clickType: Int, position: Int, offAction: Runnable = Runnable {}) {
        if(clickType == CLICK_SHORT) {
            if(!selectionMode)
                offAction.run()
            else {
                if(!selectedPositions.contains(position))
                    selectedPositions.add(position)
                else {
                    selectedPositions.remove(position)

                    if(selectedPositions.isEmpty()) {
                        selectionMode = false

                        assignOnBackBehavior()
                    }

                }

                adapter.notifyItemChanged(position)
            }
        } else {
            if(!selectionMode) {
                selectionMode = true
                selectedPositions.add(position)

                adapter.notifyItemChanged(position)

                assignOnBackBehavior()
            } else {
                if(!selectedPositions.contains(position))
                    selectedPositions.add(position)
                else {
                    selectedPositions.remove(position)

                    if(selectedPositions.isEmpty()) {
                        selectionMode = false

                        assignOnBackBehavior()
                    }
                }
            }

            adapter.notifyItemChanged(position)
         }
    }

    fun assignOnBackBehavior() {
        activity.onBackBehavior =
                if(selectionMode) onBackBehavior else null
    }
}