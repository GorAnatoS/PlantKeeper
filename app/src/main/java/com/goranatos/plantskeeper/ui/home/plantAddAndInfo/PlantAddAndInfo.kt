package com.goranatos.plantskeeper.ui.home.plantAddAndInfo

import android.Manifest
import android.R.attr.maxHeight
import android.R.attr.maxWidth
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.databinding.FragmentAddAndChangePlantBinding
import com.goranatos.plantskeeper.ui.base.ScopedFragment
import com.goranatos.plantskeeper.ui.home.MyPlantsFragment.Companion.uiScope
import com.goranatos.plantskeeper.ui.home.MyPlantsViewModel
import com.goranatos.plantskeeper.ui.home.MyPlantsViewModelFactory
import com.goranatos.plantskeeper.util.Helper.Companion.hideKeyboard
import com.ramotion.circlemenu.CircleMenuView
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/*
    Добавляет новые и редактирует имеющийся цветок\растение

    при создание -1 -> создание нового цветка, иначе - редактирование номера в БД
 */

class PlantAddAndInfo : ScopedFragment(), DIAware {

    private fun isToCreateNewPlant() {
        plant_id = arguments?.getInt("plant_id_in_database")!!
        isAddNewPlant = plant_id == -1
    }

    companion object {
        var isAddNewPlant: Boolean = false

        //id and Plant которые надо изменить в БД
        lateinit var thePlant: Plant
        var plant_id = 0


        private const val SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage"

        //Camera
        const val REQUEST_IMAGE_CAPTURE = 631

        //selectPicture
        const val REQUEST_CHOOSE_FROM_GALLERY = 632

        private var currentPhotoPath = ""

    }

    override val di by closestDI()

    private lateinit var viewModel: MyPlantsViewModel
    private val viewModelFactory: MyPlantsViewModelFactory by instance()

