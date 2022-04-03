const std = @import("std");



pub export fn spinUntilChange( spinPtr:*const u64, lastValue:u64) callconv(.C) u64 {

    var newValue = lastValue;

    while( newValue == lastValue ) {
        std.atomic.spinLoopHint();
        newValue = @atomicLoad(u64, spinPtr, std.builtin.AtomicOrder.Monotonic );
    }
    return newValue;
}

pub export fn writeChange( writePtr:*u64, newValue:u64) callconv(.C) void {
    @atomicStore(u64, writePtr, newValue, std.builtin.AtomicOrder.Monotonic);
}