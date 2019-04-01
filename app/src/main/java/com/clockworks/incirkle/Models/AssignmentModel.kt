package com.clockworks.incirkle.Models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Restaurant POJO.
 */
@IgnoreExtraProperties
data class AssignmentModel(
    var name: String? = null,
    var detail: String? = null,
    var dueDate: Timestamp? = null,
    var assignmentAttachmentUrl: String? = null,
    var assignmentAttachmentFileType: String? = null,
    var attachmentCreator: DocumentReference? = null,
    var timestamp: Timestamp = Timestamp.now()
)
{

}
