package org.samples;


import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;


public class GreetingServiceTest {


    static final String END_POINT = "http://greetings:8080/greetings/rest/hello/%s";


    @Test
    public void testStatusCode() throws Exception {

	URL resource = new URL(String.format(END_POINT, "franck"));
	HttpURLConnection connection = (HttpURLConnection) resource.openConnection();

	int responseCode = connection.getResponseCode();

	assertEquals(200, responseCode);

    }

}
