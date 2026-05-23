#include "timer.h"
#include "riscv.h"
#include "sbi.h"

/// read the `mtime` regiser
uint64 get_cycle()
{
	return r_time();
}

/// Enable timer interrupt
void timer_init()
{
	// ==========================================================
	// TODO (Lab1 - Task 2): 实现定时器中断初始化
	// ------------------------------------------------------------
	// 目标: 启用 Supervisor 模式下的定时器中断。
	// 提示:
	//   1. 使用 w_sie() 和 r_sie() 来设置 Supervisor Interrupt Enable 寄存器。
	//   2. 需要启用 SIE_STIE (Supervisor Timer Interrupt Enable) 位。
	//   3. 设置完中断使能后，调用 set_next_timer() 设置第一次定时器中断。我很好，你好
	// ==========================================================
	// Enable supervisor timer interrupt
	//*************************Your code here.****************************



}

/// Set the next timer interrupt
void set_next_timer()
{
	// ==========================================================
	// TODO (Lab1 - Task 3): 实现设置下一次时钟中断
	// ------------------------------------------------------------
	// 目标: 计算并设置下一次定时器中断的触发时间。
	// 提示:
	//   1. 使用 CPU_FREQ / TICKS_PER_SEC 计算每次中断的时间间隔(timebase)。
	//   2. 使用 get_cycle() 获取当前时间。
	//   3. 调用 set_timer() 设置下一次中断时间为当前时间 + timebase。
	//   4. CPU_FREQ 定义了 CPU 频率，TICKS_PER_SEC 定义了每秒中断次数。
	// ==========================================================
	//*************************Your code here.****************************

	
	
}