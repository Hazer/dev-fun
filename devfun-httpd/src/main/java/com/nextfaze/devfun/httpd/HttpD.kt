package com.nextfaze.devfun.httpd

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import androidx.appcompat.app.AlertDialog
import com.google.auto.service.AutoService
import com.nextfaze.devfun.category.DeveloperCategory
import com.nextfaze.devfun.core.AbstractDevFunModule
import com.nextfaze.devfun.core.DevFun
import com.nextfaze.devfun.core.DevFunModule
import com.nextfaze.devfun.core.call
import com.nextfaze.devfun.core.devFun
import com.nextfaze.devfun.function.DeveloperFunction
import com.nextfaze.devfun.function.InvokeResult
import com.nextfaze.devfun.inject.InstanceProvider
import com.nextfaze.devfun.inject.captureInstance
import com.nextfaze.devfun.internal.android.*
import com.nextfaze.devfun.internal.log.*
import com.nextfaze.devfun.internal.string.*
import fi.iki.elonen.NanoHTTPD.IHTTPSession
import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.Response.Status.BAD_REQUEST
import fi.iki.elonen.NanoHTTPD.Response.Status.INTERNAL_ERROR
import fi.iki.elonen.NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED
import fi.iki.elonen.NanoHTTPD.Response.Status.NOT_FOUND
import fi.iki.elonen.NanoHTTPD.Response.Status.OK
import fi.iki.elonen.NanoHTTPD.Response.Status.REQUEST_TIMEOUT
import fi.iki.elonen.router.RouterNanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource
import fi.iki.elonen.router.RouterNanoHTTPD.newFixedLengthResponse
import java.net.NetworkInterface
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.LazyThreadSafetyMode.NONE


private const val DEFAULT_PORT = 0x5A23 // 23075
private const val AVD_IP = "10.0.2.15"

private const val PRIVILEGED_PORTS_END = 1023
private const val EPHEMERAL_PORTS_START = 49152

/**
 * The port to use for HTTPD server. Default value is 23075.
 *
 * This value needs to be set before [DevHttpD.init].
 *
 * More simply, it can also be overridden in resources: `R.integer.devDefaultPort`
 */
var devDefaultPort = DEFAULT_PORT

@AutoService(DevFunModule::class)
class DevHttpD : AbstractDevFunModule() {
    private val log = logger()
    internal val handler = Handler(Looper.getMainLooper())

    private var port = devDefaultPort

    lateinit var nano: HttpDRouter
    private lateinit var captured: InstanceProvider

    override fun init(context: Context) {
        port = context.resources.getInteger(R.integer.df_httpd_default_port).takeUnless { it <= 0 } ?: devDefaultPort

        // We'll try requested port and one based on package name
        // Maybe only use package-generated one unless manually specified?
        var lastException: Throwable? = null
        val nanoHttpD = run startNanoHttpD@{
            listOf(port, generatePortFromPackage(port, context)).forEach { p ->
                try {
                    // TODO? do this in another thread? (I suspect this will block during start() until new server thread is bound)
                    return@startNanoHttpD HttpDRouter(context, p).apply { start() }
                } catch (t: Throwable) {
                    log.w(t) { "Failed to start NanoHttpD on port $p, attempting port generated from application package..." }
                    lastException = t
                }
            }
            return@startNanoHttpD null
        } ?: throw lastException ?: RuntimeException("Failed to initialize NanoHttpD - unknown error")

        nano = nanoHttpD
        devFun.instanceProviders += captureInstance { nano }.also { captured = it }
    }

    override fun dispose() {
        nano.stop()
        devFun.instanceProviders -= captured
    }

    private fun generatePortFromPackage(startingPort: Int, context: Context): Int {
        val port = startingPort xor (context.packageName.hashCode() and 0xffff)
        return when {
            port <= PRIVILEGED_PORTS_END -> port + PRIVILEGED_PORTS_END * 2
            port >= EPHEMERAL_PORTS_START -> port - (port - EPHEMERAL_PORTS_START) * 2
            else -> port
        }
    }
}

val DevFun.devHttpD get() = get<DevHttpD>()

@DeveloperCategory("DevFun", "HTTPD")
class HttpDRouter(private val context: Context, private val port: Int) : RouterNanoHTTPD(port) {
    private val log = logger()

    init {
        setRoutePrioritizer(InsertionOrderRoutePrioritizer())
        addMappings()
    }

    override fun addMappings() {
        setNotImplementedHandler(NotImplementedHandler::class.java)
        setNotFoundHandler(Error404UriHandler::class.java)
        addRoute("/invoke/:hashCode", InvokeHandler::class.java)
    }

