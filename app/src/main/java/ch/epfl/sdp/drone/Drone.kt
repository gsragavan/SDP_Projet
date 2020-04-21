package ch.epfl.sdp.drone

import androidx.lifecycle.MutableLiveData
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.System
import io.mavsdk.mission.Mission
import io.reactivex.disposables.Disposable
import timber.log.Timber
import kotlin.math.pow
import kotlin.math.sqrt

object Drone {
    //must be IP address where the mavsdk_server is running
    private const val BACKEND_IP_ADDRESS = "10.0.2.2"
    private const val BACKEND_PORT = 50051

    // Maximum distance betwen passes in the strategy
    const val GROUND_SENSOR_SCOPE: Double = 15.0

    private val disposables: MutableList<Disposable> = ArrayList()
    val currentPositionLiveData: MutableLiveData<LatLng> = MutableLiveData()
    val currentBatteryLevelLiveData: MutableLiveData<Float> = MutableLiveData()
    val currentAbsoluteAltitudeLiveData: MutableLiveData<Float> = MutableLiveData()
    val currentSpeedLiveData: MutableLiveData<Float> = MutableLiveData()
    val currentMissionLiveData: MutableLiveData<List<Mission.MissionItem>> = MutableLiveData()

    private val instance: System

    init {
        instance = System(BACKEND_IP_ADDRESS, BACKEND_PORT)

        disposables.add(instance.telemetry.flightMode.distinct()
                .subscribe { flightMode -> Timber.d("flight mode: $flightMode") })
        disposables.add(instance.telemetry.armed.distinct()
                .subscribe { armed -> Timber.d("armed: $armed") })
        disposables.add(instance.telemetry.position.subscribe { position ->
            val latLng = LatLng(position.latitudeDeg, position.longitudeDeg)
            currentPositionLiveData.postValue(latLng)
            currentAbsoluteAltitudeLiveData.postValue(position.absoluteAltitudeM)
        })
        disposables.add(instance.telemetry.battery.subscribe { battery ->
            currentBatteryLevelLiveData.postValue(battery.remainingPercent)
        })
        disposables.add(instance.telemetry.groundSpeedNed.subscribe { groundSpeed ->
            val speed = sqrt(groundSpeed.velocityEastMS.pow(2) +
                    groundSpeed.velocityEastMS.pow(2))
            currentSpeedLiveData.postValue(speed)
        })
    }

    fun startMission(mission: List<Mission.MissionItem>) {
        this.currentMissionLiveData.postValue(mission)
        val isConnectedCompletable = instance.core.connectionState
                .filter { state -> state.isConnected }
                .firstOrError()
                .toCompletable()

        isConnectedCompletable
                .andThen(instance.mission.setReturnToLaunchAfterMission(true))
                .andThen(instance.mission.uploadMission(mission))
                .andThen(instance.action.arm())
                .andThen(instance.mission.startMission())
                .subscribe()
    }
}