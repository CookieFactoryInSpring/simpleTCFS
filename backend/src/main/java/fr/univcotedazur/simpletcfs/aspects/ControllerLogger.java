package fr.univcotedazur.simpletcfs.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ControllerLogger {

        private static final Logger LOG = LoggerFactory.getLogger(ControllerLogger.class);
        private static final String PREFIX = "TCFS:Rest-Controller:";

        @Pointcut("execution(public * fr.univcotedazur.simpletcfs.controllers..*(..))")
        private void allControllerMethods() {} // This enables to attach the pointcut to a method name we can reuse below

        @Before("allControllerMethods()")
        public void logMethodNameAndParametersAtEntry(JoinPoint joinPoint) {
            LOG.info(PREFIX + joinPoint.getThis() + ":Called {}", joinPoint.getSignature().getName() + " " + joinPoint.getArgs());
        }

        @AfterReturning(pointcut = "allControllerMethods()", returning = "resultVal")
        public void logMethodReturningProperly(JoinPoint joinPoint, Object resultVal) {
            LOG.info(PREFIX + joinPoint.getThis() + ":Returned {}", joinPoint.getSignature().getName() + " with value " + resultVal);
        }

        @AfterThrowing(pointcut = "allControllerMethods()", throwing = "exception")
        public void logMethodException(JoinPoint joinPoint, Exception exception) {
            LOG.warn(PREFIX + joinPoint.getThis() + ":Exception from {}", joinPoint.getSignature().getName() + " with exception " + exception);
        }

}
