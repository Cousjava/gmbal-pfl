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
package org.glassfish.pfl.dynamic.codegen.impl ;

import java.lang.reflect.Method ;

import java.security.ProtectionDomain ;
import java.security.PrivilegedAction ;
import java.security.AccessController ;
import java.security.Permission ;
import java.lang.reflect.ReflectPermission ;

/** Class used to get a class directly from code generated by
 * a runtime code generator.
 * The code generator extends this base class, and must implement
 * the getClassData method.
 * Most of this is independent of BCEL, but finalizeMethod is
 * specific to the BCEL framework.
 */
public class CodeGeneratorUtil 
{
    private CodeGeneratorUtil() { }

    // Name that Java uses for constructor methods
    public static final String CONSTRUCTOR_METHOD_NAME = "<init>" ;

    // Name that Java uses for a classes static initializer method
    public static final String STATIC_INITIALIZER_METHOD_NAME = "<clinit>" ;

    // A Method for the protected ClassLoader.defineClass method, which we access
    // using reflection.  This requires the supressAccessChecks permission.
    private static final Method defineClassMethod = AccessController.doPrivileged(
	new PrivilegedAction<Method>() {
	    public Method run() {
		try {
		    Method meth = ClassLoader.class.getDeclaredMethod( 
			"defineClass", String.class, 
			byte[].class, int.class, int.class,
			ProtectionDomain.class ) ;
		    meth.setAccessible( true ) ;
		    return meth ;
		} catch (Exception exc) {
		    throw new RuntimeException( 
			"Could not find defineClass method!", exc ) ;
		}
	    } 
	}  
    ) ;

    private static final Permission accessControlPermission = 
	new ReflectPermission( "suppressAccessChecks" ) ;

    // XXX This requires a permission check!
    public static Class<?> makeClass( String name, byte[] def, ProtectionDomain pd,
	ClassLoader loader ) {

	SecurityManager sman = System.getSecurityManager() ;
	if (sman != null)
	    sman.checkPermission( accessControlPermission ) ;

	try {
	    return (Class)defineClassMethod.invoke( loader, 
		name, def, 0, def.length, pd ) ;
	} catch (Exception exc) {
	    throw new RuntimeException( "Could not invoke defineClass!",
		exc ) ;
	}
    }
}
