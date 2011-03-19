/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package example ;

import org.glassfish.basic.logex.Chain;
import org.glassfish.basic.logex.Log ;
import org.glassfish.basic.logex.Message ;
import org.glassfish.basic.logex.LogLevel ;
import org.glassfish.basic.logex.ExceptionWrapper ;
import org.glassfish.basic.logex.WrapperGenerator ;

import java.io.IOException;

/* A very simple example of an Exceptions interface.
 * This shows the basic use of the various annotations used for logging.
 */
@ExceptionWrapper( idPrefix="TEST" )
public interface TestExceptions {
    TestExceptions self = WrapperGenerator.makeWrapper( 
        TestExceptions.class ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "This is a simple log with 1 parameter {0}" )
    void logOnly( String data ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Log and return an exception with value {0}" )
    RuntimeException anException( int arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Exception occurred in this call" )
    RuntimeException logAndChain( @Chain Exception exc ) ;
}
