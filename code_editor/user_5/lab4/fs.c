// File system implementation.  Five layers:
//   + Blocks: allocator for raw disk blocks.
//   + Log: crash recovery for multi-step updates.
//   + Files: inode allocator, reading, writing, metadata.
//   + Directories: inode with special contents (list of other inodes!)
//   + Names: paths like /usr/rtm/xv6/fs.c for convenient naming.
//
// This file contains the low-level file system manipulation
// routines.  The (higher-level) system call implementations
// are in sysfile.c.

#include "fs.h"
#include "bio.h"
#include "defs.h"
#include "file.h"
#include "proc.h"
#include "riscv.h"
#include "types.h"
// there should be one superblock per disk device, but we run with
// only one device
struct superblock sb;

// Read the super block.
static void readsb(int dev, struct superblock *sb)
{
	struct buf *bp;
	bp = bread(dev, 1);
	memmove(sb, bp->data, sizeof(*sb));
	brelse(bp);
}

// Init fs
void fsinit()
{
	int dev = ROOTDEV;
	readsb(dev, &sb);
	if (sb.magic != FSMAGIC) {
		panic("invalid file system");
	}
}

// Zero a block.
static void bzero(int dev, int bno)
{
	struct buf *bp;
	bp = bread(dev, bno);
	memset(bp->data, 0, BSIZE);
	bwrite(bp);
	brelse(bp);
}

// Blocks.

// Allocate a zeroed disk block.
static uint balloc(uint dev)
{
	int b, bi, m;
	struct buf *bp;

	bp = 0;
	// ==========================================================
	// TODO (Lab4 - Task 15): 实现磁盘块分配
	// ------------------------------------------------------------
	// 目标: 从磁盘位图中分配一个空闲块，标记为已使用并清零。
	// 提示:
	//   1. 遍历位图块（每个位图块管理 BPB 个磁盘块）。
	//   2. 使用 BBLOCK(b, sb) 计算第 b 块对应的位图块号。
	//   3. 检查位图中的每一位，找到值为0的空闲位。
	//   4. 将该位设置为1（标记为已使用）。
	//   5. 写回位图块并释放缓冲区。
	//   6. 调用 bzero() 清零新分配的磁盘块。
	//   7. 返回分配的块号。
	// ==========================================================
	//*************************Your code here.****************************



	panic("balloc: out of blocks");
	return 0;
}

// Free a disk block.
static void bfree(int dev, uint b)
{
	struct buf *bp;
	int bi, m;

	// ==========================================================
	// TODO (Lab4 - Task 16): 实现磁盘块释放
	// ------------------------------------------------------------
	// 目标: 将磁盘块标记为空闲，更新位图。
	// 提示:
	//   1. 使用 BBLOCK(b, sb) 计算块 b 对应的位图块号。
	//   2. 读取位图块到缓冲区。
	//   3. 计算块 b 在位图中的位位置（bi = b % BPB）。
	//   4. 检查该位是否已经是0（如果是则panic）。
	//   5. 将该位设置为0（使用 &= ~m）。
	//   6. 写回位图块并释放缓冲区。
	// ==========================================================
	//*************************Your code here.****************************


	
}

//The inode table in memory
struct {
	struct inode inode[NINODE];
} itable;

static struct inode *iget(uint dev, uint inum);

// Allocate an inode on device dev.
// Mark it as allocated by  giving it type `type`.
// Returns an allocated and referenced inode.
struct inode *ialloc(uint dev, short type)
{
	int inum;
	struct buf *bp;
	struct dinode *dip;

	for (inum = 1; inum < sb.ninodes; inum++) {
		bp = bread(dev, IBLOCK(inum, sb));
		dip = (struct dinode *)bp->data + inum % IPB;
		if (dip->type == 0) { // a free inode
			memset(dip, 0, sizeof(*dip));
			dip->type = type;
			bwrite(bp);
			brelse(bp);
			return iget(dev, inum);
		}
		brelse(bp);
	}
	panic("ialloc: no inodes");
	return 0;
}

// Copy a modified in-memory inode to disk.
// Must be called after every change to an ip->xxx field
// that lives on disk.
void iupdate(struct inode *ip)
{
	struct buf *bp;
	struct dinode *dip;

	bp = bread(ip->dev, IBLOCK(ip->inum, sb));
	dip = (struct dinode *)bp->data + ip->inum % IPB;
	dip->type = ip->type;
	dip->size = ip->size;
	// LAB4: you may need to update link count here
	memmove(dip->addrs, ip->addrs, sizeof(ip->addrs));
	bwrite(bp);
	brelse(bp);
}

