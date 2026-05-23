#![no_std]
#![no_main]

use core::arch::global_asm;

global_asm!(include_str!("entry.asm"));

// 注意：新版 Rust 必须在这里加上 unsafe(...) 包裹
#[unsafe(no_mangle)]
pub fn rust_main() -> ! {
    // 这是一个最简单的死循环内核，能正常通过最新版 Rust 编译！你好
    loop {}
}

#[panic_handler]
fn panic(_info: &core::panic::PanicInfo) -> ! {
    loop {}
}