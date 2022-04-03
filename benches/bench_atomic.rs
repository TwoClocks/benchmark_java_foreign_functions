use criterion::*;

use core_affinity::CoreId;
use async_bench::atomic_spin::MappedAtomics;
use async_bench::{CLIENT_CPU, SERVER_CPU};

fn kotlin_unsafe(c: &mut Criterion) {
    // map memory
    let client = MappedAtomics::new(true);

    core_affinity::set_for_current(CoreId { id: CLIENT_CPU });

    let mut child = async_bench::bench_utils::launch_local_java(
        "kotlin/build/libs/kotlin_servers-all.jar",
        "kotlin_servers.UnsafeSpinKt",
        Some(async_bench::bench_utils::JAVA_OPTS.as_ref()),
        vec![SERVER_CPU].as_ref(),
    );

    async_bench::bench_utils::run_bench(c, "atomic_spin", "unsafe", &client);

    client.close();
    child.kill().expect("error killing server process");
}

fn kotlin_foreignMem(c: &mut Criterion) {
    // map memory
    let client = MappedAtomics::new(true);

    core_affinity::set_for_current(CoreId { id: CLIENT_CPU });

    let mut child = async_bench::bench_utils::launch_local_java(
        "kotlin/build/libs/kotlin_servers-all.jar",
        "kotlin_servers.ForeignMemSpinKt",
        Some(async_bench::bench_utils::JAVA_OPTS.as_ref()),
        vec![SERVER_CPU].as_ref(),
    );

    async_bench::bench_utils::run_bench(c, "atomic_spin", "ForeignMem", &client);

    client.close();
    child.kill().expect("error killing server process");
}

fn kotlin_foreignFunc(c: &mut Criterion) {
    // map memory
    let client = MappedAtomics::new(true);

    core_affinity::set_for_current(CoreId { id: CLIENT_CPU });

    let mut child = async_bench::bench_utils::launch_local_java(
        "kotlin/build/libs/kotlin_servers-all.jar",
        "kotlin_servers.ForeignFuncSpinKt",
        Some(async_bench::bench_utils::JAVA_OPTS.as_ref()),
        vec![SERVER_CPU].as_ref(),
    );

    async_bench::bench_utils::run_bench(c, "atomic_spin", "foreignFunc", &client);

    client.close();
    child.kill().expect("error killing server process");
}

fn kotlin_jnaFunc(c: &mut Criterion) {
    // map memory
    let client = MappedAtomics::new(true);

    core_affinity::set_for_current(CoreId { id: CLIENT_CPU });

    let mut child = async_bench::bench_utils::launch_local_java(
        "kotlin/build/libs/kotlin_servers-all.jar",
        "kotlin_servers.JnaSpinKt",
        Some(async_bench::bench_utils::JAVA_OPTS.as_ref()),
        vec![SERVER_CPU].as_ref(),
    );

    async_bench::bench_utils::run_bench(c, "atomic_spin", "janFunc", &client);

    client.close();
    child.kill().expect("error killing server process");
}


criterion_group!(
    benches,
    kotlin_unsafe,
    kotlin_foreignMem,
    kotlin_jnaFunc,
    kotlin_foreignFunc,
);
// criterion_group!(benches, kotlin_bench);
criterion_main!(benches);
