package ch.epfl.sdp.database.data_manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.database.data.UserData
import ch.epfl.sdp.database.providers.HeatmapRepositoryProvider
import ch.epfl.sdp.database.providers.MarkerRepositoryProvider
import ch.epfl.sdp.database.providers.SearchGroupRepositoryProvider
import ch.epfl.sdp.database.providers.UserRepositoryProvider
import ch.epfl.sdp.utils.Auth

class SearchGroupDataManager {

    private val searchGroupRepository = SearchGroupRepositoryProvider.provide()
    private val userRepository = UserRepositoryProvider.provide()
    private val markerRepository = MarkerRepositoryProvider.provide()
    private val heatmapRepository = HeatmapRepositoryProvider.provide()
    private val groupIdsOfUser = userRepository.getGroupIdsOfUserByEmail(Auth.email.value!!)

    fun deleteSearchGroup(searchGroupId: String) {
        searchGroupRepository.removeSearchGroup(searchGroupId)
        userRepository.removeAllUserOfSearchGroup(searchGroupId)
        markerRepository.removeAllMarkersOfSearchGroup(searchGroupId)
        heatmapRepository.removeAllHeatmapsOfSearchGroup(searchGroupId)
    }

    fun createSearchGroup(name: String): String {
        val groupId = searchGroupRepository.createGroup(SearchGroupData(null, name))
        TODO("Not implemented yet need to add new user to search group")
//        return groupId
    }

    fun getAllGroups(): LiveData<List<SearchGroupData>> {
        return Transformations.switchMap(groupIdsOfUser) { ids ->
            Transformations.map(searchGroupRepository.getAllGroups()) { groups ->
                groups.filter { group ->
                    ids.contains(group.uuid)
                }
            }
        }
    }

    fun getGroupById(groupId: String): LiveData<SearchGroupData> {
        return searchGroupRepository.getGroupById(groupId)
    }

    fun editGroup(searchGroupData: SearchGroupData) {
        return searchGroupRepository.updateGroup(searchGroupData)
    }

    fun getOperatorsOfSearchGroup(searchGroupId: String): LiveData<Set<UserData>> {
        return userRepository.getOperatorsOfSearchGroup(searchGroupId)
    }

    fun getRescuersOfSearchGroup(searchGroupId: String): LiveData<Set<UserData>> {
        return userRepository.getRescuersOfSearchGroup(searchGroupId)
    }

    fun removeUserOfSearchGroup(searchGroupId: String, userId: String) {
        //TODO check rights
        userRepository.removeUserFromSearchGroup(searchGroupId, userId)
    }

    fun addUserToSearchGroup(searchGroupId: String, email: String, role: Role) {
        userRepository.addUserToSearchGroup(searchGroupId, UserData(email, role = role))
    }
}