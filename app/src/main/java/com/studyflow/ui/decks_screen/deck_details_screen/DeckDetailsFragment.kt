package com.studyflow.ui.decks_screen.deck_details_screen

import android.annotation.SuppressLint
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.studyflow.R
import com.studyflow.data.local.entity.CardEntity
import com.studyflow.data.local.entity.DeckEntity
import com.studyflow.data.local.entity.DeckWithCards
import com.studyflow.databinding.FragmentDeckDetailsBinding
import com.studyflow.databinding.LayoutDeleteBottomSheetBinding
import com.studyflow.databinding.LayoutEditDeckBottomSheetBinding
import com.studyflow.ui.study_screen.StudyScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue
import kotlin.math.abs

@AndroidEntryPoint
class DeckDetailsFragment : Fragment() {
    val viewModel: DeckDetailsViewModel by viewModels()
    private var _binding: FragmentDeckDetailsBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<DeckDetailsFragmentArgs>()
    private val adapter by lazy {
        CardAdapter(
            onEditClick = { card ->
                editCard(card)
            },
            onDeleteClick = { card ->
                displayDeleteBottomSheet(
                    title = "Delete Card?",
                    message = "Are you sure you want to delete this card?"
                ) {
                    viewModel.deleteCard(card)
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeckDetailsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        adapter.submitList(null)
        adapter.notifyDataSetChanged()
        adapter.currentList.clear()
        adapter.notifyItemRangeRemoved(0, adapter.itemCount)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerViews()
        observeViewModel()
        setupClickListeners()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect deck details
                launch {
                    viewModel.deckWithCards.collectLatest { data ->
                        data?.let { updateUI(it) }
                    }
                }
                // Collect mastery
                launch {
                    viewModel.masteryPercentage.collectLatest { percentage ->
                        binding.tvMasteryPercent.text = getString(R.string.percentage_format, percentage)
                    }
                }
                // Collect deletion event
                launch {
                    viewModel.deckDeleted.collectLatest {
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    private fun updateUI(it: DeckWithCards) {
        val isEmpty = it.cards.isEmpty()
        val isDescriptionEmpty = it.deck.description.isNotEmpty()
        adapter.submitList(it.cards)
        binding.apply {
            collapsingToolbar.title = it.deck.title
            tvDescription.text = it.deck.description
            tvDescription.isVisible = isDescriptionEmpty
            titleDescription.isVisible = isDescriptionEmpty
            tvCategory.text = it.deck.category
            tvTotalCardsCount.text = it.cards.size.toString()
            tvPreviewCount.text = it.cards.size.toString()
            val iconRes = if (it.deck.image != null && it.deck.image != 0) it.deck.image else R.drawable.ic_decks
            Glide.with(requireContext())
                .load(iconRes)
                .into(deckImage)
            emptyStateLayout.isVisible = isEmpty
            rvCards.isVisible = !isEmpty
            container.isVisible = !isEmpty
            btnSession.isVisible = !isEmpty
            stat.isVisible = !isEmpty
            btnAddCard.isVisible = !isEmpty
        }
    }

    private fun setupClickListeners() {
        val deckId = args.deckId
        val action = DeckDetailsFragmentDirections.actionDeckDetailsToAddCard(deckId)
        binding.btnAddCard.setOnClickListener {
            findNavController().navigate(action)
        }
        binding.btnAddFirstCard.setOnClickListener {
            findNavController().navigate(action)
        }
        binding.btnReview.setOnClickListener {
            action(StudyScreen.StudyMode.REVIEW, deckId)
        }
        binding.btnStartQuiz.setOnClickListener {
            action(StudyScreen.StudyMode.QUIZ, deckId)
        }
    }

    private fun action(mode: StudyScreen.StudyMode,id:Int) {
        val action = DeckDetailsFragmentDirections.actionDeckDetailsToStudyFragment(
            mode = mode,
            selectedDeck = id
        )
        findNavController().navigate(action)
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnEdit.setOnClickListener {
            editDeck()
        }
        binding.btnMore.setOnClickListener {
            showPopupMenu()
        }
        binding.toolbarBtnMenu.setOnClickListener {
            showPopupMenu()
        }
        binding.toolbarBtnEdit.setOnClickListener {
            editDeck()
        }
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            if (totalScrollRange == 0) return@addOnOffsetChangedListener
            val percentage = abs(verticalOffset).toFloat() / totalScrollRange.toFloat()
            val isCollapsed = percentage >= 0.9f

            binding.toolbar.isVisible = isCollapsed
            binding.nestedScrollView.isSmoothScrollingEnabled = true
            binding.deckImage.alpha = 1f - percentage
            binding.tvCategory.alpha = 1f - percentage
        }
    }

    private fun editDeck(){
        viewModel.deckWithCards.value?.deck?.let { deck ->
            displayEditSheet(
                title = "Edit Deck Details",
                initialName = deck.title,
                initialDesc = deck.description,
                nameHint = "Title",
                descHint = "Description",
                onSave = { name, desc ->
                    viewModel.updateDeck(deck.copy(title = name, description = desc))
                },
                onDelete = {
                    displayDeleteBottomSheet(
                        title = "Delete Deck?",
                        message = "Are you sure you want to delete this deck? This action cannot be undone.",
                        deleteAction = {
                            viewModel.deleteDeck(deck)
                        }
                    )
                }
            )
        }
    }
    private fun editCard(card: CardEntity){
        displayEditSheet(
            title = "Edit Card Details",
            initialName = card.question,
            initialDesc = card.answer,
            nameHint = "Question",
            descHint = "Answer",
            onSave = { question, answer ->
                viewModel.updateCard(card.copy(question = question, answer = answer))
            },
            onDelete = {
                displayDeleteBottomSheet(
                    title = "Delete Card?",
                    message = "Are you sure you want to delete this card? This action cannot be undone.",
                    deleteAction = {
                        viewModel.deleteCard(card)
                    }
                )
            }

        )
    }
    private fun setupRecyclerViews() {
        binding.rvCards.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = this@DeckDetailsFragment.adapter
        }
    }

    private fun displayEditSheet(
        title: String,
        initialName: String,
        initialDesc: String,
        nameHint: String,
        descHint: String,
        onSave: (String, String) -> Unit,
        onDelete: () -> Unit
    ) {
        val bottomSheet = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetTheme)
        val sheetBinding = LayoutEditDeckBottomSheetBinding.inflate(layoutInflater)
        bottomSheet.setContentView(sheetBinding.root)

        sheetBinding.apply {
            tvEditDeckDetails.text = title
            tilEditDeckName.hint = nameHint
            tilEditDescription.hint = descHint
            etEditDeckName.setText(initialName)
            etEditDescription.setText(initialDesc)

            btnSaveChanges.setOnClickListener {
                val name = etEditDeckName.text.toString().trim()
                val desc = etEditDescription.text.toString().trim()
                if (name.isNotEmpty()) {
                    onSave(name, desc)
                    bottomSheet.dismiss()
                } else {
                    etEditDeckName.error = "$nameHint cannot be empty"
                }
            }
            btnDeleteDeck.setOnClickListener {
                onDelete()
                bottomSheet.dismiss()
            }
        }
        bottomSheet.show()
    }
    private fun displayDeleteBottomSheet(title: String, message: String, deleteAction: () -> Unit) {
        val bottomSheet = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetTheme)
        val sheetBinding = LayoutDeleteBottomSheetBinding.inflate(layoutInflater)
        bottomSheet.setContentView(sheetBinding.root)
        sheetBinding.apply {
            tvTitle.text = title
            tvMessage.text = message
        }
        sheetBinding.btnCancel.setOnClickListener {
            bottomSheet.dismiss()
        }
        sheetBinding.btnDelete.setOnClickListener {
            deleteAction()
            bottomSheet.dismiss()
        }
        bottomSheet.show()
    }

    private fun showPopupMenu() {
        lateinit var deck : DeckEntity
        viewModel.deckWithCards.value?.deck?.let { it ->
            deck = it
        }
        val popupContext = ContextThemeWrapper(requireContext(), R.style.CustomPopupMenu)
        val popupMenu = PopupMenu(popupContext, binding.btnMore)
        popupMenu.inflate(R.menu.context_menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    editDeck()
                    true
                }
                R.id.action_delete -> {
                    viewModel.deleteDeck(deck)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
}