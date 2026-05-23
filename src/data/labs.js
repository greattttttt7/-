export const dashboardAnnouncements = [
  '欢迎使用操作系统实验教学平台，请各位同学按时完成实验任务',
  'Lab5 实验已发布，截止日期为2024年12月20日',
  '实验报告提交格式更新通知，请查看最新要求',
]

export const dashboardTodoItems = [
  { label: 'Lab1: 进程管理与调度', done: true },
  { label: 'Lab2: 内存分配算法', done: true },
  { label: 'Lab3: 虚拟存储管理', done: false },
  { label: 'Lab4: 文件系统设计', done: false },
  { label: 'Lab5: 设备驱动程序', done: false },
]

export const dashboardHistoryItems = [
  {
    name: 'Lab2 内存分配算法',
    time: '2024-05-15 14:20',
    status: 'Pass',
    tone: 'success',
  },
  {
    name: 'Lab1 进程管理与调度',
    time: '2024-05-12 09:45',
    status: 'Pass',
    tone: 'success',
  },
  {
    name: 'Lab4 文件系统设计',
    time: '2024-05-10 16:30',
    status: 'Fail',
    tone: 'danger',
  },
  {
    name: 'Lab1 进程管理与调度',
    time: '2024-05-08 11:15',
    status: 'Fail',
    tone: 'danger',
    muted: true,
  },
]

export const labDropdownItems = [
  { label: 'Lab1: 进程管理与调度', to: '/labs/lab1' },
  { label: 'Lab2: 内存分配算法', to: '/labs/lab2' },
  { label: 'Lab3: 虚拟存储管理', to: '/labs/lab3' },
  { label: 'Lab4: 文件系统设计', to: '/labs/lab4' },
  { label: 'Lab5: 设备驱动程序', to: '/labs/lab5' },
]

export const dashboardHeaderNavItems = [
  {
    label: '首页',
    to: '/',
    icon: 'lucide:home',
  },
  {
    label: '实验课程',
    icon: 'lucide:book-open',
    children: labDropdownItems,
  },
  {
    label: '个人中心',
    to: '/profile',
    icon: 'lucide:user',
  },
]

export const guideHeaderNavItems = [
  {
    label: '首页',
    to: '/',
    icon: 'ph:house-bold',
  },
  {
    label: '实验课程',
    icon: 'ph:book-bold',
    children: labDropdownItems,
  },
  {
    label: '个人中心',
    to: '/profile',
    icon: 'ph:user-bold',
  },
]

const createSection = (id, title, paragraphs, points = []) => ({
  id,
  title,
  paragraphs,
  points,
})

const createLabGuide = ({ title, summary, difficulty, duration, startTo, sections }) => ({
  title,
  summary,
  difficulty,
  duration,
  startTo,
  sections,
})

