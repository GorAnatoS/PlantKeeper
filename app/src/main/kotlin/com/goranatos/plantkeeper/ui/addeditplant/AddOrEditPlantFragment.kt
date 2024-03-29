package com.goranatos.plantkeeper.ui.addeditplant

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.goranatos.plantkeeper.R
import com.goranatos.plantkeeper.data.entity.Plant
import com.goranatos.plantkeeper.databinding.FragmentDetailedPlantBinding
import com.goranatos.plantkeeper.ui.addeditplant.AddOrEditPlantViewModel.Companion.REQUEST_CHOOSE_FROM_GALLERY
import com.goranatos.plantkeeper.ui.addeditplant.AddOrEditPlantViewModel.Companion.REQUEST_IMAGE_CAPTURE
import com.goranatos.plantkeeper.ui.addeditplant.AddOrEditPlantViewModel.Companion.uriCapturedImage
import com.goranatos.plantkeeper.ui.addeditplant.AddOrEditPlantViewModel.Companion.uriDestination
import com.goranatos.plantkeeper.ui.addeditplant.dialogs.SelectPlantImageUriFromCollectionDialogFragment
import com.goranatos.plantkeeper.ui.addeditplant.dialogs.SetFertilizingSettingsFragmentDialog
import com.goranatos.plantkeeper.ui.addeditplant.dialogs.SetHibernatingSettingsFragmentDialog
import com.goranatos.plantkeeper.ui.addeditplant.dialogs.SetWateringSettingsFragmentDialog
import com.goranatos.plantkeeper.ui.base.ScopedFragment
import com.goranatos.plantkeeper.utilities.Helper.Companion.hideKeyboard
import com.goranatos.plantkeeper.utilities.TimeHelper
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

@AndroidEntryPoint
@RuntimePermissions
class AddOrEditPlantFragment : ScopedFragment() {
    private val args: AddOrEditPlantFragmentArgs by navArgs()

    private val viewModel by viewModels<AddOrEditPlantViewModel>()

    private var _binding: FragmentDetailedPlantBinding? = null
    private val binding get() = _binding!!

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
        viewModel.initPlantDetailViewModel()
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailedPlantBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this

        binding.fragment = this

        viewModel.setPlant()

        if (args.plantId == -1) {
            requireActivity().title = getString(R.string.new_plant)
        }

        binding.viewModel = viewModel

        uiSetup()

        hideRelatedToChipsMaterialCards()

        if (viewModel.isToCreateNewPlant) {
            binding.chipHibernatingMode.isChecked =
                viewModel.thePlant.value?.is_hibernate_mode_on == 1
            binding.chipWateringMode.isChecked = viewModel.thePlant.value?.is_water_need_on == 1
            binding.chipFertilizingMode.isChecked =
                viewModel.thePlant.value?.is_fertilize_need_on == 1
        } else {
            activity?.title = getString(R.string.edit_the_plant)
        }

        viewModel.thePlant.observe(viewLifecycleOwner, { plant ->
            setUIforPlant(plant)
        })

    }

    private fun hideRelatedToChipsMaterialCards() {
        binding.materialCardHibernateGroup.visibility = View.GONE
        binding.materialCardWaterGroup.visibility = View.GONE
        binding.materialCardFertilizingGroup.visibility = View.GONE
    }


    private var isFirstStart = true

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

        binding.tvDateHibernateStartFromVal.text =
            getString(R.string.start_with_space_string,
                plant.long_to_hibernate_from_date?.let {
                    TimeHelper.getFormattedDateString(
                        it
                    )
                })

        binding.tvDateHibernateFinishVal.text = getString(R.string.start_with_space_string,
            plant.long_to_hibernate_till_date?.let {
                TimeHelper.getFormattedDateString(
                    it
                )
            }
        )
        //END HIBERNATE

        //START WATER
        if (plant.is_water_need_on == 1) {
            binding.toggleGroupToWater.check(binding.toggleButtonToWater.id)
            viewModel.setWaterNeedModeOn()
        } else {
            binding.toggleGroupToWater.uncheck(binding.toggleButtonToWater.id)
            viewModel.setWaterNeedModeOff()
        }

        if (plant.is_watering_hibernate_mode_on == 1) {
            binding.tvWateringFrequencyInHibernate.visibility = View.VISIBLE
        } else {
            binding.tvWateringFrequencyInHibernate.visibility = View.GONE
        }

        if (plant.long_next_watering_date != null) {
            binding.tvToWaterFromDateVal.text = getString(
                R.string.start_with_space_string,
                TimeHelper.getFormattedDateString(plant.long_next_watering_date!!)
            )
        }

        plant.int_watering_frequency_normal?.let {
            binding.tvWateringFrequency.text =
                getString(R.string.start_with_space_string, it.toString())
        }

        plant.int_watering_frequency_in_hibernate?.let {
            binding.tvWateringFrequencyInHibernate.text = it.toString()
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

        if (plant.is_fertilizing_hibernate_mode_on == 1) {
            binding.tvFertilizingFrequencyInHibernate.visibility = View.VISIBLE
        } else {
            binding.tvFertilizingFrequencyInHibernate.visibility = View.GONE
        }

        if (plant.long_next_fertilizing_date != null) {
            binding.tvToFertilizeFromDateVal.text = getString(
                R.string.start_with_space_string,
                TimeHelper.getFormattedDateString(plant.long_next_fertilizing_date!!)
            )
        }

        plant.int_fertilizing_frequency_normal?.let {
            binding.tvFertilizingFrequency.text =
                getString(R.string.start_with_space_string, it.toString())
        }

        plant.int_fertilizing_frequency_in_hibernate?.let {
            binding.tvFertilizingFrequencyInHibernate.text = it.toString()
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
                    getString(R.string.start_with_space_string,
                        viewModel.thePlant.value?.long_to_hibernate_from_date?.let {
                            TimeHelper.getFormattedDateString(
                                it
                            )
                        })

                binding.tvDateHibernateFinishVal.text = getString(R.string.start_with_space_string,
                    viewModel.thePlant.value?.long_to_hibernate_till_date?.let {
                        TimeHelper.getFormattedDateString(
                            it
                        )
                    }
                )

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

        val mimeTypes = arrayOf("image/jpeg", "image/png")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)

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