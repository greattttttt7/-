package com.example.oslab.mapper;

import com.example.oslab.entity.Lab;
import java.util.List;

public interface LabMapper {
    List<Lab> selectAll();
    Lab selectById(Integer labId);
}
