package com.goranatos.plantskeeper.ui.plantDetail

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.databinding.FragmentDetailedPlantBinding
import com.goranatos.plantskeeper.ui.base.ScopedFragment
import com.goranatos.plantskeeper.util.Helper.Companion.hideKeyboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import java.util.*

/*
    Добавляет новые и редактирует имеющийся цветок\растение
    при создание -1 -> создание нового цветка, иначе - редактирование номера в БД
 */

class PlantDetailFragment : ScopedFragment(), DIAware {
    override val di by closestDI()

    private val args: PlantDetailFragmentArgs by navArgs()

    private lateinit var viewModel: PlantDetailViewModel

    lateinit var binding: FragmentDetailedPlantBinding

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

            if (plant?.name != null) {
                binding.editTextTextPlantNameInputText.setText(plant.name)
            }

            if (plant?.desc != null) {
                binding.editTextTextPlantDescription.setText(plant.desc.toString())
            }

            Toast.makeText(
                requireContext(),
                viewModel.thePlant.value.toString() + "\n\n",
                Toast.LENGTH_SHORT
            ).show()
        })

        observeChanges()

        setHasOptionsMenu(true)

        return binding.root
    }

    private fun observeChanges(){

        binding.editTextTextPlantNameInputText.addTextChangedListener {
            viewModel.updatePlantName(it.toString())
        }



    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.plant_detail_menu, menu)

        val deleteItemFromDB = menu?.findItem(R.id.delete_item_from_db)

        deleteItemFromDB.setOnMenuItemClickListener {
            deletePlantItemFromDB()
            true
        }


        val savePlantToDB = menu?.findItem(R.id.save_plant_to_db)
        savePlantToDB.setOnMenuItemClickListener {
            onClickOptionMenuSavePlant()
            true
        }

        deleteItemFromDB.isVisible = !viewModel.isToCreateNewPlant

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
                    viewModel.deletePlant()

                    Snackbar.make(requireView(), getString(R.string.deleted), Snackbar.LENGTH_SHORT)
                        .show()

                    findNavController().navigateUp()
                }
            }
            .show()

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
}

// TODO: 12/5/2020 Внешний вид кропа изменить под стиль прилоржения*

// TODO: 12/8/2020 !!!

/*

@RuntimePermissions
class PlantAddAndInfoFragment : ScopedFragment(), DIAware {
    companion object {

        //Camera
        const val REQUEST_IMAGE_CAPTURE = 631

        //selectPicture
        const val REQUEST_CHOOSE_FROM_GALLERY = 632

        lateinit var uriDestination: Uri
        lateinit var uriCapturedImage: Uri

        private var formattedDateLong: Long = 0
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_add_and_change_plant,
                container,
                false
            )

        binding.lifecycleOwner = this

        binding.viewModel = viewModel

      /*  if (viewModel.isCreateNewPlant) {
            bindCreateNewPlant()
        } else {
            bindEditExistingPlant()
        }*/

        uiScope.launch {

            viewModel.thePlant.observe(viewLifecycleOwner, {

            })

            viewModel.obtainThePlant()


        }



        setDatePickerForStartWatering()

        setHibernateSwitch()

        setToggleButtons()

        setImageUriListener()
        setToWaterFromDateListener()




     /*   uiScope.launch {

            viewModel.getPlant(viewModel.plantIdTest.value).observe(viewLifecycleOwner, {
                it?.let { plant ->

                    binding.apply {







                        whenThePlantExistsSetToggleWaterGroup()

                        binding.switchIsHibernateOn.isChecked = plant.is_hibernate_on == 1

                        if (binding.switchIsHibernateOn.isChecked) {
//                            binding.includeHibernateSettings.cardHibernateGroup.visibility = View.VISIBLE
                            binding.groupHibernateData.visibility = View.VISIBLE
                        } else {
//                            binding.includeHibernateSettings.cardHibernateGroup.visibility = View.GONE
                            binding.groupHibernateData.visibility = View.GONE
                        }*//*

                    }


                }
            })
        }*/


        ///


        return binding.root
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {

                REQUEST_CHOOSE_FROM_GALLERY -> {
                    val selectedUri = data!!.data

                    uriDestination = createImageFile().toUri()
                    viewModel.thePlant.value?.image_path = uriDestination.toString()

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
                    viewModel.thePlant.value?.image_path = uriDestination.toString()

                    openCropActivity(uriCapturedImage, uriDestination)
                }

                UCrop.REQUEST_CROP -> {

                    handleCropResult(data)
                }
            }
        }
    }
