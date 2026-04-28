package com.studyflow.ui.decks_screen.add_deck_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.R
import com.studyflow.data.local.entity.DeckEntity
import com.studyflow.data.repository.DeckRepository
import com.studyflow.ui.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AddDeckViewModel @Inject constructor(
    private val repository: DeckRepository
) : ViewModel() {
    // ui state
    private val _uiState = MutableStateFlow<AddDeckState>(AddDeckState.Idle)

    val uiState: StateFlow<AddDeckState> = _uiState

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    private val _image = MutableStateFlow(R.drawable.ill_programming)
    val image: StateFlow<Int> = _image

    private val _category = MutableStateFlow("General")
    val category: StateFlow<String> = _category


    fun onTitleChanged(value: String) {
        _title.value = value
    }

    fun onDescriptionChanged(value: String) {
        _description.value = value
    }

    fun onImageChanged(value: Int){
        _image.value = value
    }

    fun onCategoryChanged(value: String) {
        _category.value = value
    }

    fun getCategories(): List<String> {
        return Utils.getCategories()
    }

    fun getImages(): List<Int> {
        return Utils.getImages()
    }

    fun saveDeck() {
        if (_title.value.isBlank()){
            _uiState.value = AddDeckState.Error("Name can't be empty")
            return
        }
        viewModelScope.launch {
            val deck = DeckEntity(
                title = _title.value,
                description = _description.value,
                image = _image.value,
                category = _category.value,
            )
            repository.insertDeckWithCards(deck, emptyList())
            _uiState.value = AddDeckState.Success
            _uiState.value = AddDeckState.Idle
        }
    }
}