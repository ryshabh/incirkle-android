package com.clockworks.incirkle.Models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import java.util.*

class ActivityPost()
{
    var reference: DocumentReference? = null

    lateinit var description: String
    lateinit var poster: DocumentReference
    // TODO: - Attachment
    lateinit var timestamp: Timestamp

    constructor(description: String, poster: DocumentReference) : this()
    {
        this.description = description
        this.poster = poster
        this.timestamp = Timestamp(Date())
    }
}