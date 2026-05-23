package com.example.oslab.service;

import com.example.oslab.entity.CodeDraft;

public interface CodeDraftService {
    CodeDraft getByUserIdAndLabId(Integer userId, Integer labId);

    CodeDraft saveOrUpdate(Integer userId, Integer labId, String codeContent);
}
