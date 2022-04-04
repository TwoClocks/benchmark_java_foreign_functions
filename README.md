# Bench marks for Java's new foreign functions (and memory)

I wanted to do a quick micro-benchmark comparing Java's new foreign function/memory incubating project with clasic `UNSAFE` and `JNA`

The results:

![](graphs/benches-AMD.png)

The JNA code was around 700 nanos, so it was distoring the graph.

So at least on this machine, the new foreign memory is faster than `UNSAFE`, and the foreign call & return costs about 20 nanoseconds. Not bad.

## details

the benchmark times how long it takes for one process to respond via shared memory. The benchmarking client write to a location in memory, and times how long it takes for the written value to show up in the second location. The code underrstand spins waiting for the value written by the client to change, then writes it to where the client is expecting it. These tests were run on a machine w/ isolated CPUs, and pinned to those CPUs.

I used the open JDK's Java-18 for all tests.
