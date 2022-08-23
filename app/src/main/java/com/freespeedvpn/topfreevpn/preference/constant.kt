package kittoku.osc.preference


internal enum class OscPreference {
    IS_VPN_CONNECTED,

    HOME_SELECTED_COUNTRY,
    ROOT_STATE,
    HOME_HOSTNAME,
    HOME_USERNAME,
    HOME_PASSWORD,
    HOME_CONNECTOR,
    HOME_STATUS,
    SSL_PORT,
    SSL_VERSION,
    SSL_DO_VERIFY,
    SSL_DO_ADD_CERT,
    SSL_CERT_DIR,
    SSL_DO_SELECT_SUITES,
    SSL_SUITES,
    PPP_MRU,
    PPP_MTU,
    PPP_PAP_ENABLED,
    PPP_MSCHAPv2_ENABLED,
    PPP_AUTH_TIMEOUT,
    PPP_IPv4_ENABLED,
    PPP_IPv6_ENABLED,
    IP_PREFIX,
    IP_ONLY_LAN,
    IP_ONLY_ULA,
    RECONNECTION_ENABLED,
    RECONNECTION_COUNT,
    RECONNECTION_INTERVAL,
    BUFFER_INCOMING,
    BUFFER_OUTGOING,
    LOG_DO_SAVE_LOG,
    LOG_DIR,
    LINK_OSC,
}
