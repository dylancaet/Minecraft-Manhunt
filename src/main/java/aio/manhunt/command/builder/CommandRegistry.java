package aio.manhunt.command.builder;

import aio.manhunt.Manhunt;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import static org.apache.commons.lang3.reflect.MethodUtils.invokeMethod;

public class CommandRegistry
{
    private static CommandRegistry instance;

    public static synchronized CommandRegistry getInstance()
    {
        if (instance == null)
            instance = new CommandRegistry();

        return instance;
    }

    public void build(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess reigster, CommandManager.RegistrationEnvironment environment)
    {
        LiteralArgumentBuilder<ServerCommandSource> root = CommandManager.literal(Manhunt.MOD_ID);

        var scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages(Manhunt.COMMAND_PACKAGE)
                .scan();

        var annotated = scanResult.getClassesWithAnnotation(Command.class.getName());

        for (ClassInfo classInfo : annotated)
        {
            Class<?> _class = classInfo.loadClass();
            Command meta = _class.getAnnotation(Command.class);
            Object classInstance;

            try {
                classInstance = _class.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(String.format("Unable to create instance of %s", _class.getName()), e);
            }

            LiteralArgumentBuilder<ServerCommandSource> classLiteral = CommandManager.literal(meta.name());

            for (Method method : _class.getDeclaredMethods())
            {
                if (!Modifier.isPublic(method.getModifiers()))
                    continue;

                String methodName = method.getName();
                Parameter[] methodParams = method.getParameters();

                if (methodParams.length == 0 || methodParams[0].getType() != CommandContext.class)
                    continue;

                LiteralArgumentBuilder<ServerCommandSource> methodLiteral = CommandManager.literal(methodName);

                if (methodParams.length == 1) {
                    methodLiteral.executes(context -> invokeCommandMethod(method, classInstance, context));
                }
                else {
                    ArgumentBuilder<ServerCommandSource, ?> argBuilder = buildArgumentChain(methodParams, method, classInstance);
                    methodLiteral.then(argBuilder);
                }

                classLiteral.then(methodLiteral);
            }

            root.then(classLiteral);
        }

        scanResult.close();
        dispatcher.register(root);
    }

    private ArgumentBuilder<ServerCommandSource, ?> buildArgumentChain(Parameter[] params, Method method, Object instance)
    {
        Parameter arg = params[1];
        String argName = arg.getName();
        Class<?> type = arg.getType();

        ArgumentBuilder<ServerCommandSource, ?> argBuilder = CommandManager.argument(argName, brigadierArgFor(type))
                .executes(context -> {
                    Object invokeArg = extractArgValue(context, type, argName);
                    return invokeCommandMethod(method, instance, context, invokeArg);
                });

        return argBuilder;
    }

    private Object extractArgValue(CommandContext<ServerCommandSource> context, Class<?> type, String argName) throws CommandSyntaxException
    {
        if (type == ServerPlayerEntity.class) return EntityArgumentType.getPlayer(context, argName);
        if (type == String.class) return StringArgumentType.getString(context, argName);
        if (type == Integer.class || type == int.class) return IntegerArgumentType.getInteger(context, argName);
        if (type == Boolean.class || type == boolean.class) return BoolArgumentType.getBool(context, argName);
        throw new IllegalArgumentException("Unsupported parameter type: " + type.getName());
    }

    private ArgumentType<?> brigadierArgFor(Class<?> type)
    {
        if (type == ServerPlayerEntity.class) return EntityArgumentType.player();
        if (type == String.class) return StringArgumentType.string();
        if (type == Integer.class || type == int.class) return IntegerArgumentType.integer();
        if (type == Boolean.class || type == boolean.class) return BoolArgumentType.bool();
        throw new IllegalArgumentException("Unsupported parameter type: " + type.getName());
    }

    private int invokeCommandMethod(Method method, Object instance, CommandContext<ServerCommandSource> context, Object... args)
    {
        try {
            Object[] invokeArgs = new Object[args.length + 1];
            invokeArgs[0] = context;
            System.arraycopy(args, 0, invokeArgs, 1, args.length);
            method.invoke(instance, invokeArgs);
            return 1;
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke " + method.getName(), e);
        }
    }

}
