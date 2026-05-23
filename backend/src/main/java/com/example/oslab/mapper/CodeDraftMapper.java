package com.example.oslab.mapper;

import com.example.oslab.entity.CodeDraft;
import org.apache.ibatis.annotations.Param;

public interface CodeDraftMapper {
    CodeDraft selectByUserIdAndLabId(@Param("userId") Integer userId, @Param("labId") Integer labId);

    int upsert(CodeDraft draft);
}
