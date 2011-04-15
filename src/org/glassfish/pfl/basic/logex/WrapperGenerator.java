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

package org.glassfish.pfl.basic.logex;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.glassfish.pfl.basic.algorithm.AnnotationAnalyzer;

import org.glassfish.pfl.basic.proxy.CompositeInvocationHandler;
import org.glassfish.pfl.basic.proxy.CompositeInvocationHandlerImpl;

/** Given an annotated interface, return a Proxy that implements that interface.
 * Interface must be annotated with @ExceptionWrapper( String idPrefix, String loggerName ).
 * id prefix defaults to empty, loggerName defaults to the package name of the annotated
 * class.
 *
 * Also, note that this returned wrapper will always implement the MessageInfo
 * interface, which provides a way to capture all of the messages and IDs used
 * in the interface.  This is used to generate resource bundles. In order for
 * this to work, it is required that the interface declare a field
 *
 * public static final [class name] self = makeWrapper( ... ) ;
 *
 * This is necessary because the extension mechanism allows the construction
 * of message IDs that cannot be predicted based on the annotations alone.
 *
 * The behavior of the implementation of each method on the interface is determined
 * in part by its return type as follows:
 * <ul>
 * <li>void.  Such a method can only log a message. Must have @Log, @Message is
 * optional.</li>
 * <li>String. Such a method may log a message, and also returns the message.
 * Both @Log and @Message are optional.</li>
 * <li>A subclass of Exception.  Such a method may log a message, and also returns
 * an exception containing the message.
 * Both @Log and @Message are optional.
 * </ul>
 *
 * Each method may be annotated as follows:
 *
 * <ul>
 * <li>@Message( String value ).  This defines the message to be placed in a resource
 * bundle (generated at build time by a separate tool).  The key to the resource
 * bundle is <loggerName>.<methodName>.  The message is prepended with the 
 * idPrefix and the id from the @Log annotation (if @Log is present, otherwise nothing
 * is prepended to the message).  If this annotation is not present, a default message
 * is created from the method name and the arguments.
 * <li>@Log( LogLevel level, int id ).  The presence of this annotation indicates that
 * a log record must be generated, and logger IF the appropriate logger is enabled at
 * the given level (note that LogLevel is an enum used for the annotation, each member
 * of which returns the java.util.logging.Level from a getLevel() method).
 * </ul>
 * 
 * In addition, the @Chain annotation may be used on a method parameter (whose type
 * must be a subclass of Throwable) of a method that returns an exception
 * to indicate that the parameter should be the cause of the returned exception.
 * All other method parameters are used as arguments in formatting the message.
 *
 * @author ken
 */
public class WrapperGenerator {
    // XXX Must support @Message without @Log, which in turn means that
    // the resource bundle key must be loggername.methodname, not the logger ID
    // as it presently is in this version.
    // XXX check the CORBA version of logex to see if optimizations are in the
    // pfl version.

    /** Hidden interface implemented by the result of the makeWrapper call.
     * This is needed in the resource file generation tool.
     */
    public interface MessageInfo {
        /** Return a map from message ID to message for all exceptions
         * defined in a @ExceptionWrapper interface.
         * The key in the result is the message ID, and the value is the
         * message string (defined in @Message).
         * @return map from message ID to message.
         */
        Map<String,String> getMessageInfo() ;
    }

    /** Extension API available to override the default behavior of the
     * WrapperGenerator.
     */
    public interface Extension {
        /** Get a message id for this log.
         *
         * @param method The method defining this log.
         * @return The message id.
         */
        String getLogId( Method method ) ;

        /** Construct an exception from the message and the exception type.
         * The method provides access to any additional annotations that may
         * be needed.
         *
         * @param msg The message to use in the exception.
         * @param method The method creating the exception.
         */
        Throwable makeException( String msg, Method method ) ;

        /** Modify the default logger name if needed.
         * 
         * @param cls The standard logger name
         * @return A possibly updated logger name
         */
        String getLoggerName( Class<?> cls );
    }

    /** Convenience base class for implementations of Extension that don't
     * need to override every method.
     */
    public static abstract class ExtensionBase implements Extension {
        @Override
        public String getLogId(Method method) {
            return WrapperGenerator.getStandardLogId(method) ;
        }

        @Override
        public Throwable makeException(String msg, Method method) {
            return WrapperGenerator.makeStandardException(msg, method) ;
        }

        @Override
        public String getLoggerName(Class<?> cls) {
            return WrapperGenerator.getStandardLoggerName( cls ) ;
        }
    }

    // Used whenever there is no user-supplied Extension.
    private static final Extension stdExtension = new ExtensionBase() {} ;

