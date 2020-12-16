package com.goranatos.plantskeeper.ui.plantDetail

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.data.entity.Plant
import com.goranatos.plantskeeper.databinding.FragmentDetailedPlantBinding
import com.goranatos.plantskeeper.internal.TimeHelper
import com.goranatos.plantskeeper.ui.base.ScopedFragment
import com.goranatos.plantskeeper.ui.plantDetail.PlantDetailViewModel.Companion.REQUEST_CHOOSE_FROM_GALLERY
import com.goranatos.plantskeeper.ui.plantDetail.PlantDetailViewModel.Companion.REQUEST_IMAGE_CAPTURE
import com.goranatos.plantskeeper.ui.plantDetail.PlantDetailViewModel.Companion.uriCapturedImage
import com.goranatos.plantskeeper.ui.plantDetail.PlantDetailViewModel.Companion.uriDestination
import com.goranatos.plantskeeper.ui.plantDetail.dialogs.*
import com.goranatos.plantskeeper.util.Helper.Companion.hideKeyboard
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
class PlantDetailFragment : ScopedFragment(), DIAware {
    override val di by closestDI()

    private val args: PlantDetailFragmentArgs by navArgs()

    private lateinit var viewModel: PlantDetailViewModel

    lateinit var binding: FragmentDetailedPlantBinding

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {

                REQUEST_CHOOSE_FROM_GALLERY -> {
                    val selectedUri = data!!.data
                    createUriDestinationForImageFile(requireContext())

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
                    createUriDestinationForImageFile(requireContext())
                    openCropActivity(uriCapturedImage, uriDestination)
                }

                UCrop.REQUEST_CROP -> {
                    handleCropResult(data)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_detailed_plant,
                container,
                false
            )
        binding.lifecycleOwner = this

        val viewModelFactory: PlantDetailViewModelFactory by instance(arg = args.plantId)

        viewModel = ViewModelProvider(this, viewModelFactory).get(PlantDetailViewModel::class.java)

        viewModel.setPlant()

        binding.viewModel = viewModel

        viewModel.thePlant.observe(viewLifecycleOwner, { plant ->
            setUIforPlant(plant)
        })

        uiSetup()

        return binding.root
    }

    //При получении Растения настраиваем элементы UI
    private fun setUIforPlant(plant: Plant) {
        binding.groupContent.visibility = View.VISIBLE
        binding.groupLoading.visibility = View.GONE

        if (plant.string_uri_image_path != null) {
            binding.plantImage.setImageURI(Uri.parse(plant.string_uri_image_path))
        }

        if (plant.str_desc != null) {
            binding.editTextTextPlantDescription.setText(plant.str_desc.toString())
        }

        if (plant.is_water_need_on != 0) {
            binding.toggleGroupToWater.check(binding.toggleButtonToWater.id)
            onWaterNeedOn()

            if (plant.is_watering_hibernate_mode_on != 0){
                binding.groupOnWateringHibernateModeOn.visibility = View.VISIBLE
            } else {
                binding.groupOnWateringHibernateModeOn.visibility = View.GONE
            }

        } else {
            binding.toggleGroupToWater.uncheck(binding.toggleButtonToWater.id)
            onWaterNeedOff()
        }

        plant.int_watering_frequency_normal?.let {
            binding.tvWateringFrequency.text = it.toString()
        }

        plant.int_watering_frequency_in_hibernate?.let {
            binding.tvWateringFrequencyInHibernate.text = it.toString()
        }

        if (plant.long_to_water_from_date != null) {
            binding.tvToWaterFromDateVal.text = TimeHelper.getFormattedDateString(plant.long_to_water_from_date!!)
        }

        if (plant.is_hibernate_mode_on != 0) {
            binding.toggleGroupToHibernate.check(binding.toggleButtonToHibernate.id)
            onHibernateModeOn()

            binding.tvDateHibernateStartFromVal.text =
                plant.long_to_hibernate_from_date?.let { TimeHelper.getFormattedDateString(it) }

            binding.tvDateHibernateFinishVal.text =
                plant.long_to_hibernate_till_date?.let { TimeHelper.getFormattedDateString(it) }


        } else {
            binding.toggleGroupToHibernate.uncheck(binding.toggleButtonToHibernate.id)
            onHibernateModeOff()
        }


        Toast.makeText(
            requireContext(),
            viewModel.thePlant.value.toString() + "\n\n",
            Toast.LENGTH_SHORT
        ).show()
    }


