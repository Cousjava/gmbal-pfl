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

package org.glassfish.dynamic.codegen.impl;

import java.util.List ;
import java.util.ArrayList ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.Set ;
import java.util.HashSet ;

import java.security.AccessController ;
import java.security.PrivilegedAction ;

import java.lang.reflect.Method ;
import java.lang.reflect.Constructor ;

import org.glassfish.dynamic.copyobject.spi.Immutable ;

import org.glassfish.dynamic.codegen.spi.Type ;
import org.glassfish.dynamic.codegen.spi.FieldInfo ;
import org.glassfish.dynamic.codegen.spi.MethodInfo ;
import org.glassfish.dynamic.codegen.spi.ClassInfo ;

import org.glassfish.dynamic.codegen.impl.FieldInfoImpl ;

@Immutable
public class ClassInfoReflectiveImpl extends ClassInfoBase {
    private boolean DEBUG = false ;

    private void dprint( String msg ) {
	System.out.println( "ClassInfoReflectImpl: " + msg ) ;
    }

    public ClassInfoReflectiveImpl( final Type type ) {
	super( type.getTypeClass().getModifiers(), type ) ;

	if (DEBUG)
	    dprint( "Constructor for type " + type ) ;

	assert !type.isPrimitive() ;
	assert !type.isArray() ;

        AccessController.doPrivileged( 
            new PrivilegedAction<Object>() {
                public Object run() {
                    Class<?> cls = type.getTypeClass() ;

                    List<Type> impls = new ArrayList<Type>() ;
                    if (DEBUG) dprint( "Setting interfaces: " ) ;

                    for (Class<?> x : cls.getInterfaces()) {
                        if (DEBUG) dprint( "\t" + x.getName() ) ;

                        impls.add( Type.type(x) ) ;
                    }

                    if (cls.isInterface()) {
                        initializeInterface( impls ) ;
                    } else {
                        Type stype = null ;
                        if (cls.getSuperclass() != null)
                            stype = Type._class( 
                                cls.getSuperclass().getName() ) ;

                        initializeClass( type, stype, impls ) ;
                    }

                    if (DEBUG) dprint( "Setting fields:" ) ;
                    for (java.lang.reflect.Field x : cls.getDeclaredFields()) {
                        if (DEBUG) dprint( "\t" + x.getName() ) ;

                        FieldInfo var = new FieldInfoImpl( 
                            ClassInfoReflectiveImpl.this, x.getModifiers(),
                            Type.type( x.getType()), x.getName() ) ;
                        addFieldInfo( var ) ;
                    }
                    
                    if (DEBUG) dprint( "Setting methods:" ) ;
                    for (Method x : cls.getDeclaredMethods()) {
                        if (DEBUG) dprint( "\t" + x ) ;
                        addMethodInfo( new MethodInfoReflectiveImpl( 
                            ClassInfoReflectiveImpl.this, x )) ;
                    }

                    if (DEBUG) dprint( "Setting constructors:" ) ;
                    for (Constructor x : cls.getDeclaredConstructors()) {
                        if (DEBUG) dprint( "\t" + x ) ;

                        addConstructorInfo( new MethodInfoReflectiveImpl( 
                            ClassInfoReflectiveImpl.this, x )) ;
                    }

                    return null ;
                }
            }
        ) ;
    }
}
