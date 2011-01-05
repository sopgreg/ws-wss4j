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

package org.apache.ws.security.saml.ext.bean;


/**
 * Class SamlAction represents the raw data required by the <code>AssertionWrapper</code> when
 * creating the <code>Action</code> element of the SAML Authorization Decision Statement.
 *
 * Created on May 19, 2009
 */
public class ActionBean {

    /** 
     * A URI reference representing the namespace in which the name of the specified action is to be
     * interpreted. If this element is absent, the namespace 
     * urn:oasis:names:tc:SAML:1.0:action:rwedcnegation specified in Section 7.2.2 is in effect.  
     */
    private String actionNamespace;

    /** 
     * An action sought to be performed on the specified resource (i.e. Read, Write, Update, Delete) 
     */
    private String contents;

    /**
     * Constructor SamlAction creates a new SamlAction instance.
     */
    public ActionBean() {
    }

    /**
     * Constructor SamlAction creates a new SamlAction instance.
     *
     * @param actionNamespace of type String
     * @param contents of type String
     */
    public ActionBean(String actionNamespace, String contents) {
        this.actionNamespace = actionNamespace;
        this.contents = contents;
    }

    /**
     * Method getActionNamespace returns the actionNamespace of this SamlAction object.
     *
     * @return the actionNamespace (type String) of this SamlAction object.
     */
    public String getActionNamespace() {
        return actionNamespace;
    }

    /**
     * Method setActionNamespace sets the actionNamespace of this SamlAction object.
     *
     * @param actionNamespace the actionNamespace of this SamlAction object.
     */
    public void setActionNamespace(String actionNamespace) {
        this.actionNamespace = actionNamespace;
    }

    /**
     * Method getContents returns the contents of this SamlAction object.
     *
     * @return the contents (type String) of this SamlAction object.
     */
    public String getContents() {
        return contents;
    }

    /**
     * Method setContents sets the contents of this SamlAction object.
     *
     * @param contents the contents of this SamlAction object.
     */
    public void setContents(String contents) {
        this.contents = contents;
    }
}
