package com.example.oslab.mapper;

import com.example.oslab.entity.Document;
import org.apache.ibatis.annotations.Param;

public interface DocumentMapper {
    Document selectByLabId(@Param("labId") Integer labId);
}
