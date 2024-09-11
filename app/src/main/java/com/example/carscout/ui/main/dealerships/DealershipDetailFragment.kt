package com.example.carscout.ui.main.dealerships

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.carscout.databinding.FragmentDealershipDetailBinding
import com.example.carscout.viewmodel.DealershipViewModel
import com.example.carscout.viewmodel.DealershipViewModelFactory
import com.example.carscout.data.repository.DealershipRepository

class DealershipDetailFragment : Fragment() {

    private var _binding: FragmentDealershipDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DealershipViewModel
    private val args: DealershipDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDealershipDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = DealershipViewModelFactory(DealershipRepository())
        viewModel = ViewModelProvider(this, factory).get(DealershipViewModel::class.java)

        observeViewModel()
        viewModel.loadDealershipById(args.dealershipId)
    }

    private fun observeViewModel() {
        viewModel.currentDealership.observe(viewLifecycleOwner) { dealership ->
            dealership?.let {
                binding.dealershipNameEditText.setText(it.name)
                binding.dealershipAddressEditText.setText(it.address)
                binding.dealershipContactEditText.setText("Placeholder Contact") // Placeholder for future contact field
            }
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
}
