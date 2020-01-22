package com.frogobox.viprox.source.model

/**
 * Created by Kusenko on 29.09.2016.
 */
data class Server(
        var hostName: String? = null,
        var ip: String? = null,
        var score: String? = null,
        var ping: String? = null,
        var speed: String? = null,
        var countryLong: String? = null,
        var countryShort: String? = null,
        var numVpnSessions: String? = null,
        var uptime: String? = null,
        var totalUsers: String? = null,
        var totalTraffic: String? = null,
        var logType: String? = null,
        var operator: String? = null,
        var message: String? = null,
        var configData: String? = null,
        var quality: Int = 0,
        var city: String? = null,
        var type: Int = 0,
        var regionName: String? = null,
        var lat: Double = 0.0,
        var lon: Double = 0.0
)