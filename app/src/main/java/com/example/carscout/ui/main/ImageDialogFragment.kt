package com.example.carscout.ui.main

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.carscout.R
import com.squareup.picasso.Picasso

class ImageDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_IMAGE_URI = "image_uri"

        fun newInstance(imageUri: Uri): ImageDialogFragment {
            val fragment = ImageDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_IMAGE_URI, imageUri)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val imageUri = arguments?.getParcelable<Uri>(ARG_IMAGE_URI)

        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.fragment_image_dialog, null)
        val imageView = view.findViewById<ImageView>(R.id.fullscreenImageView)

        Picasso.get()
            .load(imageUri)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error)
            .into(imageView)

        val builder = AlertDialog.Builder(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            .setView(view)

        val dialog = builder.create()

        imageView.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }
}
