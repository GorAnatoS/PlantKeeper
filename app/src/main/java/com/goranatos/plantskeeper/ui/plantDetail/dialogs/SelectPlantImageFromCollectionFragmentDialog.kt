package com.goranatos.plantskeeper.ui.plantDetail.dialogs

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.data.entity.OnPlantImageItemClickedListener
import com.goranatos.plantskeeper.data.entity.PlantImageItemCard
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.dialog_select_plant_image_from_collection.*


/**
 * Created by qsufff on 12/7/2020.
 */

const val IMAGE_URI = "image_ur"

class SelectPlantImageFromCollectionFragment : DialogFragment() {

    lateinit var myDialog: Dialog

    /** The system calls this to get the DialogFragment's layout, regardless
    of whether it's being displayed as a dialog or an embedded fragment. */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout to use as dialog or embedded fragment


        return inflater.inflate(
            R.layout.dialog_select_plant_image_from_collection,
            container,
            false
        )
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val myList: MutableList<Uri> = mutableListOf(
            Uri.parse("android.resource://" + requireContext().getPackageName() + "/drawable/ic_plant1"),
            Uri.parse("android.resource://" + requireContext().getPackageName() + "/drawable/ic_plant2"),
            Uri.parse("android.resource://" + requireContext().getPackageName() + "/drawable/ic_plant3"),
            Uri.parse("android.resource://" + requireContext().getPackageName() + "/drawable/ic_plant4"),
            Uri.parse("android.resource://" + requireContext().getPackageName() + "/drawable/ic_plant5"),
            Uri.parse("android.resource://" + requireContext().getPackageName() + "/drawable/ic_plant6"),
            Uri.parse("android.resource://" + requireContext().getPackageName() + "/drawable/ic_plant7"),
            Uri.parse("android.resource://" + requireContext().getPackageName() + "/drawable/ic_plant8"),

            Uri.parse("android.resource://" + requireContext().getPackageName() + "/drawable/ic_flower1"),
            Uri.parse("android.resource://" + requireContext().getPackageName() + "/drawable/ic_flower2"),
            Uri.parse("android.resource://" + requireContext().getPackageName() + "/drawable/ic_flower3"),
            Uri.parse("android.resource://" + requireContext().getPackageName() + "/drawable/ic_flower4"),
            Uri.parse("android.resource://" + requireContext().getPackageName() + "/drawable/ic_flower5"),
            Uri.parse("android.resource://" + requireContext().getPackageName() + "/drawable/ic_flower6"),
            Uri.parse("android.resource://" + requireContext().getPackageName() + "/drawable/ic_flower7"),
            Uri.parse("android.resource://" + requireContext().getPackageName() + "/drawable/ic_flower8"),

            )

        initRecycleView(myList.toPlantItemImageCard())
    }

    private fun initRecycleView(items: List<PlantImageItemCard>) {


        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(items)
        }

        recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = groupAdapter
        }

    }

    private fun List<Uri>.toPlantItemImageCard(): List<PlantImageItemCard> {
        return this.map {
            PlantImageItemCard(it, onPlantImageItemClickedListener)
        }
    }

    val onPlantImageItemClickedListener = object : OnPlantImageItemClickedListener {
        override fun onPlantImageClicked(uri: Uri) {
            findNavController().currentBackStackEntry?.savedStateHandle?.set(
                IMAGE_URI,
                uri.toString()
            )
            myDialog.dismiss()

        }
    }
}