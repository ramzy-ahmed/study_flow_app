package com.studyflow.ui.home_screen

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.studyflow.R
import com.studyflow.databinding.FragmentHomeBinding
import com.studyflow.ui.study_screen.StudyScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: RecentDecksAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupAnimations()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSeeAll.setOnClickListener {
            findNavController().navigate(R.id.decksFragment)
        }

        binding.btnNotification.setOnClickListener {
            // الانتقال لشاشة الإشعارات
            // findNavController().navigate(R.id.action_home_to_notifications)
            Toast.makeText(requireContext(), "Notifications coming soon!", Toast.LENGTH_SHORT)
                .show()
        }

        binding.btnNotification.setOnLongClickListener {
            viewModel.insertDummyData()
            true
        }

        binding.cardStartDailyQuiz.setOnClickListener {
            // بدء كويز يومي (يختار أول Deck متاح حالياً كمثال، أو يمكن تعديله ليشمل الكل)
            viewModel.decksWithCards.value.firstOrNull()?.let {
                if (it.cards.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "No cards available to start quiz",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@let
                }
                val bundle = bundleOf(
                    "mode" to StudyScreen.StudyMode.QUIZ,
                    "selectedDeck" to it.deck.id
                )
                findNavController().navigate(R.id.studyFragment, bundle)
            } ?: Toast.makeText(
                requireContext(),
                "No decks available to start quiz",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = RecentDecksAdapter { deckId ->
            val action = HomeFragmentDirections.actionHomeToDeckDetails(deckId)
            findNavController().navigate(action)
        }
        binding.rvRecentDecks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragment.adapter
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // مراقبة الـ Decks الأخيرة
                launch {
                    viewModel.recentDecks.collect { decks ->
                        adapter.submitList(decks)
                    }
                }
                // تحديث الإحصائيات (Stats)
                launch {
                    viewModel.totalDecksCount.collect { count ->
                        binding.layoutStatDecks.tvStatValue.text = count.toString()
                    }
                }

                launch {
                    viewModel.totalCardsCount.collect { count ->
                        binding.layoutStatCards.tvStatValue.text = count.toString()
                        binding.layoutStatCards.tvStatLabel.text = "cards"
                        Glide.with(requireContext())
                            .load(R.drawable.ic_cards)
                            .into(binding.layoutStatCards.ivStatIcon)

                        binding.layoutStatCards.ivStatIcon.imageTintList =
                            ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.purple
                                )
                            )
                        binding.layoutStatCards.ivStatIcon.background =
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.bg_icon_soft_purple
                            )
                    }
                }

                launch {
                    viewModel.averageScore.collect { score ->
                        binding.layoutStatScore.tvStatValue.text = "$score%"
                        binding.layoutStatScore.tvStatLabel.text = "Score"
                        Glide.with(requireContext())
                            .load(R.drawable.ic_score)
                            .into(binding.layoutStatScore.ivStatIcon)

                        binding.layoutStatScore.ivStatIcon.imageTintList =
                            ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.green
                                )
                            )
                        binding.layoutStatScore.ivStatIcon.background =
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.bg_icon_soft_green
                            )
                    }
                }

                launch {
                    viewModel.dueCardsCount.collect { count ->
                        binding.tvDailyQuizCardsCount.text = "$count Cards due for review"
                    }
                }

                launch {
                    viewModel.weeklyFocusData.collect { data ->
                        binding.weeklyChart.setData(data)
                    }
                }

                launch {
                    viewModel.weeklyGrowth.collect { growth ->
                        binding.tvWeeklyGrowth.text = growth
                        binding.tvWeeklyFocusDesc.text = "You're up $growth from last week"
                    }
                }
            }
        }
    }

    private fun setupAnimations() {
        binding.headerLayout.alpha = 0f
        binding.headerLayout.translationY = -60f
        binding.nestedScrollView.alpha = 0f
        binding.nestedScrollView.translationY = 100f

        binding.headerLayout.animate().alpha(1f).translationY(0f).setDuration(500)
            .setInterpolator(DecelerateInterpolator()).start()
        binding.nestedScrollView.animate().alpha(1f).translationY(0f).setDuration(600)
            .setStartDelay(200).setInterpolator(DecelerateInterpolator()).start()
    }
}