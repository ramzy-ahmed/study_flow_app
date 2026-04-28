package com.studyflow.ui.decks_screen.decks_screen

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.studyflow.R
import com.studyflow.databinding.FragmentDecksBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue
import kotlin.math.abs


@AndroidEntryPoint
class DecksFragment : Fragment() {
    private var _binding: FragmentDecksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DecksViewModel by viewModels()
    private val adapter by lazy {
        DeckAdapter(
            onDeleteClick = { deck -> viewModel.deleteDeck(deck) },
            onEditClick = { deck -> },
            onItemClick = { deck -> navigateToDeckDetails(deck.id) }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDecksBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupListeners()
        setupSearch()
        observeState()
    }

    private fun setupRecyclerViews() {
        binding.rvDecks.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@DecksFragment.adapter
            //setHasFixedSize(true)
        }
    }
    private fun setupListeners(){
        binding.btnAddDeck.setOnClickListener { navigateToAddDeck() }

        binding.appBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.searchBtn -> {
                    openSearchMode()
                    true
                }
                R.id.addDeck -> {
                    navigateToAddDeck()
                    true
                }
                else -> false
            }
        }

    }
    private fun setupSearch() {
        binding.searchBar.setOnClickListener { openSearchMode() }

        binding.searchBar.doOnTextChanged { text, _, _, _ ->
            viewModel.onSearchQueryChanged(text?.toString() ?: "")
        }


        binding.searchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                closeSearchMode()
                true
            } else false
        }
        /*        binding.searchBar.setOnKeyListener { _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN &&
                        keyCode == KeyEvent.KEYCODE_BACK) {
                        closeSearchMode()
                        true
                    } else false
                }*/

        // AppBar scroll behavior: hide keyboard when collapsed
        binding.appBarLayout.addOnOffsetChangedListener { appBar, verticalOffset ->
            val scrollRange = appBar.totalScrollRange
            if (scrollRange == 0) return@addOnOffsetChangedListener

            val percentage = abs(verticalOffset).toFloat() / scrollRange
            val isCollapsed = percentage > 0.9f

            if (isCollapsed && binding.searchBar.hasFocus()){
                binding.searchBar.clearFocus()
                hideKeyboard()
            }

            binding.appBar.menu.findItem(R.id.searchBtn)?.isVisible = isCollapsed
        }
    }
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleUiState(state)
                }
            }
        }
    }
    private fun handleUiState(state: DecksState) {
        binding.loadingBar.isVisible = state is DecksState.Loading
        binding.emptyStateLayout.isVisible = state is DecksState.Empty || state is DecksState.NoResultsFound
        binding.rvDecks.isVisible = state is DecksState.Success

        when (state) {
            is DecksState.Success -> {
                adapter.submitList(state.decks)
            }
            is DecksState.Empty -> {
                updateEmptyState(isSearch = false)
            }
            is DecksState.NoResultsFound -> {
                updateEmptyState(isSearch = true)
            }
            is DecksState.Error -> {
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

    private fun updateEmptyState(isSearch: Boolean) {
        binding.apply {
            if (isSearch) {
                tvState.text = getString(R.string.no_result_found, searchBar.text.toString())
                tvSubtitleState.isVisible = false
                animState.setAnimation(R.raw.not_found_anim)
                btnAddDeck.isVisible = false
            } else {
                tvState.text = getString(R.string.library_empty)
                tvSubtitleState.isVisible = true
                animState.setAnimation(R.raw.empty_anim)
                btnAddDeck.isVisible = true
            }
            animState.playAnimation()
        }
    }
    private fun openSearchMode() {
        binding.searchBar.requestFocus()
    }
    private fun closeSearchMode() {
        viewModel.clearSearch()
        binding.searchBar.clearFocus()
        binding.searchBar.text?.clear()
        hideKeyboard()
    }
    private fun navigateToDeckDetails(id: Int) {
        val action = DecksFragmentDirections.actionDecksToDeckDetails(id)
        findNavController().navigate(action)
    }
    private fun navigateToAddDeck() {
        val action = DecksFragmentDirections.actionDecksToAddDeck()
        findNavController().navigate(action)
    }
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}