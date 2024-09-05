package com.example.carscout.ui.main.cars

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
import com.example.carscout.databinding.FragmentCarListBinding
import com.example.carscout.ui.adapters.CarListAdapter
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
        observeViewModel()

        viewModel.loadCars()
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