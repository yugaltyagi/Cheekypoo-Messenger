//package com.example.cheekypoomessenger
//
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.example.cheekypoomessenger.UserAdapter.UserViewHolder
//import com.google.firebase.auth.FirebaseAuth
//
//class MessageAdapter(val context: Context, val messageList: ArrayList<Message>):
//
//    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//        val ITEM_RECIVE = 1
//    val ITEM_SENT = 2
//
//
//
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        if (viewType == 1){
//            //inflate receive
//            val view: View = LayoutInflater.from(context).inflate(R.layout.recive, parent, false)
//            return ReciveViewHolder(view)
//        }else{
//            //inflaste save
//            val view: View = LayoutInflater.from(context).inflate(R.layout.sent, parent, false)
//            return sentViewHolder(view)
//        }
//
//
//
//    }
//
//    override fun getItemCount(): Int {
//
//        return messageList.size
//
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//
//        val currentMessage = messageList[position]
//
//        if (holder.javaClass == sentViewHolder::class.java){
//            val viewHolder = holder as sentViewHolder
//
//            holder.sentMessage.text = currentMessage.message
//        }else{
//            val viewHolder = holder as ReciveViewHolder
//            holder.reciveMessage.text = currentMessage.message
//        }
//
//    }
//
//    override fun getItemViewType(position: Int): Int {
//        val currentMessage = messageList[position]
//
//        if (FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId)){
//            return ITEM_SENT
//        }else{
//            return ITEM_RECIVE
//        }
//    }
//
//    class sentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
//
//        val sentMessage = itemView.findViewById<TextView>(R.id.txt_sent_message)
//
//    }
//
//    class ReciveViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
//        val reciveMessage = itemView.findViewById<TextView>(R.id.txt_receive_message)
//    }
//}


// MessageAdapter.kt
package com.example.cheekypoomessenger

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(
    val context: Context,
    val messageList: ArrayList<Message>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_RECEIVE = 1
    private val ITEM_SENT = 2

    // selection state
    val selectedIds = mutableSetOf<String>()

    // callback
    var selectionListener: ((selectedCount: Int) -> Unit)? = null
    var clickListener: ((message: Message) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_RECEIVE) {
            val view: View = LayoutInflater.from(context).inflate(R.layout.recive, parent, false)
            ReciveViewHolder(view)
        } else {
            val view: View = LayoutInflater.from(context).inflate(R.layout.sent, parent, false)
            sentViewHolder(view)
        }
    }

    override fun getItemCount(): Int = messageList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        val messageId = currentMessage.messageId

        if (holder is sentViewHolder) {
            holder.sentMessage.text = currentMessage.message
            bindSelection(holder.itemView, messageId)
            holder.itemView.setOnLongClickListener {
                toggleSelection(messageId)
                true
            }
            holder.itemView.setOnClickListener {
                clickOrSelect(messageId, currentMessage)
            }
        } else if (holder is ReciveViewHolder) {
            holder.reciveMessage.text = currentMessage.message
            bindSelection(holder.itemView, messageId)
            holder.itemView.setOnLongClickListener {
                toggleSelection(messageId)
                true
            }
            holder.itemView.setOnClickListener {
                clickOrSelect(messageId, currentMessage)
            }
        }
    }

    private fun bindSelection(itemView: View, messageId: String?) {
        if (messageId != null && selectedIds.contains(messageId)) {
            // selected visual
            itemView.setBackgroundColor(Color.parseColor("#D1E8FF")) // light blue highlight
        } else {
            // default
            itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun toggleSelection(messageId: String?) {
        if (messageId == null) return
        if (selectedIds.contains(messageId)) selectedIds.remove(messageId) else selectedIds.add(messageId)
        notifyDataSetChanged()
        selectionListener?.invoke(selectedIds.size)
    }

    private fun clickOrSelect(messageId: String?, message: Message) {
        // if any selected, treat click as toggle
        if (selectedIds.isNotEmpty()) {
            toggleSelection(messageId)
            return
        }

        // normal click when not selecting - can be used for other actions (copy, etc)
        clickListener?.invoke(message)
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if (FirebaseAuth.getInstance().currentUser?.uid == currentMessage.senderId) {
            ITEM_SENT
        } else ITEM_RECEIVE
    }

    class sentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessage: TextView = itemView.findViewById(R.id.txt_sent_message)
    }

    class ReciveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reciveMessage: TextView = itemView.findViewById(R.id.txt_receive_message)
    }
}
