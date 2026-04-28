package com.studyflow.ui.decks_screen.add_deck_screen

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.studyflow.R
import com.studyflow.databinding.FragmentAddDeckBinding
import com.studyflow.databinding.LayoutSelectImageBottomSheetBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.collections.get

@AndroidEntryPoint
class AddDeckFragment : Fragment() {
    private lateinit var binding: FragmentAddDeckBinding
    private val viewModel: AddDeckViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddDeckBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryDropdown()

        binding.etDeckName.doAfterTextChanged {
            viewModel.onTitleChanged(it.toString())
        }
        binding.etDescription.doAfterTextChanged {
            viewModel.onDescriptionChanged(it.toString())
        }

        binding.btnSave.setOnClickListener { viewModel.saveDeck() }
        binding.btnChangeImage.setOnClickListener { showSelectImageBottomSheet() }
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        // life cycle
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AddDeckState.Success -> {
                            Toast.makeText(requireContext(), "Deck Added", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }

                        is AddDeckState.Error -> {
                            //binding.tilDeckName.error = state.message
                        }

                        else -> Unit
                    }
                }
            }
        }

    }

    private fun setupCategoryDropdown() {
        val categories = viewModel.getCategories()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)

        // Set default selection in UI
        binding.actvCategory.setText(categories[0], false)
        viewModel.onCategoryChanged(categories[0])

        binding.actvCategory.setOnItemClickListener { parent, _, position, _ ->
            val selectedCategory = parent.getItemAtPosition(position) as String
            viewModel.onCategoryChanged(selectedCategory)
        }
    }

    fun showSelectImageBottomSheet() {
        val sheetBinding = LayoutSelectImageBottomSheetBinding.inflate(layoutInflater)
        val bottomSheet = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetTheme)
        bottomSheet.setContentView(sheetBinding.root)
        val deckImages = viewModel.getImages()

        sheetBinding.apply {
            rvIcons.layoutManager = GridLayoutManager(requireContext(), 2)
            rvIcons.adapter = ImageAdapter(deckImages) { iconRes ->
                viewModel.onImageChanged(iconRes)
                binding.ivSelectedIcon.setImageResource(iconRes)
                bottomSheet.dismiss()
            }
        }
        bottomSheet.show()
    }
}