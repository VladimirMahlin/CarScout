package com.example.carscout.ui.main.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.carscout.R
import com.example.carscout.databinding.FragmentProfileBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null
    private var uploadedImageUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserProfile()

        setupImagePickerLauncher()

        binding.userAvatarImageView.setOnClickListener {
            pickImage()
        }

        binding.saveProfileButton.setOnClickListener {
            saveUserProfile()
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioUrl -> {
                    binding.avatarUrlInputLayout.visibility = View.VISIBLE
                    binding.userAvatarImageView.isClickable = false
                }
                R.id.radioUpload -> {
                    binding.avatarUrlInputLayout.visibility = View.GONE
                    binding.userAvatarImageView.isClickable = true
                }
            }
        }
    }

    private fun setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    selectedImageUri = uri
                    binding.userAvatarImageView.setImageURI(uri)
                }
            } else {
                Toast.makeText(context, "Image selection failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImage() {
        ImagePicker.with(this)
            .cropSquare()
            .compress(1024)
            .maxResultSize(512, 512)
            .createIntent { intent ->
                imagePickerLauncher.launch(intent)
            }
    }

    private fun loadUserProfile() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        setContentVisibility(View.GONE)
        setInteractionsEnabled(false)

        val userId = auth.currentUser?.uid
        userId?.let {
            firestore.collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = document.getString("name") ?: ""
                        val userEmail = document.getString("email") ?: ""
                        val userCity = document.getString("city") ?: ""
                        val avatarUrl = document.getString("avatarUrl") ?: ""

                        binding.userNameEditText.setText(userName)
                        binding.userEmailEditText.setText(userEmail)
                        binding.userCityEditText.setText(userCity)
                        binding.userAvatarUrlEditText.setText(avatarUrl)

                        if (avatarUrl.isNotEmpty()) {
                            Picasso.get()
                                .load(avatarUrl)
                                .placeholder(R.drawable.placeholder_avatar)
                                .into(binding.userAvatarImageView)
                        } else {
                            Picasso.get()
                                .load(R.drawable.placeholder_avatar)
                                .into(binding.userAvatarImageView)
                        }
                    }
                    binding.loadingProgressBar.visibility = View.GONE
                    setContentVisibility(View.VISIBLE)
                    setInteractionsEnabled(true)
                }
                .addOnFailureListener {
                    binding.loadingProgressBar.visibility = View.GONE
                    setContentVisibility(View.VISIBLE)
                    setInteractionsEnabled(true)
                    Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveUserProfile() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        setContentVisibility(View.GONE)
        setInteractionsEnabled(false)

        val userId = auth.currentUser?.uid
        userId?.let {
            if (binding.radioUpload.isChecked && selectedImageUri != null) {
                uploadImageToFirebaseStorage { imageUrl ->
                    uploadedImageUrl = imageUrl
                    saveUserProfileData(imageUrl)
                }
            } else {
                val avatarUrl = binding.userAvatarUrlEditText.text.toString()
                saveUserProfileData(avatarUrl)
            }
        }
    }

    private fun uploadImageToFirebaseStorage(onSuccess: (String) -> Unit) {
        val storageRef = storage.reference.child("avatars/${UUID.randomUUID()}.jpg")
        selectedImageUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        onSuccess(downloadUri.toString())
                    }
                }
                .addOnFailureListener {
                    binding.loadingProgressBar.visibility = View.GONE
                    setContentVisibility(View.VISIBLE)
                    setInteractionsEnabled(true)
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveUserProfileData(avatarUrl: String) {
        val userId = auth.currentUser?.uid
        userId?.let {
            val userProfile = mapOf(
                "name" to binding.userNameEditText.text.toString(),
                "email" to binding.userEmailEditText.text.toString(),
                "city" to binding.userCityEditText.text.toString(),
                "avatarUrl" to avatarUrl
            )

            firestore.collection("users").document(it).set(userProfile, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    Picasso.get()
                        .load(avatarUrl)
                        .placeholder(R.drawable.placeholder_avatar)
                        .into(binding.userAvatarImageView)

                    binding.loadingProgressBar.visibility = View.GONE
                    setContentVisibility(View.VISIBLE)
                    setInteractionsEnabled(true)
                }
                .addOnFailureListener {
                    binding.loadingProgressBar.visibility = View.GONE
                    setContentVisibility(View.VISIBLE)
                    setInteractionsEnabled(true)
                    Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun setContentVisibility(visibility: Int) {
        binding.avatarCard.visibility = visibility
        binding.nameInputLayout.visibility = visibility
        binding.emailInputLayout.visibility = visibility
        binding.cityInputLayout.visibility = visibility
        binding.avatarUrlInputLayout.visibility = visibility
        binding.saveProfileButton.visibility = visibility
    }

    private fun setInteractionsEnabled(enabled: Boolean) {
        binding.userNameEditText.isEnabled = enabled
        binding.userEmailEditText.isEnabled = false
        binding.userCityEditText.isEnabled = enabled
        binding.userAvatarUrlEditText.isEnabled = enabled
        binding.saveProfileButton.isEnabled = enabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
