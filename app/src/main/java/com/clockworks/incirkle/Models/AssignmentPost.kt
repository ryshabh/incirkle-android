package com.clockworks.incirkle.Models

import com.clockworks.incirkle.Interfaces.FirebaseDocument
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import java.util.*

class AssignmentPost(): FirebaseDocument
{
    override var reference: DocumentReference? = null

    lateinit var name: String
    lateinit var details: String
    lateinit var dueDate: Timestamp
    var attachmentPath: String? = null
    lateinit var poster: DocumentReference
    lateinit var timestamp: Timestamp

    constructor(name: String, details: String, dueDate: Date, poster: DocumentReference) : this()
    {
        this.name = name
        this.details = details
        this.dueDate = Timestamp(dueDate)
        this.poster = poster
        this.timestamp = Timestamp(Date())
    }

    fun submissionsReferece(): CollectionReference
    {
        return this.reference!!.collection("Submissions")
    }

    class Submission: FirebaseDocument
    {
        override var reference: DocumentReference? = null

        lateinit var submitter: DocumentReference
        var submissionPath: String? = null
        lateinit var timestamp: Timestamp

        constructor()
        {
            this.submitter = FirebaseAuth.getInstance().currentUser!!.documentReference()
            this.timestamp = Timestamp(Date())
        }
    }
}