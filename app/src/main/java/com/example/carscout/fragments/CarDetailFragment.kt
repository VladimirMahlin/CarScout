package com.example.carscout.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.carscout.R
import com.example.carscout.databinding.FragmentCarDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class CarDetailFragment : Fragment() {

    private var _binding: FragmentCarDetailBinding? = null
    private val binding get() = _binding!!

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val carId = arguments?.getString("carId")

        if (carId != null) {
            loadCarDetails(carId)
        } else {
            Toast.makeText(requireContext(), "Error: Car ID is missing", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCarDetails(carId: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).collection("cars").document(carId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val car = document.toObject(Car::class.java)
                        if (car != null) {
                            binding.carModelEditText.setText(car.model)
                            binding.carYearEditText.setText(car.year.toString())
                            binding.carPriceEditText.setText("$${car.price}")

                            Picasso.get()
                                .load(car.imageUrl)
                                .placeholder(R.drawable.placeholder)
                                .error(R.drawable.error)
                                .into(binding.carImageView)
                        } else {
                            Toast.makeText(requireContext(), "Error: Car data is null", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error: Car not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error loading car details: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
