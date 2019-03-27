package com.clockworks.incirkle.filePicker

import android.app.Activity
import android.support.v4.app.Fragment
import android.content.Intent

/**
 * Created by AndroidBuffer on 11/1/18.
 */
public class KotRequest
{

    /**
     * inner class for building the camera request
     */
    class Camera(context: Activity)
    {

        var requestCode = 101
        var intent: Intent
        var activity: Activity

        init
        {
            this.activity = context
            intent = Intent(context, KotlinFilePicker::class.java)
            intent.putExtra(KotConstants.EXTRA_FILE_SELECTION, KotConstants.SELECTION_TYPE_CAMERA)
        }

        /**
         * secondary constructor pass for initialization of camera class
         * @param requestCode by default it is 101 give a value to change it
         * @param context
         */
        constructor(context: Activity, requestCode: Int = 101) : this(context)
        {
            this.activity = context
            this.requestCode = requestCode
        }

        /**
         * set the request code & returns object of Camera class for launching the camera intent
         * @param requestCode for tracking in onActivityResult(...)
         * @return {@link Camera}
         */
        fun setRequestCode(requestCode: Int): Camera
        {
            this.requestCode = requestCode
            return this
        }

        /**
         * returns a intent for camera if initialized using constructor
         */
        fun getCameraIntent(): Intent
        {
            return intent
        }

        /**
         * starts intent to capture image from camera KotlinFilePicker.class
         */
        fun pick()
        {
            activity.startActivityForResult(intent, requestCode)
        }
    }

    /**
     * inner class for starting recording video
     */
    class Video(context: Activity)
    {
        private var activity: Activity
        private var requestCode = 102;
        private var intent: Intent

        init
        {
            this.activity = context
            intent = Intent(activity, KotlinFilePicker::class.java)
            intent.putExtra(KotConstants.EXTRA_FILE_SELECTION, KotConstants.SELECTION_TYPE_VIDEO)
        }

        /**
         * secondary constructor for changing the request code
         */
        constructor(context: Activity, requestCode: Int) : this(context)
        {
            this.activity = context
            this.requestCode = requestCode
        }

        /**
         * set the request code if missed in constructor initialization
         */
        fun setRequestCode(requestCode: Int): Video
        {
            this.requestCode = requestCode
            return this
        }

        /**
         * set the mime type of the file selection,
         * please note by default value is @see KotConstants FILE_TYPE_VIDEO_ALL
         * setting mime type will override the default one
         * @param mimeType
         */
        fun setMimeType(mimeType: String): Video
        {
            intent.putExtra(KotConstants.EXTRA_FILE_MIME_TYPE, mimeType)
            return this
        }

        /**
         * if set and initialized through constructor then we can get intent back,
         * use pick() instead if intent is not required
         * @return Intent
         */
        fun getVideoIntent(): Intent
        {
            return intent
        }

        /**
         * call this method after initialization in last
         */
        fun pick()
        {
            activity.startActivityForResult(intent, requestCode)
        }
    }

    /**
     * inner class for picking images from gallery
     */
    class Gallery(context: Activity)
    {
        private var activity: Activity
        private var requestCode = 103;
        private var intent: Intent

        init
        {
            this.activity = context
            intent = Intent(activity, KotlinFilePicker::class.java)
            intent.putExtra(KotConstants.EXTRA_FILE_SELECTION, KotConstants.SELECTION_TYPE_GALLERY)
            putMultiple(false)
        }

        /**
         * for selection single or multiple
         * @param multipleEnabled
         */
        private fun putMultiple(multipleEnabled: Boolean)
        {
            intent.putExtra(KotConstants.EXTRA_MULTIPLE_ENABLED, multipleEnabled)
        }

        /**
         * secondary constructor for changing the request code
         * @constructor pass activity and request code
         */
        constructor(context: Activity, requestCode: Int) : this(context)
        {
            this.activity = context
            this.requestCode = requestCode
        }

        /**
         * set the request code if missed in constructor initialization
         * @param requestCode
         * @return Gallery
         */
        fun setRequestCode(requestCode: Int): Gallery
        {
            this.requestCode = requestCode
            return this
        }

