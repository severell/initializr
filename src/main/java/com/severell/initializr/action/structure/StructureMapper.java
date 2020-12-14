package com.severell.initializr.action.structure;

public class StructureMapper {
    private String filename;
    private Integer start;
    private Integer length;
    private CharSequence content;

    StructureMapper(String content, Integer start, Integer length){
         this(null, content, start, length);
    }

    private StructureMapper(String filename, CharSequence content, Integer start, Integer length){
        this.filename = filename;
        this.content = content;
        this.start = start;
        this.length = length;
    }
    public Integer getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public CharSequence getContent() {
        return content;
    }

    public void setContent(CharSequence content) {
        this.content = content;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
