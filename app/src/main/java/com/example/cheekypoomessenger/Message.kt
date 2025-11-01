//package com.example.cheekypoomessenger
//
//class Message {
//    var message: String? = null
//    var senderId: String?= null
//    var messageId: String? = null
//
//    constructor(){}
//
//    constructor(message: String?, senderId:String? ){
//        this.message = message
//        this.senderId = senderId
//
//
//    }
//}

// Message.kt
package com.example.cheekypoomessenger

class Message {
    var message: String? = null
    var senderId: String? = null
    var messageId: String? = null
    var timestamp: Long? = null

    constructor() {}

    constructor(message: String?, senderId: String?, messageId: String?, timestamp: Long?) {
        this.message = message
        this.senderId = senderId
        this.messageId = messageId
        this.timestamp = timestamp
    }
}
