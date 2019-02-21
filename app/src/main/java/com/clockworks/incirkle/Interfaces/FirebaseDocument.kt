package com.clockworks.incirkle.Interfaces

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import java.lang.Exception

interface FirebaseDocument
{
    var reference: DocumentReference?
}

fun <T: FirebaseDocument>DocumentSnapshot.serialize(to: Class<T>): T
{
    this.toObject(to)?.let()
    {
        it.reference = this.reference
        return it
    }
    ?: run { throw Exception("Could not serialize document with ID ${this.id}") }
}