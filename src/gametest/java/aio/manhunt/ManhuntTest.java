package aio.manhunt;

import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.test.TestContext;

import java.lang.reflect.Method;

public class ManhuntTest implements CustomTestMethodInvoker
{
    @Override
    public void invokeTestMethod(TestContext context, Method method) throws ReflectiveOperationException
    {
        method.invoke(this, context);
    }

    @GameTest
    public void test_one(TestContext context)
    {
    }

}
