/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * 
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 * 
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.glassfish.pfl.test;

/**
 * <p> Interface groups XML constants.
 * Interface that groups all constants used throughout the <tt>XML</tt>
 * documents that are generated by the <tt>XMLJUnitResultFormatter</tt>.
 * <p>
 * As of now the DTD is:
 * <code><pre>
 * &lt;!ELEMENT testsuites (testsuite*)&gt;
 *
 * &lt;!ELEMENT testsuite (properties, testcase*,
 *                    failure?, error?,
 *                     system-out?, system-err?)&gt;
 * &lt;!ATTLIST testsuite name      CDATA #REQUIRED&gt;
 * &lt;!ATTLIST testsuite tests     CDATA #REQUIRED&gt;
 * &lt;!ATTLIST testsuite failures  CDATA #REQUIRED&gt;
 * &lt;!ATTLIST testsuite errors    CDATA #REQUIRED&gt;
 * &lt;!ATTLIST testsuite time      CDATA #REQUIRED&gt;
 * &lt;!ATTLIST testsuite package   CDATA #IMPLIED&gt;
 * &lt;!ATTLIST testsuite id        CDATA #IMPLIED&gt;
 *
 *
 * &lt;!ELEMENT properties (property*)&gt;
 *
 * &lt;!ELEMENT property EMPTY&gt;
 *   &lt;!ATTLIST property name  CDATA #REQUIRED&gt;
 *   &lt;!ATTLIST property value CDATA #REQUIRED&gt;
 *
 * &lt;!ELEMENT testcase (failure?, error?)&gt;
 *   &lt;!ATTLIST testcase name       CDATA #REQUIRED&gt;
 *   &lt;!ATTLIST testcase classname  CDATA #IMPLIED&gt;
 *   &lt;!ATTLIST testcase time       CDATA #REQUIRED&gt;
 *
 * &lt;!ELEMENT failure (#PCDATA)&gt;
 *  &lt;!ATTLIST failure message CDATA #IMPLIED&gt;
 *  &lt;!ATTLIST failure type    CDATA #REQUIRED&gt;
 *
 * &lt;!ELEMENT error (#PCDATA)&gt;
 *   &lt;!ATTLIST error message CDATA #IMPLIED&gt;
 *   &lt;!ATTLIST error type    CDATA #REQUIRED&gt;
 *
 * &lt;!ELEMENT system-err (#PCDATA)&gt;
 *
 * &lt;!ELEMENT system-out (#PCDATA)&gt;
 *
 * </pre></code>
 * @see XMLJUnitResultFormatter
 * @see XMLResultAggregator
 */
public interface XMLConstants {
    /** the testsuites element for the aggregate document */
    String TESTSUITES = "testsuites";

    /** the testsuite element */
    String TESTSUITE = "testsuite";

    /** the testcase element */
    String TESTCASE = "testcase";

    /** the error element */
    String ERROR = "error";

    /** the failure element */
    String FAILURE = "failure";

    /** the system-err element */
    String SYSTEM_ERR = "system-err";

    /** the system-out element */
    String SYSTEM_OUT = "system-out";

    /** package attribute for the aggregate document */
    String ATTR_PACKAGE = "package";

    /** name attribute for property, testcase and testsuite elements */
    String ATTR_NAME = "name";

    /** time attribute for testcase and testsuite elements */
    String ATTR_TIME = "time";

    /** errors attribute for testsuite elements */
    String ATTR_ERRORS = "errors";

    /** failures attribute for testsuite elements */
    String ATTR_FAILURES = "failures";

    /** tests attribute for testsuite elements */
    String ATTR_TESTS = "tests";

    /** type attribute for failure and error elements */
    String ATTR_TYPE = "type";

    /** message attribute for failure elements */
    String ATTR_MESSAGE = "message";

    /** the properties element */
    String PROPERTIES = "properties";

    /** the property element */
    String PROPERTY = "property";

    /** value attribute for property elements */
    String ATTR_VALUE = "value";

    /** classname attribute for testcase elements */
    String ATTR_CLASSNAME = "classname";

    /** id attribute */
    String ATTR_ID = "id";

    /**
     * timestamp of test cases
     */
    String TIMESTAMP = "timestamp";

    /**
     * name of host running the tests
     */
    String HOSTNAME = "hostname";
}
