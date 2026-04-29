package com.studyflow.ui.result_screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val correctCount: Int = checkNotNull(savedStateHandle["correctCount"])
    val incorrectCount: Int = checkNotNull(savedStateHandle["incorrectCount"])
    val totalCount: Int = checkNotNull(savedStateHandle["totalCount"])

    val accuracy: Int = if (totalCount > 0) (correctCount.toFloat() / totalCount * 100).toInt() else 0

    fun getResultStatus(): String {
        return when {
            accuracy >= 90 -> "أداء مذهل! 🎉"
            accuracy >= 75 -> "عمل رائع! 👍"
            accuracy >= 50 -> "أداء جيد، استمر! 💪"
            else -> "لا تستسلم، حاول مرة أخرى! ❤️"
        }
    }

    fun getResultMessage(): String {
        return "لقد أتقنت $correctCount من أصل $totalCount كارت بنجاح."
    }
}
