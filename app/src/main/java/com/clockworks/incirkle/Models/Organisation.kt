package com.clockworks.incirkle.Models

import com.clockworks.incirkle.Interfaces.FirebaseDocument
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FirebaseFirestore

class Organisation(): FirebaseDocument
{
    companion object
    {
        val reference =  FirebaseFirestore.getInstance().collection("Organisations")
    }

    lateinit var name: String
    lateinit var location: String

    @Exclude
    override var reference: DocumentReference? = null

    constructor(name: String, location: String) : this()
    {
        this.name = name
        this.location = location
    }
}