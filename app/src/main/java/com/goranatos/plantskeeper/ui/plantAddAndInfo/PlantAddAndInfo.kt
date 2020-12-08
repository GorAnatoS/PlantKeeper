package com.goranatos.plantskeeper.ui.plantAddAndInfo

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
import com.google.android.material.datepicker.MaterialDatePicker

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.databinding.FragmentAddAndChangePlantBinding
import com.goranatos.plantskeeper.internal.Time
import com.goranatos.plantskeeper.ui.base.ScopedFragment
import com.goranatos.plantskeeper.ui.home.MyPlantsFragment.Companion.uiScope
import com.goranatos.plantskeeper.ui.home.MyPlantsViewModel
import com.goranatos.plantskeeper.ui.home.MyPlantsViewModelFactory
import com.goranatos.plantskeeper.ui.plantAddAndInfo.selecPlantImageFromCollection.IMAGE_URI
import com.goranatos.plantskeeper.ui.plantAddAndInfo.selecPlantImageFromCollection.SelectPlantImageFromCollectionFragment
import com.goranatos.plantskeeper.util.Helper.Companion.hideKeyboard
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

        // TODO: 12/8/2020 new structure DATA
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

        private var formattedDateLong: Long = 0
    }

    override val di by closestDI()

    private lateinit var viewModel: MyPlantsViewModel
    private val viewModelFactory: MyPlantsViewModelFactory by instance()

    lateinit var binding: FragmentAddAndChangePlantBinding

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
            bindCreateNewPlant()
        } else {
            bindEditExistingPlant()
        }

        setTimeFunctions()

        setWaterSwitch()
        setFertilizeSwitch()
        setHibernateSwitch()

        setToggleButtons()

        setImageUriListener()

        setHasOptionsMenu(true)

        return binding.root
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
                            binding.includePlantWatering.editTextNumberSignedDays.text.toString(),
                            if (binding.switchIsHibernateOn.isChecked) 1 else 0
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
                        binding.includePlantWatering.editTextNumberSignedDays.text.toString(),
                        if (binding.switchIsHibernateOn.isChecked) 1 else 0
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
    
    private fun setTimeFunctions() {
        binding.tvDateWaterStartFromVal.text = Time.getFormattedDateString()


        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Поливать с")
        val materialDatePicker = builder.build()

        materialDatePicker.addOnPositiveButtonClickListener {
            binding.tvDateWaterStartFromVal.text = Time.getFormattedDateString(it)
            formattedDateLong = it
        }

        binding.tvDateWaterStartFromVal.setOnClickListener{
            materialDatePicker.show(parentFragmentManager, "DATE_PICKER")
        }


        if (isNewPlant){

        } else {

        }
    }

    private fun setImageUriListener() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(IMAGE_URI)
            ?.observe(viewLifecycleOwner) { uri_string ->
                binding.plantImage.setImageURI(Uri.parse(uri_string))

                currentPhotoPath = uri_string
            }
    }

    private fun setToggleButtons() {

        binding.toggleTakeImage.setOnClickListener {

            val fragmentManager = getParentFragmentManager()
            val newFragment = SelectPlantImageFromCollectionFragment()

            // The device is using a large layout, so show the fragment as a dialog
            newFragment.show(fragmentManager, "dialog")

        }

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

    private fun isToCreateNewPlant() {
        plant_id = arguments?.getInt("plant_id_in_database")!!
        isNewPlant = plant_id == -1
    }


    // TODO: 12/8/2020 material change
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

    private fun bindCreateNewPlant() {
        binding.groupContent.visibility = View.VISIBLE
        binding.groupLoading.visibility = View.GONE

        binding.switchWater.isChecked = true
        binding.includePlantWatering.waterGroup.visibility = View.VISIBLE

        currentPhotoPath = "android.resource://" + requireContext().getPackageName() + "/drawable/ic_plant1"
        binding.plantImage.setImageURI(Uri.parse(currentPhotoPath))

        binding.switchIsHibernateOn.isChecked = false
        binding.includeHibernateSettings.cardHibernateGroup.visibility = View.GONE
        binding.groupHibernateData.visibility = View.GONE
    }

    private fun bindEditExistingPlant() = launch(Dispatchers.IO) {

        withContext(Dispatchers.Main) {

            viewModel.getPlant(plant_id).observe(viewLifecycleOwner, {
                it?.let { plant ->
                    thePlant = plant

                    binding.apply {
                        binding.editTextTextPlantName.editText?.setText(thePlant.name.toString())
                        binding.editTextTextPlantDescription.setText(thePlant.desc.toString())

                        if (!plant.image_path.isNullOrEmpty()) {
                            binding.plantImage.setImageURI(Uri.parse(plant.image_path))
                            currentPhotoPath = plant.image_path
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

                        binding.switchIsHibernateOn.isChecked = plant.is_hibernate_on == 1

                        if (binding.switchIsHibernateOn.isChecked){
                            binding.includeHibernateSettings.cardHibernateGroup.visibility = View.VISIBLE
                            binding.groupHibernateData.visibility = View.VISIBLE
                        } else {
                            binding.includeHibernateSettings.cardHibernateGroup.visibility = View.GONE
                            binding.groupHibernateData.visibility = View.GONE
                        }

                    }
                }
            })

            binding.groupContent.visibility = View.VISIBLE
            binding.groupLoading.visibility = View.GONE
        }
    }


    private fun setHibernateSwitch() {
        binding.switchIsHibernateOn.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.includeHibernateSettings.cardHibernateGroup.visibility = View.VISIBLE
                binding.groupHibernateData.visibility = View.VISIBLE
            } else {
                binding.includeHibernateSettings.cardHibernateGroup.visibility = View.GONE
                binding.groupHibernateData.visibility = View.GONE
            }
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
            deletePlantItemFromDB()
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

    private fun deletePlantItemFromDB() {
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

    //Gallery And Photo functions

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun dispatchTakePictureIntent() {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "$timeStamp.jpg"
        val storageDir: File = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )

        var pictureImagePath = storageDir.absolutePath + "/" + imageFileName

        val file: File = File(pictureImagePath)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    // TODO: 12/5/2020 Внешний вид кропа изменить под стиль прилоржения*


    // TODO: 12/8/2020 !!! 
    

}