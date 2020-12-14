package com.severell.initializr.models.parameter;

import com.severell.core.crypto.PasswordUtils;
import com.severell.core.http.Request;
import com.severell.initializr.action.GeneratorException;
import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import javax.lang.model.SourceVersion;
import javax.servlet.ServletContext;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InputParameter implements Parameter{

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

    public String getSessionHash(){
        return request.getSession().getId().replaceAll("\\W+", "_");
    }

    public ServletContext getContext(){
        return request.getServletContext();
    }

    private void validateInput() throws GeneratorException {
        String exceptions = buildException();
        if(StringUtils.isNotEmpty(exceptions)){
            throw new GeneratorException(exceptions);
        }
    }
    private String buildException(){
        StringBuilder exceptionBuilder = new StringBuilder();
        List<String> inputLexemes = List.of(getName(),getArtifactId(), getGroupId(), getVersion());
        List<String> filteredInputLexemes = inputLexemes.parallelStream().filter(SourceVersion::isKeyword).collect(Collectors.toList());
        if( filteredInputLexemes.size() != inputLexemes.size()){
            filteredInputLexemes.parallelStream().forEach(input -> exceptionBuilder.append(" Use of Java keyword is forbidden -> ").append(input).append("\n"));
        }
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
