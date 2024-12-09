package com.diparoy.lunarwhisper.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.diparoy.lunarwhisper.R
import com.diparoy.lunarwhisper.data.ClickedUser
import java.util.Locale

class SearchAdapter(
    private val context: Context,
    private var users: ArrayList<ClickedUser>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>(), Filterable {

    private var filteredUsers: ArrayList<ClickedUser> = users

    interface OnItemClickListener {
        fun onItemClick(clickedUser: ClickedUser)
    }

    fun updateData(newData: List<ClickedUser>) {
        users.clear()
        users.addAll(newData)
        filteredUsers = ArrayList(users)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.users_images, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = filteredUsers[position]

        Glide.with(context)
            .load(user.imageUri)
            .placeholder(R.drawable.img_4)
            .into(holder.itemView.findViewById(R.id.user_image))

        holder.itemView.findViewById<TextView>(R.id.user_name).text = user.name

        Glide.with(context)
            .load(user.profileBackgroundImage)
            .into(holder.itemView.findViewById(R.id.profile_background_image))

        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(user)
        }
    }

    override fun getItemCount(): Int {
        return filteredUsers.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(filteredUsers[position])
                }
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = ArrayList<ClickedUser>()

                if (constraint.isNullOrBlank()) {
                    filteredList.addAll(users)
                } else {
                    val filterPattern = constraint.toString().trim().toLowerCase(Locale.ROOT)

                    for (user in users) {
                        if (user.name?.lowercase(Locale.ROOT)?.contains(filterPattern) == true) {
                            filteredList.add(user)
                        }
                    }
                }

                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredUsers.clear()
                filteredUsers.addAll(results?.values as ArrayList<ClickedUser>)
                notifyDataSetChanged()
            }
        }
    }
}
