#include "defs.h"
#include "proc.h"
#include "sync.h"

struct mutex *mutex_create(int blocking)
{
	struct proc *p = curr_proc();
	if (p->next_mutex_id >= LOCK_POOL_SIZE) {
		return NULL;
	}
	struct mutex *m = &p->mutex_pool[p->next_mutex_id];
	p->next_mutex_id++;
	m->blocking = blocking;
	m->locked = 0;
	if (blocking) {
		// blocking mutex need wait queue but spinning mutex not
		init_queue(&m->wait_queue, WAIT_QUEUE_MAX_LENGTH,
			   m->_wait_queue_data);
	}
	return m;
}

void mutex_lock(struct mutex *m)
{
	// ==========================================================
	// TODO (Lab5 - Task 26): 实现互斥锁加锁
	// ------------------------------------------------------------
	// 目标: 实现互斥锁的加锁操作，支持自旋锁和阻塞锁两种模式。
	// 提示:
	//   1. 如果锁未被占用（!m->locked），直接获取锁（m->locked = 1）。
	//   2. 如果是自旋锁（!m->blocking），使用 while 循环轮询等待。
	//   3. 如果是阻塞锁（m->blocking），将当前线程加入等待队列。
	//   4. 将线程状态设置为 SLEEPING，调用 sched() 让出CPU。
	// ==========================================================
	//*************************Your code here.****************************
    if (!m->locked) {
		m->locked = 1;
		debugf("lock a free mutex");
		return;
	}
	if (!m->blocking) {
		// spin mutex will just poll
		debugf("try to lock spin mutex");
		while (m->locked) {
			yield();
		}
		debugf("lock spin mutex after some trials");
		return;
	}
	// blocking mutex will wait in the queue
	struct thread *t = curr_thread();
	push_queue(&m->wait_queue, task_to_id(t));
	// don't forget to change thread state to SLEEPING
	t->state = SLEEPING;
	debugf("block to wait for mutex");
	sched();
	debugf("blocking mutex passed to me");
	// here lock is released (with locked = 1) and passed to me, so just do nothing



}

void mutex_unlock(struct mutex *m)
{
	// ==========================================================
	// TODO (Lab5 - Task 27): 实现互斥锁解锁
	// ------------------------------------------------------------
	// 目标: 实现互斥锁的解锁操作，支持自旋锁和阻塞锁两种模式。
	// 提示:
	//   1. 如果是阻塞锁（m->blocking）：
	//      - 从等待队列取出一个等待线程。
	//      - 如果没有等待线程，直接释放锁（m->locked = 0）。
	//      - 如果有等待线程，将其状态设置为 RUNNABLE 并加入就绪队列。
	//   2. 如果是自旋锁，直接释放锁（m->locked = 0）。
	// ==========================================================
	//*************************Your code here.****************************



}

struct semaphore *semaphore_create(int count)
{
	struct proc *p = curr_proc();
	if (p->next_semaphore_id >= LOCK_POOL_SIZE) {
		return NULL;
	}
	struct semaphore *s = &p->semaphore_pool[p->next_semaphore_id];
	p->next_semaphore_id++;
	s->count = count;
	init_queue(&s->wait_queue, WAIT_QUEUE_MAX_LENGTH, s->_wait_queue_data);
	return s;
}

void semaphore_up(struct semaphore *s)
{
	// ==========================================================
	// TODO (Lab5 - Task 28): 实现信号量V操作（up）
	// ------------------------------------------------------------
	// 目标: 增加信号量计数值，如果有等待线程则唤醒一个。
	// 提示:
	//   1. 先增加信号量计数值（s->count++）。
	//   2. 如果增加后 count <= 0，说明有等待线程：
	//      - 从等待队列取出一个线程。
	//      - 将其状态设置为 RUNNABLE 并加入就绪队列。
	// ==========================================================
	//*************************Your code here.****************************



	debugf("semaphore up from %d to %d", s->count - 1, s->count);
}

void semaphore_down(struct semaphore *s)
{
	// ==========================================================
	// TODO (Lab5 - Task 29): 实现信号量P操作（down）
	// ------------------------------------------------------------
	// 目标: 减少信号量计数值，如果资源不足则阻塞等待。
	// 提示:
	//   1. 先减少信号量计数值（s->count--）。
	//   2. 如果减少后 count < 0，说明资源不足：
	//      - 将当前线程加入等待队列。
	//      - 将线程状态设置为 SLEEPING。
	//      - 调用 sched() 让出CPU。
	// ==========================================================
	//*************************Your code here.****************************



	debugf("finish semaphore_down with count = %d", s->count);
}

struct condvar *condvar_create()
{
	struct proc *p = curr_proc();
	if (p->next_condvar_id >= LOCK_POOL_SIZE) {
		return NULL;
	}
	struct condvar *c = &p->condvar_pool[p->next_condvar_id];
	p->next_condvar_id++;
	init_queue(&c->wait_queue, WAIT_QUEUE_MAX_LENGTH, c->_wait_queue_data);
	return c;
}

void cond_signal(struct condvar *cond)
{
	// ==========================================================
	// TODO (Lab5 - Task 30): 实现条件变量通知
	// ------------------------------------------------------------
	// 目标: 唤醒一个等待在该条件变量上的线程。
	// 提示:
	//   1. 从等待队列取出一个线程。
	//   2. 如果有等待线程，将其状态设置为 RUNNABLE 并加入就绪队列。
	//   3. 如果没有等待线程，说明是空信号，不做任何操作。
	// ==========================================================
	//*************************Your code here.****************************



}

void cond_wait(struct condvar *cond, struct mutex *m)
{
	// ==========================================================
	// TODO (Lab5 - Task 31): 实现条件变量等待
	// ------------------------------------------------------------
	// 目标: 等待条件变量，原子性地释放互斥锁并阻塞，唤醒后重新获取锁。
	// 提示:
	//   1. 先调用 mutex_unlock(m) 释放互斥锁。
	//   2. 将当前线程加入条件变量的等待队列。
	//   3. 将线程状态设置为 SLEEPING。
	//   4. 调用 sched() 让出CPU。
	//   5. 被唤醒后，调用 mutex_lock(m) 重新获取互斥锁。
	// ==========================================================
	//*************************Your code here.****************************


	
}
