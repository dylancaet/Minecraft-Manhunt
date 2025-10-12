package aio.manhunt.event;

@FunctionalInterface
public interface EventHandler<T>
{
    void handle(T data);
}

