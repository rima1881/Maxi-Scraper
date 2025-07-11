package org.pois_noir.botzilla

import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


internal val jmDNS: JmDNS by lazy {
    JmDNS.create(InetAddress.getLocalHost())
}

fun getComponents(timeoutMillis: Long = 1000): Map<String, String> {
    val result = ConcurrentHashMap<String, String>()
    val latch = CountDownLatch(1)

    val listener = object : ServiceListener {
        override fun serviceAdded(event: ServiceEvent) {
            jmDNS.requestServiceInfo(event.type, event.name, true)
        }

        override fun serviceRemoved(event: ServiceEvent) {}

        override fun serviceResolved(event: ServiceEvent) {
            val info = event.info
            val address = info.inet4Addresses.firstOrNull()?.hostAddress
            if (address != null) {
                result[info.name] = "$address:${info.port}"
            }
        }
    }

    jmDNS.addServiceListener("_botzilla._tcp.local.", listener)

    // Wait for services to resolve
    latch.await(timeoutMillis, TimeUnit.MILLISECONDS)

    jmDNS.removeServiceListener("_botzilla._tcp.local.", listener)

    return result
}

fun getComponent(name: String, timeoutMillis: Long = 5000): String? {
    val latch = CountDownLatch(1)
    var component: String? = null

    val listener = object : ServiceListener {
        override fun serviceAdded(event: ServiceEvent) {
            if (event.name == name) {
                jmDNS.requestServiceInfo(event.type, event.name, true)
            }
        }

        override fun serviceRemoved(event: ServiceEvent) {}

        override fun serviceResolved(event: ServiceEvent) {
            if (event.name == name) {
                val info = event.info
                val address = info.inet4Addresses.firstOrNull()?.hostAddress
                if (address != null) {
                    component = "$address:${info.port}"
                    latch.countDown()
                }
            }
        }
    }

    jmDNS.addServiceListener("_botzilla._tcp.local.", listener)

    latch.await(timeoutMillis, TimeUnit.MILLISECONDS)

    jmDNS.removeServiceListener("_botzilla._tcp.local.", listener)

    return component
}
