package com.example.carscout.ui.main.dealerships

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carscout.databinding.FragmentDealershipAddBinding
import com.example.carscout.viewmodel.DealershipViewModel
import com.example.carscout.viewmodel.DealershipViewModelFactory
import com.example.carscout.data.repository.DealershipRepository
import com.example.carscout.adapters.ImageAdapter
import com.example.carscout.ui.main.ImageDialogFragment
import com.github.dhaval2404.imagepicker.ImagePicker

class DealershipAddFragment : Fragment() {

    private var _binding: FragmentDealershipAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DealershipViewModel
    private val imageUris = mutableListOf<Uri>()
    private lateinit var imageAdapter: ImageAdapter

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
        _binding = FragmentDealershipAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupImageRecyclerView()
        binding.addImageButton.setOnClickListener { openImagePicker() }
        binding.saveDealershipButton.setOnClickListener { saveDealership() }

        observeViewModel()
    }

    private fun setupImageRecyclerView() {
        imageAdapter = ImageAdapter(
            imageUris,
            onImageClick = { uri ->
                showImageFullScreen(uri)
            }
        )

        binding.imageRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }
    }

    private fun openImagePicker() {
        if (imageUris.size >= 5) {
            showToast("You can only add up to 5 images.")
            return
        }

        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val uri: Uri = data?.data ?: return
            if (imageUris.size < 5) {
                imageUris.add(uri)
                imageAdapter.notifyItemInserted(imageUris.size - 1)
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            showToast(ImagePicker.getError(data))
        }
    }

    private fun saveDealership() {
        val name = binding.dealershipNameEditText.text.toString().trim()
        val address = binding.dealershipAddressEditText.text.toString().trim()
        val phoneNumber = binding.dealershipPhoneEditText.text.toString().trim()
        val email = binding.dealershipEmailEditText.text.toString().trim()

        if (name.isEmpty() || address.isEmpty() || phoneNumber.isEmpty() || email.isEmpty() || imageUris.isEmpty()) {
            showToast("Please fill out all fields and upload at least one image.")
            return
        }

        viewModel.addDealership(name, address, phoneNumber, email, imageUris)
    }

    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.dealershipNameInputLayout.isEnabled = !isLoading
            binding.dealershipAddressInputLayout.isEnabled = !isLoading
            binding.dealershipPhoneInputLayout.isEnabled = !isLoading
            binding.dealershipEmailInputLayout.isEnabled = !isLoading
            binding.addImageButton.isEnabled = !isLoading
            binding.saveDealershipButton.isEnabled = !isLoading
            binding.imageRecyclerView.alpha = if (isLoading) 0.5f else 1.0f
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            showToast(errorMessage)
            if (errorMessage.startsWith("Dealership added successfully")) {
                findNavController().navigateUp()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showImageFullScreen(uri: Uri) {
        val dialog = ImageDialogFragment.newInstance(uri)
        dialog.show(childFragmentManager, "image_dialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}