#include "proc.h"
#include "defs.h"
#include "loader.h"
#include "trap.h"
#include "vm.h"
#include "queue.h"

struct proc pool[NPROC];
__attribute__((aligned(16))) char kstack[NPROC][PAGE_SIZE];
__attribute__((aligned(4096))) char trapframe[NPROC][TRAP_PAGE_SIZE];

extern char boot_stack_top[];
struct proc *current_proc;
struct proc idle;
struct queue task_queue;

int threadid()
{
	return curr_proc()->pid;
}

struct proc *curr_proc()
{
	return current_proc;
}

// initialize the proc table at boot time.
void proc_init()
{
	struct proc *p;
	for (p = pool; p < &pool[NPROC]; p++) {
		p->state = UNUSED;
		p->kstack = (uint64)kstack[p - pool];
		p->trapframe = (struct trapframe *)trapframe[p - pool];
	}
	idle.kstack = (uint64)boot_stack_top;
	idle.pid = IDLE_PID;
	current_proc = &idle;
	init_queue(&task_queue);
}

int allocpid()
{
	static int PID = 1;
	return PID++;
}

struct proc *fetch_task()
{
	int index = pop_queue(&task_queue);
	if (index < 0) {
		debugf("No task to fetch\n");
		return NULL;
	}
	debugf("fetch task %d(pid=%d) to task queue\n", index, pool[index].pid);
	return pool + index;
}

void add_task(struct proc *p)
{
	push_queue(&task_queue, p - pool);
	debugf("add task %d(pid=%d) to task queue\n", p - pool, p->pid);
}

// Look in the process table for an UNUSED proc.
// If found, initialize state required to run in the kernel.
// If there are no free procs, or a memory allocation fails, return 0.
struct proc *allocproc()
{
	struct proc *p;
	for (p = pool; p < &pool[NPROC]; p++) {
		if (p->state == UNUSED) {
			goto found;
		}
	}
	return 0;

found:
	// init proc
	p->pid = allocpid();
	p->state = USED;
	p->ustack = 0;
	p->max_page = 0;
	p->parent = NULL;
	p->exit_code = 0;
	p->pagetable = uvmcreate((uint64)p->trapframe);
	p->program_brk = 0;
        p->heap_bottom = 0;
	memset(&p->context, 0, sizeof(p->context));
	memset((void *)p->kstack, 0, KSTACK_SIZE);
	memset((void *)p->trapframe, 0, TRAP_PAGE_SIZE);
	p->context.ra = (uint64)usertrapret;
	p->context.sp = p->kstack + KSTACK_SIZE;
	return p;
}

// Scheduler never returns.  It loops, doing:
//  - choose a process to run.
//  - swtch to start running that process.
//  - eventually that process transfers control
//    via swtch back to the scheduler.
void scheduler()
{
	struct proc *p;
	for (;;) {
		// ==========================================================
		// TODO (Lab3 - Task 10): 实现调度器主循环
		// ------------------------------------------------------------
		// 目标: 从任务队列中获取进程并切换执行。
		// 提示:
		//   1. 调用 fetch_task() 从任务队列中获取一个可运行的进程。
		//   2. 如果没有可用进程（p == NULL），调用 panic() 结束。
		//   3. 将进程状态设置为 RUNNING。
		//   4. 更新 current_proc 指向当前进程。
		//   5. 调用 swtch(&idle.context, &p->context) 切换到进程执行。
		// ==========================================================
		//*************************Your code here.****************************



	}
}

// Switch to scheduler.  Must hold only p->lock
// and have changed proc->state. Saves and restores
// intena because intena is a property of this
// kernel thread, not this CPU. It should
// be proc->intena and proc->noff, but that would
// break in the few places where a lock is held but
// there's no process.
void sched()
{
	struct proc *p = curr_proc();
	if (p->state == RUNNING)
		panic("sched running");
	swtch(&p->context, &idle.context);
}

// Give up the CPU for one scheduling round.
void yield()
{
	// ==========================================================
	// TODO (Lab3 - Task 11): 实现让出CPU
	// ------------------------------------------------------------
	// 目标: 当前进程让出CPU，进入就绪队列等待下次调度。
	// 提示:
	//   1. 将当前进程状态设置为 RUNNABLE。
	//   2. 调用 add_task() 将当前进程加入任务队列。
	//   3. 调用 sched() 切换到调度器。
	// ==========================================================
	//*************************Your code here.****************************



}

