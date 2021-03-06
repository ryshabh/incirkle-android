package com.clockworks.incirkle.Models

import android.util.Patterns
import com.clockworks.incirkle.Interfaces.FirebaseDocument
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*

class User(): FirebaseDocument
{
    enum class Type { TEACHER, STUDENT; }
    enum class Gender { MALE, FEMALE; }

    companion object
    {
        fun collectionReference(): CollectionReference
        {
            return FirebaseFirestore.getInstance().collection("Users")
        }

        fun iterate(userIDs: ArrayList<String>, onIteration: (Int, String, Task<QuerySnapshot>) -> Unit)
        {
            for (index in 0 until userIDs.size)
            {
                val userID = userIDs[index]
                (if (Patterns.PHONE.matcher(userID).matches()) "phoneNumber"
                else if (Patterns.PHONE.matcher(userID).matches()) "emailAddress"
                else null)?.let()
                {
                    key ->
                    val task = User.collectionReference().whereEqualTo(key, userID).get()
                    onIteration(index, key, task)
                }
            }
        }
    }

    lateinit var firstName: String
    lateinit var lastName: String
    lateinit var gender: Gender
    var emailAddress: String? = null
    var phoneNumber: String? = null
    var profilepic: String? = null
    lateinit var type: Type
    var courses = ArrayList<DocumentReference>()

    @Exclude
    override var reference: DocumentReference? = null

    constructor(firstName: String, lastName: String, gender: Gender, emailAddress: String?, phoneNumber: String?, type: Type): this()
    {
        this.firstName = firstName
        this.lastName = lastName
        this.gender = gender
        this.emailAddress = emailAddress
        this.phoneNumber = phoneNumber
        this.type = type
    }

    fun fullName() : String
    {
        return "$firstName $lastName"
    }

    fun userID() : String
    {
        return this.emailAddress ?: this.phoneNumber ?: "User ID"
    }
}

fun FirebaseUser.documentReference(): DocumentReference
{
    return User.collectionReference().document(this.uid)
}