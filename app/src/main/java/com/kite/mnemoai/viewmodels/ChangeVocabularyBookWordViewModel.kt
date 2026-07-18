package com.kite.mnemoai.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.ViewModelInitializer
import com.kite.mnemoai.MainApplication
import com.kite.mnemoai.data.repository.WordRepository

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import com.kite.mnemoai.data.model.WordListItem
import com.kite.mnemoai.data.repository.GroupRepository
import com.kite.mnemoai.data.repository.IRepositoryCallback
import com.kite.mnemoai.model.VocabularyBookChangedWord
import com.kite.mnemoai.uistate.ChangeVocabularyBookWordUIState

class ChangeVocabularyBookWordViewModel(
    val wordRepository: WordRepository,
    val groupRepository: GroupRepository,
    val savedStateHandle: SavedStateHandle
): ViewModel() {
    private val _uiStatus: MutableLiveData<ChangeVocabularyBookWordUIState> = MutableLiveData()
    val uiStatus: LiveData<ChangeVocabularyBookWordUIState> get() = _uiStatus

    private var groupId: Long = -1
    private var operationType = ChangeVocabularyBookWordUIState.ChangeType.ADD
    private var searchText:String = ""
    private var optionalWords = mutableListOf<WordListItem>()
    private val alterWords = mutableListOf<VocabularyBookChangedWord>()

    init {
        groupId = savedStateHandle.get<Long>("group_id") ?: -1
        performOptionWordsSearch()
    }

    fun setOperationType(type: ChangeVocabularyBookWordUIState.ChangeType){
        this.operationType = type
        this.optionalWords.clear()
        updateUIStatus()
        performOptionWordsSearch()
    }

    fun setSearchText(searchText: String){
        this.searchText = searchText
        performOptionWordsSearch()
    }

    fun addAlterWords(wordListItem: WordListItem){
        val changedWord = VocabularyBookChangedWord(
            wordListItem.id,
            wordListItem.word,
            wordListItem.phonetic,
            wordListItem.translation,
            wordListItem.reviewState,
            this.operationType
        )
        alterWords.add(changedWord)
        optionalWords.remove(wordListItem)
        updateUIStatus()
    }

    fun removeAlterWords(vocabularyBookChangedWord: VocabularyBookChangedWord){
        val changeWord = WordListItem(
            vocabularyBookChangedWord.id,
            vocabularyBookChangedWord.phonetic,
            vocabularyBookChangedWord.reviewState,
            vocabularyBookChangedWord.translation,
            vocabularyBookChangedWord.word
        )
        alterWords.remove(vocabularyBookChangedWord)
        optionalWords.add(changeWord)
        updateUIStatus()
    }

    private fun performOptionWordsSearch(){
        when(operationType){
            ChangeVocabularyBookWordUIState.ChangeType.ADD -> {
                wordRepository.performVocabularyBookAddOptionWordsSearch(groupId, searchText, object :
                    IRepositoryCallback<List<WordListItem>> {
                    override fun onComplete(t: List<WordListItem>?) {
                        t?.let { wordListItems ->
                            val idsToExclude = alterWords.map { it.id }.toSet()
                            optionalWords = wordListItems.filter { it.id !in idsToExclude } as MutableList<WordListItem>
                            updateUIStatus()
                        }
                    }

                    override fun onError(t: Throwable?) {

                    }

                })
            }
            ChangeVocabularyBookWordUIState.ChangeType.REMOVE -> {
                wordRepository.performVocabularyBookRemoveOptionWordsSearch(groupId, searchText, object :
                    IRepositoryCallback<List<WordListItem>> {
                    override fun onComplete(t: List<WordListItem>?) {
                        t?.let { wordListItems ->
                            val idsToExclude = alterWords.map { it.id }.toSet()
                            optionalWords = wordListItems.filter { it.id !in idsToExclude } as MutableList<WordListItem>
                            updateUIStatus()
                        }
                    }

                    override fun onError(t: Throwable?) {

                    }

                })
            }
        }
    }

    fun applyAlterWords(){
        val addIds = alterWords.filter { it.operation == ChangeVocabularyBookWordUIState.ChangeType.ADD }.map { it.id }
        val removeIds = alterWords.filter { it.operation == ChangeVocabularyBookWordUIState.ChangeType.REMOVE }.map { it.id }
        groupRepository.addAlterWords(groupId, addIds)
        groupRepository.removeAlterWords(groupId, removeIds)
    }

    private fun updateUIStatus(){
        _uiStatus.value = ChangeVocabularyBookWordUIState(groupId, operationType, searchText, optionalWords, alterWords)
    }


    companion object{
        val initializer = ViewModelInitializer(
            ChangeVocabularyBookWordViewModel::class.java
        ){
            val app = this[APPLICATION_KEY] as MainApplication
            val savedStateHandle = createSavedStateHandle()
            ChangeVocabularyBookWordViewModel(app.wordRepository, app.groupRepository, savedStateHandle)
        }
    }

}