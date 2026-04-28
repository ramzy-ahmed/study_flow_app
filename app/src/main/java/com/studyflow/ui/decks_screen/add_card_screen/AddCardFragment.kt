package com.studyflow.ui.decks_screen.add_card_screen

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.studyflow.R
import com.studyflow.databinding.FragmentAddCardBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddCardFragment : Fragment() {
    private val viewModel : AddCardViewModel by viewModels()
    private lateinit var binding: FragmentAddCardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddCardBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTextField()
        setupButtons()
        observeState()
    }

    fun setupTextField(){
        binding.etFront.doAfterTextChanged { text ->
            viewModel.onQuestionChanged(text.toString())
        }
        binding.etBack.doAfterTextChanged { text ->
            viewModel.onAnswerChanged(text.toString())
        }
    }

    fun observeState(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.uiState.collect { state ->
                    when(state){
                        is AddCardState.Error ->{
                            Toast.makeText(requireContext(),state.message, Toast.LENGTH_SHORT).show()
                        }
                        is AddCardState.Success ->{
                            Toast.makeText(requireContext(),"Card Added", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    fun setupButtons(){
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSave.setOnClickListener {
            viewModel.saveCard()
        }
    }
}