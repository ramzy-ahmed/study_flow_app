package com.studyflow.ui.utils

import com.studyflow.R

class Utils {
    companion object {
        fun getCategories(): List<String> {
            val categories = listOf(
                "General",
                "Languages",
                "Programming",
                "Medicine",
                "History",
                "Science",
                "Arts"
            )
            return categories
        }
        fun getImages(): List<Int> {
            val images = listOf(
                R.drawable.ill_language, // لغات
                R.drawable.ill_programming,   // طب
                R.drawable.ill_programming,      // برمجة
                R.drawable.ill_history,      // رياضة
                R.drawable.ic_biotech,       // فنون
                R.drawable.ill_cimistry    // علوم
            )
            return images
        }
    }
}