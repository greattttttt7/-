#include "trap.h"
#include "defs.h"
#include "loader.h"
#include "syscall.h"
#include "timer.h"

extern char trampoline[], uservec[];
extern void *userret(uint64);

void kerneltrap()
{
	if ((r_sstatus() & SSTATUS_SPP) == 0)
		panic("kerneltrap: not from supervisor mode");
	panic("trap from kernel\n");
}

// set up to take exceptions and traps while in the kernel.
void set_usertrap(void)
{
	w_stvec((uint64)uservec & ~0x3); // DIRECT
}

void set_kerneltrap(void)
{
	w_stvec((uint64)kerneltrap & ~0x3); // DIRECT
}

// set up to take exceptions and traps while in the kernel.
void trap_init(void)
{
	set_kerneltrap();
}

void unknown_trap()
{
	errorf("unknown trap: %p, stval = %p\n", r_scause(), r_stval());
	exit(-1);
}

//
// handle an interrupt, exception, or system call from user space.
// called from trampoline.S
//
void usertrap()
{
	set_kerneltrap();
	struct trapframe *trapframe = curr_proc()->trapframe;

	if ((r_sstatus() & SSTATUS_SPP) != 0)
		panic("usertrap: not from user mode");

	uint64 cause = r_scause();
	// ==========================================================
	// TODO (Lab1 - Task 4): 实现时钟中断处理
	// ------------------------------------------------------------
	// 目标: 处理 Supervisor 模式下的定时器中断，实现进程调度。
	// 提示:
	//   1. 中断的 cause 最高位(bit 63)为1，表示是中断(interrupt)，否则是异常(exception)。
	//   2. 使用 cause & (1ULL << 63) 检查是否为中断。
	//   3. 如果是中断，先清除最高位: cause &= ~(1ULL << 63)。
	//   4. 检查 cause 是否为 SupervisorTimer (值为5)。
	//   5. 如果是时钟中断:
	//      - 调用 set_next_timer() 设置下一次中断。
	//      - 调用 yield() 进行进程调度。
	//   6. 如果是其他未处理的中断，调用 unknown_trap()。
	// ==========================================================
	//*************************Your code here.****************************
	
	
	else {
		switch (cause) {
		case UserEnvCall:
			trapframe->epc += 4;
			syscall();
			break;
		case StoreMisaligned:
		case StorePageFault:
		case InstructionMisaligned:
		case InstructionPageFault:
		case LoadMisaligned:
		case LoadPageFault:
			printf("%d in application, bad addr = %p, bad instruction = %p, "
			       "core dumped.\n",
			       cause, r_stval(), trapframe->epc);
			exit(-2);
			break;
		case IllegalInstruction:
			printf("IllegalInstruction in application, core dumped.\n");
			exit(-3);
			break;
		default:
			unknown_trap();
			break;
		}
	}
	usertrapret();
}

//
// return to user space
//
void usertrapret()
{
	set_usertrap();
	struct trapframe *trapframe = curr_proc()->trapframe;
	trapframe->kernel_satp = r_satp(); // kernel page table
	trapframe->kernel_sp =
		curr_proc()->kstack + PGSIZE; // process's kernel stack
	trapframe->kernel_trap = (uint64)usertrap;
	trapframe->kernel_hartid = r_tp(); // hartid for cpuid()

	w_sepc(trapframe->epc);
	// set up the registers that trampoline.S's sret will use
	// to get to user space.

	// set S Previous Privilege mode to User.
	uint64 x = r_sstatus();
	x &= ~SSTATUS_SPP; // clear SPP to 0 for user mode
	x |= SSTATUS_SPIE; // enable interrupts in user mode
	w_sstatus(x);

	// tell trampoline.S the user page table to switch to.
	// uint64 satp = MAKE_SATP(p->pagetable);
	userret((uint64)trapframe);
}