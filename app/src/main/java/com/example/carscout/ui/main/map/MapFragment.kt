package com.example.carscout.ui.main.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class MapFragment : Fragment(), OnMapReadyCallback, LocationListener {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var mMap: GoogleMap
    private val db = FirebaseFirestore.getInstance()
    private lateinit var locationManager: LocationManager
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    @SuppressLint("ServiceCast")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)



        binding.plusButton.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomIn())
        }

        binding.minusButton.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomOut())
        }
        //location.setOnClickListener()
        return binding.root


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isBuildingsEnabled = true
        mMap.isIndoorEnabled = true

        checkLocationPermission()
        moveToCurrentLocation()

        fetchDealerships { dealerships ->
            dealerships.forEach { dealership ->
                getLatLngFromAddress(dealership.address) { latLng ->
                    if (latLng != null) {
                        val marker = mMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(dealership.name)
                                .snippet(dealership.address)
                        )
                        marker?.tag = dealership
                    }
                }
            }
        }

        mMap.setOnMarkerClickListener { marker ->
            val dealership = marker.tag as? Dealership
            dealership?.let {
                showDealershipDialog(dealership)
            }
            true
        }
    }

    private fun showDealershipDialog(dealership: Dealership) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(dealership.name)
            .setMessage("Address: ${dealership.address}")
            .setPositiveButton("Go to") { dialog, which ->
                val action = MapFragmentDirections.actionMapFragmentToDealershipDetailFragment(dealershipId = dealership.id)
                findNavController().navigate(action)
            }
            .setNegativeButton("Close", null)
            .create()

        dialog.show()
    }

    class DealershipDetailActivity : AppCompatActivity() {

        @SuppressLint("MissingInflatedId")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.fragment_dealership_detail)

            val dealershipName = intent.getStringExtra("DEALERSHIP_NAME")
            val dealershipAddress = intent.getStringExtra("DEALERSHIP_ADDRESS")

            findViewById<TextView>(R.id.dealershipNameTextView).text = dealershipName
            findViewById<TextView>(R.id.dealershipAddressTextView).text = dealershipAddress
        }
    }


    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        }
    }
    private fun moveToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationProvider = LocationManager.GPS_PROVIDER

            val lastKnownLocation = locationManager.getLastKnownLocation(locationProvider)
            lastKnownLocation?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            }
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun fetchDealerships(callback: (List<Dealership>) -> Unit) {
        db.collection("dealerships")
            .get()
            .addOnSuccessListener { documents ->
                val dealerships = documents.map { document ->
                    Dealership(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        address = document.getString("address") ?: ""
                    )
                }
                callback(dealerships)
            }
            .addOnFailureListener { exception ->
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

    override fun onLocationChanged(location: Location) {
        TODO("Not yet implemented")
    }
}
