package com.goranatos.plantskeeper.ui.home.plantAddAndInfo

import android.Manifest
import android.R.attr.maxHeight
import android.R.attr.maxWidth
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/*
    Добавляет новые и редактирует имеющийся цветок\растение

    при создание -1 -> создание нового цветка, иначе - редактирование номера в БД
 */

@RuntimePermissions
class PlantAddAndInfo : ScopedFragment(), DIAware {
    companion object {
        var isNewPlant: Boolean = false

        //id and Plant которые надо изменить в БД
        lateinit var thePlant: Plant
        var plant_id = 0

        //Camera
        const val REQUEST_IMAGE_CAPTURE = 631

        //selectPicture
        const val REQUEST_CHOOSE_FROM_GALLERY = 632

        private var currentPhotoPath = ""

        lateinit var uriDestination: Uri
        lateinit var uriCapturedImage: Uri
    }

    override val di by closestDI()

    private lateinit var viewModel: MyPlantsViewModel
    private val viewModelFactory: MyPlantsViewModelFactory by instance()

    lateinit var binding: FragmentAddAndChangePlantBinding

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {

                REQUEST_CHOOSE_FROM_GALLERY -> {
                    val selectedUri = data!!.data

                    uriDestination = createImageFile().toUri()
                    currentPhotoPath = uriDestination.toString()

                    if (selectedUri != null) {
                        openCropActivity(selectedUri, uriDestination)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            R.string.toast_cannot_retrieve_selected_image,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                REQUEST_IMAGE_CAPTURE -> {
                    uriDestination = createImageFile().toUri()
                    currentPhotoPath = uriDestination.toString()

                    openCropActivity(uriCapturedImage, uriDestination)
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

        if (isNewPlant) {
            binding.groupContent.visibility = View.VISIBLE
            binding.groupLoading.visibility = View.GONE

            binding.switchWater.isChecked = true
            binding.includePlantWatering.waterGroup.visibility = View.VISIBLE
        } else {
            bindUISetPlant()

        }

        setWaterSwitch()
        setFertilizeSwitch()

        setCircleButton()

        setToggleButtons()

        setHasOptionsMenu(true)

        return binding.root
    }


    private fun clickSavePlantToDB() {
        if (binding.editTextTextPlantName.editText?.text.toString().isNullOrEmpty()) {
            showWrongInput()
            Snackbar.make(
                requireView(),
                getString(R.string.give_a_name_to_a_plant),
                Snackbar.LENGTH_SHORT
            ).show()
        } else {
            if (isNewPlant) {
                uiScope.launch {

                    binding.apply {
                        val newPlant = Plant(
                            0,
                            editTextTextPlantName.editText?.text.toString(),
                            editTextTextPlantDescription.text.toString(),
                            currentPhotoPath,
                            binding.includePlantWatering.editTextNumberSignedDays.text.toString()
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
                        binding.editTextTextPlantName.editText?.text.toString(),
                        binding.editTextTextPlantDescription.text.toString(),
                        currentPhotoPath,
                        binding.includePlantWatering.editTextNumberSignedDays.text.toString()
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

    private fun setToggleButtons() {

        /*  binding.toggleTakeImage.setOnClickListener {
              val items = arrayOf("Item 1", "Item 2", "Item 3")

              MaterialAlertDialogBuilder(requireContext())
                  .setTitle(resources.getString(R.string.add))
                  .setItems(items) { dialog, which ->
                      // Respond to item chosen
                  }
                  .show()
          }*/

        binding.toggleTakePhoto.setOnClickListener {
            val items = arrayOf(getString(R.string.from_gallery), getString(R.string.take_photo))

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.choose_photo))
                .setItems(items) { dialog, which ->
                    // Respond to item chosen
                    when (which) {
                        0 -> {
                            chooseFromGallery()
                        }
                        1 -> {
                            dispatchTakePictureIntentWithPermissionCheck()
                        }
                    }
                }
                .show()
        }

    }

    private fun setCircleButton() {
        binding.circleButton.eventListener = object :
            CircleMenuView.EventListener() {

            override fun onButtonClickAnimationStart(
                view: CircleMenuView,
                index: Int
            ) {
                when (index) {
                    0 -> {
                        binding.plantImage.setImageResource(R.drawable.ic_flower)
                        currentPhotoPath =
                            Uri.parse("android.resource://" + requireContext().getPackageName() + "/drawable/ic_flower")
                                .toString()

                    }
                    1 -> {
                        binding.plantImage.setImageResource(R.drawable.ic_cactus)
                        currentPhotoPath =
                            Uri.parse("android.resource://" + requireContext().getPackageName() + "/drawable/ic_cactus")
                                .toString()
                    }
                    2 -> {
                        binding.plantImage.setImageResource(R.drawable.ic_plant)
                        currentPhotoPath =
                            Uri.parse("android.resource://" + requireContext().getPackageName() + "/drawable/ic_plant")
                                .toString()
                    }
                    3 -> {
                        binding.plantImage.setImageResource(R.drawable.ic_tree)
                        currentPhotoPath =
                            Uri.parse("android.resource://" + requireContext().getPackageName() + "/drawable/ic_tree")
                                .toString()
                    }
                    4 -> {
                        dispatchTakePictureIntentWithPermissionCheck()
                    }
                    5 -> {
                        chooseFromGallery()
                    }
                }
            }
        }
    }

    private fun isToCreateNewPlant() {
        plant_id = arguments?.getInt("plant_id_in_database")!!
        isNewPlant = plant_id == -1
    }

    private fun showWrongInput() {
        hideKeyboard()

        val color = binding.editTextTextPlantName.editText?.currentHintTextColor
        if (binding.editTextTextPlantName.editText?.text.isNullOrEmpty()) binding.editTextTextPlantName.editText?.setHintTextColor(
            ContextCompat.getColor(requireContext(), R.color.errorColor)
        )
        else binding.editTextTextPlantName.editText?.setHintTextColor(
            ContextCompat.getColor(
                requireContext(),
                color!!
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
                binding.editTextTextPlantName.editText?.setHintTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.textColor
                    )
                )
            }
        }
    }


    private fun bindUISetPlant() = launch(Dispatchers.IO) {
        withContext(Dispatchers.Main) {

            viewModel.getPlant(plant_id).observe(viewLifecycleOwner, {
                it?.let { plant ->
                    thePlant = plant

                    binding.apply {
                        binding.editTextTextPlantName.editText?.setText(thePlant.name.toString())
                        binding.editTextTextPlantDescription.setText(thePlant.desc.toString())

                        if (!plant.image_path.isNullOrEmpty()) {
                            binding.plantImage.setImageURI(Uri.parse(plant.image_path))
                        }

                        if (plant.water_need.isNullOrEmpty()) {
                            binding.includePlantWatering.waterGroup.visibility = View.GONE
                            binding.includePlantWatering.hibernateMode.visibility = View.GONE

                            binding.includePlantFertilizing.fertiliteGroup.visibility = View.GONE
                            binding.includePlantFertilizing.hibernateMode.visibility = View.GONE
                        } else {
                            binding.includePlantFertilizing.fertiliteGroup.visibility = View.VISIBLE
                            binding.includePlantFertilizing.hibernateMode.visibility = View.VISIBLE
                        }
                    }
                }
            })

            binding.groupContent.visibility = View.VISIBLE
            binding.groupLoading.visibility = View.GONE
        }
    }

    private fun setWaterSwitch() {
        if (isNewPlant) {
            binding.switchWater.isChecked = true
            binding.includePlantWatering.switchHibernate.isChecked = false

            binding.includePlantWatering.waterGroup.visibility = View.VISIBLE
            binding.includePlantWatering.hibernateMode.visibility = View.GONE
        }

        binding.switchWater.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.includePlantWatering.waterGroup.visibility = View.VISIBLE
            } else {
                binding.includePlantWatering.waterGroup.visibility = View.GONE
            }
        }

        binding.includePlantWatering.switchHibernate.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.includePlantWatering.hibernateMode.visibility = View.VISIBLE
            } else {
                binding.includePlantWatering.hibernateMode.visibility = View.GONE
            }
        }
    }

    private fun setFertilizeSwitch() {
        if (isNewPlant) {
            binding.switchFertilize.isChecked = true
            binding.includePlantFertilizing.switchHibernate.isChecked = false

            binding.includePlantFertilizing.fertiliteGroup.visibility = View.VISIBLE
            binding.includePlantFertilizing.hibernateMode.visibility = View.GONE
        }

        binding.switchFertilize.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.includePlantFertilizing.fertiliteGroup.visibility = View.VISIBLE
            } else {
                binding.includePlantFertilizing.fertiliteGroup.visibility = View.GONE
            }
        }

