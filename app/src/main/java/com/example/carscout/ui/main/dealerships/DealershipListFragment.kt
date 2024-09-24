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

class DealershipListFragment : Fragment() {

    private var _binding: FragmentDealershipListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DealershipViewModel
    private lateinit var adapter: DealershipListAdapter

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} //FIXME: Fix image showing
