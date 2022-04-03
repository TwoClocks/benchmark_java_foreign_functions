package kotlin_servers

import jdk.incubator.foreign.CLinker
import jdk.incubator.foreign.FunctionDescriptor
import jdk.incubator.foreign.MemoryAddress
import jdk.incubator.foreign.MemorySegment
import jdk.incubator.foreign.ResourceScope
import jdk.incubator.foreign.SymbolLookup
import jdk.incubator.foreign.ValueLayout
import net.openhft.affinity.AffinityLock
import java.io.File
import java.nio.ByteOrder

// import java.nio.ByteBuffer

object ForeignFuncSpin {

    val scope = ResourceScope.newSharedScope()

    val longLe = ValueLayout.JAVA_LONG.withOrder(ByteOrder.LITTLE_ENDIAN).withName("LongLe")
    val link = CLinker.systemCLinker()
    val symLook = SymbolLookup.loaderLookup()
    val writeChange = link.downcallHandle(
        symLook.lookup("writeChange").get(),
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, longLe)
    )
    val spinUntilChange = link.downcallHandle(
        symLook.lookup("spinUntilChange").get(),
        FunctionDescriptor.of(longLe, ValueLayout.ADDRESS, longLe)
    )

    fun doLoop(cliBuf: MemorySegment, srvBuf: MemorySegment) {
        var lastValue = 0L
        while (true) {
            lastValue = spinUntilChange.invoke(cliBuf, lastValue) as Long
            writeChange.invoke(srvBuf, lastValue)
        }
    }
}

fun main(args: Array<String>) {

    val cpu_num = args.get(0).toInt()

    val (cliBuf, srvBuf) = mapMemory()
    val cliMem = MemorySegment.ofAddress(
        MemoryAddress.ofLong(cliBuf.addressForRead(0L)),
        Long.SIZE_BYTES.toLong(),
        ForeignFuncSpin.scope
    )
    val srvMem = MemorySegment.ofAddress(
        MemoryAddress.ofLong(srvBuf.addressForRead(0L)),
        Long.SIZE_BYTES.toLong(),
        ForeignFuncSpin.scope
    )

    AffinityLock.acquireLock(cpu_num).use {
        ForeignFuncSpin.doLoop(cliMem, srvMem)
    }
}