    private static final AnnotationAnalyzer aa = new AnnotationAnalyzer() ;

    private WrapperGenerator() {}

    // Find the outer index in pannos for which the element array
    // contains an annotation of type cls.
    static int findAnnotatedParameter( Annotation[][] pannos,
        Class<? extends Annotation> cls ) {
        for (int ctr1=0; ctr1<pannos.length; ctr1++ ) {
            final Annotation[] annos = pannos[ctr1] ;
            for (int ctr2=0; ctr2< annos.length; ctr2++ ) {
                Annotation anno = annos[ctr2] ;
                if (cls.isInstance(anno)) {
                    return ctr1 ;
                }
            }
        }

        return -1 ;
    }

    static Object[] getWithSkip( Object[] args, int skip ) {
        if (skip >= 0) {
            Object[] result = new Object[args.length-1] ;
            int rindex = 0 ;
            for (int ctr=0; ctr<args.length; ctr++) {
                if (ctr != skip) {
                    result[rindex++] = args[ctr] ;
                }
            }
            return result ;
        } else {
            return args ;
        }
    }

    // Return the key used in the resource bundle.
    private String getMsgKey( Logger logger, Method method ) {
	return logger.getName() + "." + method.getName() ;
    }

    /** Expose the standard log ID for the method.  This is simply
     * the annotated value in the @Log annotation: it is not processed in
     * any way.
     *
     * @param method The method for which the ID is requested.
     * @return The ID (as a string).
     */
    public static String getStandardLogId( Method method ) {
	Log log = aa.getAnnotation( method, Log.class ) ;
        if (log == null) {
            throw new RuntimeException(
                "No Log annotation present for " + method ) ;
        }

        return String.format( "%05d", log.id() ) ;
    }

    static Map<String,String> getMessageMap( Class<?> cls,
        Extension extension ) {

        final Map<String,String> result = new TreeMap<String,String>() ;
        final ExceptionWrapper ew = aa.getAnnotation( cls, ExceptionWrapper.class ) ;
        final String idPrefix = ew.idPrefix() ;

	// A message is defined for every method, even if no annotations are
	// present!
        for (Method method : cls.getMethods()) {
	    // FIXME: need id to be loggername.methodname, not ID (which is not always present)
            final String msgId = extension.getLogId( method ) ;
            final String msg = getMessage( method, idPrefix, msgId ) ;
            result.put( idPrefix + msgId, msg ) ;
        }

        return result ;
    }

    // This should ONLY be used for constructing the message map.
    static String getMessage( Method method, 
        String idPrefix, String logId ) {

        final Message message = aa.getAnnotation( method, Message.class ) ;
        final StringBuilder sb = new StringBuilder() ;
        sb.append( idPrefix ) ;
        sb.append( logId ) ;
        sb.append( ": " ) ;
                    
        if (message == null) {
            sb.append( method.getName() ) ;
            sb.append( ' ' ) ;
            for (int ctr=0; ctr<method.getParameterTypes().length; ctr++) {
                if (ctr>0) {
                    sb.append( ", " ) ;
                }

                sb.append( "arg" ) ;
                sb.append( ctr ) ;
		sb.append("={").append(ctr).append( "}") ;
            }
        } else {
            sb.append( message.value() ) ;
        }

        return sb.toString() ;
    }

    private static final String cihiName = 
        CompositeInvocationHandlerImpl.class.getName() ;

    static Throwable makeStandardException( final String msg,
        final Method method ) {

        Throwable result ;
        final Class<?> rtype = method.getReturnType() ;
        try {
            @SuppressWarnings("unchecked")
            final Constructor<Throwable> cons =
                (Constructor<Throwable>)rtype.getConstructor(String.class);
            result = cons.newInstance(msg);
        } catch (InstantiationException ex) {
            throw new RuntimeException( ex ) ;
        } catch (IllegalAccessException ex) {
            throw new RuntimeException( ex ) ;
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException( ex ) ;
        } catch (InvocationTargetException ex) {
            throw new RuntimeException( ex ) ;
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException( ex ) ;
        } catch (SecurityException ex) {
            throw new RuntimeException( ex ) ;
        }

        return result ;

    }

    static String getStandardLoggerName( Class<?> cls ) {
        final ExceptionWrapper ew = aa.getAnnotation( cls, ExceptionWrapper.class ) ;
        String str = ew.loggerName() ;
        if (str.length() == 0) {
            str = cls.getPackage().getName() ;
        }
        return str ;
    }

