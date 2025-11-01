//package com.example.cheekypoomessenger
//
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.appcompat.widget.Toolbar
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import android.widget.EditText
//import android.widget.ImageView
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.*
//
//class ChatActivity : AppCompatActivity() {
//
//    private lateinit var chatRecyclerView: RecyclerView
//    private lateinit var messageBox: EditText
//    private lateinit var sendButton: ImageView
//
//    private lateinit var messageAdapter: MessageAdapter
//    private lateinit var messageList: ArrayList<Message>
//    private lateinit var mDbRef: DatabaseReference
//
//    private var receiverRoom: String? = null
//    private var senderRoom: String? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_chat)
//
//        val name = intent.getStringExtra("name")
//        val receiverUid = intent.getStringExtra("uid")
//        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
//
//        // Toolbar setup
//        val toolbar = findViewById<Toolbar>(R.id.chatToolbar)
//        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setDisplayShowHomeEnabled(true)
//        supportActionBar?.title = name
//
//        if (senderUid == null || receiverUid == null) {
//            finish()
//            return
//        }
//
//        senderRoom = receiverUid + senderUid
//        receiverRoom = senderUid + receiverUid
//        mDbRef = FirebaseDatabase.getInstance().getReference()
//
//        chatRecyclerView = findViewById(R.id.chatRecyclerView)
//        messageBox = findViewById(R.id.messageBox)
//        sendButton = findViewById(R.id.sentButton)
//
//        messageList = ArrayList()
//        messageAdapter = MessageAdapter(this, messageList)
//
//        chatRecyclerView.layoutManager = LinearLayoutManager(this)
//        chatRecyclerView.adapter = messageAdapter
//
//        mDbRef.child("chats").child(senderRoom!!).child("messages")
//            .addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    messageList.clear()
//                    for (postSnapshot in snapshot.children) {
//                        val message = postSnapshot.getValue(Message::class.java)
//                        if (message != null) {
//                            messageList.add(message)
//                        }
//                    }
//                    messageAdapter.notifyDataSetChanged()
//                    chatRecyclerView.scrollToPosition(messageList.size - 1)
//                }
//
//                override fun onCancelled(error: DatabaseError) {}
//            })
//
//        sendButton.setOnClickListener {
//            val message = messageBox.text.toString().trim()
//            if (message.isNotEmpty()) {
//                val messageObject = Message(message, senderUid)
//
//                mDbRef.child("chats").child(senderRoom!!).child("messages")
//                    .push().setValue(messageObject).addOnSuccessListener {
//                        mDbRef.child("chats").child(receiverRoom!!).child("messages")
//                            .push().setValue(messageObject)
//                    }
//                messageBox.setText("")
//            }
//        }
//    }
//
//    override fun onSupportNavigateUp(): Boolean {
//        onBackPressed()
//        return true
//    }
//}


package com.example.cheekypoomessenger

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cheekypoomessenger.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ActionMode

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var mDbRef: DatabaseReference
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>

    private var receiverRoom: String? = null
    private var senderRoom: String? = null

    private var receiverUid: String? = null
    private var senderUid: String? = null

    // Action Mode for multi-select delete
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Get receiver data
        val name = intent.getStringExtra("name")
        receiverUid = intent.getStringExtra("uid")
        senderUid = FirebaseAuth.getInstance().currentUser?.uid

        // ✅ Setup custom toolbar
        setSupportActionBar(binding.chatToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = name

        binding.chatToolbar.setNavigationOnClickListener { finish() }

        // Firebase reference
        mDbRef = FirebaseDatabase.getInstance().reference

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        // ✅ Setup RecyclerView & Adapter
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = messageAdapter

        // ✅ Listen for item selection from adapter
        messageAdapter.selectionListener = { count ->
            if (count > 0) {
                if (actionMode == null)
                    actionMode = startSupportActionMode(actionModeCallback)
                actionMode?.title = "$count selected"
            } else {
                actionMode?.finish()
            }
        }

        // ✅ Fetch messages from Firebase
        mDbRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        message?.let { messageList.add(it) }
                    }
                    messageAdapter.notifyDataSetChanged()
                    binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        // ✅ Send button
        binding.sentButton.setOnClickListener {
            val message = binding.messageBox.text.toString().trim()
            binding.messageBox.setText("")

            if (message.isNotEmpty()) {
                val key = mDbRef.child("chats").child(senderRoom!!).child("messages").push().key
                val timestamp = System.currentTimeMillis()
                val msgObj = Message(message, senderUid, key, timestamp)

                mDbRef.child("chats").child(senderRoom!!).child("messages").child(key!!)
                    .setValue(msgObj).addOnSuccessListener {

                        mDbRef.child("chats").child(receiverRoom!!).child("messages").child(key)
                            .setValue(msgObj)
                    }
            }
        }
    }

    // ✅ Toolbar menu (always visible)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_select_all -> selectAllMessages()
            R.id.action_delete -> confirmDeleteAll()
        }
        return super.onOptionsItemSelected(item)
    }

    // ✅ ActionMode callback for long-press delete
    private val actionModeCallback = object : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.action_mode_menu, menu)
            return true
        }

        override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?) = false

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.action_delete -> {
                    deleteSelectedMessages()
                    true
                }
                R.id.action_select_all -> {
                    selectAllMessages()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            messageAdapter.selectedIds.clear()
            messageAdapter.notifyDataSetChanged()
            actionMode = null
        }
    }

    private fun deleteSelectedMessages() {
        val selected = messageAdapter.selectedIds.toList()
        val updates = hashMapOf<String, Any?>()

        selected.forEach { id ->
            updates["chats/$senderRoom/messages/$id"] = null
            updates["chats/$receiverRoom/messages/$id"] = null
        }

        mDbRef.updateChildren(updates).addOnCompleteListener {
            actionMode?.finish()
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectAllMessages() {
        messageAdapter.selectedIds.clear()
        messageList.forEach { msg -> msg.messageId?.let { messageAdapter.selectedIds.add(it) } }
        messageAdapter.notifyDataSetChanged()
        messageAdapter.selectionListener?.invoke(messageAdapter.selectedIds.size)
    }

    private fun confirmDeleteAll() {
        AlertDialog.Builder(this)
            .setTitle("Delete All Messages?")
            .setMessage("This will delete chat permanently.")
            .setPositiveButton("Delete") { _, _ -> deleteAllMessages() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAllMessages() {
        val updates = hashMapOf<String, Any?>(
            "chats/$senderRoom/messages" to null,
            "chats/$receiverRoom/messages" to null
        )

        mDbRef.updateChildren(updates).addOnCompleteListener {
            actionMode?.finish()
            Toast.makeText(this, "All messages deleted", Toast.LENGTH_SHORT).show()
        }
    }
}