        binding.includePlantFertilizing.switchHibernate.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.includePlantFertilizing.hibernateMode.visibility = View.VISIBLE
            } else {
                binding.includePlantFertilizing.hibernateMode.visibility = View.GONE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.plant_info_menu, menu)


        val deleteItemFromDB = menu?.findItem(R.id.delete_item_from_db)

        deleteItemFromDB.setOnMenuItemClickListener {
            deleteItemFromDB()
            true
        }


        val savePlantToDB = menu?.findItem(R.id.save_plant_to_db)
        savePlantToDB.setOnMenuItemClickListener {
            clickSavePlantToDB()
            true
        }


        // TODO: 12/6/2020 сделать название кнопки сохранить или изменить
        if (isNewPlant) {
            deleteItemFromDB.isVisible = false
            //savePlantToDB.text = requireContext().getString(R.string.create) 

        } else {
            //binding.buttonAddAndChange.text = getString(R.string.change)   
        }



        return super.onCreateOptionsMenu(menu, inflater)
    }

    private fun deleteItemFromDB() {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.delete_plant_from_db))
            .setMessage(resources.getString(R.string.are_you_sure_to_delete_the_plant_from_db))
            .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
                // Respond to neutral button press
            }
            .setPositiveButton(resources.getString(R.string.delete_item)) { dialog, which ->
                // Respond to positive button press
                launch(Dispatchers.IO) {
                    viewModel.deletePlant(thePlant)

                    Snackbar.make(requireView(), getString(R.string.deleted), Snackbar.LENGTH_SHORT)
                        .show()

                    findNavController().navigateUp()
                }
            }
            .show()

    }

    private var pictureImagePath = ""


    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun dispatchTakePictureIntent() {
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
        uriCapturedImage = outputFileUri

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


    @Throws(IOException::class)
    fun createImageFile(): File {
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

    // TODO: 12/5/2020 Внешний вид кропа изменить под стиль прилоржения*
}