package ch.milog.kavavin.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.milog.kavavin.R
import ch.milog.kavavin.models.Bottle

class CellarListAdapter(private val activity: Activity, private val onItemClick: (bottle: Bottle) -> Unit) :
    RecyclerView.Adapter<CellarListAdapter.ViewHolder>() {

    private var bottles = ArrayList<Bottle>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bottle, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bottle = bottles[position]
        if (bottle.year != null) {
            holder.name?.text = bottle.name + " (" + bottle.year + ")"
        } else {
            holder.name?.text = bottle.name
        }
        holder.quantity?.text = "" + bottle.quantity + "x"
        when (bottle.type) {
            1L -> holder.type?.setImageResource(R.drawable.wine_bottle)
            2L -> holder.type?.setImageResource(R.drawable.wine_bottle_white)
            3L -> holder.type?.setImageResource(R.drawable.champagne_bottle)
            4L -> holder.type?.setImageResource(R.drawable.wine_bottle_other)
        }

    }

    override fun getItemCount(): Int {
        return bottles.size
    }

    inner class ViewHolder(itemView: View, private val onItemClick: (bottle: Bottle) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                onItemClick(bottles[adapterPosition])
            }
        }

        var name: TextView? = itemView.findViewById(R.id.name)
        var type: ImageView? = itemView.findViewById(R.id.type)
        var quantity: TextView? = itemView.findViewById(R.id.quantity)
    }

    fun setBottles(bottlesToAdd: ArrayList<Bottle>) {
        this.bottles.clear()
        this.bottles = ArrayList(bottlesToAdd)
        notifyDataSetChanged()
    }

    fun refreshBottle(bottle: Bottle) {
        for (i in bottles.indices) {
            if (bottles[i].id == bottle.id) {
                bottles[i] = bottle
                notifyItemChanged(i)
                break
            }
        }
    }
}