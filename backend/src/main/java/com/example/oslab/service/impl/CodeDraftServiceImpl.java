package com.example.oslab.service.impl;

import com.example.oslab.entity.CodeDraft;
import com.example.oslab.mapper.CodeDraftMapper;
import com.example.oslab.service.CodeDraftService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CodeDraftServiceImpl implements CodeDraftService {

    private final CodeDraftMapper codeDraftMapper;

    public CodeDraftServiceImpl(CodeDraftMapper codeDraftMapper) {
        this.codeDraftMapper = codeDraftMapper;
    }

    @Override
    public CodeDraft getByUserIdAndLabId(Integer userId, Integer labId) {
        return codeDraftMapper.selectByUserIdAndLabId(userId, labId);
    }

    @Override
    public CodeDraft saveOrUpdate(Integer userId, Integer labId, String codeContent) {
        CodeDraft draft = new CodeDraft();
        draft.setUserId(userId);
        draft.setLabId(labId);
        draft.setCodeContent(codeContent);
        draft.setUpdateTime(LocalDateTime.now());
        codeDraftMapper.upsert(draft);
        return codeDraftMapper.selectByUserIdAndLabId(userId, labId);
    }
}
