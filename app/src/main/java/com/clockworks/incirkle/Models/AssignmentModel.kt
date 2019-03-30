package com.clockworks.incirkle.Models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Restaurant POJO.
 */
@IgnoreExtraProperties
data class AssignmentModel(
    var name: String? = null,
    var detail: String? = null,
    var dueDate: Timestamp? = null,
    var assignmentAttachmentInfo: String? = null,
    var solutionAttachmentInfo: String? = null,
    var attachmentCreator: String? = null,
    var timestamp: Timestamp = Timestamp.now()


)
{

}
