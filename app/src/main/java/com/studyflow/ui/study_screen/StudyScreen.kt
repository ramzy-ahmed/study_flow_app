package com.studyflow.ui.study_screen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.studyflow.R
import com.studyflow.data.local.entity.CardEntity
import com.studyflow.databinding.FragmentStudyBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class StudyScreen : Fragment() {
    private lateinit var binding: FragmentStudyBinding
    private val viewModel: StudyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStudyBinding.inflate(layoutInflater, container, false)
        setupCameraDistance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUIByMode()
        observeViewModel()
        setupClickListeners()

        // استماع لإشارة الإعادة من شاشة النتائج
        parentFragmentManager.setFragmentResultListener("study_request", viewLifecycleOwner) { _, bundle ->
            if (bundle.getString("action") == "retry") {
                viewModel.resetSession() // استدعاء دالة التصفير
            }
        }
    }

    private fun setupUIByMode() {
        // نستخدم الوضع القادم من الـ ViewModel
        val isQuiz = viewModel.mode == StudyMode.QUIZ
        binding.quizNavigation.isVisible = isQuiz
        binding.reviewNavigation.isVisible = !isQuiz
        binding.cardFront.layoutFlipHint.isVisible = !isQuiz
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.cards.collectLatest { cards ->
                if (cards.isNotEmpty()) {
                    updateUI(cards, viewModel.currentIndex.value)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentIndex.collectLatest { index ->
                if (index == 0){
                    binding.btnPrevCard.isEnabled = false
                    binding.btnPrevCard.alpha = 0.5f
                }else{
                    binding.btnPrevCard.isEnabled = true
                    binding.btnPrevCard.alpha = 1.0f
                }
                updateUI(viewModel.cards.value, index)
            }
        }
        // مراقبة حالة قلب الكارت
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.showBack.collectLatest { showBack ->
                if (showBack) flipCard(true) else flipCard(false)
            }
        }
        // مراقبة نهاية الجلسة والانتقال لشاشة النتائج
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isFinished.collect {
                val action = StudyScreenDirections.actionStudyFragmentToResultsScreen(
                    correctCount = viewModel.correctAnswers.value + viewModel.gotItCount.value,
                    incorrectCount = viewModel.incorrectAnswers.value + viewModel.missedCount.value,
                    totalCount = viewModel.cards.value.size
                )
                findNavController().navigate(action)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnClose.setOnClickListener { findNavController().popBackStack() }

        binding.cardContainer.setOnClickListener {
            if (viewModel.mode == StudyMode.REVIEW) { viewModel.onFlipCard() }
        }

        binding.btnReset.setOnClickListener {
            viewModel.resetSession()
            showErrorState(false)
            Toast.makeText(requireContext(), "Session Reset", Toast.LENGTH_SHORT).show()
        }

        // Review Mode Feedback
        binding.btnMissed.setOnClickListener { viewModel.updateCardProgress("Hard") }
        binding.btnGood.setOnClickListener { viewModel.updateCardProgress("Good") }
        binding.btnGotIt.setOnClickListener { viewModel.updateCardProgress("Easy") }

        // Navigation
        binding.btnNextCard.setOnClickListener {
            if (viewModel.mode == StudyMode.QUIZ){
                flipCard(false)
                showErrorState(false)
                binding.btnPrevCard.isEnabled = true
                binding.btnPrevCard.alpha = 1.0f
                binding.interactionArea.isVisible = true
            }
            viewModel.nextCard()
        }
        binding.btnPrevCard.setOnClickListener { viewModel.previousCard() }

        // Quiz Mode
        binding.btnSubmit.setOnClickListener {
            val answer = binding.userAnswerEditText.text.toString()
            if (answer.isBlank()) {
                Toast.makeText(requireContext(), "Please enter an answer", Toast.LENGTH_SHORT).show()
            } else {
                val isCorrect = viewModel.submitAnswer(answer)
                if (isCorrect) {
                    Toast.makeText(requireContext(), "إجابة صحيحة! 🎉", Toast.LENGTH_SHORT).show()
                    binding.btnPrevCard.isEnabled = true
                } else {
                    showErrorState(true)
                    Toast.makeText(requireContext(), "للأسف، إجابة خاطئة", Toast.LENGTH_SHORT).show()
                }
                viewModel.submitAnswer(answer)
            }
        }

        binding.btnRetry.setOnClickListener {
            showErrorState(false)
        }

        binding.btnSkip.setOnClickListener {
            showErrorState(false)
            flipCard(true)
            binding.interactionArea.isVisible = false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(cards: List<CardEntity>, index: Int) {
        val card = cards.getOrNull(index) ?: return
        val total = cards.size
        binding.apply {
            cardFront.tvQuestionContent.text = card.question
            cardBack.tvAnswerContent.text = card.answer
            cardFront.badgeDifficulty.text = card.difficultyLevel
            tvCounter.text = "${index + 1} / $total"
            studyProgress.setProgress(((index + 1).toFloat() / total * 100).toInt(), true)
        }
        updateBadgeColor(card.difficultyLevel)
    }

    private fun updateBadgeColor(difficulty: String) {
        val colorRes = when (difficulty) {
            "Easy" -> R.color.green
            "Hard" -> R.color.red
            else -> R.color.blue
        }
        val mainColor = ContextCompat.getColor(requireContext(), colorRes)
        val backgroundColor = androidx.core.graphics.ColorUtils.setAlphaComponent(mainColor, 30)
        binding.apply {
            cardFront.badgeDifficulty.setTextColor(mainColor)
            cardFront.badgeDot.backgroundTintList = ContextCompat.getColorStateList(requireContext(), colorRes)
            cardFront.badgeLayout.setCardBackgroundColor(backgroundColor)
            cardFront.badgeLayout.strokeColor = mainColor
        }
    }

    private fun flipCard(showBack: Boolean) {
        val rotation = if (showBack) 180f else 0f
        binding.apply {
            cardContainer.animate()
                .setDuration(400)
                .rotationY(rotation)
                .withLayer()
                .setUpdateListener {
                    if (it.animatedFraction >= 0.5f) {
                        cardFront.cardFront.isVisible = !showBack
                        cardBack.cardBack.isVisible = showBack
                        cardBack.cardBack.rotationY = 180f
                    }
                }.start()
        }
    }

    private fun showErrorState(isError: Boolean) {
        binding.quizNavigation.isVisible = !isError
        binding.btnSubmit.isVisible = !isError
        binding.errorNavigation.isVisible = isError
        binding.userAnswerEditText.clearFocus()
        binding.userAnswerEditText.text?.clear()

        // تعطيل زر السابق في حالة الخطأ لضمان عدم الغش
        if (isError) {
            binding.btnPrevCard.isEnabled = false
            binding.btnPrevCard.alpha = 0.5f
        } else {
            binding.btnPrevCard.isEnabled = true
            binding.btnPrevCard.alpha = 1.0f
        }
    }

    private fun setupCameraDistance() {
        val distance = 8000
        val scale = resources.displayMetrics.density * distance
        binding.cardContainer.cameraDistance = scale
    }

    enum class StudyMode { QUIZ, REVIEW }
}