package com.severell.initializr.models.parameter;

import com.severell.core.http.Request;
import com.severell.initializr.action.GeneratorException;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class InputParameterTest {

    @Test
    public void testInputMethod() throws GeneratorException, IOException {
        Map<String, String> data = new HashMap<>();
        data.put("name", "TestInput");
        data.put("group", "com.sample.test");
        data.put("artifact", "tester");
        data.put("description", "Testing");
        data.put("version", "0.0.1");
        String queryData = data.entrySet()
                .stream()
                .map(entry -> "\"".concat(entry.getKey()).concat("\"").concat(":").concat("\"").concat(entry.getValue()).concat("\""))
                .collect(joining(","));
        HttpServletRequest req = mock(HttpServletRequest.class);
        given(req.getContentType()).willReturn("application/json");
        given(req.getReader()).willReturn(new BufferedReader(new StringReader("{".concat(queryData).concat("}"))));

        HttpSession mockSession = mock(HttpSession.class);
        given(req.getSession()).willReturn(mockSession);
        String expectedVal = "abcd1234";
        String testVal = expectedVal + ".%&*";
        given(mockSession.getId()).willReturn(testVal);

        Request r = new Request(req);
        InputParameter parameter = new InputParameter(r);
        assertEquals(data.get("name"), parameter.getName());
        assertEquals(data.get("group"), parameter.getGroupId());
        assertEquals(data.get("artifact"), parameter.getArtifactId());
        assertEquals(data.get("description"), parameter.getDescription());
        assertEquals(data.get("version"), parameter.getVersion());

        String retVal = parameter.getSessionHash();
        assertEquals(retVal, expectedVal + "_");
    }


    @Test
    public void testInputValidation() throws IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        Request r = new Request(req);
        assertThrows(NullPointerException.class, () -> {new InputParameter(r);});

        Map<String, String> data = new HashMap<>();
        data.put("name", "20");
        data.put("group", "native.while.for");
        data.put("artifact", "if");
        data.put("description", "Testing");
        data.put("version", "0.0.1");
        String queryData = data.entrySet()
                .stream()
                .map(entry -> "\"".concat(entry.getKey()).concat("\"").concat(":").concat("\"").concat(entry.getValue()).concat("\""))
                .collect(joining(","));

        given(req.getContentType()).willReturn("application/json");
        given(req.getReader()).willReturn(new BufferedReader(new StringReader("{".concat(queryData).concat("}"))));
        Request request = new Request(req);

        GeneratorException generatorException = assertThrows(GeneratorException.class, () -> {
            new InputParameter(request);
        });

        String expectedMessage = " Use of Java keyword is forbidden -> if\n Use of Java keyword is forbidden -> native.while.for\n Can't start input with number digit -> 20\n";
        String actualMessage = generatorException.getMessage();

        assertTrue(actualMessage.contains(expectedMessage), String.format("Expected: %s \nGot: %s", expectedMessage, actualMessage));


    }
}
