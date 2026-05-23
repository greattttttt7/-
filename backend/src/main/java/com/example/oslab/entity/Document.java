package com.example.oslab.entity;

public class Document {
    private Integer docId;
    private Integer labId;
    private String docTitle;
    private String docContent;
    private String docPath;
    private String docTask;
    private String docPrinciple;
    private String docTarget;

    public Integer getDocId() {
        return docId;
    }

    public void setDocId(Integer docId) {
        this.docId = docId;
    }

    public Integer getLabId() {
        return labId;
    }

    public void setLabId(Integer labId) {
        this.labId = labId;
    }

    public String getDocTitle() {
        return docTitle;
    }

    public void setDocTitle(String docTitle) {
        this.docTitle = docTitle;
    }

    public String getDocContent() {
        return docContent;
    }

    public void setDocContent(String docContent) {
        this.docContent = docContent;
    }

    public String getDocPath() {
        return docPath;
    }

    public void setDocPath(String docPath) {
        this.docPath = docPath;
    }

    public String getDocTask() {
        return docTask;
    }

    public void setDocTask(String docTask) {
        this.docTask = docTask;
    }

    public String getDocPrinciple() {
        return docPrinciple;
    }

    public void setDocPrinciple(String docPrinciple) {
        this.docPrinciple = docPrinciple;
    }

    public String getDocTarget() {
        return docTarget;
    }

    public void setDocTarget(String docTarget) {
        this.docTarget = docTarget;
    }
}
