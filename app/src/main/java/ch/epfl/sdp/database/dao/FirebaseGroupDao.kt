package ch.epfl.sdp.database.dao

import android.util.Log
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.SearchGroupData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseGroupDao : GroupDao {
    private var database: FirebaseDatabase = Firebase.database

    private val groups: MutableLiveData<List<SearchGroupData>> = MutableLiveData(mutableListOf())
    private val watchedGroupsById: MutableMap<String, MutableLiveData<SearchGroupData>> = mutableMapOf()

    init {
        val myRef = database.getReference("search_groups")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                groups.value = (dataSnapshot.children.map { c ->
                    // Get group data (without key)
                    val group = c.getValue(SearchGroupData::class.java)
                    // Retrieve group key generated by google and use it
                    group?.uuid = c.key
                    group!!
                })
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("FIREBASE", "Failed to read value.", error.toException())
            }
        })
    }

    override fun getGroups(): MutableLiveData<List<SearchGroupData>> {
        return groups
    }

    override fun getGroupById(groupId: String): MutableLiveData<SearchGroupData> {
        if (!watchedGroupsById.containsKey(groupId)) {
            val myRef = database.getReference("search_groups/$groupId")
            watchedGroupsById[groupId] = MutableLiveData()
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    watchedGroupsById[groupId]!!.value = dataSnapshot.getValue(SearchGroupData::class.java)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("FIREBASE", "Failed to read group from firebase.", error.toException())
                }
            })
        }
        return watchedGroupsById[groupId]!!
    }
}