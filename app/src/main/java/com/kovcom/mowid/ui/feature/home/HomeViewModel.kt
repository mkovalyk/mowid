package com.kovcom.mowid.ui.feature.home

import com.kovcom.domain.repository.QuotesRepository
import com.kovcom.domain.repository.UserRepository
import com.kovcom.mowid.base.ui.BaseViewModelV2
import com.kovcom.mowid.base.ui.DataProvider
import com.kovcom.mowid.base.ui.IntentProcessor
import com.kovcom.mowid.base.ui.Publisher
import com.kovcom.mowid.base.ui.Reducer
import com.kovcom.mowid.model.toUIModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class HomeViewModel constructor(
    intentProcessor: HomeIntentProcessor,
    reducer: HomeReducer,
    publisher: HomePublisher,
    groupsDataProvider: QuotesDataProvider,
    userProvider: UserProvider,
) : BaseViewModelV2<HomeState, HomeEvent, HomeEffect, HomeUserIntent>(
    intentProcessor,
    reducer,
    publisher,
    dataProviders = listOf(groupsDataProvider, userProvider)
) {

    override fun createInitialState(): HomeState = HomeState(
        isLoading = true,
        groupPhraseList = emptyList(),
        isLoggedIn = false,
    )

    override fun tag(): String = "HomeViewModel"
}

class QuotesDataProvider(
    private val quotesRepository: QuotesRepository,
) : DataProvider<HomeEffect> {

    override fun observe(): Flow<HomeEffect> {
        return quotesRepository.getGroupsFlow()
            .flatMapLatest {
                flowOf(HomeEffect.Loading(false), HomeEffect.Loaded(it))
            }
            .onStart { emit(HomeEffect.Loading(true)) }
            .catch {
                emit(HomeEffect.Loading(false))
                emit(HomeEffect.ShowError("Error: $it"))
            }
            .distinctUntilChanged()
    }
}

class UserProvider(
    private val userRepository: UserRepository,
) : DataProvider<HomeEffect> {

    override fun observe(): Flow<HomeEffect> {
        return userRepository.userFlow.map {
            HomeEffect.UserLoaded(isLoggedIn = it != null)
        }
    }
}

class HomeIntentProcessor constructor(
    private val quotesRepository: QuotesRepository,
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
                quotesRepository.addGroup(
                    name = intent.name, description = intent.description
                )
                flowOf(
                    HomeEffect.ShowGroupCreatedMessage,
                    HomeEffect.HideGroupModal
                )
            }

            is HomeUserIntent.GroupItemClicked -> flow {
                quotesRepository.selectGroup(intent.groupPhrase.id)
                emit(HomeEffect.OpenDetails(intent.groupPhrase))
            }

            is HomeUserIntent.HideGroupModal -> flowOf(HomeEffect.HideGroupModal)
            is HomeUserIntent.OnItemDelete -> flowOf(
                HomeEffect.RemoveGroup(
                    id = intent.id,
                    name = intent.name,
                    groupType = intent.groupType
                )
            )

            is HomeUserIntent.RemoveGroupConfirmed -> flow {
                quotesRepository.deleteGroup(intent.id, intent.groupType)
                emit(HomeEffect.RemoveGroupConfirmed(intent.name))
                emit(HomeEffect.DismissRemoveGroupConfirmation)
            }

            is HomeUserIntent.OnEditClicked -> emptyFlow()
            is HomeUserIntent.ShowGroupModal -> emptyFlow()
            is HomeUserIntent.HideGroupConfirmationDialog -> flowOf(HomeEffect.DismissRemoveGroupConfirmation)
        }
    }
}

class HomeReducer : Reducer<HomeEffect, HomeState> {

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
            is HomeEffect.RemoveGroupConfirmed,
            -> state

            is HomeEffect.UserLoaded -> state.copy(
                isLoggedIn = effect.isLoggedIn
            )

            is HomeEffect.RemoveGroup,
            -> state.copy(
                dialogType = HomeState.DialogType.RemoveGroupConfirmation(
                    id = effect.id,
                    name = effect.name,
                    groupType = effect.groupType,
                )
            )

            is HomeEffect.DismissRemoveGroupConfirmation -> state.copy(
                dialogType = HomeState.DialogType.None
            )
        }
    }

}

class HomePublisher : Publisher<HomeEffect, HomeEvent, HomeState> {

    override fun publish(effect: HomeEffect, currentState: HomeState): HomeEvent? {
        return when (effect) {
            is HomeEffect.ShowError -> HomeEvent.ShowError(effect.message)
            is HomeEffect.ShowGroupCreatedMessage -> HomeEvent.ShowSnackbar("Group created")
            is HomeEffect.ShowLoginScreen -> HomeEvent.ShowLoginScreen
            is HomeEffect.HideGroupModal -> HomeEvent.HideGroupModal
            is HomeEffect.OpenDetails -> HomeEvent.ItemClicked(effect.groupPhrase)

            is HomeEffect.RemoveGroupConfirmed -> HomeEvent.ShowSnackbar("Group ${effect.name} removed")
            is HomeEffect.ShowAddGroupModel -> HomeEvent.ShowAddGroupModal

            is HomeEffect.RemoveGroup,
            is HomeEffect.Loaded,
            is HomeEffect.Loading,
            is HomeEffect.ShowEditGroupModal,
            is HomeEffect.UserLoaded,
            is HomeEffect.DismissRemoveGroupConfirmation,
            -> null

        }
    }

}
