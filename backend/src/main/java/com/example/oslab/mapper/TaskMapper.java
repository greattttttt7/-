package com.example.oslab.mapper;

import com.example.oslab.entity.Task;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskMapper {
    List<Task> selectByLabId(@Param("labId") Integer labId);

    Task selectById(@Param("taskId") Integer taskId);
}
