package com.freespeedvpn.topfreevpn.model

data class CountryServerList(
    val countryServerList: List<CountryServer>
)

data class CountryServer(
    val countryName: String,
    val host: String,
    val pass: String,
    val port: String,
    val user: String,
    var latency: Long
)