    static String handleMessageOnly( Method method, Extension extension,
        Logger logger, Object[] messageParams ) {

        // Just format the message: no exception ID or log level
        // This code is adapted from java.util.logging.Formatter.formatMessage
        final String msg = aa.getAnnotation( method, Message.class).value() ;
        String transMsg ;
        final ResourceBundle catalog = logger.getResourceBundle() ;
        if (catalog == null) {
            transMsg = msg ;
        } else {
            final String logId = extension.getLogId( method ) ;
	    // FIXME: can't use log id here
            transMsg = catalog.getString( logId ) ;
        }

        final String result ;
        if (transMsg.indexOf( "{0" ) >= 0 ) {
            result = java.text.MessageFormat.format( transMsg, messageParams ) ;
        } else {
            result = transMsg ;
        }

        return result ;
    }

    enum ReturnType { EXCEPTION, STRING, NULL } ;

    static ReturnType classifyReturnType( Method method ) {
        Class<?> rtype = method.getReturnType() ;
        if (rtype.equals( void.class ) ) {
            return ReturnType.NULL ;
        } else if (rtype.equals( String.class)) {
            return ReturnType.STRING ;
        } else if (Throwable.class.isAssignableFrom(rtype)) {
            return ReturnType.EXCEPTION ;
        } else {
            throw new RuntimeException( "Method " + method
                + " has an illegal return type" ) ;
        }
    }

    static LogRecord makeLogRecord( Level level, String key,
        Object[] args, Logger logger ) {
        LogRecord result = new LogRecord( level, key ) ;
        if (args != null && args.length > 0) {
            result.setParameters( args ) ;
        }

        result.setLoggerName( logger.getName() ) ;
        result.setResourceBundle( logger.getResourceBundle() ) ;

        return result ;
    }
    
    // Note: This is used ONLY to format the message used in the method
    // result, not in the actual log handler.
    // We define this class simply to re-use the code in formatMessage.
    static class ShortFormatter extends Formatter {
        @Override
        public String format( LogRecord record ) {
            StringBuilder sb = new StringBuilder() ;
            sb.append(record.getLevel().getLocalizedName());
            sb.append(": ");
            String message = formatMessage( record ) ;
            sb.append(message);
            return sb.toString() ;
        }
    }

    final static ShortFormatter formatter = new ShortFormatter() ;

    static void trimStackTrace( Throwable exc, LogRecord lrec ) {
            // Massage exception into appropriate form, and get the caller's
            // class and method.
            final StackTraceElement[] st = exc.getStackTrace() ;
            final List<StackTraceElement> filtered =
                new ArrayList<StackTraceElement>() ;

            boolean skipping = true ;
            for (StackTraceElement ste : st) {
                if (skipping) {
                    if (ste.getClassName().equals( cihiName )
                        && ste.getMethodName().equals( "invoke" )) {
                        skipping = false ;
                    }
                } else {
                    filtered.add( ste ) ;
                }
            }

            exc.setStackTrace( filtered.toArray(
                new StackTraceElement[filtered.size()] ) ) ;

            // First stack element we want is the Proxy$n.exceptionMethod
            // from the exception interface.  Second gives us the
            // caller class and method name.
            StackTraceElement caller = filtered.get(1) ;
            lrec.setSourceClassName( caller.getClassName() );
            lrec.setSourceMethodName( caller.getMethodName() );
    }

    static Object handleFullLogging( Log log, Method method,
        ReturnType rtype, Logger logger,
        String idPrefix, Object[] messageParams, Throwable cause,
        Extension extension )  {

        final Level level = log.level().getLevel() ;

	// FIXME: key is loggername.methodname
        final String msgString = getMessage( method, idPrefix, 
	    extension.getLogId( method )) ;
        final LogRecord lrec = makeLogRecord( level, msgString,
            messageParams, logger ) ;
        final String message = formatter.format( lrec ) ;

        Throwable exc = null ;
        if (rtype == ReturnType.EXCEPTION) {
            exc = extension.makeException( message, method ) ;

	    if (exc != null) {
                trimStackTrace( exc, lrec);
		if (cause != null) {
		    exc.initCause( cause ) ;
		}

		if (level != Level.INFO) {
		    lrec.setThrown( exc ) ;
		}
	    }
        } else {
            // Just do this to correctly set the source class and method name
            // in the log record.
            trimStackTrace( new Throwable(), lrec ) ;
        }

        if (logger.isLoggable(level)) {
            final String context = OperationTracer.getAsString() ;
            String newMsg = msgString ;
            if (context.length() > 0) {
                newMsg += "\nCONTEXT:" + context ;
                lrec.setMessage( newMsg ) ;
            }
            logger.log( lrec ) ;
        }

        switch (rtype) {
            case EXCEPTION : return exc ;
            case STRING : return message ;
            default : return null ;
        }
    }