    lateinit var binding: FragmentAddAndChangePlantBinding

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == RESULT_OK) {
            when (requestCode) {

                REQUEST_CHOOSE_FROM_GALLERY -> {
                    val selectedUri = data!!.data
                    if (selectedUri != null) {
                        openCropActivity(selectedUri, createImageFile().toUri())
                    } else {
                        Toast.makeText(
                            requireContext(),
                            R.string.toast_cannot_retrieve_selected_image,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                REQUEST_IMAGE_CAPTURE -> {
                    openCropActivity(uri, createImageFile().toUri())
                }

                UCrop.REQUEST_CROP -> {

                    handleCropResult(data)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isToCreateNewPlant()

        viewModel = ViewModelProvider(this, viewModelFactory).get(MyPlantsViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_add_and_change_plant,
                container,
                false
            )

        binding.lifecycleOwner = this

        if (!isAddNewPlant) {
            setHasOptionsMenu(true)
            bindUI()
            binding.buttonAddAndChange.text = getString(R.string.change)
        } else {
            binding.buttonAddAndChange.text = getString(R.string.create)
        }

        setCircleButton()

        binding.buttonAddAndChange.setOnClickListener {

            if (binding.editTextTextPlantName.text.toString().isNullOrEmpty()) {
                showWrongInput()
                Snackbar.make(
                    requireView(),
                    getString(R.string.give_a_name_to_a_plant),
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                if (isAddNewPlant) {
                    uiScope.launch {

                        binding.apply {
                            val newPlant = Plant(
                                0,
                                editTextTextPlantName.text.toString(),
                                editTextTextPlantDescription.text.toString()
                            )

                            viewModel.insertPlant(newPlant)

                        }
                    }

                    Snackbar.make(requireView(), getString(R.string.added), Snackbar.LENGTH_SHORT)
                        .show()
                } else {

                    uiScope.launch(Dispatchers.IO) {

                        val newPlant = Plant(
                            plant_id,
                            binding.editTextTextPlantName.text.toString(),
                            binding.editTextTextPlantDescription.text.toString()
                        )

                        viewModel.updatePlant(newPlant)

                    }
                    Snackbar.make(requireView(), getString(R.string.changed), Snackbar.LENGTH_SHORT)
                        .show()
                }

                hideKeyboard()
                findNavController().navigateUp()
            }
        }

        return binding.root
    }


    private fun setCircleButton() {
        binding.circleButton.eventListener = object :
            CircleMenuView.EventListener() {

            override fun onButtonClickAnimationStart(
                view: CircleMenuView,
                index: Int
            ) {
                when (index) {
                    0 -> binding.plantImage.setImageResource(R.drawable.ic_flower)
                    1 -> binding.plantImage.setImageResource(R.drawable.ic_cactus)
                    2 -> binding.plantImage.setImageResource(R.drawable.ic_plant)
                    3 -> binding.plantImage.setImageResource(R.drawable.ic_tree)
                    4 -> {
                        dispatchTakePictureIntent()
                    }
                    5 -> {
                        chooseFromGallery()
                    }
                }
            }
        }
    }


    private fun showWrongInput() {
        hideKeyboard()

        val color = binding.editTextTextPlantName.currentHintTextColor
        if (binding.editTextTextPlantName.text.isEmpty()) binding.editTextTextPlantName.setHintTextColor(
            ContextCompat.getColor(requireContext(), R.color.errorColor)
        )
        else binding.editTextTextPlantName.setHintTextColor(
            ContextCompat.getColor(
                requireContext(),
                color
            )
        )

        val mTimerTask =
            MyTimerTask()
        val mTimer = Timer()
        mTimer.schedule(mTimerTask, 850)
    }

    internal inner class MyTimerTask : TimerTask() {
        override fun run() {
            activity?.runOnUiThread {
                binding.editTextTextPlantName.setHintTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.textColor
                    )
                )
            }
        }
    }

    private fun bindUI() = launch(Dispatchers.IO) {
        withContext(Dispatchers.Main) {

            viewModel.getPlant(plant_id).observe(viewLifecycleOwner, {
                it?.let { plant ->
                    thePlant = plant

                    binding.apply {
                        binding.editTextTextPlantName.setText(thePlant.name.toString())
                        binding.editTextTextPlantDescription.setText(thePlant.desc.toString())
                    }
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.plant_info_menu, menu)

        val deleteItemFromDB = menu?.findItem(R.id.delete_item_from_db)

        deleteItemFromDB.setOnMenuItemClickListener {
            deleteItemFromDB()
            true
        }
        return super.onCreateOptionsMenu(menu, inflater)
    }

    private fun deleteItemFromDB() {
        launch(Dispatchers.IO) {
            viewModel.deletePlant(thePlant)

            Snackbar.make(requireView(), getString(R.string.deleted), Snackbar.LENGTH_SHORT)
                .show()

            findNavController().navigateUp()
        }
    }

    private var pictureImagePath = ""


    lateinit var uri: Uri
    private fun dispatchTakePictureIntent() {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "$timeStamp.jpg"
        val storageDir: File = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        pictureImagePath = storageDir.absolutePath + "/" + imageFileName
        val file: File = File(pictureImagePath)
        //val outputFileUri = Uri.fromFile(file)
        val outputFileUri = FileProvider.getUriForFile(
            requireContext(),
            requireContext().applicationContext.packageName.toString() + ".provider",
            file
        )
        uri = outputFileUri

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
    }

    private fun chooseFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
            .setType("image/*")
            .addCategory(Intent.CATEGORY_OPENABLE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }

        startActivityForResult(
            Intent.createChooser(
                intent,
                getString(R.string.label_select_picture)
            ), REQUEST_CHOOSE_FROM_GALLERY
        )
    }

    private fun openCropActivity(sourceUri: Uri, destinationUri: Uri) {
        UCrop.of(sourceUri, destinationUri)
            .withMaxResultSize(maxWidth, maxHeight)
            .withAspectRatio(1f, 1f)
            .start(requireContext(), this)
    }

    private fun handleCropResult(data: Intent?) {
        val resultUri = UCrop.getOutput(data!!)
        binding.plantImage.setImageURI(resultUri)
    }

    lateinit var currentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

}