package pro.filemanager.core.tools.toolbar

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pro.filemanager.R
import pro.filemanager.databinding.LayoutBottomToolbarItemBinding

class ToolbarAdapter(val context: Context, val toolbarItems: MutableList<ToolbarItem>, val layoutInflater: LayoutInflater) : RecyclerView.Adapter<ToolbarAdapter.ToolBarBottomModalSheetItemViewHolder>() {

    class ToolBarBottomModalSheetItemViewHolder(val binding: LayoutBottomToolbarItemBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var toolbarItem: ToolbarItem

        init {
            binding.layoutToolBarBottomModalSheetItemRootLayout.setOnClickListener {
                if(this::toolbarItem.isInitialized)
                    toolbarItem.action.invoke()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolBarBottomModalSheetItemViewHolder =
            ToolBarBottomModalSheetItemViewHolder(LayoutBottomToolbarItemBinding.inflate(layoutInflater, parent, false))

    override fun onBindViewHolder(holder: ToolBarBottomModalSheetItemViewHolder, position: Int) {
        holder.toolbarItem = toolbarItems[position]

        holder.binding.layoutToolBarBottomModalSheetItemIcon.setImageResource(holder.toolbarItem.iconRes)

        holder.binding.layoutToolBarBottomModalSheetItemTitle.text = holder.toolbarItem.title
    }

    override fun getItemCount(): Int = toolbarItems.size
}