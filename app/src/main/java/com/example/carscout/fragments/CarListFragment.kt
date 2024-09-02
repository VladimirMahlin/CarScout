package com.example.carscout.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carscout.R
import com.example.carscout.databinding.FragmentCarListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CarListFragment : Fragment() {

    private var _binding: FragmentCarListBinding? = null
    private val binding get() = _binding!!

    private lateinit var carListAdapter: CarListAdapter
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupAddCarButton()
        loadCarList()
    }

    private fun setupRecyclerView() {
        carListAdapter = CarListAdapter { car ->
            val action = CarListFragmentDirections.actionCarListFragmentToCarDetailFragment(car.id)
            findNavController().navigate(action)
        }

        binding.carListRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = carListAdapter
        }
    }

    private fun setupAddCarButton() {
        binding.addCarButton.setOnClickListener {
            findNavController().navigate(R.id.action_carListFragment_to_carAddFragment)
        }
    }

    private fun loadCarList() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).collection("cars")
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Toast.makeText(requireContext(), "Error loading cars: ${exception.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val carList = snapshot.documents.map { document ->
                            document.toObject(Car::class.java)?.copy(id = document.id)
                        }.filterNotNull()
                        carListAdapter.submitList(carList)
                    }
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
