# ==================== 阶段 1: 编译 QEMU 7.0.0 ====================
# 这里给本地已经存在的 ubuntu:20.04 起一个绝对安全的本地别名：base_image
FROM ubuntu:20.04 AS base_image

FROM base_image AS build_qemu
ARG QEMU_VERSION=7.0.0

RUN sed -i 's/archive.ubuntu.com/mirrors.tuna.tsinghua.edu.cn/g' /etc/apt/sources.list && \ 
    sed -i 's/security.ubuntu.com/mirrors.tuna.tsinghua.edu.cn/g' /etc/apt/sources.list && \ 
    apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y wget build-essential libglib2.0-dev libfdt-dev libpixman-1-dev zlib1g-dev ninja-build

RUN wget https://download.qemu.org/qemu-${QEMU_VERSION}.tar.xz && \
    tar xf qemu-${QEMU_VERSION}.tar.xz && \
    cd qemu-${QEMU_VERSION} && \
    ./configure --target-list=riscv64-softmmu,riscv64-linux-user && \
    make -j$(nproc) && \
    make install

# ==================== 阶段 2: 构建纯正的 C 语言 uCore 实验环境 ====================
# 极其重要：这里不再写 ubuntu:20.04，而是直接写上面定义好的本地别名 base_image！
# 这样 Docker 100% 不会再去联网，直接闭眼使用本地已经缓存好的环境。
FROM base_image

# 换清华源，并安装 C 语言开发必备工具链及 RISC-V 交叉编译器 gcc
RUN sed -i 's/archive.ubuntu.com/mirrors.tuna.tsinghua.edu.cn/g' /etc/apt/sources.list && \
    sed -i 's/security.ubuntu.com/mirrors.tuna.tsinghua.edu.cn/g' /etc/apt/sources.list && \
    apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y \
    build-essential \
    git \
    make \
    # 核心：安装 RISC-V 64位 C 语言交叉编译器和工具链
    gcc-riscv64-unknown-elf \
    binutils-riscv64-unknown-elf \
    # 调试与验证工具
    gdb-multiarch \
    && rm -rf /var/lib/apt/lists/*

# 从阶段 1 把编译好的 QEMU 复制过来
COPY --from=build_qemu /usr/local/bin/* /usr/local/bin

# 设置工作目录
WORKDIR /home/ucore

# 配合 Java 后端的纯本地覆盖机制
COPY . .