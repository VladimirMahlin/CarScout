package com.example.carscout.ui.main.dealerships

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carscout.R
import com.example.carscout.databinding.FragmentDealershipListBinding
import com.example.carscout.adapters.DealershipListAdapter
import com.example.carscout.viewmodel.DealershipViewModel
import com.example.carscout.viewmodel.DealershipViewModelFactory
import com.example.carscout.data.repository.DealershipRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DealershipListFragment : Fragment() {

    private var _binding: FragmentDealershipListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DealershipViewModel
    private lateinit var adapter: DealershipListAdapter
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDealershipListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = DealershipViewModelFactory(DealershipRepository())
        viewModel = ViewModelProvider(this, factory).get(DealershipViewModel::class.java)

        setupRecyclerView()
        observeViewModel()

        binding.addDealershipButton.setOnClickListener {
            findNavController().navigate(R.id.action_dealershipListFragment_to_dealershipAddFragment)
        }

        viewModel.loadDealerships()
        checkUserBusinessStatus()
    }

    private fun setupRecyclerView() {
        adapter = DealershipListAdapter { dealership ->
            val action = DealershipListFragmentDirections.actionDealershipListFragmentToDealershipDetailFragment(dealership.id)
            findNavController().navigate(action)
        }
        binding.dealershipListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.dealershipListRecyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.dealerships.observe(viewLifecycleOwner) { dealerships ->
            adapter.submitList(dealerships)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkUserBusinessStatus() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).get().addOnSuccessListener { document ->
                val isBusiness = document.getBoolean("isBusiness") ?: false

                if (!isBusiness) {
                    binding.addDealershipButton.visibility = View.GONE
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                binding.addDealershipButton.visibility = View.GONE
            }
        } else {
            binding.addDealershipButton.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

