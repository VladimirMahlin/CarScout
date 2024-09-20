package com.example.carscout.ui.main.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.carscout.databinding.FragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.example.carscout.R
import com.example.carscout.data.model.Dealership
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import android.location.Geocoder
import java.util.*

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var mMap: GoogleMap
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        // Инициализация карты
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)


        // Обработка кнопок зума
        binding.plusButton.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomIn())
        }

        binding.minusButton.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomOut())
        }

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //var point = LatLng(32.0754593462546, 34.774867017009946)
        //mMap.addMarker(MarkerOptions().position(point).title("Dizingof_Center"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 14f))
        mMap.isBuildingsEnabled = true
        mMap.isIndoorEnabled = true
        fetchDealerships{dealerships ->
            dealerships.forEach { dealership ->
                getLatLngFromAddress(dealership.address) { latLng ->
                    if (latLng != null) {
                        mMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(dealership.name)
                                .snippet(dealership.address)
                        )
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun fetchDealerships(callback: (List<Dealership>) -> Unit) {
        db.collection("dealerships") // "dealerships" - это коллекция в Firestore
            .get()
            .addOnSuccessListener { documents ->
                val dealerships = documents.map { document ->
                    Dealership(
                        name = document.getString("name") ?: "",
                        address = document.getString("address") ?: ""
                    )
                }
                callback(dealerships)
            }
            .addOnFailureListener { exception ->
                // Обработка ошибок
                exception.printStackTrace()
            }
    }
    fun getLatLngFromAddress(address: String, callback: (LatLng?) -> Unit) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocationName(address, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            val location = addresses[0]
            val latLng = LatLng(location.latitude, location.longitude)
            callback(latLng)
        } else {
            callback(null)
        }
    }
}
