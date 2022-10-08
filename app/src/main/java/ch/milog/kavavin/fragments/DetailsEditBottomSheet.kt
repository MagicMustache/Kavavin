package ch.milog.kavavin.fragments

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import ch.milog.kavavin.databinding.ModalDetailsEditBinding
import ch.milog.kavavin.models.Bottle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DetailsEditBottomSheet(private val bottle: Bottle, private val parent: CellarFragment) : BottomSheetDialogFragment() {
    private var binding: ModalDetailsEditBinding? = null
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = ModalDetailsEditBinding.inflate(inflater, container, false)
        binding!!.let { binding ->
            binding.name.setText(bottle.name)
            val types = listOf("vin rouge", "vin blanc", "champagne", "autre")
            binding.type.adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_dropdown_item, types)
            binding.type.setSelection((bottle.type!! - 1).toInt())
            binding.grape.setText(bottle.grape ?: "")
            binding.year.setText(bottle.year?.toString() ?: "")
            binding.producer.setText(bottle.producer)
            binding.country.setText(bottle.country)
            binding.region.setText(bottle.region)
            if (bottle.price == null) {
                binding.price.setText("")
            } else {
                binding.price.setText("${bottle.price}")
            }
            binding.quantity.text = bottle.quantity.toString()
            binding.remove.setOnClickListener {
                updateQuantity(-1)
            }
            binding.add.setOnClickListener {
                updateQuantity(1)
            }
            binding.doneBtn.setOnClickListener {
                updateBottle()
            }
        }

        return binding!!.root
    }

    private fun updateBottle() {
        val name = binding!!.name.text.toString()
        val type = binding!!.type.selectedItemPosition + 1
        val grape = binding!!.grape.text.toString()
        val year = binding!!.year.text.toString()
        val producer = binding!!.producer.text.toString()
        val country = binding!!.country.text.toString()
        val region = binding!!.region.text.toString()

        val regex = Regex("[^0-9]")
        var price = binding!!.price.text.toString()
        val result = regex.replace(price, "")
        price = result

        val bottle = Bottle(
            id = bottle.id,
            name = name,
            type = type.toLong(),
            grape = grape.ifEmpty { null },
            year = if (year.isEmpty()) null else year.toLong(),
            producer = producer,
            country = country,
            region = region,
            price = if (price.isEmpty()) null else price.toLong(),
            quantity = binding!!.quantity.text.toString().toLong(),
            userId = bottle.userId
        )

        db.collection("bottles").document(bottle.id!!).set(bottle).addOnSuccessListener {
            requireActivity().runOnUiThread {
                Toast.makeText(context, "Bouteille mise à jour", Toast.LENGTH_SHORT).show()
                parent.refreshBottle(bottle)
                dismiss()
            }
        }.addOnFailureListener {
            requireActivity().runOnUiThread {
                Toast.makeText(context, "Erreur lors de la mise à jour de la bouteille", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateQuantity(quantity: Int) {
        bottle.quantity = bottle.quantity!! + quantity
        binding?.quantity?.text = (bottle.quantity).toString()
        bottle.id?.let { db.collection("bottles").document(it).update("quantity", bottle.quantity) }?.addOnSuccessListener {
            if (bottle.quantity == 0L) {
                dismiss()
                parent.refresh()
            }
        }?.addOnFailureListener {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Erreur lors de la mise à jour de la quantité", Toast.LENGTH_LONG)
                    .show()
                bottle.quantity = bottle.quantity!! - quantity
                binding?.quantity?.text = (bottle.quantity).toString()
            }
        }
    }
}