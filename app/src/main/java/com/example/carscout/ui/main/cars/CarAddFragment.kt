package com.example.carscout.ui.main.cars

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
import com.example.carscout.data.repository.CarRepository
import com.example.carscout.databinding.FragmentCarAddBinding
import com.example.carscout.ui.adapters.ImageAdapter
import com.example.carscout.viewmodel.CarViewModel
import com.example.carscout.viewmodel.CarViewModelFactory
import com.github.dhaval2404.imagepicker.ImagePicker

class CarAddFragment : Fragment() {

    private var _binding: FragmentCarAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CarViewModel
    private val imageUris = mutableListOf<Uri>()
    private lateinit var imageAdapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = CarRepository()
        val factory = CarViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(CarViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupImageRecyclerView()
        binding.addImageButton.setOnClickListener { openImagePicker() }
        binding.saveCarButton.setOnClickListener { saveCar() }

        observeViewModel()
    }

    private fun setupImageRecyclerView() {
        imageAdapter = ImageAdapter(imageUris) { position ->
            imageUris.removeAt(position)
            imageAdapter.notifyItemRemoved(position)
        }

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

    private fun saveCar() {
        val model = binding.carModelEditText.text.toString().trim()
        val year = binding.carYearEditText.text.toString().trim().toIntOrNull()
        val price = binding.carPriceEditText.text.toString().trim().toDoubleOrNull()

        if (model.isEmpty() || year == null || price == null || imageUris.isEmpty()) {
            showToast("Please fill out all fields and upload at least one image.")
            return
        }

        viewModel.addCar(model, year, price, imageUris)
    }

    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.carModelInputLayout.isEnabled = !isLoading
            binding.carYearInputLayout.isEnabled = !isLoading
            binding.carPriceInputLayout.isEnabled = !isLoading
            binding.addImageButton.isEnabled = !isLoading
            binding.saveCarButton.isEnabled = !isLoading
            binding.imageRecyclerView.alpha = if (isLoading) 0.5f else 1.0f
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            showToast(errorMessage)
            if (errorMessage.startsWith("Car added successfully")) {
                findNavController().navigateUp()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}