#include "kalloc.h"
#include "defs.h"
#include "riscv.h"

extern char ekernel[];

struct linklist {
	struct linklist *next;
};

struct {
	struct linklist *freelist;
} kmem;

void freerange(void *pa_start, void *pa_end)
{
	char *p;
	p = (char *)PGROUNDUP((uint64)pa_start);
	for (; p + PGSIZE <= (char *)pa_end; p += PGSIZE)
		kfree(p);
}

void kinit()
{
	freerange(ekernel, (void *)PHYSTOP);
}

// Free the page of physical memory pointed at by v,
// which normally should have been returned by a
// call to kalloc().  (The exception is when
// initializing the allocator; see kinit above.)
void kfree(void *pa)
{
	struct linklist *l;
	if (((uint64)pa % PGSIZE) != 0 || (char *)pa < ekernel ||
	    (uint64)pa >= PHYSTOP)
		panic("kfree");
	// ==========================================================
	// TODO (Lab2 - Task 5): 实现物理页面释放
	// ------------------------------------------------------------
	// 目标: 将物理页面添加回空闲链表，供后续分配使用。
	// 提示:
	//   1. 使用 memset(pa, 1, PGSIZE) 填充垃圾数据以检测悬空引用。
	//   2. 将 pa 转换为 linklist 结构体指针。
	//   3. 将新释放的页面插入到空闲链表头部（头插法）。
	//   4. 更新 kmem.freelist 指向新的链表头。
	// ==========================================================
	//*************************Your code here.****************************
	


}

// Allocate one 4096-byte page of physical memory.
// Returns a pointer that the kernel can use.
// Returns 0 if the memory cannot be allocated.
void *kalloc(void)
{
	struct linklist *l;
	// ==========================================================
	// TODO (Lab2 - Task 6): 实现物理页面分配
	// ------------------------------------------------------------
	// 目标: 从空闲链表中分配一个物理页面。
	// 提示:
	//   1. 获取 kmem.freelist 的头节点。
	//   2. 如果空闲链表不为空：
	//      - 将空闲链表头指针指向下一个节点。
	//      - 使用 memset 填充垃圾数据（值为5）。
	//   3. 返回分配的页面指针（如果链表为空则返回0）。
	// ==========================================================
	//*************************Your code here.****************************
	

	
}