
package com.example.bchatclient.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bchatclient.data.models.Room
import com.example.bchatclient.databinding.ItemRoomBinding

class RoomAdapter(
    private val rooms: List<Room>,
    private val onRoomClick: (Room) -> Unit
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(rooms[position])
    }

    override fun getItemCount(): Int = rooms.size

    inner class RoomViewHolder(private val binding: ItemRoomBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(room: Room) {
            binding.tvRoomName.text = room.room_name
            binding.tvRoomDescription.text = room.description.ifEmpty { "No description" }
            binding.tvRoomType.text = if (room.is_private) "Private" else "Public"

            binding.root.setOnClickListener {
                onRoomClick(room)
            }
        }
    }
}