export const labGuides = {
  lab1: createLabGuide({
    title: 'Lab1：进程管理与调度',
    summary: '本实验围绕进程创建、状态转换和调度队列展开，帮助你建立进程管理的基础模型。',
    difficulty: '中等',
    duration: '2小时',
    startTo: '/labs/lab1/editor',
    sections: [
      createSection(
        'objective',
        '实验目的',
        ['理解进程控制块（PCB）的组成，掌握进程状态转换与就绪队列的管理方式。'],
        ['认识创建、就绪、运行、阻塞四种状态', '理解 PCB 中保存的关键字段', '掌握 FCFS 调度的核心流程'],
      ),
      createSection(
        'task',
        '实验任务',
        ['根据实验要求完成进程建模、队列维护和调度输出，并验证不同到达顺序下的执行结果。'],
        ['设计进程数据结构', '实现队列入队/出队逻辑', '输出完整调度过程'],
      ),
      createSection(
        'principle',
        '实验原理',
        ['FCFS 以到达顺序决定执行顺序，属于非抢占式调度，能够直观体现队列调度思想。'],
        ['先进先出', '不发生时间片抢占', '适合观察调度公平性'],
      ),
      createSection(
        'content',
        '实验内容',
        ['按照“建模—调度—输出—分析”的流程完成实验，最终形成完整的实验报告。'],
        ['完成基础样例', '记录运行结果', '整理实验总结'],
      ),
    ],
  }),
  lab2: createLabGuide({
    title: 'Lab2：内存分配算法',
    summary: '本实验聚焦内存分配与回收，帮助你理解空闲分区管理和碎片处理。',
    difficulty: '中等',
    duration: '2小时',
    startTo: '/labs/lab2/editor',
    sections: [
      createSection(
        'objective',
        '实验目的',
        ['理解动态分区分配、空闲链表管理以及内存碎片产生的原因。'],
        ['掌握连续分配的基本思想', '理解外部碎片与内部碎片', '认识内存分配与回收流程'],
      ),
      createSection(
        'task',
        '实验任务',
        ['实现至少一种内存分配策略，并完成分配、释放与合并空闲块的模拟。'],
        ['设计内存块结构', '实现分配与回收算法', '输出分配过程与结果'],
      ),
      createSection(
        'principle',
        '实验原理',
        ['通过空闲区表维护可用内存，常见策略包括首次适应、最佳适应和最坏适应。'],
        ['空闲区按地址或大小排序', '分配后可能产生碎片', '释放时需要考虑合并'],
      ),
      createSection(
        'content',
        '实验内容',
        ['对若干内存申请与释放场景进行模拟，观察不同算法对空间利用率的影响。'],
        ['模拟多次分配/释放', '比较不同策略结果', '总结碎片变化规律'],
      ),
    ],
  }),
  lab3: createLabGuide({
    title: 'Lab3：虚拟存储管理',
    summary: '本实验围绕虚拟存储和页面置换展开，帮助你理解页表、缺页中断与地址映射。',
    difficulty: '进阶',
    duration: '3小时',
    startTo: '/labs/lab3/editor',
    sections: [
      createSection(
        'objective',
        '实验目的',
        ['理解虚拟地址到物理地址的转换过程，并掌握页表与缺页处理的基本机制。'],
        ['认识页、块和页表项', '理解地址转换过程', '掌握缺页中断的处理思路'],
      ),
      createSection(
        'task',
        '实验任务',
        ['完成页表查找、页框分配和页面置换的模拟，实现一个简化的虚拟存储系统。'],
        ['设计页表与页框结构', '实现访问命中/缺页判断', '模拟页面置换策略'],
      ),
      createSection(
        'principle',
        '实验原理',
        ['利用程序访问局部性，将经常访问的页面保留在内存中，减少磁盘访问次数。'],
        ['时间局部性与空间局部性', '页表记录映射关系', '置换策略影响命中率'],
      ),
      createSection(
        'content',
        '实验内容',
        ['按照访问序列运行模拟器，统计命中次数、缺页次数和页面置换过程。'],
        ['输入访问序列', '观察命中/缺页', '分析策略差异'],
      ),
    ],
  }),
  lab4: createLabGuide({
    title: 'Lab4：文件系统设计',
    summary: '本实验聚焦文件系统的组织方式，帮助你理解目录树、文件分配与元数据管理。',
    difficulty: '进阶',
    duration: '3小时',
    startTo: '/labs/lab4/editor',
    sections: [
      createSection(
        'objective',
        '实验目的',
        ['理解文件系统的层次结构、文件控制块和磁盘空间管理的基本思想。'],
        ['认识目录与文件的关系', '理解 inode/FCB 的作用', '掌握文件分配与回收'],
      ),
      createSection(
        'task',
        '实验任务',
        ['实现一个简化文件系统模型，支持目录创建、文件读写与删除等操作。'],
        ['设计目录树结构', '实现文件基本操作', '维护空闲空间信息'],
      ),
      createSection(
        'principle',
        '实验原理',
        ['文件系统通过元数据记录位置与权限信息，并使用块映射管理数据在磁盘上的存放。'],
        ['层次目录结构', '索引分配或链式分配', '空闲块回收与重用'],
      ),
      createSection(
        'content',
        '实验内容',
        ['模拟常见的文件创建、修改与删除场景，观察目录树和磁盘空间的变化。'],
        ['创建目录和文件', '执行读写操作', '整理文件系统实验报告'],
      ),
    ],
  }),
  lab5: createLabGuide({
    title: 'Lab5：设备驱动程序',
    summary: '本实验围绕设备驱动和 I/O 交互展开，帮助你理解中断、缓冲区与请求队列。',
    difficulty: '进阶',
    duration: '3小时',
    startTo: '/labs/lab5/editor',
    sections: [
      createSection(
        'objective',
        '实验目的',
        ['理解操作系统如何通过驱动程序屏蔽硬件差异，并掌握中断驱动 I/O 的基本流程。'],
        ['认识设备与驱动的分层关系', '理解轮询与中断的区别', '掌握缓冲区和请求队列'],
      ),
      createSection(
        'task',
        '实验任务',
        ['设计一个简化的驱动模型，能够处理设备请求、完成回调和状态更新。'],
        ['实现设备请求队列', '模拟中断完成流程', '输出设备处理结果'],
      ),
      createSection(
        'principle',
        '实验原理',
        ['驱动程序负责把高层 I/O 请求转换为具体硬件操作，并在完成后通知上层。'],
        ['驱动与硬件解耦', '请求排队与调度', '缓冲区提升 I/O 效率'],
      ),
      createSection(
        'content',
        '实验内容',
        ['模拟键盘、磁盘或串口等设备的请求处理过程，观察驱动层的工作方式。'],
        ['提交设备请求', '观察完成回调', '记录驱动模拟过程'],
      ),
    ],
  }),
}

export const defaultEditorCode = `def hello_world():
    # 这是一个示例代码
    message = "欢迎来到在线实验平台！"
    print(message)

    for i in range(5):
        print(f"正在执行第 {i + 1} 步...")

hello_world()`

export const defaultEditorOutput = [
  '欢迎来到在线实验平台！',
  '正在执行第 1 步...',
  '正在执行第 2 步...',
  '正在执行第 3 步...',
  '正在执行第 4 步...',
  '正在执行第 5 步...',
]

export const editorFinalLine = '>>> 程序运行结束 (耗时: 0.02s)'
