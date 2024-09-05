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
            binding.carModelEditText.setText(car.model)
            binding.carYearEditText.setText(car.year.toString())
            binding.carPriceEditText.setText(car.price.toString())
            imageAdapter = CarImageAdapter(car.imageUrls)
            binding.carImagesRecyclerView.adapter = imageAdapter
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.carImagesRecyclerView.alpha = if (isLoading) 0.5f else 1.0f
            binding.carModelInputLayout.isEnabled = !isLoading
            binding.carYearInputLayout.isEnabled = !isLoading
            binding.carPriceInputLayout.isEnabled = !isLoading
            binding.editSaveButton.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun enableEditing(enable: Boolean) {
        isEditing = enable
        binding.carModelEditText.isEnabled = enable
        binding.carYearEditText.isEnabled = enable
        binding.carPriceEditText.isEnabled = enable
        binding.editSaveButton.text = if (enable) "Save" else "Edit"
    }

    private fun saveCarDetails() {
        val model = binding.carModelEditText.text.toString().trim()
        val year = binding.carYearEditText.text.toString().toIntOrNull()
        val price = binding.carPriceEditText.text.toString().toDoubleOrNull()

        if (model.isEmpty() || year == null || price == null) {
            Toast.makeText(requireContext(), "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.updateCar(args.carId, model, year, price)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}