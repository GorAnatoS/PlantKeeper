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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.databinding.FragmentDetailedPlantBinding
import com.goranatos.plantskeeper.internal.Time
import com.goranatos.plantskeeper.ui.base.ScopedFragment
import com.goranatos.plantskeeper.ui.plantDetail.PlantDetailViewModel.Companion.REQUEST_CHOOSE_FROM_GALLERY
import com.goranatos.plantskeeper.ui.plantDetail.PlantDetailViewModel.Companion.REQUEST_IMAGE_CAPTURE
import com.goranatos.plantskeeper.ui.plantDetail.PlantDetailViewModel.Companion.formattedDateLong
import com.goranatos.plantskeeper.ui.plantDetail.PlantDetailViewModel.Companion.uriCapturedImage
import com.goranatos.plantskeeper.ui.plantDetail.PlantDetailViewModel.Companion.uriDestination
import com.goranatos.plantskeeper.ui.plantDetail.dialogs.IMAGE_URI
import com.goranatos.plantskeeper.ui.plantDetail.dialogs.SetWateringSettingsFragmentDialog
import com.goranatos.plantskeeper.ui.plantDetail.dialogs.TO_WATER_FROM_DATE_STRING
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


    //special var for firstUse check
    var isToShowFirstSetWaterSettings  = false

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

            binding.groupContent.visibility = View.VISIBLE
            binding.groupLoading.visibility = View.GONE

            if (plant?.image_path != null) {
                binding.plantImage.setImageURI(Uri.parse(plant.image_path))
            }

            if (plant?.desc != null) {
                binding.editTextTextPlantDescription.setText(plant.desc.toString())
            }

            if (plant?.water_need.isNullOrEmpty())
                binding.toggleGroupToWater.uncheck(binding.toggleButtonToWater.id)
            else {
                binding.toggleGroupToWater.check(binding.toggleButtonToWater.id)
                binding.tvToWaterFromDateVal.text = plant.water_need
            }



            if (plant.is_hibernate_on != 0){
                binding.groupHibernateData.visibility = View.VISIBLE
                isToShowFirstSetWaterSettings = true
            } else {
                binding.groupHibernateData.visibility = View.GONE
            }


            Toast.makeText(
                requireContext(),
                viewModel.thePlant.value.toString() + "\n\n",
                Toast.LENGTH_SHORT
            ).show()
        })

        uiSetup()


        return binding.root
    }

    private fun uiSetup() {
        setOnPlantNameEditTextChangedListener()

        setToggleGroupSelectImageForThePlant()

        setImageUriListener()

        setDatePickerForStartWatering()

        setToggleGroupWatering()

        setHibernateSwitch()

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

    private fun setToggleGroupSelectImageForThePlant() {
        binding.toggleSelectImage.setOnClickListener {
            viewModel.toggleSelectImageClicked(parentFragmentManager)
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


    //FUNCTIONS FOR SELECTIONG IMAGE OF THE PLANT START
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
            viewModel.thePlant.value?.image_path = absolutePath
        }
    }

    fun createUriDestinationForImageFile(context: Context) {
        uriDestination = createImageFile(context).toUri()
        viewModel.thePlant.value?.image_path = uriDestination.toString()
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
            showWrongInput()
            Snackbar.make(
                requireView(),
                getString(R.string.give_a_name_to_a_plant),
                Snackbar.LENGTH_SHORT
            ).show()

            return
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

    /**
     * Ожидаем, когда изменится uri изображения
     */
    private fun setImageUriListener() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(IMAGE_URI)
            ?.observe(viewLifecycleOwner) { uri_string ->
                binding.plantImage.setImageURI(Uri.parse(uri_string))

                viewModel.thePlant.value?.image_path = uri_string
            }
    }

    private fun setOnPlantNameEditTextChangedListener() {
        binding.editTextTextPlantNameInputText.addTextChangedListener {
            viewModel.updatePlantName(it.toString())
        }
    }

    var tempCheckedtoggleGroupToWater = false
    //START WaterToggleGroup
    private fun setToggleGroupWatering() {


        binding.toggleGroupToWater.addOnButtonCheckedListener { _, _, isChecked ->
            // Respond to button selection
            if (isChecked) {
                tempCheckedtoggleGroupToWater = true
            } else {
                //binding.tvToWaterFromDateVal.text = null
                viewModel.thePlant.value?.water_need = null
                binding.tvToWaterFromDateVal.visibility = View.GONE
                tempCheckedtoggleGroupToWater = false
            }
        }

        binding.toggleButtonToWater.setOnClickListener {
            if (tempCheckedtoggleGroupToWater) {

                val fragmentManager = getParentFragmentManager()
                val newFragment = SetWateringSettingsFragmentDialog()
                newFragment.show(fragmentManager, "dialog")

                binding.tvToWaterFromDateVal.visibility = View.VISIBLE
            }
        }

        // Ожидаем, когда из диплога выберем следующую дату полива
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(TO_WATER_FROM_DATE_STRING)
            ?.observe(viewLifecycleOwner) { to_water_from_date_string ->



                if (to_water_from_date_string == "uncheck") {
                    binding.toggleGroupToWater.uncheck(binding.toggleButtonToWater.id)
                    return@observe
                }

                viewModel.thePlant.value?.water_need = to_water_from_date_string
            }
    }

    
    private fun setDatePickerForStartWatering() {
        //binding.tvToWaterFromDateVal.text = Time.getFormattedDateString()

        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Поливать с")
        val materialDatePicker = builder.build()

        materialDatePicker.addOnPositiveButtonClickListener {
            binding.tvToWaterFromDateVal.text = Time.getFormattedDateString(it)
            formattedDateLong = it
        }

        binding.tvToWaterFromDateVal.setOnClickListener {
            materialDatePicker.show(parentFragmentManager, "DATE_PICKER")
        }
    }


    //END WaterToggleGroup


    //START HIBERNATE MODE settings
    private fun setHibernateSwitch() {
        binding.switchIsHibernateOn.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                viewModel.thePlant.value?.is_hibernate_on = 1
                binding.groupHibernateData.visibility = View.VISIBLE
            } else {
                viewModel.thePlant.value?.is_hibernate_on = 0
                binding.groupHibernateData.visibility = View.GONE
            }
        }

        binding.tvDateHibernateStartFromVal.setOnClickListener {
            viewModel.startDatePicker(
                "Режим покоя начинается с",
                parentFragmentManager,
                binding.tvDateHibernateStartFromVal
            )
        }

        binding.tvDateHibernateFinishVal.setOnClickListener {
            viewModel.startDatePicker(
                "Режим покоя заканчивается",
                parentFragmentManager,
                binding.tvDateHibernateFinishVal
            )
        }
    }
    //END HIBERNATE MODE settings
}