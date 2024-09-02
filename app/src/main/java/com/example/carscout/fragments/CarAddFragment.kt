package com.example.carscout.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.carscout.databinding.FragmentCarAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CarAddFragment : Fragment() {

    private var _binding: FragmentCarAddBinding? = null
    private val binding get() = _binding!!

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saveCarButton.setOnClickListener {
            saveCar()
        }
    }

    private fun saveCar() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        setContentVisibility(View.GONE)
        setInteractionsEnabled(false)

        val model = binding.carModelEditText.text.toString().trim()
        val year = binding.carYearEditText.text.toString().trim().toIntOrNull()
        val price = binding.carPriceEditText.text.toString().trim().toDoubleOrNull()
        val imageUrl = binding.carImageUrlEditText.text.toString().trim()

        if (model.isEmpty() || year == null || price == null || imageUrl.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show()
            binding.loadingProgressBar.visibility = View.GONE
            setContentVisibility(View.VISIBLE)
            setInteractionsEnabled(true)
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "You must be logged in to add a car.", Toast.LENGTH_SHORT).show()
            binding.loadingProgressBar.visibility = View.GONE
            setContentVisibility(View.VISIBLE)
            setInteractionsEnabled(true)
            return
        }

        val car = mapOf(
            "model" to model,
            "year" to year,
            "price" to price,
            "imageUrl" to imageUrl,
            "ownerId" to currentUser.uid
        )

        firestore.collection("users").document(currentUser.uid).collection("cars").add(car)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Car added successfully!", Toast.LENGTH_SHORT).show()
                binding.loadingProgressBar.visibility = View.GONE
                requireActivity().onBackPressed()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error adding car: ${it.message}", Toast.LENGTH_LONG).show()
                binding.loadingProgressBar.visibility = View.GONE
                setContentVisibility(View.VISIBLE)
                setInteractionsEnabled(true)
            }
    }

    private fun setContentVisibility(visibility: Int) {
        binding.carModelInputLayout.visibility = visibility
        binding.carYearInputLayout.visibility = visibility
        binding.carPriceInputLayout.visibility = visibility
        binding.carImageUrlInputLayout.visibility = visibility
        binding.saveCarButton.visibility = visibility
    }

    private fun setInteractionsEnabled(enabled: Boolean) {
        binding.carModelEditText.isEnabled = enabled
        binding.carYearEditText.isEnabled = enabled
        binding.carPriceEditText.isEnabled = enabled
        binding.carImageUrlEditText.isEnabled = enabled
        binding.saveCarButton.isEnabled = enabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
