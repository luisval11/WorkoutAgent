package com.tfg.workoutagent.presentation.ui.routines.trainer.fragments

import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.tfg.workoutagent.R
import com.tfg.workoutagent.data.repositoriesImpl.ExerciseRepositoryImpl
import com.tfg.workoutagent.data.repositoriesImpl.RoutineRepositoryImpl
import com.tfg.workoutagent.databinding.FragmentAddActivityBinding
import com.tfg.workoutagent.domain.routineUseCases.ManageRoutineUseCaseImpl
import com.tfg.workoutagent.models.ActivitySet
import com.tfg.workoutagent.presentation.ui.routines.trainer.adapters.CategoryListAdapter
import com.tfg.workoutagent.presentation.ui.routines.trainer.adapters.ExerciseListAdapter
import com.tfg.workoutagent.presentation.ui.routines.trainer.adapters.SetListAdapter
import com.tfg.workoutagent.presentation.ui.routines.trainer.viewModels.CreateRoutineViewModel
import com.tfg.workoutagent.presentation.ui.routines.trainer.viewModels.CreateRoutineViewModelFactory
import com.tfg.workoutagent.vo.getAllCategories
import kotlinx.android.synthetic.main.fragment_add_activity.*

class AddActivityFragment : DialogFragment() {

    private lateinit var toolbar: Toolbar

    private lateinit var binding: FragmentAddActivityBinding

    private val viewModel by lazy {
        ViewModelProvider(
            requireActivity(), CreateRoutineViewModelFactory(
                ManageRoutineUseCaseImpl(RoutineRepositoryImpl(), ExerciseRepositoryImpl())
            )
        ).get(CreateRoutineViewModel::class.java)
    }

    private lateinit var setListAdapter: SetListAdapter

    private lateinit var categoryListAdapter: CategoryListAdapter

