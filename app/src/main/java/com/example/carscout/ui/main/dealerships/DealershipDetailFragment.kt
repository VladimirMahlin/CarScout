package com.example.carscout.ui.main.dealerships

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carscout.databinding.FragmentDealershipDetailBinding
import com.example.carscout.viewmodel.DealershipViewModel
import com.example.carscout.viewmodel.DealershipViewModelFactory
import com.example.carscout.data.repository.DealershipRepository
import com.example.carscout.adapters.ImageAdapter
import com.example.carscout.ui.main.ImageDialogFragment

class DealershipDetailFragment : Fragment() {

    private var _binding: FragmentDealershipDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DealershipViewModel
    private val args: DealershipDetailFragmentArgs by navArgs()

    private var isEditing = false
    private lateinit var imageAdapter: ImageAdapter
    private var isAuthor = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = DealershipRepository()
        val factory = DealershipViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[DealershipViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDealershipDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupImageRecyclerView()
        binding.editSaveButton.setOnClickListener {
            if (isEditing) saveDealershipDetails() else enableEditing(true)
        }

        observeViewModel()
        viewModel.loadDealershipById(args.dealershipId)
    }

    private fun setupImageRecyclerView() {
        imageAdapter = ImageAdapter(
            emptyList(),
            onImageClick = { uri ->
                showImageFullScreen(uri)
            }
        )
        binding.dealershipImagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }
    }

    private fun showImageFullScreen(uri: Uri) {
        val dialog = ImageDialogFragment.newInstance(uri)
        dialog.show(childFragmentManager, "image_dialog")
    }


    private fun observeViewModel() {
        viewModel.currentDealership.observe(viewLifecycleOwner) { dealership ->
            dealership?.let {
                binding.dealershipNameEditText.setText(dealership.name)
                binding.dealershipAddressEditText.setText(dealership.address)
                binding.dealershipPhoneEditText.setText(dealership.phoneNumber)
                binding.dealershipEmailEditText.setText(dealership.email)

                val imageUris = dealership.imageUrls.map { Uri.parse(it) }
                imageAdapter = ImageAdapter(
                    imageUris,
                    onImageClick = { uri ->
                        showImageFullScreen(uri)
                    }
                )
                binding.dealershipImagesRecyclerView.adapter = imageAdapter

                isAuthor = viewModel.isCurrentUserAuthor(dealership.ownerId)
                updateEditButtonVisibility()

                enableEditing(false)
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.dealershipImagesRecyclerView.alpha = if (isLoading) 0.5f else 1.0f
            binding.editSaveButton.isEnabled = !isLoading && isAuthor
            toggleInputFields(!isLoading)
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEditButtonVisibility() {
        binding.editSaveButton.visibility = if (isAuthor) View.VISIBLE else View.GONE
    }

    private fun toggleInputFields(isEnabled: Boolean) {
        binding.dealershipNameInputLayout.isEnabled = isEnabled && isAuthor
        binding.dealershipAddressInputLayout.isEnabled = isEnabled && isAuthor
        binding.dealershipPhoneInputLayout.isEnabled = isEnabled && isAuthor
        binding.dealershipEmailInputLayout.isEnabled = isEnabled && isAuthor
    }

    private fun enableEditing(enable: Boolean) {
        if (!isAuthor) return
        isEditing = enable
        toggleInputFields(enable)
        binding.editSaveButton.text = if (enable) "Save" else "Edit"
    }

    private fun saveDealershipDetails() {
        if (!isAuthor) {
            Toast.makeText(requireContext(), "You are not authorized to edit this dealership", Toast.LENGTH_SHORT).show()
            return
        }

        val name = binding.dealershipNameEditText.text.toString().trim()
        val address = binding.dealershipAddressEditText.text.toString().trim()
        val phoneNumber = binding.dealershipPhoneEditText.text.toString().trim()
        val email = binding.dealershipEmailEditText.text.toString().trim()

        if (name.isEmpty() || address.isEmpty() || phoneNumber.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.updateDealership(
            args.dealershipId,
            name,
            address,
            phoneNumber,
            email
        )
        enableEditing(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
//TODO: Add image expansion on click
//TODO: Add deleting functionality