/*


    private fun setDatePickerForStartWatering() {
        binding.tvToWaterFromDateVal.text = Time.getFormattedDateString()

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


        /*  if (isNewPlant) {

          } else {

          }*/
    }

    private fun setImageUriListener() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(IMAGE_URI)
            ?.observe(viewLifecycleOwner) { uri_string ->
                binding.plantImage.setImageURI(Uri.parse(uri_string))

                viewModel.thePlant.value?.image_path = uri_string
            }
    }

    private fun setToWaterFromDateListener() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(
            TO_WATER_FROM_DATE_STRING
        )
            ?.observe(viewLifecycleOwner) { to_water_from_date_string ->
                binding.tvToWaterFromDateVal.text = to_water_from_date_string
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



    private fun bindCreateNewPlant() {


        whenCreateNewPlantSetWaterToggleGroup()

        //createNewPlantEntity()

        // TODO: 12/8/2020
        //binding.plantImage.setImageURI(Uri.parse(viewModel.thePlant.value?.image_path))

        binding.switchIsHibernateOn.isChecked = false

        binding.groupHibernateData.visibility = View.GONE
    }

    private fun bindEditExistingPlant() = launch(Dispatchers.IO) {

        withContext(Dispatchers.Main) {

/*            viewModel.getPlant(plant_id).observe(viewLifecycleOwner, {
                it?.let { plant ->
                    viewModel.thePlant = plant

                    binding.apply {

                        //binding.editTextTextPlantName.editText?.setText(viewModel?.thePlant?.name.toString())
                        binding.editTextTextPlantDescription.setText(viewModel?.thePlant?.desc.toString())

                        if (!plant.image_path.isNullOrEmpty()) {
                            binding.plantImage.setImageURI(Uri.parse(plant.image_path))
                            viewModel?.thePlant?.image_path = plant.image_path
                        }

                        whenThePlantExistsSetToggleWaterGroup()

                        binding.switchIsHibernateOn.isChecked = plant.is_hibernate_on == 1

                        if (binding.switchIsHibernateOn.isChecked) {
//                            binding.includeHibernateSettings.cardHibernateGroup.visibility = View.VISIBLE
                            binding.groupHibernateData.visibility = View.VISIBLE
                        } else {
//                            binding.includeHibernateSettings.cardHibernateGroup.visibility = View.GONE
                            binding.groupHibernateData.visibility = View.GONE
                        }

                    }
                }
            })*/

            binding.groupContent.visibility = View.VISIBLE
            binding.groupLoading.visibility = View.GONE
        }
    }


    private fun setHibernateSwitch() {
        binding.switchIsHibernateOn.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.groupHibernateData.visibility = View.VISIBLE
            } else {
                binding.groupHibernateData.visibility = View.GONE
            }
        }

        binding.tvDateHibernateStartFromVal.setOnClickListener {
            val builder = MaterialDatePicker.Builder.datePicker()

            builder.setTitleText("Режим покоя начинается с")
            val materialDatePicker = builder.build()

            materialDatePicker.addOnPositiveButtonClickListener {
                binding.tvDateHibernateStartFromVal.text = Time.getFormattedDateString(it)
            }

            materialDatePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        binding.tvDateHibernateFinishVal.setOnClickListener {
            val builder = MaterialDatePicker.Builder.datePicker()

            builder.setTitleText("Режим покоя заканчивается")
            val materialDatePicker = builder.build()

            materialDatePicker.addOnPositiveButtonClickListener {
                binding.tvDateHibernateFinishVal.text = Time.getFormattedDateString(it)
            }

            materialDatePicker.show(parentFragmentManager, "DATE_PICKER")
        }


    }


//START WaterToggleGroup

    private fun whenThePlantExistsSetToggleWaterGroup() {
        if (viewModel.thePlant.value?.water_need.isNullOrEmpty()) {
            binding.toggleGroupToWater.uncheck(binding.toggleButtonToWater.id)
        } else {
            binding.toggleGroupToWater.check(binding.toggleButtonToWater.id)
        }

        setToggleGroupToWaterAddOnButtonCheckedListener()
    }

    private fun whenCreateNewPlantSetWaterToggleGroup() {
        binding.toggleGroupToWater.uncheck(binding.toggleButtonToWater.id)

        setToggleGroupToWaterAddOnButtonCheckedListener()
    }

    private fun setToggleGroupToWaterAddOnButtonCheckedListener() {
        binding.toggleGroupToWater.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            // Respond to button selection
            if (isChecked) {
                val fragmentManager = getParentFragmentManager()
                val newFragment = SetWateringSettingsFragmentDialog()
                newFragment.show(fragmentManager, "dialog")

                binding.tvToWaterFromDateVal.visibility = View.VISIBLE
            } else {
                binding.tvToWaterFromDateVal.visibility = View.GONE
                binding.tvToWaterFromDateVal.text = null
            }
        }
    }

//END WaterToggleGroup





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
            viewModel.thePlant.value?.image_path = absolutePath
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
 */