    /** Given an interface annotated with @ExceptionWrapper, return a proxy
     * implementing the interface.
     *
     * @param <T> The annotated interface type.
     * @param cls The class of the annotated interface.
     * @return An instance of the interface.
     */
    public static <T> T makeWrapper( final Class<T> cls ) {
        return makeWrapper(cls, stdExtension ) ;
    }

    /** Given an interface annotated with @ExceptionWrapper, return a proxy
     * implementing the interface.
     *
     * @param <T> The annotated interface type.
     * @param cls The class of the annotated interface.
     * @param extension The extension instance used to override the default
     * behavior.
     * @return An instance of the interface.
     */
    @SuppressWarnings({"unchecked", "unchecked"})
    public static <T> T makeWrapper( final Class<T> cls,
        final Extension extension ) {

        try {
            // Must have an interface to use a Proxy.
            if (!cls.isInterface()) {
                throw new IllegalArgumentException( "Class " + cls +
                    "is not an interface" ) ;
            }

            final ExceptionWrapper ew = aa.getAnnotation( cls, ExceptionWrapper.class ) ;
            final String idPrefix = ew.idPrefix() ;
            final String name = extension.getLoggerName( cls );

            // Get the logger with the resource bundle if it is available,
            // otherwise without it.  This is needed because sometimes
            // when we load a class to generate a .properties file, the
            // ResourceBundle is (obviously!) not availabe, and a static
            // initializer must initialize a log wrapper WITHOUT a
            // ResourceBundle, in order to generate a properties file which
            // implements the ResourceBundle.
            //
            // Issue GLASSFISH-14269: Do this outside of the construction of the
            // InvocationHandler, because Logger.getLogger is an expensive
            // synchronized call.
            Logger lg = null ;
            try {
                lg = Logger.getLogger( name, name ) ;
            } catch (MissingResourceException exc) {
                lg = Logger.getLogger( name ) ;
            }
            final Logger logger = lg ;

            InvocationHandler inh = new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable {

                    final ReturnType rtype = classifyReturnType(method) ;
                    final Log log = aa.getAnnotation( method, Log.class ) ;

                    // Issue GLASSFISH-14852: If there is no message and no logging
                    // needed, return early and avoid unneeded computation.
                    if (rtype == ReturnType.NULL) {
                        if (log == null)  {
                            return null ;
                        } else {
                            final LogLevel level = log.level() ;
                            if (!logger.isLoggable(level.getLevel())) {
                                return null ;
                            }
                        }
                    }

                    final Annotation[][] pannos =
                        method.getParameterAnnotations() ;
                    final int chainIndex = findAnnotatedParameter( pannos,
                        Chain.class ) ;
                    final Object[] messageParams = getWithSkip( args, 
                         chainIndex ) ;

                    if (log == null) {
                        if (rtype != ReturnType.STRING) {
                            throw new IllegalArgumentException(
                                "No @Log annotation present on "
                                + cls.getName() + "." + method.getName() ) ;
                        }

                        return handleMessageOnly( method, extension, logger,
                            messageParams ) ;
                    } else {
                        Throwable cause = null ;
                        if (chainIndex >= 0) {
                            cause = (Throwable)args[chainIndex] ;
                        }

                        return handleFullLogging( log, method, rtype, logger,
                            idPrefix, messageParams, cause, extension ) ;
                    }
                }
            } ;

            InvocationHandler inhmi = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable {

                    if (method.getName().equals( "getMessageInfo")) {
                        return getMessageMap( cls, extension ) ;
                    }

                    throw new RuntimeException( "Unexpected method " + method ) ;
                }
            } ;

            final CompositeInvocationHandler cih =
                new CompositeInvocationHandlerImpl() {
                    private static final long serialVersionUID =
                        3086904407674824236L;

                    @Override
                    public String toString() {
                        return "ExceptionWrapper[" + cls.getName() + "]" ;
                    }
                } ;

            cih.addInvocationHandler( cls, inh ) ;
            cih.addInvocationHandler( MessageInfo.class, inhmi) ;

            // Load the Proxy using the same ClassLoader that loaded the
            // interface
            ClassLoader loader = cls.getClassLoader() ;
            Class<?>[] classes = { cls, MessageInfo.class } ;
            return (T)Proxy.newProxyInstance(loader, classes, cih ) ;
        } catch (Throwable thr) {
            // This method must NEVER throw an exception, because it is usually
            // called from a static initializer, and uncaught exceptions in
            // static initializers are VERY hard to debug.
            Logger.getLogger( WrapperGenerator.class.getName()).log(Level.SEVERE, 
                "Error in makeWrapper for " + cls, thr );

            return null ;
        }
    }
}
