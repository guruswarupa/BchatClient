
package com.example.bchatclient.network

import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketManager {

    private const val SERVER_URL = "http://0.0.0.0:5000"
    private var socket: Socket? = null

    fun connect(token: String): Socket? {
        try {
            val options = IO.Options()
            options.query = "token=$token"
            options.forceNew = true

            socket = IO.socket(SERVER_URL, options)
            socket?.connect()

            return socket
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
        return null
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }

    fun getSocket(): Socket? = socket

    fun isConnected(): Boolean = socket?.connected() == true
}
