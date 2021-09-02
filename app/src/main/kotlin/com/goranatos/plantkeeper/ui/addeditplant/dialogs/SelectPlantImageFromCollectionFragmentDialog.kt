package com.goranatos.plantkeeper.ui.addeditplant.dialogs

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.goranatos.plantkeeper.data.entity.OnPlantImageItemClickedListener
import com.goranatos.plantkeeper.data.entity.Plant
import com.goranatos.plantkeeper.data.entity.PlantImageItemCard
import com.goranatos.plantkeeper.databinding.DialogSelectPlantImageFromCollectionBinding
import com.goranatos.plantkeeper.ui.addeditplant.AddOrEditPlantViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder


/**
 * Created by qsufff on 12/7/2020.
 */

class SelectPlantImageUriFromCollectionDialogFragment(val viewModelAddOrEdit: AddOrEditPlantViewModel) :
    DialogFragment() {

    lateinit var myDialog: Dialog

    private var _binding: DialogSelectPlantImageFromCollectionBinding? = null
    private val binding get() = _binding!!


    lateinit var plant: Plant

    /** The system calls this to get the DialogFragment's layout, regardless
    of whether it's being displayed as a dialog or an embedded fragment. */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSelectPlantImageFromCollectionBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    /** The system calls this only when creating the layout in a dialog. */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        myDialog = super.onCreateDialog(savedInstanceState)
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        return myDialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val myList: List<Uri> = listOf(
            Uri.parse("android.resource://" + requireActivity().packageName + "/drawable/ic_plant1"),
            Uri.parse("android.resource://" + requireActivity().packageName + "/drawable/ic_plant2"),
            Uri.parse("android.resource://" + requireActivity().packageName + "/drawable/ic_plant3"),
            Uri.parse("android.resource://" + requireActivity().packageName + "/drawable/ic_plant4"),
            Uri.parse("android.resource://" + requireActivity().packageName + "/drawable/ic_plant5"),
            Uri.parse("android.resource://" + requireActivity().packageName + "/drawable/ic_plant6"),
            Uri.parse("android.resource://" + requireActivity().packageName + "/drawable/ic_plant7"),
            Uri.parse("android.resource://" + requireActivity().packageName + "/drawable/ic_plant8"),

            Uri.parse("android.resource://" + requireActivity().packageName + "/drawable/ic_flower1"),
            Uri.parse("android.resource://" + requireActivity().packageName + "/drawable/ic_flower2"),
            Uri.parse("android.resource://" + requireActivity().packageName + "/drawable/ic_flower3"),
            Uri.parse("android.resource://" + requireActivity().packageName + "/drawable/ic_flower4"),
            Uri.parse("android.resource://" + requireActivity().packageName + "/drawable/ic_flower5"),
            Uri.parse("android.resource://" + requireActivity().packageName + "/drawable/ic_flower6"),
            Uri.parse("android.resource://" + requireActivity().packageName + "/drawable/ic_flower7"),
            Uri.parse("android.resource://" + requireActivity().packageName + "/drawable/ic_flower8"),
        )

        initRecycleView(myList.toPlantItemImageCard())

        plant = viewModelAddOrEdit.thePlant.value!!
    }

    private fun initRecycleView(items: List<PlantImageItemCard>) {

        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(items)
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = groupAdapter
        }

    }

    private fun List<Uri>.toPlantItemImageCard(): List<PlantImageItemCard> {
        return this.map {
            PlantImageItemCard(it, onPlantImageItemClickedListener)
        }
    }

    private val onPlantImageItemClickedListener = object : OnPlantImageItemClickedListener {
        override fun onPlantImageClicked(uri: Uri) {
            plant.string_uri_image_path = uri.toString()
            viewModelAddOrEdit.updateThePlantOutside(plant)

            myDialog.dismiss()

        }
    }
}
