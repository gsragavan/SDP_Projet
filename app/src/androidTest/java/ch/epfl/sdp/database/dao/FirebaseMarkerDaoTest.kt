package ch.epfl.sdp.database.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.data.MarkerData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FirebaseMarkerDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    companion object {
        private const val DUMMY_GROUP_ID = "Dummy_group_name"
        private const val DUMMY_MARKER_ID = "Dummy_marker_id"
        private const val ASYNC_CALL_TIMEOUT = 5L
    }

    @Before
    fun setup() {
        Firebase.database.goOffline()
        Firebase.database.reference.removeValue()
    }

    @Test
    fun getMarkersOfSearchGroupReturnsExpectedValues() {
        val dao = FirebaseMarkersDao()
        val marker = MarkerData(LatLng(41.0, 10.0))
        val called = CountDownLatch(1)

        //Populate database
        Firebase.database.getReference("markers/$DUMMY_GROUP_ID")
                .push().setValue(marker)

        //Validate g1 data
        val data = dao.getMarkersOfSearchGroup(DUMMY_GROUP_ID)
        data.observeForever {
            // Test once database has been populated
            if (it.isNotEmpty()) {
                // Uuid is generated automatically so we don't test
                marker.uuid = it.first().uuid
                assertThat(it.firstOrNull(), equalTo(marker))
                called.countDown()
            }
        }

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))
    }

    @Test
    fun addMarkerAddsMarker() {
        val dao = FirebaseMarkersDao()
        val expectedMarker = MarkerData(LatLng(41.0, 10.0))
        val called = CountDownLatch(1)

        val ref = Firebase.database.getReference("markers/$DUMMY_GROUP_ID")

        val listener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                val actualMarker = dataSnapshot.getValue(MarkerData::class.java)!!
                // We do not want to compare uuids as they are generated at adding time by firebase
                expectedMarker.uuid = actualMarker.uuid
                assertThat(actualMarker, equalTo(expectedMarker))
                called.countDown()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
        }
        ref.addChildEventListener(listener)

        dao.addMarker(DUMMY_GROUP_ID, expectedMarker)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))
        ref.removeEventListener(listener)
    }

    @Test
    fun removeMarkerRemovesMarker() {
        val dao = FirebaseMarkersDao()
        val expectedRemovedMarker = MarkerData(LatLng(41.0, 10.0), DUMMY_MARKER_ID)
        val called = CountDownLatch(1)
        val added = CountDownLatch(1)
        val ref = Firebase.database.getReference("markers/$DUMMY_GROUP_ID")

        var actualRemovedMarker: MarkerData? = null

        val listener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                // Set the uuid of the expected marker to the uuid generated by firebase
                expectedRemovedMarker.uuid = dataSnapshot.key!!
                added.countDown()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                actualRemovedMarker = dataSnapshot.getValue(MarkerData::class.java)!!
                actualRemovedMarker!!.uuid = dataSnapshot.key!!
                called.countDown()
            }
        }
        ref.addChildEventListener(listener)

        ref.push().setValue(expectedRemovedMarker)
        added.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        dao.removeMarker(DUMMY_GROUP_ID, expectedRemovedMarker.uuid!!)
        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        assertThat(actualRemovedMarker, equalTo(expectedRemovedMarker))
        ref.removeEventListener(listener)
    }

    @Test
    fun removeAllMarkersOfSearchGroupRemovesAllMarkersOfSearchGroup() {
        val dao = FirebaseMarkersDao()
        val expectedRemovedMarker = MarkerData(LatLng(41.0, 10.0), DUMMY_MARKER_ID)
        val called = CountDownLatch(2)
        val added = CountDownLatch(2)
        val ref = Firebase.database.getReference("markers/$DUMMY_GROUP_ID")

        var actualRemovedMarker: MarkerData? = null

        val listener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                // Set the uuid of the expected marker to the uuid generated by firebase
                expectedRemovedMarker.uuid = dataSnapshot.key!!
                added.countDown()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                actualRemovedMarker = dataSnapshot.getValue(MarkerData::class.java)!!
                actualRemovedMarker!!.uuid = dataSnapshot.key!!
                called.countDown()
            }
        }
        ref.addChildEventListener(listener)

        ref.push().setValue(expectedRemovedMarker)
        ref.push().setValue(expectedRemovedMarker)
        added.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        dao.removeAllMarkersOfSearchGroup(DUMMY_GROUP_ID)
        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        assertThat(actualRemovedMarker, equalTo(expectedRemovedMarker))
        ref.removeEventListener(listener)
    }
}