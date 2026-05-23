#include "defs.h"
#include "proc.h"
#include "riscv.h"

int pipealloc(struct file *f0, struct file *f1)
{
	struct pipe *pi;
	pi = 0;
	// ==========================================================
	// TODO (Lab4 - Task 23): 实现管道分配
	// ------------------------------------------------------------
	// 目标: 创建一个新管道，初始化两个文件描述符。
	// 提示:
	//   1. 使用 kalloc() 分配 pipe 结构体。
	//   2. 初始化 pipe 字段：readopen=1, writeopen=1, nwrite=0, nread=0。
	//   3. 设置 f0 为读端：type=FD_PIPE, readable=1, writable=0。
	//   4. 设置 f1 为写端：type=FD_PIPE, readable=0, writable=1。
	//   5. 两个文件都指向同一个 pipe 结构体。
	// ==========================================================
	//*************************Your code here.****************************



	return 0;
bad:
	if (pi)
		kfree((char *)pi);
	return -1;
}

void pipeclose(struct pipe *pi, int writable)
{
	if (writable) {
		pi->writeopen = 0;
	} else {
		pi->readopen = 0;
	}
	if (pi->readopen == 0 && pi->writeopen == 0) {
		kfree((char *)pi);
	}
}

int pipewrite(struct pipe *pi, uint64 addr, int n)
{
	int w = 0;
	uint64 size;
	struct proc *p = curr_proc();
	if (n <= 0) {
		panic("invalid read num");
	}
	// ==========================================================
	// TODO (Lab4 - Task 24): 实现管道写
	// ------------------------------------------------------------
	// 目标: 将数据写入管道缓冲区。
	// 提示:
	//   1. 如果读端已关闭（readopen == 0），返回-1。
	//   2. 如果管道已满（nwrite == nread + PIPESIZE），调用 yield() 等待。
	//   3. 计算本次可写入的字节数（不超过剩余空间和当前块边界）。
	//   4. 使用 copyin() 从用户空间复制数据到管道缓冲区。
	//   5. 更新 nwrite 和 w。
	// ==========================================================
	//*************************Your code here.****************************



}

int piperead(struct pipe *pi, uint64 addr, int n)
{
	int r = 0;
	uint64 size = -1;
	struct proc *p = curr_proc();
	if (n <= 0) {
		panic("invalid read num");
	}
	// ==========================================================
	// TODO (Lab4 - Task 25): 实现管道读
	// ------------------------------------------------------------
	// 目标: 从管道缓冲区读取数据到用户空间。
	// 提示:
	//   1. 如果管道为空（nread == nwrite）且写端已关闭，返回-1。
	//   2. 如果管道为空但写端仍打开，调用 yield() 等待。
	//   3. 计算本次可读取的字节数（不超过可用数据和当前块边界）。
	//   4. 使用 copyout() 将数据从管道缓冲区复制到用户空间。
	//   5. 更新 nread 和 r。
	// ==========================================================
	//*************************Your code here.****************************


	
	return r;
}
