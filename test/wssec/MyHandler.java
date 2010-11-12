/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package wssec;

import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.handler.WSHandler;
import org.apache.ws.security.handler.RequestData;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;


/**
 * A trivial extension of the WSHandler type for use in unit-testing.
 */
public class MyHandler extends WSHandler {
    
    private Map<String, Object> optionsMap = new HashMap<String, Object>();

    public Object 
    getOption(String key) {
        return optionsMap.get(key);
    }
    
    public void
    setOption(String key, Object option) {
        optionsMap.put(key, option);
    }

    @SuppressWarnings("unchecked")
    public void 
    setProperty(
        Object ctx, 
        String key, 
        Object value
    ) {
        ((java.util.Map<String, Object>)ctx).put(key, value);
    }

    public Object 
    getProperty(Object ctx, String key) {
        if (ctx instanceof java.util.Map<?,?>) {
            return ((java.util.Map<?,?>)ctx).get(key);
        }
        return null;
    }

    public void 
    setPassword(Object msgContext, String password) {
    }

    public String 
    getPassword(Object msgContext) {
        if (msgContext instanceof java.util.Map<?,?>) {
            return (String)((java.util.Map<?,?>)msgContext).get("password");
        }
        return null;
    }

    public void send(
        int action, 
        Document doc,
        RequestData reqData, 
        java.util.List<Integer> actions,
        boolean request
    ) throws org.apache.ws.security.WSSecurityException {
        doSenderAction(
            action, 
            doc, 
            reqData, 
            actions,
            request
        );
    }
    
    public void receive(
        int action, 
        RequestData reqData
    ) throws org.apache.ws.security.WSSecurityException {
        doReceiverAction(
            action, 
            reqData
        );
    }

    public void signatureConfirmation(
        RequestData requestData,
        java.util.List<WSSecurityEngineResult> results
    ) throws org.apache.ws.security.WSSecurityException {
        checkSignatureConfirmation(requestData, results);
    }
    
    public boolean checkResults(
        java.util.List<WSSecurityEngineResult> results,
        java.util.List<Integer> actions
    ) throws org.apache.ws.security.WSSecurityException {
        return checkReceiverResults(results, actions);
    }

    public boolean checkResultsAnyOrder(
        java.util.List<WSSecurityEngineResult> results,
        java.util.List<Integer> actions
    ) throws org.apache.ws.security.WSSecurityException {
        return checkReceiverResultsAnyOrder(results, actions);
    }
    
    
}
