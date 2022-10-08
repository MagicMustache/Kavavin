package ch.milog.kavavin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import ch.milog.kavavin.R
import ch.milog.kavavin.databinding.FragmentAddBinding
import ch.milog.kavavin.models.Bottle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hbb20.countrypicker.models.CPCountry


class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private val items = listOf("Vin rouge", "Vin blanc", "Champagne", "Autre")
    private var type = "Autre"
    private var bottles = ArrayList<Bottle>()
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true);

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        val currentUser = Firebase.auth.currentUser
        db.collection("bottles").whereEqualTo("userId", currentUser?.uid).get().addOnSuccessListener { result ->
            for (document in result) {
                if (document.data.isNotEmpty()) {
                    val bottle = document.data
                    bottles.add(
                        Bottle(
                            null,
                            null,
                            bottle["name"] as String,
                            null,
                            bottle["region"] as String?,
                            null,
                            bottle["grape"] as String?,
                            null,
                            bottle["producer"] as String?,
                            null,
                            null
                        )
                    )
                }
            }
            bottles.let {
                requireActivity().runOnUiThread {
                    val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu_popup_item, items)
                    binding.autoCompleteTextView.setAdapter(arrayAdapter)
                    val countryPicker = binding.countryPicker
                    countryPicker.cpViewHelper.cpViewConfig.viewTextGenerator = { cpCountry: CPCountry ->
                        cpCountry.name
                    }
                    countryPicker.cpViewHelper.refreshView()

                    val names = bottles.map { it.name }
                    binding.nameAutoComplete.setAdapter(
                        ArrayAdapter(
                            requireContext(),
                            R.layout.dropdown_menu_popup_item,
                            names.distinct()
                        )
                    )
                    val regions = bottles.map { it.region.toString() }
                    binding.regionAutoComplete.setAdapter(
                        ArrayAdapter(
                            requireContext(),
                            R.layout.dropdown_menu_popup_item,
                            regions.distinct()
                        )
                    )
                    val grapes = bottles.map { it.grape.toString() }
                    binding.grapeAutoComplete.setAdapter(
                        ArrayAdapter(
                            requireContext(),
                            R.layout.dropdown_menu_popup_item,
                            grapes.distinct()
                        )
                    )
                    val producers = bottles.map { it.producer.toString() }
                    binding.producerAutoComplete.setAdapter(
                        ArrayAdapter(
                            requireContext(),
                            R.layout.dropdown_menu_popup_item,
                            producers.distinct()
                        )
                    )
                }
            }
        }
        binding.floatingActionButton.setOnClickListener {
            saveBottle()
        }
        return binding.root
    }

    private fun saveBottle() {
        val name = binding.nameAutoComplete.text.toString()
        if (name.isEmpty()) {
            binding.nameAutoComplete.error = "Ne peut pas être vide"
            return
        }
        val country = binding.countryPicker.tvCountryInfo.text.toString()
        val type = when (this.type) {
            "Vin rouge" -> 1
            "Vin blanc" -> 2
            "Champagne" -> 3
            else -> 4
        }
        val region = binding.regionAutoComplete.text.toString()
        val grape = binding.grapeAutoComplete.text.toString()
        val producer = binding.producerAutoComplete.text.toString()
        val year = binding.textInputLayout6.editText?.text.toString()
        val price = binding.textInputLayout4.editText?.text.toString()
        val quantity = binding.textInputLayout7.editText?.text.toString()
        if (quantity.isEmpty()) {
            binding.textInputLayout7.error = "Ne peut pas être vide"
            return
        }
        val auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val newBottle = hashMapOf(
                "name" to name,
                "country" to country,
                "type" to type,
                "region" to region,
                "year" to year.toLongOrNull(),
                "grape" to grape,
                "price" to price.toLongOrNull(),
                "producer" to producer,
                "quantity" to quantity.toInt(),
                "userId" to currentUser.uid
            )
            db.collection("bottles")
                .add(newBottle)
                .addOnSuccessListener {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "Bouteille ajoutée",
                            Toast.LENGTH_SHORT
                        ).show()
                        requireActivity().onBackPressed()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "La bouteille n'a pas pu être ajoutée, veuillez réessayer",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
        } else {
            Toast.makeText(requireContext(), "Vous devez être connecté pour ajouter une bouteille", Toast.LENGTH_SHORT)
                .show()
        }

    }

    override fun getView(): View? {
        binding.autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            binding.autoCompleteTextView.setText(items[position])
        }
        (binding.spinnerLayout.editText as AutoCompleteTextView).onItemClickListener =
            OnItemClickListener { _, _, position, _ ->
                type = items[position]
            }
        return super.getView()
    }

    @Deprecated("Deprecated in Java")
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

}