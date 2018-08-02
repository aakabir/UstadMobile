/**
 * Copyright (C) 2009-2012 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ustadmobile.port.gwt.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fusesource.restygwt.client.Resource;


/**
 * A simple example of how you can use the Jackson object mapper reuse the
 * RestyGWT DTOs to process the RestyGWT service requests.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class HelloServlet extends HttpServlet {

    private static final long serialVersionUID = -5364009274470240593L;

    public HelloServlet() {
    	System.out.println("HELLO SERVLET");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        System.out.println("Processing Toppings Listing");
        try {
            ObjectMapper mapper = new ObjectMapper();

            ArrayList<String> all_hellos = new ArrayList<String>();
            all_hellos.add("Hello from the outisdeee");

            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, all_hellos);
            System.out.println("Response: " + sw.toString());

            resp.setContentType(Resource.CONTENT_TYPE_JSON);
            mapper.writeValue(resp.getOutputStream(), all_hellos);

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            System.out.flush();
            System.err.flush();
        }
    }

    
}
