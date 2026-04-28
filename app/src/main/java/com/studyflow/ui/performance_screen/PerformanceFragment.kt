package com.studyflow.ui.performance_screen

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.studyflow.R
import com.studyflow.databinding.FragmentPerformanceBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class PerformanceFragment : Fragment() {
    private var _binding: FragmentPerformanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PerformanceViewModel by viewModels()
    private lateinit var statesAdapter: PerformanceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerformanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        statesAdapter = PerformanceAdapter()
        binding.rvStatsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = statesAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.totalCardsLearned.collect { count ->
                        binding.tvCardsLearned.text = String.format(Locale.getDefault(), "%,d", count)
                    }
                }

                launch {
                    viewModel.totalTimeSpent.collect { totalSeconds ->
                        val hours = totalSeconds / 3600.0
                        binding.tvTimeSpent.text = String.format(Locale.getDefault(), "%.1fh", hours)
                    }
                }

                launch {
                    viewModel.averageAccuracy.collect { accuracy ->
                        binding.tvAccuracy.text = String.format(Locale.getDefault(), "%d%%", accuracy)
                    }
                }

                launch {
                    viewModel.masteredCardsCount.collect { count ->
                        binding.tvTimeMastered.text = String.format(Locale.getDefault(), "%,d", count)
                    }
                }

                launch {
                    viewModel.currentStreak.collect { streak ->
                        binding.tvStreakDays.text = String.format(Locale.getDefault(), "%d Days", streak)
                        binding.tvStreakDesc.text = if (streak > 0) "Top 5% of learners this week!" else "Start your first streak today!"
                    }
                }

                launch {
                    viewModel.weeklyGoalProgress.collect { progress ->
                        binding.pbWeeklyGoal.progress = progress
                        binding.tvGoalPercentage.text = String.format(Locale.getDefault(), "%d%%", progress)
                        binding.tvWeeklyInsight.text = if (progress > 50)
                            "You're doing great! You've reached $progress% of your weekly goal."
                        else
                            "Keep pushing! You're at $progress% of your weekly goal."
                    }
                }

                launch {
                    viewModel.overallMastery.collect { mastery ->
                        binding.masteryChart.progress = mastery
                        binding.tvMasteryPercentage.text = String.format(Locale.getDefault(), "%d%%", mastery)
                        binding.tvMasteryDesc.text = "You have mastered $mastery% of your total collection. Keep going!"
                    }
                }

                launch {
                    viewModel.deckStats.collect { stats ->
                        statesAdapter.submitList(stats)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}