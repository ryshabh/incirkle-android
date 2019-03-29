package com.clockworks.incirkle.Models

import com.clockworks.incirkle.Interfaces.FirebaseDocument
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import java.util.*

class Comment(): FirebaseDocument
{
    override var reference: DocumentReference? = null

    lateinit var content: String
    lateinit var poster: DocumentReference
    lateinit var timestamp: Timestamp
    var attachmentPath: String? = null

    constructor(content: String, poster: DocumentReference) : this()
    {
        this.content = content
        this.poster = poster
        this.timestamp = Timestamp(Date())
    }


}