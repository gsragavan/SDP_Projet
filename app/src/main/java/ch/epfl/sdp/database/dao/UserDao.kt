package ch.epfl.sdp.database.dao

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.database.data.UserData

interface UserDao {
    fun getUsersOfGroupWithRole(groupId: String, role: Role): MutableLiveData<Set<UserData>>
    fun removeUserFromSearchGroup(searchGroupId: String, userId: String)
    fun removeAllUserOfSearchGroup(searchGroupId: String)
    fun addUserToSearchGroup(searchGroupId: String, user: UserData)
}