package com.kovcom.mowid.ui.feature.home

import com.kovcom.domain.repository.QuotesRepository
import com.kovcom.domain.repository.UserRepository
import com.kovcom.mowid.Group
import com.kovcom.mowid.base.ui.*
import com.kovcom.mowid.model.toUIModel
import kotlinx.coroutines.flow.*

class HomeViewModel constructor(
    intentProcessor: HomeIntentProcessor,
    reducer: HomeReducer,
    publisher: HomePublisher,
    groupsDataProvider: QuotesDataProvider,
    userProvider: UserProvider,
) : BaseViewModel<HomeState, HomeEvent, HomeEffect, HomeUserIntent>(
    intentProcessor,
    reducer,
    publisher,
    dataProviders = listOf(groupsDataProvider, userProvider),
    initialState = HomeState.EMPTY,
) {

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
            is HomeUserIntent.OnGroupSelectionChanged -> flow {
                quotesRepository.saveSelection(
                    groupId = intent.id,
                    groupType = intent.groupType,
                    isSelected = intent.isSelected
                )
                emit(HomeEffect.ShowGroupSelectionMessage(intent.isSelected))
            }
        }
    }
}

class HomeReducer : Reducer<HomeEffect, HomeState> {

    override fun reduce(effect: HomeEffect, state: HomeState): HomeState {
        return when (effect) {
            is HomeEffect.Loaded -> state.copy(
                groupList = effect.information.map { it.toUIModel() }
            )

            is HomeEffect.Loading -> state.copy(
                isLoading = effect.isLoading
            )


            is HomeEffect.UserLoaded -> state.copy(
                isLoggedIn = effect.isLoggedIn
            )

            is HomeEffect.RemoveGroup,
            -> state.copy(
                dialogType = HomeState.DialogType.RemoveGroupConfirmation(
                    id = effect.id,
                    name = effect.name,
                    groupType = effect.groupType,
                ),
                groupList = state.groupList.map {
                    if (it.id == effect.id) {
                        it.copy(isSwipeToDeleteOpened = true)
                    } else {
                        it
                    }
                }
            )

            is HomeEffect.DismissRemoveGroupConfirmation -> state.copy(
                dialogType = HomeState.DialogType.None,
                groupList = state.groupList.map {
                    it.copy(isSwipeToDeleteOpened = false)
                }
            )

            is HomeEffect.ShowGroupSelectionMessage,
            is HomeEffect.ShowAddGroupModel,
            is HomeEffect.ShowEditGroupModal,
            is HomeEffect.ShowError,
            is HomeEffect.ShowGroupCreatedMessage,
            is HomeEffect.ShowLoginScreen,
            is HomeEffect.HideGroupModal,
            is HomeEffect.OpenDetails,
            is HomeEffect.RemoveGroupConfirmed,
            -> state
        }
    }
}

class HomePublisher : Publisher<HomeEffect, HomeEvent, HomeState> {

    override fun publish(effect: HomeEffect, currentState: HomeState): HomeEvent? {
        return when (effect) {
            is HomeEffect.ShowError -> HomeEvent.ShowError(effect.message)
            is HomeEffect.ShowGroupCreatedMessage -> HomeEvent.ShowSnackbar(Group.Created.value)
            is HomeEffect.ShowLoginScreen -> HomeEvent.ShowLoginScreen
            is HomeEffect.HideGroupModal -> HomeEvent.HideGroupModal
            is HomeEffect.OpenDetails -> HomeEvent.ItemClicked(effect.groupPhrase)

            is HomeEffect.RemoveGroupConfirmed -> HomeEvent.ShowSnackbar(Group.Removed.value(effect.name))
            is HomeEffect.ShowAddGroupModel -> HomeEvent.ShowAddGroupModal
            is HomeEffect.ShowGroupSelectionMessage -> {
                when (effect.selected) {
                    true -> HomeEvent.ShowSnackbar(Group.Selected.value)
                    false -> HomeEvent.ShowSnackbar(Group.Unselected.value)
                }
            }

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
