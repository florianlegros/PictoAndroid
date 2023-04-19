package com.example.pictopicto

import android.view.DragEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

//gere les evenments d'un drag and drop
class MyDragListener : View.OnDragListener {
    override fun onDrag(v: View, e: DragEvent): Boolean {
        when (e.action) {
            DragEvent.ACTION_DRAG_STARTED -> {}
            DragEvent.ACTION_DRAG_ENTERED -> {}
            //event quand le drag est stopper
            DragEvent.ACTION_DRAG_EXITED -> {
                (e.localState as View).visibility = View.VISIBLE
            }
            //event quand l'objet est drop
            DragEvent.ACTION_DROP -> {
                //recuperes l'endroit du drop (ici c'est un recycler)
                val container = v as RecyclerView
                //recuperes les données de l'objet (ici c'est juste un string "nom de l'image")
                val item = e.clipData.getItemAt(0).intent.getStringExtra("item")

                //verifies si on a bien recup le nom de l'image
                if ((item.isNullOrBlank().not())) {
                    //recup l'adapter du recyler et on ajoute l'image
                    val adapter: MyItemRecyclerViewAdapter =
                        container.adapter as MyItemRecyclerViewAdapter
                    adapter.addItem(item.toString())

                    //verif si ya deja une image en dessous
                        val intercept =
                            container.findChildViewUnder(e.x, e.y)
                    println(intercept)
                    //si oui on echange les places
                    if (intercept != null && container.getChildAdapterPosition(intercept) != -1) {
                        println(container.getChildAdapterPosition(intercept))

                        adapter.onRowMoved((adapter.itemCount - 1), container.getChildAdapterPosition(intercept))
                    }

                }

            }
            //event quand le drag est arreter
            DragEvent.ACTION_DRAG_ENDED -> {
                (e.localState as View).visibility = View.VISIBLE
            }
            else -> {}
        }
        return true
    }


}