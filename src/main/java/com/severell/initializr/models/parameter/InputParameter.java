package com.severell.initializr.models.parameter;

import com.severell.core.http.Request;
import com.severell.initializr.action.GeneratorException;
import com.severell.initializr.action.structure.StructureGenerator;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.SourceVersion;
import javax.servlet.ServletContext;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InputParameter implements Parameter{
    private final static Logger LOG = LoggerFactory.getLogger(InputParameter.class);
    private Request request = null;

    public InputParameter(Request request) throws GeneratorException {
        this.request = request;
        validateInput();
    }

    @Override
    public String getName(){
        return request.input("name");
    }

    @Override
    public String getVersion(){
        return request.input("version");
    }

    @Override
    public String getArtifactId(){
        return request.input("artifact");
    }

    @Override
    public String getGroupId(){
        return request.input("group");
    }

    @Override
    public String getDescription() {
        return request.input("description");
    }

    public String getSessionHash(){
        return request.getSession().getId().replaceAll("\\W+", "_");
    }

    public ServletContext getContext(){
        return request.getServletContext();
    }

    private void validateInput() throws GeneratorException {
        LOG.info(String.format("validating input  Name:%s Artifact:%s,Group:%s,Version:%s, Description:%s...",
                getName(), getArtifactId(),getGroupId(), getVersion(), getDescription()));
        String exceptions = buildException();
        if(StringUtils.isNotEmpty(exceptions)){
            throw new GeneratorException(exceptions);
        }
    }
    private String buildException(){
        StringBuilder exceptionBuilder = new StringBuilder();
        List<String> inputLexemes = List.of(getName(),getArtifactId(), getGroupId(), getVersion());
        List<List<String>> filteredInputLexemes = inputLexemes.stream().map(e -> {
            return Arrays.stream(e.split("\\.")).filter(SourceVersion::isKeyword).collect(Collectors.toList());
        }).collect(Collectors.toList());

        filteredInputLexemes.forEach(input -> {
                if(input != null && !input.isEmpty()) {
                    exceptionBuilder.append(" Use of Java keyword is forbidden -> ").append(String.join(".", input)).append("\n");
                }
            }
        );

        List<String> startLexemes = List.of(getName(),getArtifactId(), getGroupId());
        startLexemes.parallelStream().forEach(input -> {
            boolean startsWithNumber = input.matches("^(\\d+.*|-)");
            if(startsWithNumber) {
                exceptionBuilder.append(" Can't start input with number digit -> ").append(input).append("\n");
            }
        });
        return String.valueOf(exceptionBuilder);
    }
}