// Free a process's page table, and free the
// physical memory it refers to.
void freepagetable(pagetable_t pagetable, uint64 max_page)
{
	uvmunmap(pagetable, TRAMPOLINE, 1, 0);
	uvmunmap(pagetable, TRAPFRAME, 1, 0);
	uvmfree(pagetable, max_page);
}

void freeproc(struct proc *p)
{
	if (p->pagetable)
		freepagetable(p->pagetable, p->max_page);
	p->pagetable = 0;
	p->state = UNUSED;
}

int fork()
{
	struct proc *np;
	struct proc *p = curr_proc();
	// ==========================================================
	// TODO (Lab3 - Task 12): 实现进程创建(fork)
	// ------------------------------------------------------------
	// 目标: 创建一个新进程，复制父进程的资源。
	// 提示:
	//   1. 调用 allocproc() 分配一个新进程结构。
	//   2. 调用 uvmcopy() 复制父进程的用户内存到子进程。
	//   3. 复制父进程的 max_page 到子进程。
	//   4. 复制父进程的 trapframe 到子进程。
	//   5. 设置子进程的 a0 寄存器为 0（fork在子进程中返回0）。
	//   6. 设置子进程的 parent 指向父进程。
	//   7. 将子进程状态设置为 RUNNABLE。
	//   8. 调用 add_task() 将子进程加入任务队列。
	//   9. 返回子进程的 pid（在父进程中返回）。
	// ==========================================================
	//*************************Your code here.****************************



}

int exec(char *name)
{
	int id = get_id_by_name(name);
	if (id < 0)
		return -1;
	struct proc *p = curr_proc();
	uvmunmap(p->pagetable, 0, p->max_page, 1);
	p->max_page = 0;
	loader(id, p);
	return 0;
}

int wait(int pid, int *code)
{
	struct proc *np;
	int havekids;
	struct proc *p = curr_proc();

	for (;;) {
		// ==========================================================
		// TODO (Lab3 - Task 13): 实现等待子进程(wait)
		// ------------------------------------------------------------
		// 目标: 等待子进程退出，获取子进程的退出码。
		// 提示:
		//   1. 遍历进程池，查找当前进程的子进程。
		//   2. 如果找到 ZOMBIE 状态的子进程：
		//      - 将其状态设置为 UNUSED。
		//      - 保存子进程的 pid 和 exit_code。
		//      - 返回子进程的 pid。
		//   3. 如果没有找到子进程(havekids==0)，返回 -1。
		//   4. 如果有子进程但没有退出，将当前进程加入就绪队列并调度。
		// ==========================================================
		//*************************Your code here.****************************



	}
}

// Exit the current process.
void exit(int code)
{
	struct proc *p = curr_proc();
	// ==========================================================
	// TODO (Lab3 - Task 14): 实现进程退出(exit)
	// ------------------------------------------------------------
	// 目标: 退出当前进程，释放资源并处理子进程。
	// 提示:
	//   1. 设置当前进程的 exit_code。
	//   2. 调用 freeproc() 释放进程资源。
	//   3. 如果有父进程，将状态设置为 ZOMBIE 等待父进程 wait。
	//   4. 将所有子进程的 parent 设置为 NULL（孤儿进程处理）。
	//   5. 调用 sched() 切换到调度器。
	// ==========================================================
	//*************************Your code here.****************************


	
}

// Grow or shrink user memory by n bytes.
// Return 0 on succness, -1 on failure.
int growproc(int n)
{
        uint64 program_brk;
        struct proc *p = curr_proc();
        program_brk = p->program_brk;
        int new_brk = program_brk + n - p->heap_bottom;
        if(new_brk < 0){
                return -1;
        }
        if(n > 0){
                if((program_brk = uvmalloc(p->pagetable, program_brk, program_brk + n, PTE_W)) == 0) {
                        return -1;
                }
        } else if(n < 0){
                program_brk = uvmdealloc(p->pagetable, program_brk, program_brk + n);
        }
        p->program_brk = program_brk;
        return 0;
}
