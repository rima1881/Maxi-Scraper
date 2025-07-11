package org.pois_noir.botzilla

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.net.InetAddress
import java.net.ServerSocket
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

class Component {
    val name: String
    val key: ByteArray
    val service: JmDNS
    var onMessage: (Map<String, String>) -> Result<Map<String, String>>

    constructor(name: String, key: String) {

        this.name = name
        this.key = key.toByteArray()
        this.onMessage = { data ->
            println(data)
            Result.success(mapOf())
        }

        val socket = ServerSocket(0)
        val port = socket.localPort

        val jmdns = JmDNS.create(InetAddress.getLocalHost())

        val serviceType = "_botzilla._tcp.local."
        val serviceName = name
        val props = mapOf("id" to "botzilla_$name")

        val serviceInfo = ServiceInfo.create(
            serviceType,
            serviceName,
            port,
            0,
            0,
            props
        )
        jmdns.registerService(serviceInfo)
        this.service = jmdns

        tcpRunner(socket)
    }

    fun sendMessage(componentName: String,message: Map<String, String>) : Result<Map<String, String>> = runCatching {

        // Todo
        // fix me pls
        val destinationAddress: String = getComponent(componentName)!!

        val address = destinationAddress.split(":")

        val ip = address[0]
        val port = address[1].toInt()

        val encodedMessage = Json.encodeToString(message).toByteArray(Charsets.UTF_8)

        val encodedResponse = request(
            ip,
            port,
            encodedMessage,
            USER_MESSAGE_OPERATION_CODE,
            key
        ).getOrThrow()

        val response: Map<String, String> = Json.decodeFromString(
            encodedResponse.toString(Charsets.UTF_8)
        )

        response

    }

    private fun tcpRunner(socket: ServerSocket) : Job {
        return CoroutineScope(Dispatchers.IO).launch {

            while(true) {
                val client = socket.accept()
                launch{
                    requestHandler(client, key ,onMessage)
                }
            }
        }
    }

}