    /** Start the HTTPD server. */
    @DeveloperFunction
    override fun start() = super.start()

    override fun start(timeout: Int, daemon: Boolean) {
        super.start(timeout, daemon)
        logServerInfo()
    }

    /** Stop the HTTPD server. */
    @DeveloperFunction
    override fun stop() = super.stop()

    @DeveloperFunction
    private fun showServerInfoDialog(activity: Activity) = showServerInfoDialog(activity, "Server Info", logServerInfo())

    @DeveloperFunction("Show Server Info Dialog TL;DR;")
    private fun showServerInfoDialogTlDr(activity: Activity) = showServerInfoDialog(activity, "Server Info TL;DR;", serverInfoTlDr())

    private fun showServerInfoDialog(activity: Activity, title: String, message: String): String {
        val cmd = "${adbForwardCommand()} && (open ${localHostUrl()} | xdg-open ${localHostUrl()})"
        val msg = SpannableStringBuilder(message).apply {
            this += "\n\n"
            this += background(pre(cmd), 0xFFCCCCCC.toInt())
        }
        AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
            .setNeutralButton("Copy Forwarding Command") { d, _ ->
                val clip = ClipData.newPlainText("DevFun ADB HTTPD Forward Command", cmd)
                activity.clipboardManager.primaryClip = clip
                d.dismiss()
            }
            .show()
        return "$title\n$message" // we return this so DevFun will also log the details
    }

    private fun serverInfoTlDr() = """
Run this: ${adbForwardCommand()}
Visit this: ${localHostUrl()}"""

    private fun adbForwardCommand() = "adb forward tcp:$port tcp:$port"

    private fun localHostUrl() = "http://127.0.0.1:$port/"

    @DeveloperFunction
    private fun logServerInfo(): String {
        val b = StringBuilder()

        val wifiIpV4 = wifiIpV4
        "Local HTTP server started. (hasWifiIpV4=${wifiIpV4 != 0}, isEmulator=$isEmulator, isAvd=$isAvd)".also {
            b.append(it).append("\n")
            log.i { it }
        }

        // Use wifi IPv4 if present
        if (wifiIpV4 != 0) {
            "Access via wifi: http://${wifiIpV4.toIpV4String()}:$port/".also {
                b.append(it).append("\n")
                log.i { it }
            }
        }

        // If we're in an AVD then log help/setup info
        if (isAvd) {
            """================================================
This device appears to be a local AVD instance.
To enable access from host machine, port forwarding must be enabled for device.


//
// Forward using ADB
//

- Use ADB 'forward' command:
> adb forward tcp:<machinePort> tcp:<emulatorPort>
e.g.
adb forward tcp:$port tcp:$port


//
// Forward using Emulator Console
//

- Connect to emulator console via telnet (default is 5554)
> telnet localhost <port>
- Authenticate with telnet (auth token found in user home directory e.g. ~/.emulator_console_auth_token)
> auth <auth_token>
- Add port redirection (emulatorPort=$port)
> redir add tcp:<machinePort>:<emulatorPort>

==== Commands for default environment ====
> telnet localhost 5554
> auth <auth_token>
> redir add tcp:$port:$port
- Access via: http://127.0.0.1:$port/

==== Additional Info ====
Emulator console port can be found via:
- Window title of emulator ("Android Emulator - Nexus_5X_API_23:5554")  // 5554
- ADB command "adb devices" > "emulator-5554   device" // 5554

On Linux auth token can be seen via "cat ~/.emulator_console_auth_token"
On other machines, after connecting to emulator console you will be told where you can find your auth token.


//
// Remarks
//

When adding the port redirect:
- <machinePort> can be whatever you want (it will be the port you use in browser URL)
- <emulatorPort> needs to be the value passed in to "initHttpD" (DEFAULT_PORT=$DEFAULT_PORT)

Official documentation:
- ADB forwarding: https://developer.android.com/studio/command-line/adb.html#forwardports
- Console access: https://developer.android.com/studio/run/emulator-commandline.html#console
- Network config: https://developer.android.com/studio/run/emulator-commandline.html#emulatornetworking

Once completed access via: 127.0.0.1:<machinePort>
If you used the same <machinePort> as <emulatorPort>: http://127.0.0.1:$port/


TL;DR;
${serverInfoTlDr()}
================================================""".also {
                b.append(it).append("\n")
                log.i { it }
            }
        }

        // Dump all possible network address
        val addresses = NetworkInterface.getNetworkInterfaces().asSequence()
            .flatMap { it.inetAddresses.asSequence().filter { inet -> inet.address != null && inet.address.isNotEmpty() } }
            .map { if (it.address.size == 4) it.hostAddress else "[${it.hostAddress}]" }
            .filter { it != AVD_IP }
            .toSet()
        if (addresses.isNotEmpty()) {
            """Other possible access addresses from device all-networks list (these might not work):
${addresses.joinToString("\n") { "http://$it:$port/" }}""".also {
                b.append(it).append("\n")
                log.i { it }
            }
        }

        return b.toString()
    }

