package com.example.carscout.ui.main.cars

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carscout.data.repository.CarRepository
import com.example.carscout.databinding.FragmentCarListBinding
import com.example.carscout.adapters.CarListAdapter
import com.example.carscout.viewmodel.CarViewModel
import com.example.carscout.viewmodel.CarViewModelFactory

class CarListFragment : Fragment() {

    private var _binding: FragmentCarListBinding? = null
    private val binding get() = _binding!!

    private lateinit var carListAdapter: CarListAdapter
    private lateinit var viewModel: CarViewModel

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
        _binding = FragmentCarListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupAddCarButton()
        setupManufacturerSpinner()  // Setup spinner
        setupFilterButton()         // Setup filter button
        setupResetFilterButton()    // Setup reset button
        setupToggleFilterButton()   // Setup toggle button for filter panel
        observeViewModel()

        viewModel.loadCars()  // Load all cars initially
    }

    private fun setupResetFilterButton() {
        binding.resetFilterButton.setOnClickListener {
            // Reset filters to default values
            binding.manufacturerSpinner.setSelection(0)  // Reset the spinner to the first option
            binding.minPriceEditText.text.clear()  // Clear min price input
            binding.maxPriceEditText.text.clear()  // Clear max price input

            // Reload all cars
            viewModel.loadCars()
        }
    }


    private fun setupToggleFilterButton() {
        var isFilterPanelVisible = false

        binding.toggleFilterButton.setOnClickListener {
            if (isFilterPanelVisible) {
                // Slide up animation to hide
                binding.filterPanel.animate().alpha(0f).translationY(-binding.filterPanel.height.toFloat()).withEndAction {
                    binding.filterPanel.visibility = View.GONE
                }
                binding.toggleFilterButton.text = "Show Filters"
            } else {
                // Slide down animation to show
                binding.filterPanel.visibility = View.VISIBLE
                binding.filterPanel.alpha = 0f
                binding.filterPanel.translationY = -binding.filterPanel.height.toFloat()
                binding.filterPanel.animate().alpha(1f).translationY(0f)
                binding.toggleFilterButton.text = "Hide Filters"
            }
            isFilterPanelVisible = !isFilterPanelVisible
        }
    }



    private fun setupManufacturerSpinner() {
        // Populate spinner with car manufacturer options
        val manufacturers = listOf("All", "Toyota", "Ford", "Honda", "BMW") // Add more as needed
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, manufacturers)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.manufacturerSpinner.adapter = adapter
    }

    private fun setupFilterButton() {
        binding.applyFilterButton.setOnClickListener {
            val selectedManufacturer = binding.manufacturerSpinner.selectedItem.toString()
            val minPrice = binding.minPriceEditText.text.toString().toDoubleOrNull()
            val maxPrice = binding.maxPriceEditText.text.toString().toDoubleOrNull()

            // Apply the filters
            viewModel.filterCars(selectedManufacturer, minPrice, maxPrice)
        }
    }


    private fun setupRecyclerView() {
        carListAdapter = CarListAdapter { car ->
            val action = CarListFragmentDirections.actionCarListFragmentToCarDetailFragment(car.id)
            findNavController().navigate(action)
        }

        binding.carListRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = carListAdapter
        }
    }

    private fun setupAddCarButton() {
        binding.addCarButton.setOnClickListener {
            findNavController().navigate(CarListFragmentDirections.actionCarListFragmentToCarAddFragment())
        }
    }

    private fun observeViewModel() {
        viewModel.cars.observe(viewLifecycleOwner) { cars ->
            carListAdapter.submitList(cars)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.carListRecyclerView.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
            binding.addCarButton.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}