package com.example.carscout.ui.main.cars

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carscout.data.repository.CarRepository
import com.example.carscout.databinding.FragmentCarDetailBinding
import com.example.carscout.ui.adapters.CarImageAdapter
import com.example.carscout.viewmodel.CarViewModel
import com.example.carscout.viewmodel.CarViewModelFactory

class CarDetailFragment : Fragment() {

    private var _binding: FragmentCarDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CarViewModel
    private val args: CarDetailFragmentArgs by navArgs()

    private var isEditing = false
    private lateinit var imageAdapter: CarImageAdapter
    private var isAuthor = false

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
        _binding = FragmentCarDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupImageRecyclerView()
        binding.editSaveButton.setOnClickListener {
            if (isEditing) saveCarDetails() else enableEditing(true)
        }

        observeViewModel()
        viewModel.loadCarById(args.carId)
    }

    private fun setupImageRecyclerView() {
        imageAdapter = CarImageAdapter(emptyList())
        binding.carImagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.currentCar.observe(viewLifecycleOwner) { car ->
            car?.let {
                binding.carManufacturerEditText.setText(car.manufacturer)
                binding.carModelEditText.setText(car.model)
                binding.carYearEditText.setText(car.year.toString())
                binding.carMileageEditText.setText(car.mileage.toString())
                binding.carConditionEditText.setText(car.condition)
                binding.carDescriptionEditText.setText(car.description)
                binding.carPriceEditText.setText(car.price.toString())
                imageAdapter = CarImageAdapter(car.imageUrls)
                binding.carImagesRecyclerView.adapter = imageAdapter

                // Check if current user is the author
                isAuthor = viewModel.isCurrentUserAuthor(car.ownerId)
                updateEditButtonVisibility()

                enableEditing(false)
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.carImagesRecyclerView.alpha = if (isLoading) 0.5f else 1.0f
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
        binding.carManufacturerInputLayout.isEnabled = isEnabled && isAuthor
        binding.carModelInputLayout.isEnabled = isEnabled && isAuthor
        binding.carYearInputLayout.isEnabled = isEnabled && isAuthor
        binding.carMileageInputLayout.isEnabled = isEnabled && isAuthor
        binding.carConditionInputLayout.isEnabled = isEnabled && isAuthor
        binding.carDescriptionInputLayout.isEnabled = isEnabled && isAuthor
        binding.carPriceInputLayout.isEnabled = isEnabled && isAuthor
    }

    private fun enableEditing(enable: Boolean) {
        if (!isAuthor) return
        isEditing = enable
        toggleInputFields(enable)
        binding.editSaveButton.text = if (enable) "Save" else "Edit" //TODO fix editing
    }

    private fun saveCarDetails() {
        if (!isAuthor) {
            Toast.makeText(requireContext(), "You are not authorized to edit this listing", Toast.LENGTH_SHORT).show()
            return
        }

        val manufacturer = binding.carManufacturerEditText.text.toString().trim()
        val model = binding.carModelEditText.text.toString().trim()
        val yearString = binding.carYearEditText.text.toString().trim()
        val mileageString = binding.carMileageEditText.text.toString().trim()
        val condition = binding.carConditionEditText.text.toString().trim()
        val description = binding.carDescriptionEditText.text.toString().trim()
        val priceString = binding.carPriceEditText.text.toString().trim()

        if (manufacturer.isEmpty() || model.isEmpty() || yearString.isEmpty() || mileageString.isEmpty() ||
            condition.isEmpty() || description.isEmpty() || priceString.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        val year = yearString.toIntOrNull()
        val mileage = mileageString.toIntOrNull()
        val price = priceString.toDoubleOrNull()

        if (year == null || mileage == null || price == null) {
            Toast.makeText(requireContext(), "Invalid input for year, mileage, or price", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.updateCar(
            args.carId,
            manufacturer,
            model,
            year,
            mileage,
            condition,
            description,
            price
        )
        enableEditing(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}