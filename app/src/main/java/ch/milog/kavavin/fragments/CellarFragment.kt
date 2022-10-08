package ch.milog.kavavin.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ch.milog.kavavin.NavigationInterface
import ch.milog.kavavin.adapters.CellarListAdapter
import ch.milog.kavavin.databinding.FragmentCellarBinding
import ch.milog.kavavin.models.Bottle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*


class CellarFragment : Fragment() {

    private var _binding: FragmentCellarBinding? = null
    private val binding get() = _binding!!
    private var bottles: ArrayList<Bottle> = ArrayList()
    private lateinit var bottlesAdapter: CellarListAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var navigationInterface: NavigationInterface
    private lateinit var filteredBottles: ArrayList<Bottle>
    private var doubleBackToExitPressedOnce = false
    private val db = Firebase.firestore
    private var sortName = false
    private var sortYear = false
    private var sortQuantity = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCellarBinding.inflate(inflater, container, false)
        val recyclerView = binding.bottlesList
        recyclerView.addItemDecoration(
            androidx.recyclerview.widget.DividerItemDecoration(
                recyclerView.context,
                androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
            )
        )
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        bottlesAdapter = CellarListAdapter(requireActivity()) { bottle -> onItemClicked(bottle) }
        recyclerView.adapter = bottlesAdapter
        swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            refresh()
        }

        binding.floatingActionButton.setOnClickListener {
            it.findNavController().navigate(CellarFragmentDirections.actionCellarFragmentToAddFragment3())
        }
        activity?.let {
            navigationInterface = it as NavigationInterface
        }
        navigationInterface.referenceCellar(this)
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    requireActivity().finish()
                }
                doubleBackToExitPressedOnce = true
                Toast.makeText(requireContext(), "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
            }
        })
        return binding.root
    }

    private fun onItemClicked(bottle: Bottle) {
        val modalBottomSheet = DetailsBottomSheet(bottle) { refresh() }
        modalBottomSheet.show(childFragmentManager, "bottle_details")
    }

    fun refresh() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            db.collection("bottles").whereEqualTo("userId", currentUser.uid).get().addOnSuccessListener { result ->
                bottles.clear()
                for (document in result) {
                    if (document.data.isNotEmpty()) {
                        val bottle = document.data
                        if (bottle["quantity"] as Long != 0L) {
                            bottles.add(
                                Bottle(
                                    document.id,
                                    bottle["type"] as Long?,
                                    bottle["name"] as String?,
                                    bottle["country"] as String?,
                                    bottle["region"] as String?,
                                    bottle["year"] as Long?,
                                    bottle["grape"] as String?,
                                    bottle["price"] as Long?,
                                    bottle["producer"] as String?,
                                    bottle["quantity"] as Long?,
                                    bottle["userId"] as String?
                                )
                            )
                        }
                    }
                }
                activity?.runOnUiThread {
                    if (bottles.isNotEmpty()) {
                        binding.noBottles.visibility = View.GONE
                        bottles.sortBy { it.name!!.lowercase(Locale.getDefault()) }
                        bottlesAdapter.setBottles(bottles)
                    } else {
                        binding.noBottles.visibility = View.VISIBLE
                        bottlesAdapter.setBottles(bottles)
                    }
                    swipeRefreshLayout.isRefreshing = false
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error : please try again", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun refreshBottle(bottle: Bottle) {
        for (i in 0 until bottles.size) {
            if (bottles[i].id == bottle.id) {
                bottles[i] = bottle
                break
            }
        }
        requireActivity().runOnUiThread {
            bottlesAdapter.refreshBottle(bottle)
        }
    }

    fun filter(type: Int) {
        filteredBottles = ArrayList()
        for (bottle in bottles) {
            if (bottle.type == type.toLong()) {
                filteredBottles.add(bottle)
            } else if (type == 0) {
                filteredBottles.add(bottle)
            }
        }
        if (filteredBottles.isEmpty()) {
            binding.noBottlesFilter.visibility = View.VISIBLE
        } else {
            binding.noBottlesFilter.visibility = View.GONE
        }
        bottlesAdapter.setBottles(filteredBottles)
    }

    fun sort(type: Int) {
        val sortBottles: ArrayList<Bottle> = if (this::filteredBottles.isInitialized && filteredBottles.isNotEmpty()) {
            filteredBottles
        } else {
            bottles
        }
        when (type) {
            1 -> {
                sortName = if (sortName) {
                    sortBottles.sortBy { it.name }
                    false
                } else {
                    sortBottles.sortByDescending { it.name }
                    true
                }
            }
            2 -> {
                sortYear = if (sortYear) {
                    sortBottles.sortBy { it.year }
                    false
                } else {
                    sortBottles.sortByDescending { it.year }
                    true
                }
            }
            3 -> {
                sortQuantity = if (sortQuantity) {
                    sortBottles.sortBy { it.quantity }
                    false
                } else {
                    sortBottles.sortByDescending { it.quantity }
                    true
                }
            }
        }

        bottlesAdapter.setBottles(sortBottles)
    }

    fun search(query: String) {
        filteredBottles = ArrayList<Bottle>()
        for (bottle in bottles) {
            if (bottle.name!!.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault()))) {
                filteredBottles.add(bottle)
            } else if (bottle.country?.lowercase(Locale.getDefault())?.contains(query.lowercase(Locale.getDefault())) == true) {
                filteredBottles.add(bottle)
            } else if (bottle.region?.lowercase(Locale.getDefault())?.contains(query.lowercase(Locale.getDefault())) == true) {
                filteredBottles.add(bottle)
            } else if (bottle.producer?.lowercase(Locale.getDefault())?.contains(query.lowercase(Locale.getDefault())) == true) {
                filteredBottles.add(bottle)
            } else if (bottle.year.toString().contains(query)) {
                filteredBottles.add(bottle)
            } else if (bottle.grape?.lowercase(Locale.getDefault())?.contains(query.lowercase(Locale.getDefault())) == true) {
                filteredBottles.add(bottle)
            }
        }
        if (filteredBottles.isEmpty()) {
            binding.noBottlesFilter.visibility = View.VISIBLE
        } else {
            binding.noBottlesFilter.visibility = View.GONE
        }
        bottlesAdapter.setBottles(filteredBottles)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

}