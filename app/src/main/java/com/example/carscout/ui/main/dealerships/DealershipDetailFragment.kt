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

        // Set button click listener
        binding.editSaveButton.setOnClickListener {
            if (isEditing) {
                saveDealershipDetails()
            } else {
                enableEditing(true) // Switch to edit mode when the button is clicked
            }
        }

        // Start observing the ViewModel
        observeViewModel()

        // Load dealership details by ID
        viewModel.loadDealershipById(args.dealershipId)

        // Ensure view mode is displayed by default (not edit mode)
        enableEditing(false) // This ensures the initial state is view mode
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
                // Setting up edit and view values
                binding.dealershipNameEditText.setText(dealership.name)
                binding.dealershipAddressEditText.setText(dealership.address)
                binding.dealershipPhoneEditText.setText(dealership.phoneNumber)
                binding.dealershipEmailEditText.setText(dealership.email)
                binding.dealershipInfoEditText.setText(dealership.info)

                // Setting TextViews for view mode
                binding.dealershipNameValueTextView.text = dealership.name
                binding.dealershipAddressValueTextView.text = dealership.address
                binding.dealershipPhoneValueTextView.text = dealership.phoneNumber
                binding.dealershipEmailValueTextView.text = dealership.email
                binding.dealershipInfoValueTextView.text = dealership.info

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
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEditButtonVisibility() {
        binding.editSaveButton.visibility = if (isAuthor) View.VISIBLE else View.GONE
    }

    private fun toggleInputFields(isEnabled: Boolean) {
        val visibilityInEditMode = if (isEnabled) View.VISIBLE else View.GONE
        val visibilityInViewMode = if (isEnabled) View.GONE else View.VISIBLE

        // Toggle between TextViews (view mode) and TextInputLayouts (edit mode)
        binding.dealershipNameTextView.visibility = visibilityInViewMode
        binding.dealershipNameValueTextView.visibility = visibilityInViewMode
        binding.dealershipNameInputLayout.visibility = visibilityInEditMode

        binding.dealershipAddressTextView.visibility = visibilityInViewMode
        binding.dealershipAddressValueTextView.visibility = visibilityInViewMode
        binding.dealershipAddressInputLayout.visibility = visibilityInEditMode

        binding.dealershipPhoneTextView.visibility = visibilityInViewMode
        binding.dealershipPhoneValueTextView.visibility = visibilityInViewMode
        binding.dealershipPhoneInputLayout.visibility = visibilityInEditMode

        binding.dealershipEmailTextView.visibility = visibilityInViewMode
        binding.dealershipEmailValueTextView.visibility = visibilityInViewMode
        binding.dealershipEmailInputLayout.visibility = visibilityInEditMode

        binding.dealershipInfoTextView.visibility = visibilityInViewMode
        binding.dealershipInfoValueTextView.visibility = visibilityInViewMode
        binding.dealershipInfoInputLayout.visibility = visibilityInEditMode
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
        val info = binding.dealershipInfoEditText.text.toString().trim()

        if (name.isEmpty() || address.isEmpty() || phoneNumber.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        val imageUrls = imageAdapter.getImageUris().map { it.toString() }

        viewModel.updateDealership(
            args.dealershipId,
            name,
            address,
            phoneNumber,
            email,
            info,
            imageUrls
        )
        enableEditing(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
