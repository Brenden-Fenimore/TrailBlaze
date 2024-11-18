package com.example.trailblaze.ui.profile

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.trailblaze.R
import java.io.File
import java.io.FileOutputStream

class PostcardDialogFragment : DialogFragment() {

    private lateinit var imageUrl: String
    private lateinit var location: String
    private lateinit var uploadDate: String
    private lateinit var caption: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.postcard_layout, container, false)

        // Set views
        val photoImageView: ImageView = view.findViewById(R.id.photoImageView)
        val locationTextView: TextView = view.findViewById(R.id.locationTextView)
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        val captionText: TextView = view.findViewById(R.id.captionText)
        val confirmButton: Button = view.findViewById(R.id.confirmButton)

        // Load photo
        Glide.with(requireContext()).load(imageUrl).into(photoImageView)

        // Set location, date, and caption
        locationTextView.text = location
        dateTextView.text = uploadDate
        captionText.text = caption

        // Confirm button logic
        confirmButton.setOnClickListener {

            // Generate postcard image
            val postcardBitmap = createPostcardBitmap(view)

            // Share postcard
            sharePostcard(postcardBitmap)

            // Close dialog
            dismiss()
        }

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            imageUrl = it.getString("imageUrl") ?: "" // Fix here
            location = it.getString("location") ?: "Unknown Location"
            uploadDate = it.getString("uploadDate") ?: "Unknown Date"
            caption = it.getString("caption") ?: ""
        }
    }

    private fun createPostcardBitmap(view: View): Bitmap {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(view.height, View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }

    private fun sharePostcard(bitmap: Bitmap) {
        val cachePath = File(requireContext().cacheDir, "postcards")
        cachePath.mkdirs()
        val file = File(cachePath, "shared_postcard.png")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.close()

        val imageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, imageUri)
            type = "image/png"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share postcard via"))
    }

    companion object {
        fun newInstance(imageUrl: String, location: String, uploadDate: String, caption: String): PostcardDialogFragment {
            val args = Bundle().apply {
                putString("imageUrl", imageUrl)
                putString("location", location)
                putString("uploadDate", uploadDate)
                putString("caption", caption)
            }
            val fragment = PostcardDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }
}