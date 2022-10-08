package ch.milog.kavavin.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import ch.milog.kavavin.databinding.ModalDetailsBinding
import ch.milog.kavavin.models.Bottle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class DetailsBottomSheet(private val bottle: Bottle, private val refresh: () -> Unit) : BottomSheetDialogFragment() {
    private var binding: ModalDetailsBinding? = null
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ModalDetailsBinding.inflate(inflater, container, false)
        binding!!.let { binding ->
            binding.name.text = bottle.name
            when (bottle.type) {
                1L -> binding.type.text = "vin rouge"
                2L -> binding.type.text = "vin blanc"
                3L -> binding.type.text = "champagne"
                4L -> binding.type.text = "autre"
            }
            binding.grape.text = bottle.grape ?: ""
            binding.year.text = bottle.year?.toString() ?: ""
            binding.producer.text = bottle.producer
            binding.country.text = bottle.country
            binding.region.text = bottle.region
            if (bottle.price == null) {
                binding.price.text = ""
            } else {
                binding.price.text = "" + bottle.price + " .-"
            }
            binding.quantity.text = bottle.quantity.toString()

            binding.remove.setOnClickListener {
                updateQuantity(-1)
            }
            binding.add.setOnClickListener {
                updateQuantity(1)
            }
            binding.editBtn.setOnClickListener {
                val edit = DetailsEditBottomSheet(bottle, parentFragment as CellarFragment)
                dismiss()
                edit.show(requireActivity().supportFragmentManager, "bottle_details_edit")
            }
        }

        return binding!!.root
    }

    private fun updateQuantity(quantity: Int) {
        bottle.quantity = bottle.quantity!! + quantity
        binding?.quantity?.text = (bottle.quantity).toString()

        bottle.id?.let { db.collection("bottles").document(it).update("quantity", bottle.quantity) }?.addOnSuccessListener {
            if (bottle.quantity == 0L) {
                dismiss()
                refresh()
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (parentFragment as CellarFragment).refreshBottle(bottle)
    }

}