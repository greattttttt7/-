// Buffer cache.
//
// The buffer cache is a linked list of buf structures holding
// cached copies of disk block contents.  Caching disk blocks
// in memory reduces the number of disk reads and also provides
// a synchronization point for disk blocks used by multiple processes.
//
// Interface:
// * To get a buffer for a particular disk block, call bread.
// * After changing buffer data, call bwrite to write it to disk.
// * When done with the buffer, call brelse.
// * Do not use the buffer after calling brelse.
// * Only one process at a time can use a buffer,
//     so do not keep them longer than necessary.

#include "bio.h"
#include "defs.h"
#include "fs.h"
#include "riscv.h"
#include "types.h"
#include "virtio.h"

struct {
	struct buf buf[NBUF];
	struct buf head;
} bcache;

void binit()
{
	struct buf *b;
	// Create linked list of buffers
	bcache.head.prev = &bcache.head;
	bcache.head.next = &bcache.head;
	for (b = bcache.buf; b < bcache.buf + NBUF; b++) {
		b->next = bcache.head.next;
		b->prev = &bcache.head;
		bcache.head.next->prev = b;
		bcache.head.next = b;
	}
}

// Look through buffer cache for block on device dev.
// If not found, allocate a buffer.
static struct buf *bget(uint dev, uint blockno)
{
	struct buf *b;
	// ==========================================================
	// TODO (Lab4 - Task 22): 实现缓冲区获取
	// ------------------------------------------------------------
	// 目标: 从缓冲区缓存中获取指定设备和块号的缓冲区。
	// 提示:
	//   1. 遍历缓存链表，查找是否已有缓存的块（dev和blockno都匹配）。
	//   2. 如果找到，增加引用计数并返回该缓冲区。
	//   3. 如果未找到，使用LRU策略查找一个空闲缓冲区（refcnt == 0）。
	//   4. 更新缓冲区的dev、blockno，设置valid为0，refcnt为1。
	// ==========================================================
	//*************************Your code here.****************************
	// Is the block already cached?
	

	
	// Not cached.
	// Recycle the least recently used (LRU) unused buffer.
	
	

	panic("bget: no buffers");
	return 0;
}

const int R = 0;
const int W = 1;

// Return a buf with the contents of the indicated block.
struct buf *bread(uint dev, uint blockno)
{
	struct buf *b;
	b = bget(dev, blockno);
	if (!b->valid) {
		virtio_disk_rw(b, R);
		b->valid = 1;
	}
	return b;
}

// Write b's contents to disk.
void bwrite(struct buf *b)
{
	virtio_disk_rw(b, W);
}

// Release a buffer.
// Move to the head of the most-recently-used list.
void brelse(struct buf *b)
{
	b->refcnt--;
	if (b->refcnt == 0) {
		// no one is waiting for it.
		b->next->prev = b->prev;
		b->prev->next = b->next;
		b->next = bcache.head.next;
		b->prev = &bcache.head;
		bcache.head.next->prev = b;
		bcache.head.next = b;
	}
}

void bpin(struct buf *b)
{
	b->refcnt++;
}

void bunpin(struct buf *b)
{
	b->refcnt--;
}
