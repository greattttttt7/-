package com.example.oslab.service.impl;

import com.example.oslab.entity.Document;
import com.example.oslab.entity.Lab;
import com.example.oslab.entity.Task;
import com.example.oslab.mapper.DocumentMapper;
import com.example.oslab.mapper.LabMapper;
import com.example.oslab.mapper.TaskMapper;
import com.example.oslab.service.LabService;
import com.example.oslab.vo.DocumentVO;
import com.example.oslab.vo.LabDetailVO;
import com.example.oslab.vo.TaskVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LabServiceImpl implements LabService {

    private final LabMapper labMapper;
    private final DocumentMapper documentMapper;
    private final TaskMapper taskMapper;

    public LabServiceImpl(LabMapper labMapper, DocumentMapper documentMapper, TaskMapper taskMapper) {
        this.labMapper = labMapper;
        this.documentMapper = documentMapper;
        this.taskMapper = taskMapper;
    }

    @Override
    public List<Lab> listLabs() {
        return labMapper.selectAll();
    }

    @Override
    public LabDetailVO getLabDetail(Integer labId) {
        Lab lab = labMapper.selectById(labId);
        if (lab == null) {
            return null;
        }

        Document document = documentMapper.selectByLabId(labId);
        List<Task> tasks = taskMapper.selectByLabId(labId);

        LabDetailVO vo = new LabDetailVO();
        vo.setLabId(lab.getLabId());
        vo.setLabCode(lab.getLabCode());
        vo.setLabName(lab.getLabName());
        vo.setLabDesc(lab.getLabDesc());
        vo.setDifficulty(lab.getDifficulty());
        vo.setEstimatedTime(lab.getEstimatedTime());
        vo.setCategory(lab.getCategory());

        if (document != null) {
            DocumentVO documentVO = new DocumentVO();
            documentVO.setDocId(document.getDocId());
            documentVO.setLabId(document.getLabId());
            documentVO.setDocTitle(document.getDocTitle());
            documentVO.setDocContent(document.getDocContent());
            documentVO.setDocTask(document.getDocTask());
            documentVO.setDocPrinciple(document.getDocPrinciple());
            documentVO.setDocTarget(document.getDocTarget());
            documentVO.setDocPath(document.getDocPath());
            vo.setDocument(documentVO);
        }

        List<TaskVO> taskVOList = new ArrayList<>();
        for (Task task : tasks) {
            TaskVO taskVO = new TaskVO();
            taskVO.setTaskId(task.getTaskId());
            taskVO.setLabId(task.getLabId());
            taskVO.setTaskName(task.getTaskName());
            taskVO.setFilePath(task.getFilePath());
            taskVO.setAnswerPath(task.getAnswerPath());
            taskVO.setLineStart(task.getLineStart());
            taskVO.setLineEnd(task.getLineEnd());
            taskVO.setTodoContent(task.getTodoContent());
            taskVOList.add(taskVO);
        }
        vo.setTasks(taskVOList);
        return vo;
    }
}
