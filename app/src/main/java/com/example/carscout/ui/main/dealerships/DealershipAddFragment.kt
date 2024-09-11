package com.example.carscout.ui.main.dealerships

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.carscout.databinding.FragmentDealershipAddBinding
import com.example.carscout.viewmodel.DealershipViewModel
import com.example.carscout.viewmodel.DealershipViewModelFactory
import com.example.carscout.data.repository.DealershipRepository

class DealershipAddFragment : Fragment() {

    private var _binding: FragmentDealershipAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DealershipViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDealershipAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = DealershipViewModelFactory(DealershipRepository())
        viewModel = ViewModelProvider(this, factory).get(DealershipViewModel::class.java)

        binding.saveDealershipButton.setOnClickListener {
            val name = binding.dealershipNameEditText.text.toString().trim()
            val address = binding.dealershipAddressEditText.text.toString().trim()

            if (name.isEmpty() || address.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addDealership(name, address)
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
