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
import com.goranatos.plantskeeper.ui.plantDetail.dialogs.SelectPlantImageUriFromCollectionDialogFragment
import com.goranatos.plantskeeper.ui.plantDetail.dialogs.SetFertilizingSettingsFragmentDialog
import com.goranatos.plantskeeper.ui.plantDetail.dialogs.SetHibernatingSettingsFragmentDialog
import com.goranatos.plantskeeper.ui.plantDetail.dialogs.SetWateringSettingsFragmentDialog
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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

        binding.viewModel = viewModel

        binding.fragment = this

        uiSetup()

        hideRelatedToChipsMaterialCards()
        if (viewModel.isToCreateNewPlant) {
            binding.chipHibernatingMode.isChecked =
                viewModel.thePlant.value?.is_hibernate_mode_on == 1
            binding.chipWateringMode.isChecked = viewModel.thePlant.value?.is_water_need_on == 1
            binding.chipFertilizingMode.isChecked =
                viewModel.thePlant.value?.is_fertilize_need_on == 1
        }

        viewModel.thePlant.observe(viewLifecycleOwner, { plant ->
            setUIforPlant(plant)
        })

        viewModel.setPlant()

        return binding.root
    }

    private fun hideRelatedToChipsMaterialCards() {
        binding.materialCardHibernateGroup.visibility = View.GONE
        binding.materialCardWaterGroup.visibility = View.GONE
        binding.materialCardFertilizingGroup.visibility = View.GONE
    }


    var isFirstStart = true

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


        //START_CHIPS
        if (isFirstStart) {
            binding.chipHibernatingMode.isChecked = plant.is_hibernate_mode_on == 1
            binding.chipWateringMode.isChecked = plant.is_water_need_on == 1
            binding.chipFertilizingMode.isChecked = plant.is_fertilize_need_on == 1

            isFirstStart = false
        }
        //END CHIPS

        //START HIBERNATE
        if (plant.is_hibernate_mode_on == 1) {
            binding.toggleGroupToHibernate.check(binding.toggleButtonToHibernate.id)
            viewModel.setHibernateModeOn()
        } else {
            binding.toggleGroupToHibernate.uncheck(binding.toggleButtonToHibernate.id)
            viewModel.setHibernateModeOff()
        }
        //END HIBERNATE

        //START WATER
        if (plant.is_water_need_on == 1) {
            binding.toggleGroupToWater.check(binding.toggleButtonToWater.id)
            viewModel.setWaterNeedModeOn()
        } else {
            binding.toggleGroupToWater.uncheck(binding.toggleButtonToWater.id)
            viewModel.setWaterNeedModeOff()
        }

        plant.int_watering_frequency_in_hibernate?.let {
            binding.tvWateringFrequencyInHibernate.text = it.toString()
        }

        if (plant.is_watering_hibernate_mode_on == 1) {
            binding.tvWateringFrequencyInHibernate.visibility = View.VISIBLE
        } else {
            binding.tvWateringFrequencyInHibernate.visibility = View.GONE
        }
        //END WATER

        //START FERTILIZE
        if (plant.is_fertilize_need_on == 1) {
            binding.toggleGroupToFertilize.check(binding.toggleButtonToFertilize.id)
            viewModel.setFertilizeNeedModeOn()
        } else {
            binding.toggleGroupToFertilize.uncheck(binding.toggleButtonToFertilize.id)
            viewModel.setFertilizeNeedModeOff()
        }

        plant.int_fertilizing_frequency_in_hibernate?.let {
            binding.tvFertilizingFrequencyInHibernate.text = it.toString()
        }

        if (plant.is_fertilizing_hibernate_mode_on == 1) {
            binding.tvFertilizingFrequencyInHibernate.visibility = View.VISIBLE
        } else {
            binding.tvFertilizingFrequencyInHibernate.visibility = View.GONE
        }
        //END FERTILIZE
    }

    private fun uiSetup() {
        setOnPlantNameEditTextChangedListener()

        setSelectImageForThePlantToggleGroup()

        setChipsClickListeners()

        setWaterToggleGroup()
        setFertilizeToggleGroup()
        setHibernateToggleGroup()
    }

    private fun setChipsClickListeners() {
        binding.chipHibernatingMode.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> {
                    binding.materialCardHibernateGroup.visibility = View.VISIBLE

                    if (viewModel.thePlant.value?.is_hibernate_mode_on == 1) {
                        binding.toggleGroupToHibernate.check(binding.toggleButtonToHibernate.id)
                        binding.groupHibernateDetails.visibility = View.VISIBLE
                    } else {
                        binding.toggleGroupToHibernate.uncheck(binding.toggleButtonToHibernate.id)
                        binding.groupHibernateDetails.visibility = View.GONE
                    }
                }
                false -> {
                    binding.materialCardHibernateGroup.visibility = View.GONE
                    viewModel.setHibernateModeOff()
                }
            }
        }

        binding.chipWateringMode.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> {
                    binding.materialCardWaterGroup.visibility = View.VISIBLE

                    if (viewModel.thePlant.value?.is_water_need_on == 1) {
                        binding.toggleGroupToWater.check(binding.toggleButtonToWater.id)
                    } else {
                        binding.toggleGroupToWater.uncheck(binding.toggleButtonToWater.id)
                    }

                    if (viewModel.thePlant.value?.is_watering_hibernate_mode_on == 1 && viewModel.thePlant.value?.is_water_need_on == 1) {
                        binding.tvWateringFrequencyInHibernate.visibility = View.VISIBLE
                    } else binding.tvWateringFrequencyInHibernate.visibility = View.GONE
                }
                false -> {
                    binding.materialCardWaterGroup.visibility = View.GONE
                    viewModel.setWaterNeedModeOff()
                }
            }
        }

        binding.chipFertilizingMode.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> {
                    binding.materialCardFertilizingGroup.visibility = View.VISIBLE

                    if (viewModel.thePlant.value?.is_fertilize_need_on == 1) {
                        binding.toggleGroupToFertilize.check(binding.toggleButtonToFertilize.id)
                    } else {
                        binding.toggleGroupToFertilize.uncheck(binding.toggleButtonToFertilize.id)
                    }

                    if (viewModel.thePlant.value?.is_fertilizing_hibernate_mode_on == 1 && viewModel.thePlant.value?.is_fertilize_need_on == 1) {
                        binding.tvFertilizingFrequencyInHibernate.visibility = View.VISIBLE
                    } else binding.tvFertilizingFrequencyInHibernate.visibility = View.GONE
                }

                false -> {
                    binding.materialCardFertilizingGroup.visibility = View.GONE
                    viewModel.setFertilizeNeedModeOff()
                }
            }
        }
    }

    private fun setOnPlantNameEditTextChangedListener() {
        binding.editTextTextPlantNameInputText.addTextChangedListener {
            viewModel.setPlantName(it.toString())
        }

        binding.editTextTextPlantDescription.addTextChangedListener {
            viewModel.setPlantDescription(it.toString())
        }
    }

    //START WaterToggleGroup
    private var tempCheckedToggleGroupToWater = false
    private fun setWaterToggleGroup() {
        binding.toggleGroupToWater.addOnButtonCheckedListener { _, _, isChecked ->
            if (isChecked) {
                tempCheckedToggleGroupToWater = true

                binding.groupWateringDetails.visibility = View.VISIBLE

                if (viewModel.thePlant.value?.long_next_watering_date != null) {
                    binding.tvToWaterFromDateVal.text =
                        TimeHelper.getFormattedDateString(viewModel.thePlant.value?.long_next_watering_date!!)
                }

                viewModel.thePlant.value?.int_watering_frequency_normal?.let {
                    binding.tvWateringFrequency.text = it.toString()
                }

                viewModel.setWaterNeedModeOn()
            } else {
                tempCheckedToggleGroupToWater = false

                binding.groupWateringDetails.visibility = View.GONE

                viewModel.setWaterNeedModeOff()
            }
        }
    }

    fun startSetWateringSettingsFragmentDialog() {
        //Проверка на чек при старте, чтобы не было
        if (tempCheckedToggleGroupToWater) {
            val fragmentManager = parentFragmentManager
            val newFragment = SetWateringSettingsFragmentDialog(viewModel)
            newFragment.show(fragmentManager, "dialog")
        }
    }
    //END WaterToggleGroup

    //START FERTILIZING PART
    private var tempCheckedToggleGroupToFertilize = false
    private fun setFertilizeToggleGroup() {
        binding.toggleGroupToFertilize.addOnButtonCheckedListener { _, _, isChecked ->
            if (isChecked) {
                tempCheckedToggleGroupToFertilize = true

                binding.groupFertilizingDetails.visibility = View.VISIBLE

                if (viewModel.thePlant.value?.long_next_fertilizing_date != null) {
                    binding.tvToFertilizeFromDateVal.text =
                        TimeHelper.getFormattedDateString(viewModel.thePlant.value?.long_next_fertilizing_date!!)
                }

                viewModel.thePlant.value?.int_fertilizing_frequency_normal?.let {
                    binding.tvFertilizingFrequency.text = it.toString()
                }

                viewModel.setFertilizeNeedModeOn()
            } else {
                tempCheckedToggleGroupToFertilize = false

                binding.groupFertilizingDetails.visibility = View.GONE

                viewModel.setFertilizeNeedModeOff()
            }
        }
    }

    fun startSetFertilizingSettingsFragmentDialog() {
        //Проверка на чек при старте, чтобы не было
        if (tempCheckedToggleGroupToFertilize) {
            val fragmentManager = parentFragmentManager
            val newFragment = SetFertilizingSettingsFragmentDialog(viewModel)
            newFragment.show(fragmentManager, "dialog")
        }
    }
    //END FERTILIZING PART

    //START HIBERNATE MODE settings
    private var tempCheckedToggleGroupToHibernate = false
    private fun setHibernateToggleGroup() {
        binding.toggleGroupToHibernate.addOnButtonCheckedListener { _, _, isChecked ->
            if (isChecked) {
                tempCheckedToggleGroupToHibernate = true

                binding.groupHibernateDetails.visibility = View.VISIBLE

                binding.tvDateHibernateStartFromVal.text =
                    viewModel.thePlant.value?.long_to_hibernate_from_date?.let {
                        TimeHelper.getFormattedDateString(
                            it
                        )
                    }

                binding.tvDateHibernateFinishVal.text =
                    viewModel.thePlant.value?.long_to_hibernate_till_date?.let {
                        TimeHelper.getFormattedDateString(
                            it
                        )
                    }

                viewModel.setHibernateModeOn()
            } else {
                tempCheckedToggleGroupToHibernate = false

                binding.groupHibernateDetails.visibility = View.GONE

                viewModel.setHibernateModeOff()
            }
        }
    }

    fun startSetHibernateSettingsFragmentDialog() {
        //Проверка на чек при старте, чтобы не было
        if (tempCheckedToggleGroupToHibernate) {
            val fragmentManager = parentFragmentManager
            val newFragment = SetHibernatingSettingsFragmentDialog(viewModel)
            newFragment.show(fragmentManager, "dialog")
        }
    }
    //END HIBERNATE MODE settings

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.plant_detail_menu, menu)

        val deleteItemFromDB = menu.findItem(R.id.delete_item_from_db)

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
                .setItems(items) { _, which ->
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

        val pictureImagePath = storageDir.absolutePath + "/" + imageFileName

        val file = File(pictureImagePath)
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

    private fun createUriDestinationForImageFile(context: Context) {
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
    //FUNCTIONS FOR SELECTION IMAGE OF THE PLANT END

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
        launch(Dispatchers.Main) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.delete_plant_from_db))
                .setMessage(resources.getString(R.string.are_you_sure_to_delete_the_plant_from_db))
                .setNeutralButton(resources.getString(R.string.cancel)) { _, _ ->
                }
                .setPositiveButton(resources.getString(R.string.delete_item)) { _, _ ->
                    launch(Dispatchers.IO) {
                        viewModel.deletePlant()

                        Snackbar.make(
                            requireView(),
                            getString(R.string.deleted),
                            Snackbar.LENGTH_SHORT
                        )
                            .show()

                        findNavController().navigateUp()
                    }
                }.show()
        }
    }

    private fun onClickOptionMenuSavePlant() {
        if (binding.editTextTextPlantName.editText?.text.toString().isEmpty()) {
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
}