package aio.manhunt.command.builder;

import aio.manhunt.Manhunt;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import lombok.Getter;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

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

            LiteralArgumentBuilder<ServerCommandSource> subcommand = CommandManager.literal(meta.name());

            for (Method method : _class.getDeclaredMethods())
            {
                if (!Modifier.isPublic(method.getModifiers()))
                    continue;

                String methodName = method.getName();
                Parameter[] methodParams = method.getParameters();

                if (methodParams.length == 0 || methodParams[0].getType() != CommandContext.class)
                    continue;

                LiteralArgumentBuilder<ServerCommandSource> subcommandMethod = CommandManager.literal(methodName);

                ArgumentBuilder<ServerCommandSource, ?> argBuilder;

                if (methodParams.length > 1) {
                    Parameter arg = methodParams[1];
                    Class<?> argType = arg.getType();

                    if (argType == ServerPlayerEntity.class) {
                        argBuilder = argument(arg.getName(), EntityArgumentType.player());
                    } else if (argType == String.class) {
                        argBuilder = argument(arg.getName(), StringArgumentType.string());
                    } else if (argType == Integer.class) {
                        argBuilder = argument(arg.getName(), IntegerArgumentType.integer());
                    } else if (argType == Boolean.class) {
                        argBuilder = argument(arg.getName(), BoolArgumentType.bool());
                    } else {
                        throw new IllegalArgumentException("Unsupported parameter type: " + argType.getTypeName());
                    }

                    dispatcher.register(
                            root.then(
                                    subcommand.then(
                                            subcommandMethod.then(
                                                    argBuilder.executes(context -> {

                                                        Object invokeArg;

                                                        if (argType == ServerPlayerEntity.class) {
                                                            invokeArg = EntityArgumentType.getPlayer(context, arg.getName());
                                                        } else if (argType == Boolean.class) {
                                                            invokeArg = BoolArgumentType.getBool(context, arg.getName());
                                                        } else if (argType == Integer.class) {
                                                            invokeArg = IntegerArgumentType.getInteger(context, arg.getName());
                                                        } else {
                                                            invokeArg = StringArgumentType.getString(context, arg.getName());
                                                        }

                                                        try {
                                                            method.invoke(classInstance, context, invokeArg);
                                                        } catch (Exception e) {
                                                            throw new RuntimeException(e);
                                                        }

                                                        return 1;
                                                    })
                                            )
                                    )
                            )
                    );
                }
                else {
                    dispatcher.register(
                            root.then(
                                    subcommand.then(
                                            subcommandMethod.executes(context -> {
                                                try {
                                                    method.invoke(classInstance, context);
                                                } catch (Exception e) {
                                                    throw new RuntimeException(e);
                                                }

                                                return 1;
                                            })
                                    )
                            )
                    );
                }

            }

        }

        scanResult.close();
    }
}