// Find the inode with number inum on device dev
// and return the in-memory copy. Does not read
// it from disk.
static struct inode *iget(uint dev, uint inum)
{
	struct inode *ip, *empty;
	// Is the inode already in the table?
	empty = 0;
	for (ip = &itable.inode[0]; ip < &itable.inode[NINODE]; ip++) {
		if (ip->ref > 0 && ip->dev == dev && ip->inum == inum) {
			ip->ref++;
			return ip;
		}
		if (empty == 0 && ip->ref == 0) // Remember empty slot.
			empty = ip;
	}

	// Recycle an inode entry.
	if (empty == 0)
		panic("iget: no inodes");

	ip = empty;
	ip->dev = dev;
	ip->inum = inum;
	ip->ref = 1;
	ip->valid = 0;
	return ip;
}

// Increment reference count for ip.
// Returns ip to enable ip = idup(ip1) idiom.
struct inode *idup(struct inode *ip)
{
	ip->ref++;
	return ip;
}

// Reads the inode from disk if necessary.
void ivalid(struct inode *ip)
{
	struct buf *bp;
	struct dinode *dip;
	if (ip->valid == 0) {
		bp = bread(ip->dev, IBLOCK(ip->inum, sb));
		dip = (struct dinode *)bp->data + ip->inum % IPB;
		ip->type = dip->type;
		ip->size = dip->size;
		// LAB4: You may need to get lint count here
		memmove(ip->addrs, dip->addrs, sizeof(ip->addrs));
		brelse(bp);
		ip->valid = 1;
		if (ip->type == 0)
			panic("ivalid: no type");
	}
}

// Drop a reference to an in-memory inode.
// If that was the last reference, the inode table entry can
// be recycled.
// If that was the last reference and the inode has no links
// to it, free the inode (and its content) on disk.
// All calls to iput() must be inside a transaction in
// case it has to free the inode.
void iput(struct inode *ip)
{
	// LAB4: Unmark the condition and change link count variable name (nlink) if needed
	if (ip->ref == 1 && ip->valid && 0 /*&& ip->nlink == 0*/) {
		// inode has no links and no other references: truncate and free.
		itrunc(ip);
		ip->type = 0;
		iupdate(ip);
		ip->valid = 0;
	}
	ip->ref--;
}

// Inode content
//
// The content (data) associated with each inode is stored
// in blocks on the disk. The first NDIRECT block numbers
// are listed in ip->addrs[].  The next NINDIRECT blocks are
// listed in block ip->addrs[NDIRECT].

// Return the disk block address of the nth block in inode ip.
// If there is no such block, bmap allocates one.
static uint bmap(struct inode *ip, uint bn)
{
	uint addr, *a;
	struct buf *bp;

	// ==========================================================
	// TODO (Lab4 - Task 17): 实现块映射
	// ------------------------------------------------------------
	// 目标: 根据逻辑块号获取对应的物理磁盘块号，必要时分配新块。
	// 提示:
	//   1. 如果 bn < NDIRECT：直接使用 ip->addrs[bn]。
	//   2. 如果地址为0，调用 balloc() 分配新块并更新 addrs。
	//   3. 如果 bn >= NDIRECT：使用间接块。
	//   4. 先检查 ip->addrs[NDIRECT] 是否存在，不存在则分配。
	//   5. 读取间接块，检查对应条目，不存在则分配。
	// ==========================================================
	//*************************Your code here.****************************



		brelse(bp);
		return addr;
	}

	panic("bmap: out of range");
	return 0;
}

// Truncate inode (discard contents).
void itrunc(struct inode *ip)
{
	int i, j;
	struct buf *bp;
	uint *a;

	for (i = 0; i < NDIRECT; i++) {
		if (ip->addrs[i]) {
			bfree(ip->dev, ip->addrs[i]);
			ip->addrs[i] = 0;
		}
	}

	if (ip->addrs[NDIRECT]) {
		bp = bread(ip->dev, ip->addrs[NDIRECT]);
		a = (uint *)bp->data;
		for (j = 0; j < NINDIRECT; j++) {
			if (a[j])
				bfree(ip->dev, a[j]);
		}
		brelse(bp);
		bfree(ip->dev, ip->addrs[NDIRECT]);
		ip->addrs[NDIRECT] = 0;
	}

	ip->size = 0;
	iupdate(ip);
}

