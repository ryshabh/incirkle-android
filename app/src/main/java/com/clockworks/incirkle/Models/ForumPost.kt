package com.clockworks.incirkle.Models

import com.clockworks.incirkle.Interfaces.FirebaseDocument
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import java.util.*
import kotlin.collections.ArrayList

class ForumPost(): FirebaseDocument
{
    override var reference: DocumentReference? = null

    lateinit var name: String
    lateinit var description: String
    var attachmentPath: String? = null
    lateinit var poster: DocumentReference
    lateinit var timestamp: Timestamp

    constructor(name: String, description: String, poster: DocumentReference) : this()
    {
        this.name = name
        this.description = description
        this.poster = poster
        this.timestamp = Timestamp(Date())
    }

    class Comment()
    {
        lateinit var content: String
        lateinit var poster: DocumentReference
        lateinit var timestamp: Timestamp

        constructor(content: String, poster: DocumentReference) : this()
        {
            this.content = content
            this.poster = poster
            this.timestamp = Timestamp(Date())
        }
    }
}