package com.example.pictopicto.ui.adapter

import android.content.ClipData
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.pictopicto.ItemMoveCallback
import com.example.pictopicto.databinding.FragmentItemBinding
import com.example.pictopicto.model.Pictogramme
import java.util.*


class PictoAdapter(
    private val values: ArrayList<Pictogramme>
) : RecyclerView.Adapter<PictoAdapter.ViewHolder>(),
    ItemMoveCallback.ItemTouchHelperContract {
    var clicklistener = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

        with(holder.itemView) {
            //donne les image en fonction d'un string
            holder.imageView.setImageResource(
                context.resources.getIdentifier(
                    item.pictoImgfile.replace(".png","").lowercase(),
                    "drawable",
                    context.packageName
                )
            )
            if (!clicklistener) {
                //on choisis un LongClick plutot qu'un Click normal
                holder.itemView.setOnLongClickListener {
                    val intent = Intent()
                    intent.putExtra("item", item)

                    //on peut stocker des données dans un ClipData, ici on stock le nom de l'image
                    val data: ClipData = ClipData.newIntent("intent", intent)

                    //on creer une ombre c'est a dire une copie trasparent de l'image qui s'affiche pendant le drag
                    val myShadow = View.DragShadowBuilder(it)

                    //on lance la drag and drop
                    it.startDragAndDrop(
                        data,
                        myShadow,
                        it,
                        0
                    )
                    //on rend l'image invisible
                    it.visibility = View.INVISIBLE
                    true
                }
            }
        }
    }

    override fun getItemCount(): Int {

        return values.size
    }

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView: ImageView = binding.imageView

    }

    //fonction pour echanger la place de deux images
    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(values, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(values, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(myViewHolder: ViewHolder?) {
    }

    override fun onRowClear(myViewHolder: ViewHolder?) {
    }

    //ajouter une image
    fun addItem(item: Pictogramme) {
        values.add(item)
        notifyItemInserted(itemCount - 1)
    }

    //enlever une image
    fun rmItem(position: Int) {
        values.removeAt(position)
        notifyItemRemoved(position)
    }
}

