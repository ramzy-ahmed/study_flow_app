package com.studyflow.ui.result_screen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.studyflow.R
import com.studyflow.databinding.FragmentResultsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResultsScreen : Fragment() {

    private lateinit var binding: FragmentResultsBinding
    private val viewModel: ResultsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentResultsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupClickListeners()
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI() {
        // تحديث المؤشر الدائري مع أنيميشن
        binding.resultProgress.setProgress(viewModel.accuracy, true)
        binding.tvScorePercent.text = "${viewModel.accuracy}%"

        // تحديث عدادات الإحصائيات
        binding.tvCorrectCount.text = viewModel.correctCount.toString()
        binding.tvWrongCount.text = viewModel.incorrectCount.toString()

        // تحديث الرسائل التشجيعية بناءً على النتيجة
        binding.tvResultStatus.text = viewModel.getResultStatus()
        binding.tvResultMessage.text = viewModel.getResultMessage()
    }

    private fun setupClickListeners() {
        // العودة لشاشة تفاصيل الـ Deck مباشرة
        binding.btnDone.setOnClickListener {
            findNavController().popBackStack(R.id.deckDetails, false)
        }
        binding.btnRetry.setOnClickListener {
            // إرسال نتيجة "retry" لشاشة المذاكرة وتصفير البيانات هناك
            setFragmentResult("study_request", bundleOf("action" to "retry"))
            findNavController().popBackStack()
        }
        binding.btnClose.setOnClickListener {
            findNavController().popBackStack(R.id.deckDetails, false)
        }
    }
}