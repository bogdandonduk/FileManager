package pro.filemanager.core.tools.toolbar_options

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pro.filemanager.databinding.LayoutToolbarBottomModalSheetItemBinding

class ToolBarBottomModalSheetAdapter(val context: Context, val toolBarOptionItems: MutableList<ToolBarOptionItem>, val layoutInflater: LayoutInflater) : RecyclerView.Adapter<ToolBarBottomModalSheetAdapter.ToolBarBottomModalSheetItemViewHolder>() {
    class ToolBarBottomModalSheetItemViewHolder(val binding: LayoutToolbarBottomModalSheetItemBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var toolBarOptionItem: ToolBarOptionItem

        init {
            binding.layoutToolBarBottomModalSheetItemRootLayout.setOnClickListener {
                if(this::toolBarOptionItem.isInitialized)
                    toolBarOptionItem.action.run()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolBarBottomModalSheetItemViewHolder =
            ToolBarBottomModalSheetItemViewHolder(LayoutToolbarBottomModalSheetItemBinding.inflate(layoutInflater, parent, false))

    override fun onBindViewHolder(holder: ToolBarBottomModalSheetItemViewHolder, position: Int) {
        holder.toolBarOptionItem = toolBarOptionItems[position]

        holder.binding.layoutToolBarBottomModalSheetItemIcon.setImageResource(holder.toolBarOptionItem.iconRes)

        holder.binding.layoutToolBarBottomModalSheetItemRootLayoutContent.run {
            post {
                holder.binding.layoutToolBarBottomModalSheetItemTitle.textSize = (this.width / 22).toFloat()
                holder.binding.layoutToolBarBottomModalSheetItemTitle.text = holder.toolBarOptionItem.title
            }
        }
    }

    override fun getItemCount(): Int = toolBarOptionItems.size
}