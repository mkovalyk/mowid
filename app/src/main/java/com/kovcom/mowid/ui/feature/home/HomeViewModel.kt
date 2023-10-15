package com.kovcom.mowid.ui.feature.home

import com.kovcom.domain.interactor.MotivationPhraseInteractor
import com.kovcom.mowid.base.ui.*
import com.kovcom.mowid.model.toUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    intentProcessor: HomeIntentProcessor,
    reducer: HomeReducer,
    publisher: HomePublisher,
) : BaseViewModelV2<HomeState, HomeEvent, HomeEffect, HomeUserIntent>(
    intentProcessor,
    reducer,
    publisher,
    initialUserIntent = HomeUserIntent.Load,
) {

    override fun createInitialState(): HomeState = HomeState(
        isLoading = true,
        groupPhraseList = emptyList(),
        isLoggedIn = false,
    )

    override val tag: String = "HomeViewModel"

}

class HomeIntentProcessor @Inject constructor(
    private val motivationPhraseInteractor: MotivationPhraseInteractor,
) : IntentProcessor<HomeState, HomeUserIntent, HomeEffect> {

    override suspend fun processIntent(intent: HomeUserIntent, currentState: HomeState): Flow<HomeEffect> {
        return when (intent) {
            is HomeUserIntent.AddClicked -> {
                if (currentState.isLoggedIn) {
                    flowOf(HomeEffect.ShowAddGroupModel)
                } else {
                    flowOf(HomeEffect.ShowLoginScreen)
                }
            }

            is HomeUserIntent.AddGroupClicked -> {
                motivationPhraseInteractor.addGroup(
                    name = intent.name,
                    description = intent.description
                )
                flowOf(HomeEffect.ShowGroupCreatedMessage)
            }

            is HomeUserIntent.GroupItemClicked -> flowOf(HomeEffect.OpenDetails(intent.groupPhrase))
            is HomeUserIntent.Load -> flow {
                motivationPhraseInteractor.getGroupPhraseListFlow()
                    .onEach { emit(HomeEffect.Loaded(it)) }
                    .onStart { emit(HomeEffect.Loading(true)) }
                    .catch { emit(HomeEffect.Loading(false)) }
                    .collect {
                        emit(HomeEffect.Loaded(it))
                        emit(HomeEffect.Loading(false))
                    }
            }


            is HomeUserIntent.HideGroupModal -> flowOf(HomeEffect.HideGroupModal)
            is HomeUserIntent.OnEditClicked -> emptyFlow()
            is HomeUserIntent.OnItemDeleted -> emptyFlow()
            is HomeUserIntent.ShowGroupModal -> emptyFlow()
        }
    }
}

class HomeReducer @Inject constructor() : Reducer<HomeEffect, HomeState> {

    override fun reduce(effect: HomeEffect, state: HomeState): HomeState {
        return when (effect) {
            is HomeEffect.Loaded -> state.copy(
                groupPhraseList = effect.information.map { it.toUIModel() }
            )

            is HomeEffect.Loading -> state.copy(
                isLoading = effect.isLoading
            )

            is HomeEffect.ShowAddGroupModel,
            is HomeEffect.ShowEditGroupModal,
            is HomeEffect.ShowError,
            is HomeEffect.ShowGroupCreatedMessage,
            is HomeEffect.ShowLoginScreen,
            is HomeEffect.HideGroupModal,
            is HomeEffect.OpenDetails,
            -> state

        }
    }

}

class HomePublisher @Inject constructor() : Publisher<HomeEffect, HomeEvent, HomeState> {

    override fun publish(effect: HomeEffect, currentState: HomeState): HomeEvent? {
        return when (effect) {
            is HomeEffect.Loaded,
            is HomeEffect.Loading,
            -> null

            is HomeEffect.ShowAddGroupModel -> HomeEvent.ShowGroupModal
            is HomeEffect.ShowEditGroupModal -> HomeEvent.ShowGroupModal
            is HomeEffect.ShowError -> HomeEvent.ShowError(effect.message)
            is HomeEffect.ShowGroupCreatedMessage -> HomeEvent.ShowSnackbar("Group created")
            is HomeEffect.ShowLoginScreen -> HomeEvent.ShowLoginScreen
            is HomeEffect.HideGroupModal -> HomeEvent.HideGroupModal
            is HomeEffect.OpenDetails -> HomeEvent.ItemClicked(effect.groupPhrase)
        }
    }

}
