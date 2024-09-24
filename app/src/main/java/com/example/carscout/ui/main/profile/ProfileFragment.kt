package com.example.carscout.ui.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.carscout.R
import com.example.carscout.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

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

        binding.saveProfileButton.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun loadUserProfile() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        setContentVisibility(View.GONE)
        setInteractionsEnabled(false) //TODO: Add image upload

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
        binding.userEmailEditText.isEnabled = false  // Email remains disabled
        binding.userCityEditText.isEnabled = enabled
        binding.userAvatarUrlEditText.isEnabled = enabled
        binding.saveProfileButton.isEnabled = enabled
    }

    private fun saveUserProfile() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        setContentVisibility(View.GONE)
        setInteractionsEnabled(false)

        val userId = auth.currentUser?.uid
        userId?.let {
            val userProfile = mapOf(
                "name" to binding.userNameEditText.text.toString(),
                "email" to binding.userEmailEditText.text.toString(),
                "city" to binding.userCityEditText.text.toString(),
                "avatarUrl" to binding.userAvatarUrlEditText.text.toString()
            )

            firestore.collection("users").document(it).set(userProfile)
                .addOnSuccessListener {
                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    Picasso.get()
                        .load(binding.userAvatarUrlEditText.text.toString())
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}