package ch.epfl.sdp.database.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.dao.EmptyMockSearchGroupDao
import ch.epfl.sdp.database.data.SearchGroupData
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class SearchGroupRepositoryTest {

    companion object {
        private const val ASYNC_CALL_TIMEOUT = 5L
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
        private const val DUMMY_GROUP_NAME = "Dummy_group_name"
        private val DUMMY_LOCATION = LatLng(0.123, 23.1234)
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun getGroupsCallsGetGroupsFromSearchGroupDao() {
        val called = CountDownLatch(1)
        val expectedData = MutableLiveData(listOf(
                SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME, DUMMY_LOCATION, DUMMY_LOCATION)
        ))

        val dao = object : EmptyMockSearchGroupDao() {
            override fun getGroups(): MutableLiveData<List<SearchGroupData>> {
                called.countDown()
                return expectedData
            }
        }

        SearchGroupRepository.daoProvider = { dao }

        val repo = SearchGroupRepository()
        assertThat(repo.getAllGroups(), equalTo(expectedData))

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))
    }

    @Test
    fun getGroupByIdCallsGetGroupByIdFromSearchGroupDao() {
        val called = CountDownLatch(1)
        val expectedData = MutableLiveData(
                SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME, DUMMY_LOCATION, DUMMY_LOCATION)
        )

        val dao = object : EmptyMockSearchGroupDao() {
            override fun getGroupById(groupId: String): MutableLiveData<SearchGroupData> {
                called.countDown()
                return expectedData
            }
        }

        SearchGroupRepository.daoProvider = { dao }

        assertThat(SearchGroupRepository().getGroupById(DUMMY_GROUP_ID), equalTo(expectedData))

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))
    }

    @Test
    fun createGroupCallsCreateGroupDao() {
        val called = CountDownLatch(1)
        val expectedData = SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME, DUMMY_LOCATION, DUMMY_LOCATION)
        lateinit var actualSearchGroup: SearchGroupData

        val dao = object : EmptyMockSearchGroupDao() {
            override fun createGroup(searchGroupData: SearchGroupData) {
                called.countDown()
                actualSearchGroup = searchGroupData
            }
        }

        SearchGroupRepository.daoProvider = { dao }
        SearchGroupRepository().createGroup(expectedData)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        assertThat(actualSearchGroup, equalTo(expectedData))
    }

    @Test
    fun updateGroupCallsUpdateGroupDao() {
        val called = CountDownLatch(1)
        val expectedData = SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME, DUMMY_LOCATION, DUMMY_LOCATION)
        lateinit var actualSearchGroup: SearchGroupData

        val dao = object : EmptyMockSearchGroupDao() {
            override fun updateGroup(searchGroupData: SearchGroupData) {
                called.countDown()
                actualSearchGroup = searchGroupData
            }
        }

        SearchGroupRepository.daoProvider = { dao }
        SearchGroupRepository().updateGroup(expectedData)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        assertThat(actualSearchGroup, equalTo(expectedData))
    }

    @Test
    fun removeSearchGroupCallsRemoveSearchGroupDao() {
        val called = CountDownLatch(1)
        val expectedData = DUMMY_GROUP_ID
        lateinit var actualSearchGroup: String

        val dao = object : EmptyMockSearchGroupDao() {
            override fun removeSearchGroup(searchGroupId: String) {
                called.countDown()
                actualSearchGroup = searchGroupId
            }
        }

        SearchGroupRepository.daoProvider = { dao }
        SearchGroupRepository().removeSearchGroup(expectedData)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        assertThat(actualSearchGroup, equalTo(expectedData))
    }
}