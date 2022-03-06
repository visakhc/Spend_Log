package com.app.spendlog.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.spendlog.R
import com.app.spendlog.model.SpendModel
import java.util.*

class SpendAdapter(
    private val itemList: List<SpendModel>,
    private var mOnEachListener: OnEachListener,
) :
    RecyclerView.Adapter<SpendAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_spend, parent, false)
        return ViewHolder(view, mOnEachListener)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(itemView: View, var OnEachListener: OnEachListener) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var spendType: ImageView = itemView.findViewById(R.id.iv_spend_type)
        var amount: TextView = itemView.findViewById(R.id.tv_amount_spend)
        var date: TextView = itemView.findViewById(R.id.tv_date)
        var time: TextView = itemView.findViewById(R.id.tv_time)
        override fun onClick(v: View) {
            OnEachListener.onEachClick(adapterPosition)
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    interface OnEachListener {
        fun onEachClick(position: Int)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(itemList[position]) {
            holder.amount.text = amount
            holder.time.text = time
            holder.date.text = date

            when (spendType) {
                "General" -> holder.spendType.setImageResource(R.drawable.ic_rupees)
                "Food" -> holder.spendType.setImageResource(R.drawable.ic_food)
                "Fuel" -> holder.spendType.setImageResource(R.drawable.ic_fuel)
                "Recharge" -> holder.spendType.setImageResource(R.drawable.ic_recharge)
                "Bills" -> holder.spendType.setImageResource(R.drawable.ic_bill)
                "Movies" -> holder.spendType.setImageResource(R.drawable.ic_movies)
                "Online Shopping" -> holder.spendType.setImageResource(R.drawable.ic_shopping)
            }

        }
    }
}
