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

    private var imageUrl: String? = null
    private var location: String? = null
    private var uploadDate: String? = null
    private var caption: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.postcard_layout, container, false)

        // Set views
        val photoImageView: ImageView = view.findViewById(R.id.photoImageView)
        val locationTextView: TextView = view.findViewById(R.id.locationTextView)
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        val captionText: TextView = view.findViewById(R.id.captionText)
        val confirmButton: Button = view.findViewById(R.id.confirmButton)

        // Load photo
        Glide.with(requireContext())
            .load(imageUrl)
            .placeholder(R.drawable.trailblaze_logo)
            .error(R.drawable.no_image_available)
            .into(photoImageView)

        // Set location, date, and caption
        locationTextView.text = location
        dateTextView.text = uploadDate
        captionText.text = caption

        // Confirm button logic
        confirmButton.setOnClickListener {
            view?.post {

                // Before sharing the postcard
                confirmButton.visibility = View.GONE

                // Generate postcard image
                val postcardBitmap = createPostcardBitmap(view)

                // Share postcard
                sharePostcard(postcardBitmap)

                // After sharing is done
                confirmButton.visibility = View.VISIBLE

                // Close dialog
                dismiss()
            }
        }

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            imageUrl = it.getString("imageUrl") ?: ""
            location = it.getString("location") ?: "Unknown Location"
            uploadDate = it.getString("uploadDate") ?: "Unknown Date"
            caption = it.getString("caption") ?: ""
        }
    }

    private fun createPostcardBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(
            view.width.takeIf { it > 0 } ?: 1,
            view.height.takeIf { it > 0 } ?: 1,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }

    private fun sharePostcard(bitmap: Bitmap) {
        // Save bitmap to cache directory
        val cachePath = File(requireContext().cacheDir, "postcards")
        cachePath.mkdirs()
        val file = File(cachePath, "postcard.png")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }

        // Share the image
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Postcard"))
    }

    companion object {
        fun newInstance(imageUrl: String, location: String, uploadDate: String, caption: String): PostcardDialogFragment {
                return PostcardDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("imageUrl", imageUrl)
                    putString("location", location)
                    putString("uploadDate", uploadDate)
                    putString("caption", caption)
                }
            }
        }
    }
}