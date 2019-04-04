package com.clockworks.incirkle.Models

import com.clockworks.incirkle.filePicker.KotResult
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Restaurant POJO.
 */
@IgnoreExtraProperties
data class AssignmentModel(
    var documentId: String? = null,
    var courseDocumentId: String? = null,
    var name: String? = null,
    var detail: String? = null,
    var dueDate: Timestamp? = null,
    var assignmentAttachmentUrl: String? = null,
    var assignmentAttachmentDetail: KotResult? = null,
    var attachmentCreator: DocumentReference? = null,
    var timestamp: Timestamp = Timestamp.now(),
    var user: User? = null,
    var lastSubmittedUrl:String?=null,
    var lastSumbittedTime:String?=null
)
{

}
