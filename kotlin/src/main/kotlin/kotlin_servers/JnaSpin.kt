package kotlin_servers

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import net.openhft.affinity.AffinityLock
import java.io.File
import java.nio.ByteOrder

// import java.nio.ByteBuffer

interface JnaNative : Library {
    fun spinUntilChange(readPtr: Pointer, lastValue: Long): Long
    fun writeChange(writePtr: Pointer, newValue: Long)
}

object JnaSpin {

    val instance = Native.load("nativeFuncs", JnaNative::class.java)

    fun doLoop(cliBuf: Pointer, srvBuf: Pointer) {
        var lastValue = 0L
        while (true) {
            lastValue = instance.spinUntilChange(cliBuf, lastValue)
            instance.writeChange(srvBuf, lastValue)
        }
    }
}

fun main(args: Array<String>) {

    val cpu_num = args.get(0).toInt()

    val (cliBuf, srvBuf) = mapMemory()

    val cliPtr = Pointer(cliBuf.addressForRead(0L))
    val srvPtr = Pointer(srvBuf.addressForRead(0L))

    AffinityLock.acquireLock(cpu_num).use {
        JnaSpin.doLoop(cliPtr, srvPtr)
    }
}