    private fun uiSetup() {
        setOnPlantNameEditTextChangedListener()

        setSelectImageForThePlantToggleGroup()

        setWaterToggleGroup()
        setHibernateToggleGroup()

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.plant_detail_menu, menu)

        val deleteItemFromDB = menu?.findItem(R.id.delete_item_from_db)

        deleteItemFromDB.setOnMenuItemClickListener {
            deletePlantItemFromDB()
            true
        }

        val savePlantToDB = menu.findItem(R.id.save_plant_to_db)
        savePlantToDB.setOnMenuItemClickListener {
            onClickOptionMenuSavePlant()
            true
        }

        deleteItemFromDB.isVisible = !viewModel.isToCreateNewPlant

        return super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setSelectImageForThePlantToggleGroup() {
        binding.toggleSelectImage.setOnClickListener {
            startSelectPlantImageUriFragmentDialog()
        }

        binding.toggleTakePhoto.setOnClickListener {
            val items = arrayOf(getString(R.string.from_gallery), getString(R.string.take_photo))

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.choose_photo))
                .setItems(items) { dialog, which ->
                    // Respond to item chosen
                    when (which) {
                        0 -> chooseFromGallery()
                        1 -> dispatchTakePictureIntentWithPermissionCheck()
                    }
                }
                .show()
        }
    }

    private fun startSelectPlantImageUriFragmentDialog() {
        val newFragment = SelectPlantImageUriFromCollectionDialogFragment(viewModel)
        newFragment.show(parentFragmentManager, "dialog")
    }


    //FUNCTIONS FOR SELECTION IMAGE OF THE PLANT START
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

    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            viewModel.setPlantImageUriString(absolutePath)
        }
    }

    fun createUriDestinationForImageFile(context: Context) {
        uriDestination = createImageFile(context).toUri()
        viewModel.setPlantImageUriString(uriDestination.toString())
    }

    // TODO: 12/5/2020 Внешний вид кропа изменить под стиль прилоржения*
    private fun openCropActivity(sourceUri: Uri, destinationUri: Uri) {
        UCrop.of(sourceUri, destinationUri)
            // .withMaxResultSize(maxWidth, maxHeight)
            .withAspectRatio(1f, 1f)
            .start(requireContext(), this)
    }

    private fun handleCropResult(data: Intent?) {
        val resultUri = UCrop.getOutput(data!!)
        binding.plantImage.setImageURI(resultUri)
    }
    //FUNCTIONS FOR SELECTIONG IMAGE OF THE PLANT END


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
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

    //OPTIONS MENU START
    private fun deletePlantItemFromDB() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.delete_plant_from_db))
            .setMessage(resources.getString(R.string.are_you_sure_to_delete_the_plant_from_db))
            .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
            }
            .setPositiveButton(resources.getString(R.string.delete_item)) { dialog, which ->
                launch(Dispatchers.IO) {
                    viewModel.deletePlant()

                    Snackbar.make(requireView(), getString(R.string.deleted), Snackbar.LENGTH_SHORT)
                        .show()

                    findNavController().navigateUp()
                }
            }.show()
    }

    private fun onClickOptionMenuSavePlant() {
        if (binding.editTextTextPlantName.editText?.text.toString().isNullOrEmpty()) {
            binding.editTextTextPlantName.error = "Назовите растение"
            return
        } else {
            binding.editTextTextPlantName.isErrorEnabled = false
        }

        viewModel.onInsertOrUpdatePlant()

        if (viewModel.isToCreateNewPlant) {
            Snackbar.make(requireView(), getString(R.string.added), Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(requireView(), getString(R.string.changed), Snackbar.LENGTH_SHORT).show()
        }

        hideKeyboard()
        findNavController().navigateUp()
    }
    //OPTIONS MENU END

    private fun setOnPlantNameEditTextChangedListener() {
        binding.editTextTextPlantNameInputText.addTextChangedListener {
            viewModel.setPlantName(it.toString())
        }
    }

    //START WaterToggleGroup
    var tempCheckedToggleGroupToWater = false
    private fun setWaterToggleGroup() {
        binding.toggleGroupToWater.addOnButtonCheckedListener { _, _, isChecked ->
            if (isChecked) {
                tempCheckedToggleGroupToWater = true
                onWaterNeedOn()
            } else {
                tempCheckedToggleGroupToWater = false
                onWaterNeedOff()
            }
        }

        binding.toggleButtonToWater.setOnClickListener {
            //Проверка на чек при старте, чтобы не было
            if (tempCheckedToggleGroupToWater) {
                startSetWateringSettingsFragmentDialog()
            }
        }

        binding.tvToWaterFromDateVal.setOnClickListener {
            startSetWateringSettingsFragmentDialog()
        }
        binding.tvWateringFrequency.setOnClickListener {
            startSetWateringSettingsFragmentDialog()
        }
        binding.tvWateringFrequencyInHibernate.setOnClickListener{
            startSetWateringSettingsFragmentDialog()
        }
    }

    private fun startSetWateringSettingsFragmentDialog() {
        val fragmentManager = parentFragmentManager
        val newFragment = SetWateringSettingsFragmentDialog(viewModel)
        newFragment.show(fragmentManager, "dialog")
    }

    private fun onWaterNeedOn() {
        binding.tvToWaterFromDateVal.visibility = View.VISIBLE
        binding.tvWateringFrequency.visibility = View.VISIBLE
        binding.tvWateringFrequencyInHibernate.visibility = View.VISIBLE

        viewModel.setWaterNeedModeOn()
    }

    private fun onWaterNeedOff() {
        binding.tvToWaterFromDateVal.visibility = View.GONE
        binding.tvWateringFrequency.visibility = View.GONE
        binding.tvWateringFrequencyInHibernate.visibility = View.GONE

        viewModel.setWaterNeedModeOff()
    }
    //END WaterToggleGroup


    //START HIBERNATE MODE settings
    var tempCheckedToggleGroupToHibernate = false
    private fun setHibernateToggleGroup() {
        binding.toggleGroupToHibernate.addOnButtonCheckedListener { _, _, isChecked ->
            if (isChecked) {
                onHibernateModeOn()
            } else {
                onHibernateModeOff()
            }
        }

        binding.toggleButtonToHibernate.setOnClickListener {
            //Проверка на чек при старте, чтобы не было
            if (tempCheckedToggleGroupToHibernate) {
                startSetHibernateSettingsFragmentDialog()
            }
        }

        binding.tvDateHibernateStartFromVal.setOnClickListener {
            startSetHibernateSettingsFragmentDialog()
        }

        binding.tvDateHibernateFinishVal.setOnClickListener {
            startSetHibernateSettingsFragmentDialog()
        }

    }

    private fun startSetHibernateSettingsFragmentDialog() {
        val fragmentManager = parentFragmentManager
        val newFragment = SetHibernateSettingsFragmentDialog(viewModel)
        newFragment.show(fragmentManager, "dialog")
    }

    private fun onHibernateModeOn() {
        tempCheckedToggleGroupToHibernate = true
        binding.groupHibernateData.visibility = View.VISIBLE

        viewModel.setHibernateModeOn()
    }

    private fun onHibernateModeOff() {
        tempCheckedToggleGroupToHibernate = false
        binding.groupHibernateData.visibility = View.GONE

        viewModel.setHibernateModeOff()

    }
    //END HIBERNATE MODE settings
}