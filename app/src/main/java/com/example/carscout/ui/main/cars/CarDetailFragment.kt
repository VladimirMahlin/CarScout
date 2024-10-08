package com.example.carscout.ui.main.cars

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carscout.R
import com.example.carscout.adapters.ImageAdapter
import com.example.carscout.databinding.FragmentCarDetailBinding
import com.example.carscout.ui.main.ImageDialogFragment
import com.example.carscout.viewmodel.CarViewModel
import com.example.carscout.viewmodel.CarViewModelFactory
import com.example.carscout.data.repository.CarRepository
import com.github.dhaval2404.imagepicker.ImagePicker

class CarDetailFragment : Fragment() {

    private var _binding: FragmentCarDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CarViewModel
    private val args: CarDetailFragmentArgs by navArgs()

    private var isEditing = false
    private lateinit var imageAdapter: ImageAdapter
    private var isAuthor = false

    private val imageUris = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = CarRepository()
        val factory = CarViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[CarViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCarDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupImageRecyclerView()
        enableEditing(false)

        binding.editSaveButton.setOnClickListener {
            if (isEditing) saveCarDetails() else enableEditing(true)
        }

        binding.deleteButton.setOnClickListener {
            deleteCar()
        }

        binding.addImageButton.setOnClickListener {
            openImagePicker()
        }

        observeViewModel()
        viewModel.loadCarById(args.carId)
    }

    private fun setupImageRecyclerView() {
        imageAdapter = ImageAdapter(
            imageUris,
            isEditing = isEditing,
            onImageClick = { uri ->
                showImageFullScreen(uri)
            },
            onImageDelete = { position ->
                imageUris.removeAt(position)
                imageAdapter.notifyItemRemoved(position)
            }
        )
        binding.carImagesRecyclerView.apply {
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

    @Deprecated("Deprecated in Java")
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

                binding.carManufacturerTextView.text = car.manufacturer
                binding.carModelTextView.text = car.model
                binding.carYearTextView.text = car.year.toString()
                binding.carMileageTextView.text = car.mileage.toString()
                binding.carConditionTextView.text = car.condition
                binding.carDescriptionTextView.text = car.description
                binding.carPriceTextView.text = car.price.toString()

                imageUris.clear()
                imageUris.addAll(car.imageUrls.map { Uri.parse(it) })
                imageAdapter.notifyDataSetChanged()

                isAuthor = viewModel.isCurrentUserAuthor(car.ownerId)
                updateEditButtonVisibility()

                enableEditing(false)
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.carImagesRecyclerView.alpha = if (isLoading) 0.5f else 1.0f
            binding.editSaveButton.isEnabled = !isLoading && isAuthor

            if (isLoading) {
                toggleInputFields(false)
            } else {
                enableEditing(isEditing)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEditButtonVisibility() {
        binding.editSaveButton.visibility = if (isAuthor) View.VISIBLE else View.GONE
        binding.deleteButton.visibility = if (isAuthor) View.VISIBLE else View.GONE
    }

    private fun toggleInputFields(isEnabled: Boolean) {
        val visibilityInEditMode = if (isEnabled) View.VISIBLE else View.GONE
        val visibilityInViewMode = if (isEnabled) View.GONE else View.VISIBLE

        binding.carManufacturerTextView.visibility = visibilityInViewMode
        binding.carManufacturerValueTextView.visibility = visibilityInViewMode
        binding.carManufacturerInputLayout.visibility = visibilityInEditMode

        binding.carModelTextView.visibility = visibilityInViewMode
        binding.carModelValueTextView.visibility = visibilityInViewMode
        binding.carModelInputLayout.visibility = visibilityInEditMode

        binding.carYearTextView.visibility = visibilityInViewMode
        binding.carYearValueTextView.visibility = visibilityInViewMode
        binding.carYearInputLayout.visibility = visibilityInEditMode

        binding.carMileageTextView.visibility = visibilityInViewMode
        binding.carMileageValueTextView.visibility = visibilityInViewMode
        binding.carMileageInputLayout.visibility = visibilityInEditMode

        binding.carConditionTextView.visibility = visibilityInViewMode
        binding.carConditionValueTextView.visibility = visibilityInViewMode
        binding.carConditionInputLayout.visibility = visibilityInEditMode

        binding.carDescriptionTextView.visibility = visibilityInViewMode
        binding.carDescriptionValueTextView.visibility = visibilityInViewMode
        binding.carDescriptionInputLayout.visibility = visibilityInEditMode

        binding.carPriceTextView.visibility = visibilityInViewMode
        binding.carPriceValueTextView.visibility = visibilityInViewMode
        binding.carPriceInputLayout.visibility = visibilityInEditMode
    }

    private fun enableEditing(enable: Boolean) {
        if (!isAuthor) return
        isEditing = enable
        toggleInputFields(enable)
        binding.editSaveButton.text = if (enable) "Save" else "Edit"
        binding.addImageButton.visibility = if (enable) View.VISIBLE else View.GONE

        imageAdapter.setEditingMode(isEditing)
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
            price,
            imageUris
        )
        enableEditing(false)
    }

    private fun deleteCar() {
        if (!isAuthor) {
            Toast.makeText(requireContext(), "You are not authorized to delete this listing", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete Car")
            .setMessage("Are you sure you want to delete this car?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteCar(args.carId)
                findNavController().navigateUp()
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showImageFullScreen(uri: Uri) {
        val dialog = ImageDialogFragment.newInstance(uri)
        dialog.show(childFragmentManager, "image_dialog")
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