        /**
         * set the type selection single or multiple
         * @param isMultipleEnabled
         * @return Gallery
         */
        fun isMultiple(isMultipleEnabled: Boolean): Gallery
        {
            putMultiple(isMultipleEnabled)
            return this
        }

        /**
         * set the mime type of the file selection,
         * please note by default value is @see KotConstants FILE_TYPE_IMAGE_ALL
         * setting mime type will override the default one
         * @param mimeType
         */
        fun setMimeType(mimeType: String): Gallery
        {
            intent.putExtra(KotConstants.EXTRA_FILE_MIME_TYPE, mimeType)
            return this
        }

        /**
         * if set and initialized through constructor then we can get intent back
         * use pick() instead if intent is not required
         * @return @see Intent
         */
        fun getGalleryIntent(): Intent
        {
            return intent
        }

        /**
         * By default multiple selection is false @see multipleEnabled()
         * call this method after initialization in last
         */
        fun pick()
        {
            activity.startActivityForResult(intent, requestCode)
        }
    }


    class File()
    {
        private lateinit var activity: Activity
        private var mFragment: Fragment? = null
        private var requestCode = 104;
        private lateinit var intent: Intent

        // use for activity
        constructor(context: Activity) : this()
        {
            this.activity = context
            intent = Intent(activity, KotlinFilePicker::class.java)
            intent.putExtra(KotConstants.EXTRA_FILE_SELECTION, KotConstants.SELECTION_TYPE_FILE)
            putMultiple(false)
        }

        // use for fragment
        constructor(fragment: Fragment) : this()
        {
            this.mFragment = fragment
            this.activity = fragment.activity!!
            intent = Intent(activity, KotlinFilePicker::class.java)
            intent.putExtra(KotConstants.EXTRA_FILE_SELECTION, KotConstants.SELECTION_TYPE_FILE)
            putMultiple(false)
        }

        /**
         * for selection single or multiple
         * @param multipleEnabled
         */
        private fun putMultiple(multipleEnabled: Boolean)
        {
            intent.putExtra(KotConstants.EXTRA_MULTIPLE_ENABLED, multipleEnabled)
        }

        /**
         * secondary constructor for changing the request code
         * @constructor pass activity and request code
         */
        constructor(context: Activity, requestCode: Int) : this(context)
        {
            this.activity = context
            this.requestCode = requestCode
        }

        constructor(fragment: Fragment, requestCode: Int) : this(fragment)
        {
            this.mFragment = fragment
            this.activity = fragment.activity!!
            this.requestCode = requestCode
        }

        /**
         * set the request code if missed in constructor initialization
         * @param requestCode
         * @return Gallery
         */
        fun setRequestCode(requestCode: Int): File
        {
            this.requestCode = requestCode
            return this
        }

        /**
         * set the type selection single or multiple
         * @param isMultipleEnabled
         * @return Gallery
         */
        fun isMultiple(isMultipleEnabled: Boolean): File
        {
            putMultiple(isMultipleEnabled)
            return this
        }

        /**
         * set the mime type of the file selection,
         * please note by default value is @see KotConstants FILE_TYPE_FILE_ALL
         * setting mime type will override the default one
         * @param mimeType
         */
        fun setMimeType(mimeType: String): File
        {
            intent.putExtra(KotConstants.EXTRA_FILE_MIME_TYPE, mimeType)
            return this
        }

        /**
         * if set and initialized through constructor then we can get intent back
         * use pick() instead if intent is not required
         * @return @see Intent
         */
        fun getFileIntent(): Intent
        {
            return intent
        }

        /**
         * By default multiple selection is false @see multipleEnabled()
         * call this method after initialization in last
         */
        fun pick()
        {
            if (mFragment != null)
            {
                // called from fragment
                mFragment!!.startActivityForResult(intent, requestCode)
            }
            else
            {
                // called from activity
                activity.startActivityForResult(intent, requestCode)
            }
        }
    }


}