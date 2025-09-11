/*
 * Copyright 2020 damios
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Note, the above license and copyright applies to this file only.

import com.badlogic.gdx.Version
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3NativesLoader
import org.lwjgl.system.macosx.LibC
import org.lwjgl.system.macosx.ObjCRuntime
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.management.ManagementFactory
import java.util.ArrayList
import org.lwjgl.system.JNI.invokePPP
import org.lwjgl.system.JNI.invokePPZ
import org.lwjgl.system.macosx.ObjCRuntime.objc_getClass
import org.lwjgl.system.macosx.ObjCRuntime.sel_getUid

/**
 * Adds some utilities to ensure that the JVM was started with the
 * {@code -XstartOnFirstThread} argument, which is required on macOS for LWJGL 3
 * to function. Also helps on Windows when users have names with characters from
 * outside the Latin alphabet, a common cause of startup crashes.
 * <br>
 * <a href="https://jvm-gaming.org/t/starting-jvm-on-mac-with-xstartonfirstthread-programmatically/57547">Based on this java-gaming.org post by kappa</a>
 * @author damios
 */
object StartupHelper {

    private const val JVM_RESTARTED_ARG = "jvmIsRestarted"

    init {
        // Prevent instantiation
        throw UnsupportedOperationException()
    }

    /**
     * Starts a new JVM if the application was started on macOS without the
     * {@code -XstartOnFirstThread} argument. This also includes some code for
     * Windows, for the case where the user's home directory includes certain
     * non-Latin-alphabet characters (without this code, most LWJGL3 apps fail
     * immediately for those users). Returns whether a new JVM was started and
     * thus no code should be executed.
     *
     * <u>Usage:</u>
     * ```
     * fun main(args: Array<String>) {
     *   if (StartupHelper.startNewJvmIfRequired(true)) return // This handles macOS support and helps on Windows.
     *   // after this is the actual main method code
     * }
     * ```
     *
     * @param redirectOutput whether the output of the new JVM should be rerouted to the
     * old JVM, so it can be accessed in the same place; keeps the
     * old JVM running if enabled
     * @return whether a new JVM was started and thus no code should be executed
     * in this one
     */
    @JvmStatic
    fun startNewJvmIfRequired(redirectOutput: Boolean): Boolean {
        val osName = System.getProperty("os.name").lowercase()

        if (!osName.contains("mac")) {
            if (osName.contains("windows")) {
                // Here, we are trying to work around an issue with how LWJGL3 loads its extracted .dll files.
                // By default, LWJGL3 extracts to the directory specified by "java.io.tmpdir", which is usually the user's home.
                // If the user's name has non-ASCII (or some non-alphanumeric) characters in it, that would fail.
                // By extracting to the relevant "ProgramData" folder, which is usually "C:\\ProgramData", we avoid this.
                // We also temporarily change the "user.name" property to one without any chars that would be invalid.
                // We revert our changes immediately after loading LWJGL3 natives.
                var programData = System.getenv("ProgramData")
                if (programData == null) programData = "C:\\Temp\\"
                val prevTmpDir = System.getProperty("java.io.tmpdir", programData)
                val prevUser = System.getProperty("user.name", "libGDX_User")
                System.setProperty("java.io.tmpdir", "$programData/libGDX-temp")
                System.setProperty(
                    "user.name",
                    ("User_" + prevUser.hashCode().toString() + "_GDX" + Version.VERSION)
                        .replace('.', '_')
                )
                Lwjgl3NativesLoader.load()
                System.setProperty("java.io.tmpdir", prevTmpDir)
                System.setProperty("user.name", prevUser)
            }
            return false
        }

        // There is no need for -XstartOnFirstThread on Graal native image
        if (System.getProperty("org.graalvm.nativeimage.imagecode", "").isNotEmpty()) {
            return false
        }

        // Checks if we are already on the main thread, such as from running via Construo.
        val objcMsgSend = ObjCRuntime.getLibrary().getFunctionAddress("objc_msgSend")
        val nsThread = objc_getClass("NSThread")
        val currentThread = invokePPP(nsThread, sel_getUid("currentThread"), objcMsgSend)
        val isMainThread = invokePPZ(currentThread, sel_getUid("isMainThread"), objcMsgSend)
        if (isMainThread) return false

        val pid = LibC.getpid()

        // check whether -XstartOnFirstThread is enabled
        if ("1" == System.getenv("JAVA_STARTED_ON_FIRST_THREAD_$pid")) {
            return false
        }

        // check whether the JVM was previously restarted
        // avoids looping, but most certainly leads to a crash
        if ("true" == System.getProperty(JVM_RESTARTED_ARG)) {
            System.err.println(
                "There was a problem evaluating whether the JVM was started with the -XstartOnFirstThread argument."
            )
            return false
        }

        // Restart the JVM with -XstartOnFirstThread
        val jvmArgs = ArrayList<String>()
        val separator = System.getProperty("file.separator", "/")
        // The following line is used assuming you target Java 8, the minimum for LWJGL3.
        val javaExecPath = System.getProperty("java.home") + separator + "bin" + separator + "java"
        // If targeting Java 9 or higher, you could use the following instead of the above line:
        // val javaExecPath = ProcessHandle.current().info().command().orElseThrow()

        if (!File(javaExecPath).exists()) {
            System.err.println(
                "A Java installation could not be found. If you are distributing this app with a bundled JRE, be sure to set the -XstartOnFirstThread argument manually!"
            )
            return false
        }

        jvmArgs.add(javaExecPath)
        jvmArgs.add("-XstartOnFirstThread")
        jvmArgs.add("-D$JVM_RESTARTED_ARG=true")
        jvmArgs.addAll(ManagementFactory.getRuntimeMXBean().inputArguments)
        jvmArgs.add("-cp")
        jvmArgs.add(System.getProperty("java.class.path"))
        var mainClass = System.getenv("JAVA_MAIN_CLASS_$pid")
        if (mainClass == null) {
            val trace = Thread.currentThread().stackTrace
            if (trace.isNotEmpty()) {
                mainClass = trace.last().className
            } else {
                System.err.println("The main class could not be determined.")
                return false
            }
        }
        jvmArgs.add(mainClass)

        try {
            if (!redirectOutput) {
                val processBuilder = ProcessBuilder(jvmArgs)
                processBuilder.start()
            } else {
                val process = ProcessBuilder(jvmArgs)
                    .redirectErrorStream(true)
                    .start()
                val processOutput = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?

                while (processOutput.readLine().also { line = it } != null) {
                    println(line)
                }

                process.waitFor()
            }
        } catch (e: Exception) {
            System.err.println("There was a problem restarting the JVM")
            e.printStackTrace()
        }

        return true
    }

    /**
     * Starts a new JVM if the application was started on macOS without the
     * {@code -XstartOnFirstThread} argument. Returns whether a new JVM was
     * started and thus no code should be executed. Redirects the output of the
     * new JVM to the old one.
     *
     * <u>Usage:</u>
     * ```
     * fun main(args: Array<String>) {
     *   if (StartupHelper.startNewJvmIfRequired()) return // This handles macOS support and helps on Windows.
     *   // the actual main method code
     * }
     * ```
     *
     * @return whether a new JVM was started and thus no code should be executed
     * in this one
     */
    @JvmStatic
    fun startNewJvmIfRequired(): Boolean = startNewJvmIfRequired(true)
}
