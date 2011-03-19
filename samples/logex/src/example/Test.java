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

/** A simple test example for the annotations-driven logging library.
 * 
 */
public class Test {
    private static final TestExceptions te = TestExceptions.self ;
    private static final TestExceptions tewe = TestExceptionsWithExtension.self ;

    private static void msg( String msg ) {
        System.out.println( msg ) ;
    }

    private static void tryExceptions( TestExceptions te ) {
        te.logOnly( "Foo" ) ;

        RuntimeException first = null ;

        try {
            throw te.anException( 42 ) ;
        } catch (RuntimeException exc) {
            first = exc ;
            msg( "Caught exception: " + exc ) ;
        }

        try {
            throw te.logAndChain( first ) ;
        } catch (RuntimeException exc) {
            msg( "Caught second exception: " + exc ) ;
        }
    }

    public static void main( String[] args ) {
        msg( "Testing TestExceptions" )  ;
        tryExceptions( te ) ;

        msg( "Testing TestExceptionsWithExtension" )  ;
        tryExceptions( tewe ) ;
    }
}