// Read data from inode.
// If user_dst==1, then dst is a user virtual address;
// otherwise, dst is a kernel address.
int readi(struct inode *ip, int user_dst, uint64 dst, uint off, uint n)
{
	uint tot, m;
	struct buf *bp;

	// ==========================================================
	// TODO (Lab4 - Task 18): 实现inode数据读取
	// ------------------------------------------------------------
	// 目标: 从inode读取数据到指定地址。
	// 提示:
	//   1. 检查参数合法性：off > ip->size 或 off + n < off 返回0。
	//   2. 如果 off + n > ip->size，调整 n 为剩余大小。
	//   3. 循环读取每个块：使用 bmap() 获取物理块号。
	//   4. 使用 bread() 读取块到缓冲区。
	//   5. 计算本次读取的字节数 m（不超过块大小）。
	//   6. 使用 either_copyout() 将数据复制到目标地址。
	//   7. 释放缓冲区，继续下一块。
	// ==========================================================
	//*************************Your code here.****************************



}

// Write data to inode.
// Caller must hold ip->lock.
// If user_src==1, then src is a user virtual address;
// otherwise, src is a kernel address.
// Returns the number of bytes successfully written.
// If the return value is less than the requested n,
// there was an error of some kind.
int writei(struct inode *ip, int user_src, uint64 src, uint off, uint n)
{
	uint tot, m;
	struct buf *bp;

	// ==========================================================
	// TODO (Lab4 - Task 19): 实现inode数据写入
	// ------------------------------------------------------------
	// 目标: 将数据写入inode的指定位置。
	// 提示:
	//   1. 检查参数合法性：off > ip->size 或 off + n < off 返回-1。
	//   2. 如果 off + n > MAXFILE * BSIZE 返回-1（超出最大文件大小）。
	//   3. 循环写入每个块：使用 bmap() 获取/分配物理块号。
	//   4. 使用 bread() 读取块到缓冲区。
	//   5. 计算本次写入的字节数 m（不超过块大小）。
	//   6. 使用 either_copyin() 从源地址复制数据到缓冲区。
	//   7. 使用 bwrite() 写回磁盘，然后释放缓冲区。
	//   8. 更新 ip->size 并调用 iupdate() 写回 inode。
	// ==========================================================
	//*************************Your code here.****************************

	
	

}

// Look for a directory entry in a directory.
// If found, set *poff to byte offset of entry.
struct inode *dirlookup(struct inode *dp, char *name, uint *poff)
{
	uint off, inum;
	struct dirent de;

	// ==========================================================
	// TODO (Lab4 - Task 20): 实现目录查找
	// ------------------------------------------------------------
	// 目标: 在目录中查找指定名称的文件，返回对应的inode。
	// 提示:
	//   1. 检查 dp 是否为目录类型（T_DIR），否则panic。
	//   2. 遍历目录中的所有条目（每个条目大小为 sizeof(de)）。
	//   3. 使用 readi() 读取目录条目到 de。
	//   4. 如果 de.inum == 0，跳过该条目（空闲项）。
	//   5. 使用 strncmp() 比较文件名。
	//   6. 如果找到匹配，设置 *poff（如果非NULL）并返回对应的inode。
	// ==========================================================
	//*************************Your code here.****************************



	return 0;
}

//Show the filenames of all files in the directory
int dirls(struct inode *dp)
{
	uint64 off, count;
	struct dirent de;

	if (dp->type != T_DIR)
		panic("dirlookup not DIR");

	count = 0;
	for (off = 0; off < dp->size; off += sizeof(de)) {
		if (readi(dp, 0, (uint64)&de, off, sizeof(de)) != sizeof(de))
			panic("dirlookup read");
		if (de.inum == 0)
			continue;
		printf("%s\n", de.name);
		count++;
	}
	return count;
}

// Write a new directory entry (name, inum) into the directory dp.
int dirlink(struct inode *dp, char *name, uint inum)
{
	int off;
	struct dirent de;
	struct inode *ip;
	// ==========================================================
	// TODO (Lab4 - Task 21): 实现目录链接
	// ------------------------------------------------------------
	// 目标: 在目录中添加新的目录条目。
	// 提示:
	//   1. 使用 dirlookup() 检查名称是否已存在，存在则返回-1。
	//   2. 遍历目录查找空条目（de.inum == 0）。
	//   3. 如果找到空条目，使用 strncpy() 复制文件名。
	//   4. 设置 de.inum 为新文件的 inode 号。
	//   5. 使用 writei() 将目录条目写回目录。
	// ==========================================================
	//*************************Your code here.****************************


	
}

// LAB4: You may want to add dirunlink here

//Return the inode of the root directory
struct inode *root_dir()
{
	struct inode *r = iget(ROOTDEV, ROOTINO);
	ivalid(r);
	return r;
}

//Find the corresponding inode according to the path
struct inode *namei(char *path)
{
	int skip = 0;
	// if(path[0] == '.' && path[1] == '/')
	//     skip = 2;
	// if (path[0] == '/') {
	//     skip = 1;
	// }
	struct inode *dp = root_dir();
	if (dp == 0)
		panic("fs dumped.\n");
	return dirlookup(dp, path + skip, 0);
}
