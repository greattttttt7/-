.align 4
.section .data
.global _initproc_start
.global _initproc_size
.global INIT_PROC
_initproc_start:
    .quad 0
_initproc_size:
    .quad 0
INIT_PROC:
    .byte 117,115,101,114,115,104,101,108,108,0
