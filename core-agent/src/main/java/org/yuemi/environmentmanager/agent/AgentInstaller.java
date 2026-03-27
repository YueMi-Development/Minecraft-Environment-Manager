package org.yuemi.environmentmanager.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import org.yuemi.environmentmanager.api.HijackManager;

import java.io.FileInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.lang.instrument.Instrumentation;

public final class AgentInstaller {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[EnvManager-Agent] Initializing Agent...");

        new AgentBuilder.Default()
                .ignore(ElementMatchers.nameStartsWith("net.bytebuddy."))
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .type(ElementMatchers.is(FileInputStream.class))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder
                        .visit(Advice.to(FileInputStreamConstructorAdvice.class).on(ElementMatchers.isConstructor()))
                        .visit(Advice.to(ReadAdvice.class).on(ElementMatchers.named("read").and(ElementMatchers.takesArguments(0))))
                        .visit(Advice.to(ReadBuffer1Advice.class).on(ElementMatchers.named("read").and(ElementMatchers.takesArguments(byte[].class))))
                        .visit(Advice.to(ReadBufferAdvice.class).on(ElementMatchers.named("read").and(ElementMatchers.takesArguments(byte[].class, int.class, int.class))))
                )
                .installOn(inst);
    }

    public static class FileInputStreamConstructorAdvice {
        @Advice.OnMethodExit
        public static void onExit(@Advice.This FileInputStream fis, @Advice.AllArguments Object[] args) {
            if (args.length == 0) return;
            Object arg = args[0];
            String path = null;
            if (arg instanceof String) {
                path = new File((String) arg).getAbsolutePath();
            } else if (arg instanceof File) {
                path = ((File) arg).getAbsolutePath();
            } else if (arg instanceof FileDescriptor) {
                // Cannot easily determine path from FD
                return;
            }

            if (path != null) {
                HijackManager.getInstance().associate(fis, path);
            }
        }
    }

    public static class ReadAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static int onEnter(@Advice.This FileInputStream fis) {
            int result = HijackManager.getInstance().read(fis);
            if (result != -2) {
                return result; // Skip real execution and return our byte (or -1/EOF)
            }
            return 0; // Carry on with normal execution
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.Return(readOnly = false) int returnValue, @Advice.Enter int entryValue) {
            if (entryValue != 0) {
                returnValue = entryValue;
            }
        }
    }

    public static class ReadBufferAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static int onEnter(@Advice.This FileInputStream fis, @Advice.Argument(0) byte[] b, @Advice.Argument(1) int off, @Advice.Argument(2) int len) {
            int result = HijackManager.getInstance().read(fis, b, off, len);
            if (result != -2) {
                return result; 
            }
            return 0;
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.Return(readOnly = false) int returnValue, @Advice.Enter int entryValue) {
            if (entryValue != 0) {
                returnValue = entryValue;
            }
        }
    }

    public static class ReadBuffer1Advice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static int onEnter(@Advice.This FileInputStream fis, @Advice.Argument(0) byte[] b) {
            int result = HijackManager.getInstance().read(fis, b, 0, b.length);
            if (result != -2) {
                return result; 
            }
            return 0;
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.Return(readOnly = false) int returnValue, @Advice.Enter int entryValue) {
            if (entryValue != 0) {
                returnValue = entryValue;
            }
        }
    }
}
