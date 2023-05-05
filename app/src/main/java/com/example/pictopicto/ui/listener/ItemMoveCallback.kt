package com.example.pictopicto.ui.listener

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.pictopicto.ui.adapter.PictoAdapter


class ItemMoveCallback(private val adapter: ItemTouchHelperContract) :
    ItemTouchHelper.Callback() {
    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {}
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val flags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        return makeMovementFlags(flags, flags)
    }

    override fun onMove(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {

        adapter.onRowMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
        return true
    }

    override fun onSelectedChanged(
        viewHolder: RecyclerView.ViewHolder?,
        actionState: Int
    ) {

        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder is PictoAdapter.ViewHolder) {
                val myViewHolder: PictoAdapter.ViewHolder =
                    viewHolder
                adapter.onRowSelected(myViewHolder)
            }
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) {
        super.clearView(recyclerView, viewHolder)
        if (viewHolder is PictoAdapter.ViewHolder) {
            val myViewHolder: PictoAdapter.ViewHolder =
                viewHolder
            adapter.onRowClear(myViewHolder)
        }
    }

    interface ItemTouchHelperContract {
        fun onRowMoved(fromPosition: Int, toPosition: Int)
        fun onRowSelected(myViewHolder: PictoAdapter.ViewHolder?)
        fun onRowClear(myViewHolder: PictoAdapter.ViewHolder?)
    }
}