    private val wifiIpV4 get() = wifiManager.connectionInfo?.ipAddress ?: 0

    private val isAvd by lazy(NONE) hasAvdIp@{
        NetworkInterface.getNetworkInterfaces().asSequence().any {
            it.inetAddresses.asSequence().any { inet -> inet.hostAddress == AVD_IP }
        }
    }

    private val wifiManager get() = context.wifiManager

    private fun Int.toIpV4String() = "%d.%d.%d.%d".format(this and 0xff, this shr 8 and 0xff, this shr 16 and 0xff, this shr 24 and 0xff)
}

abstract class AbstractUriHandler : RouterNanoHTTPD.UriResponder {
    private fun notAllowed(): Response = newFixedLengthResponse(METHOD_NOT_ALLOWED, "text/plain", METHOD_NOT_ALLOWED.description)
    override fun get(uriResource: UriResource, urlParams: Map<String, String>, session: IHTTPSession) = notAllowed()
    override fun put(uriResource: UriResource, urlParams: Map<String, String>, session: IHTTPSession) = notAllowed()
    override fun post(uriResource: UriResource, urlParams: Map<String, String>, session: IHTTPSession) = notAllowed()
    override fun delete(uriResource: UriResource, urlParams: Map<String, String>, session: IHTTPSession) = notAllowed()
    override fun other(method: String, uriResource: UriResource, urlParams: Map<String, String>, session: IHTTPSession) = notAllowed()
}

internal class AssetHandler : AbstractUriHandler() {
    private val log = logger()
    override fun get(uriResource: UriResource, urlParams: Map<String, String>, session: IHTTPSession): Response {
        log.t { "uriResource=$uriResource, session.uri=${session.uri}, urlParams=[${urlParams.entries.joinToString { "${it.key}=${it.value}" }}]" }
        val mimeType = uriResource.initParameter(String::class.java)
        devFun.devHttpD.context.assets.open(session.uri.substring(1)).bufferedReader().use {
            return newFixedLengthResponse(Response.Status.OK, mimeType, it.readText())
        }
    }
}

internal class InvokeHandler : AbstractUriHandler() {
    private val log = logger()

    override fun post(uriResource: UriResource, urlParams: Map<String, String>, session: IHTTPSession): Response {
        log.t { "uriResource=$uriResource, session.uri=${session.uri}, urlParams=[${urlParams.entries.joinToString { "${it.key}=${it.value}" }}]" }
        val hashCode = urlParams["hashCode"]?.toIntOrNull()
                ?: return response(NOT_FOUND, "Invoke '${urlParams["hashCode"]}' not not found.")

        log.t { devFun.categories.flatMap { it.items }.joinToString { "item[${it.hashCode()}]=$it" } }

        val item = devFun.categories.flatMap { it.items }.firstOrNull { it.hashCode() == hashCode }
                ?: return run notFoundBadRequest@{
                    "${BAD_REQUEST.description}\nItem with hashCode $hashCode not found!".let {
                        log.d { it }
                        response(BAD_REQUEST, it)
                    }
                }

        val invokeWait = CountDownLatch(1)
        try {
            var result: InvokeResult? = null
            devFun.devHttpD.handler.post {
                result = item.call()
                invokeWait.countDown()
            }
            invokeWait.await(5, TimeUnit.SECONDS)

            return if (result != null && result?.exception == null) {
                response(OK, "${result?.value}")
            } else {
                response(INTERNAL_ERROR, "Unexpected error ${result?.exception}")
            }
        } catch (ignore: InterruptedException) {
            return response(REQUEST_TIMEOUT, "Timed out waiting for $item")
        }
    }

    private fun response(status: Response.IStatus, text: String, mimeType: String = "text/plain") =
        newFixedLengthResponse(status, mimeType, "${status.description}\n$text")
}

private val isEmulator by lazy(LazyThreadSafetyMode.NONE) {
    Build.PRODUCT == "sdk" ||
            Build.PRODUCT == "google_sdk_x86" ||
            Build.PRODUCT == "sdk_x86" ||
            Build.PRODUCT == "sdk_google_phone_x86" ||
            Build.PRODUCT.contains("genymotion", ignoreCase = true) ||
            Build.DISPLAY.contains("vbox86p", ignoreCase = true)
}
