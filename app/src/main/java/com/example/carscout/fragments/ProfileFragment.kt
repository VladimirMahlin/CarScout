package com.example.carscout.fragments

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
    ): View? {
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

                        if (avatarUrl.isNotEmpty()) {
                            Picasso.get()
                                .load(avatarUrl)
                                .placeholder(R.drawable.ic_avatar_placeholder)
                                .into(binding.userAvatarImageView)
                        } else {
                            Picasso.get()
                                .load(R.drawable.ic_avatar_placeholder)
                                .into(binding.userAvatarImageView)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun saveUserProfile() {
        val userId = auth.currentUser?.uid
        userId?.let {
            val userProfile = mapOf(
                "name" to binding.userNameEditText.text.toString(),
                "email" to binding.userEmailEditText.text.toString(),
                "city" to binding.userCityEditText.text.toString(),
                "avatarUrl" to ""
            )

            firestore.collection("users").document(it).set(userProfile)
                .addOnSuccessListener {
                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
