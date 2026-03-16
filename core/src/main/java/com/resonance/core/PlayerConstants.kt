package com.resonance.core

/**
 * 播放器常量类
 * 统一管理所有播放器相关的常量
 */
object PlayerConstants {
    
    // Message types
    const val MSG_INIT = 1
    const val MSG_PREPARE = 2
    const val MSG_PLAY = 3
    const val MSG_PAUSE = 4
    const val MSG_STOP = 5
    const val MSG_SEEK_TO = 6
    const val MSG_SET_SURFACE = 7
    const val MSG_SET_VOLUME = 8
    const val MSG_SET_SPEED = 9
    const val MSG_GET_STATUS = 10
    const val MSG_RELEASE = 11
    const val MSG_SET_DATA_SOURCE = 12
    const val MSG_START = 13
    
    // Bundle keys
    const val KEY_URL = "url"
    const val KEY_POSITION = "position"
    const val KEY_VOLUME = "volume"
    const val KEY_SPEED = "speed"
    const val KEY_SURFACE = "surface"
    const val KEY_STATUS = "status"
    const val KEY_PLAYER_TYPE = "player_type"
    const val KEY_DATA_SOURCE = "data_source"
    const val KEY_SUCCESS = "success"
    const val KEY_MESSAGE = "message"
    const val KEY_ERROR_CODE = "error_code"
}