    private lateinit var exerciseListAdapter: ExerciseListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_add_activity,
            container,
            false
        )
        this.binding.viewModel = viewModel
        this.binding.lifecycleOwner = this
        toolbar = binding.root.findViewById(R.id.add_activity_toolbar)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        setupDialogWindow()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val darkMode = when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> false
            else -> true
        }
        if (darkMode) {
            // nsv_add_activity.setBackgroundResource(R.drawable.item_no_border_dark)
            // rl_add_activity_plus.setBackgroundColor(context!!.resources.getColor(R.color.black_darkMode))
            // activity_note_input.setBackgroundColor(context!!.resources.getColor(R.color.black_darkMode))
            // activity_note_input_edit.setBackgroundColor(context!!.resources.getColor(R.color.black_darkMode))
            repetitions_add_activity_input.setTextColor(Color.WHITE)
            weights_add_activity_input.setTextColor(Color.WHITE)
            activity_note_input_edit.setTextColor(Color.WHITE)
        }
        setupSetListRecycler()
        setupCategoryListRecycler(darkMode)
        setupExerciseListRecycler()
        setupToolbar()
        // setupSpinnerAdapter()
        observeErrors()
        observeDialogState()
        setupButtons()
    }

    private fun setupButtons() {
        add_set_button.setOnClickListener {
            val repetitions = repetitions_add_activity_input.text.toString().toIntOrNull()
            val weight = weights_add_activity_input.text.toString().toDoubleOrNull()

            val validSet = this.viewModel.addSet(repetitions, weight)
            if (validSet == "") {
                add_activity_set_error_message.text = ""
                add_activity_set_error_message.visibility = View.GONE
                this.setListAdapter.addElement(ActivitySet(repetitions!!, weight!!))
                repetitions_add_activity_input.text.clear()
                weights_add_activity_input.text.clear()
                repetitions_add_activity_input.requestFocus()
            } else {
                add_activity_set_error_message.text = validSet
                add_activity_set_error_message.visibility = View.VISIBLE
            }
        }
    }

    private fun observeDialogState() {
        viewModel.closeActivityDialog.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    dismiss()
                    viewModel.activityDialogClosed()
                }
            }
        })
    }

    private fun observeErrors() {
        viewModel.setsError.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it != "") {
                    binding.addActivitySetErrorMessage.text = it
                    binding.addActivitySetErrorMessage.visibility = View.VISIBLE
                } else {
                    binding.addActivitySetErrorMessage.text = ""
                    binding.addActivitySetErrorMessage.visibility = View.GONE
                }
            } ?: run {
                binding.addActivitySetErrorMessage.text = ""
                binding.addActivitySetErrorMessage.visibility = View.GONE
            }
        })

        /*viewModel.repetitionsError.observe(viewLifecycleOwner, Observer {
            binding.activityRepetitionsInputEdit.error =
                if (it != "") it else null
        })

        viewModel.weightsError.observe(viewLifecycleOwner, Observer {
            binding.activityWeightsInputEdit.error =
                if (it != "") it else null
        })*/

        viewModel.noteError.observe(viewLifecycleOwner, Observer {
            binding.activityNoteInputEdit.error =
                if (it != "") it else null
        })

        viewModel.exerciseError.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it != "") {
                    binding.addExerciseSetErrorMessage.text = it
                    binding.addExerciseSetErrorMessage.visibility = View.VISIBLE
                } else {
                    binding.addExerciseSetErrorMessage.text = ""
                    binding.addExerciseSetErrorMessage.visibility = View.GONE
                }
            } ?: run {
                binding.addExerciseSetErrorMessage.text = ""
                binding.addExerciseSetErrorMessage.visibility = View.GONE
            }
        })
    }

    private fun setupDialogWindow() {
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
            dialog.window?.setWindowAnimations(R.style.AppTheme_Slide)
        }
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            this.viewModel.clearActivityData()
            dismiss()
        }
        toolbar.title = "Add activity"
        toolbar.inflateMenu(R.menu.add_day_activity_dialog_menu)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_save_day_activity -> {
                    this.viewModel.onSaveActivity()
                }
                else -> {
                    dismiss()
                    this.viewModel.resetSelectedCategories()
                }
            }
            true
        }
    }

    /*private fun setupSpinnerAdapter() {
        viewModel.exercises.observe(viewLifecycleOwner, Observer {
            it?.let {
                val spinner: Spinner = activity_exercise_spinner
                ArrayAdapter(
                    this.requireContext(),
                    android.R.layout.simple_spinner_item,
                    it.map { e -> e.title }
                ).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View,
                            position: Int,
                            id: Long
                        ) {
                            viewModel.selectExercise(it[position])
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {
                            // Code to perform some action when nothing is selected
                        }
                    }
                }
            }
        })
        viewModel.getExercises()
    }*/

    private fun setupSetListRecycler() {
        this.setListAdapter = SetListAdapter(
            requireContext(),
            { position: Int -> this.viewModel.removeSet(position) },
            updateListener@{ repetitions, weight, position ->
                val validSet = this.viewModel.checkSet(repetitions, weight)
                if (validSet) {
                    this.viewModel.updateSet(ActivitySet(repetitions!!, weight!!, true), position)
                    return@updateListener ""
                }
                this.viewModel.updateSet(false, position)
                return@updateListener "Invalid value"
            })
        recyclerView_activity_sets.layoutManager = LinearLayoutManager(this.requireContext())
        recyclerView_activity_sets.adapter = setListAdapter
    }

    private fun setupCategoryListRecycler(darkMode: Boolean) {
        this.categoryListAdapter =
            CategoryListAdapter(this.requireContext()) { categoryName, categoryPosition, view ->
                val currentCategories = this.viewModel.getSelectedCategories()
                if (currentCategories.map { c -> c.name }.contains(categoryName)) {
                    this.viewModel.removeSelectedCategory(categoryName, categoryPosition)
                    if (darkMode) {
                        view.setBackgroundResource(R.drawable.item_border_unselected)
                    } else {
                        view.setBackgroundResource(R.drawable.item_border_unselected)
                    }
                } else {
                    this.viewModel.addSelectedCategory(categoryName, categoryPosition)
                    view.setBackgroundResource(R.drawable.item_border_selected)
                }

                val exercises = this.viewModel.updateAvailableExercises()
                if (!exercises.contains(this.viewModel.getNewSelectedExercise())) {
                    this.viewModel.updateNewSelectedExercise(null)
                    this.exerciseListAdapter.setSelectedExercise(null)
                }
                this.exerciseListAdapter.setListData(exercises)
            }

        recyclerView_activity_categories.layoutManager =
            LinearLayoutManager(this.requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView_activity_categories.adapter = this.categoryListAdapter
        this.categoryListAdapter.setListData(getAllCategories())
    }

    private fun setupExerciseListRecycler() {
        this.exerciseListAdapter =
            ExerciseListAdapter(this.requireContext()) updateExercise@{ exercise, _ ->
                val selectedExercise = this.viewModel.getNewSelectedExercise()
                if (selectedExercise == null) {
                    this.viewModel.updateNewSelectedExercise(exercise)
                    this.exerciseListAdapter.setSelectedExercise(exercise)
                } else {
                    if (selectedExercise.id == exercise.id) {
                        this.viewModel.updateNewSelectedExercise(null)
                        this.exerciseListAdapter.setSelectedExercise(null)
                    } else {
                        this.viewModel.updateNewSelectedExercise(exercise)
                        this.exerciseListAdapter.setSelectedExercise(exercise)
                    }
                }
            }
        recyclerView_activity_exercises.layoutManager =
            LinearLayoutManager(this.requireContext())
        recyclerView_activity_exercises.adapter = this.exerciseListAdapter

        viewModel.exercises.observe(viewLifecycleOwner, Observer {
            it?.let {
                val exercises = this.viewModel.updateAvailableExercises()
                this.exerciseListAdapter.setListData(exercises)
            }
        })

        this.viewModel.getExercises()
    }
}
