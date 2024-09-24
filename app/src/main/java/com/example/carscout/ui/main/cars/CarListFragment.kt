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
        setupManufacturerSpinner()
        setupFilterButton()
        setupResetFilterButton()
        setupToggleFilterButton()
        setupUserListingsButton()
        observeViewModel()

        viewModel.loadCars()
    }


    private fun setupResetFilterButton() {
        binding.resetFilterButton.setOnClickListener {
            binding.manufacturerSpinner.setSelection(0)
            binding.minPriceEditText.text.clear()
            binding.maxPriceEditText.text.clear()

            viewModel.loadCars()
        }
    }


    private fun setupToggleFilterButton() {
        var isFilterPanelVisible = false

        binding.toggleFilterButton.setOnClickListener {
            if (isFilterPanelVisible) {
                binding.filterPanel.animate().alpha(0f).translationY(-binding.filterPanel.height.toFloat()).withEndAction {
                    binding.filterPanel.visibility = View.GONE
                }
                binding.toggleFilterButton.text = "Show Filters"
            } else {
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
        val manufacturers = listOf(
            "All", "Toyota", "Ford", "Honda", "BMW", "Mercedes-Benz", "Audi", "Chevrolet",
            "Nissan", "Volkswagen", "Hyundai", "Kia", "Subaru", "Mazda", "Lexus", "Jeep",
            "Dodge", "Ram", "GMC", "Tesla", "Porsche", "Ferrari", "Lamborghini", "Jaguar",
            "Land Rover", "Volvo", "Mitsubishi", "Peugeot", "Suzuki", "Acura", "Chrysler",
            "Buick", "Infiniti", "Lincoln", "Alfa Romeo", "Fiat", "Bentley", "Aston Martin",
            "Maserati", "Genesis", "Rolls-Royce", "Citroën", "Saab", "Renault", "Bugatti",
            "McLaren", "Mini", "Smart"
        )
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, manufacturers)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.manufacturerSpinner.adapter = adapter
    }

    private fun setupFilterButton() {
        binding.applyFilterButton.setOnClickListener {
            val selectedManufacturer = binding.manufacturerSpinner.selectedItem.toString()
            val minPrice = binding.minPriceEditText.text.toString().toDoubleOrNull()
            val maxPrice = binding.maxPriceEditText.text.toString().toDoubleOrNull()

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

    private fun setupUserListingsButton() {
        binding.userListingsButton.setOnClickListener {
            val currentUserId = viewModel.getCurrentUserId()

            viewModel.filterCarsByUser(currentUserId)
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