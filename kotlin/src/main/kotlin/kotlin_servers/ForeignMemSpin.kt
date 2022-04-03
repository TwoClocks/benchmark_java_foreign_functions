package kotlin_servers

import jdk.incubator.foreign.MemoryAddress
import jdk.incubator.foreign.MemorySegment
import jdk.incubator.foreign.ResourceScope
import jdk.incubator.foreign.ValueLayout
import net.openhft.affinity.AffinityLock
import java.nio.ByteOrder

// import java.nio.ByteBuffer

object ForeignMemSpin {

    val scope = ResourceScope.newSharedScope()
    val longLe = ValueLayout.JAVA_LONG.withOrder(ByteOrder.LITTLE_ENDIAN).withName("LongLe")

    fun spinUntilChange(buf: MemorySegment, lastValue: Long): Long {
        var newValue = lastValue
        while (newValue == lastValue) {
            Thread.onSpinWait()
            newValue = buf.get(longLe, 0L)
        }
        return newValue
    }

    fun doLoop(cliBuf: MemorySegment, srvBuf: MemorySegment) {
        var lastValue = 0L
        while (true) {
            lastValue = spinUntilChange(cliBuf, lastValue)
            srvBuf.set(longLe, 0L, lastValue)
        }
    }
}

fun main(args: Array<String>) {

    val cpu_num = args.get(0).toInt()

    val (cliBuf, srvBuf) = mapMemory()
    val cliMem = MemorySegment.ofAddress(
        MemoryAddress.ofLong(cliBuf.addressForRead(0L)),
        Long.SIZE_BYTES.toLong(),
        ForeignMemSpin.scope
    )
    val srvMem = MemorySegment.ofAddress(
        MemoryAddress.ofLong(srvBuf.addressForRead(0L)),
        Long.SIZE_BYTES.toLong(),
        ForeignMemSpin.scope
    )

    AffinityLock.acquireLock(cpu_num).use {
        ForeignMemSpin.doLoop(cliMem, srvMem)
    }
}
