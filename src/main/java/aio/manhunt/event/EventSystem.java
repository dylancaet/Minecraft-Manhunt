package aio.manhunt.event;

import aio.manhunt.tracker.TrackerHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class EventSystem
{
    private static EventSystem instance;

    private final Map<EventType, List<EventHandler<?>>> subscribers = new ConcurrentHashMap<>();

    public static synchronized EventSystem getInstance()
    {
        if (instance == null)
            instance = new EventSystem();

        return instance;
    }

    public <T> void subscribe(EventType type, EventHandler<T> handler)
    {
        subscribers.computeIfAbsent(type, t -> new ArrayList<>()).add(handler); /* init subscriber method array */
    }

    public <T> void unsubscribe(EventType type, EventHandler<T> handler)
    {
        List<EventHandler<?>> handlers = subscribers.get(type);
        if (handlers != null)
            handlers.remove(handler);
    }

    public <T> void notify(EventType type, T data)
    {
        List<EventHandler<?>> handlers = subscribers.get(type);

        if (handlers == null)
            return;

        for (EventHandler<?> handler : handlers)
        {
            ((EventHandler<T>) handler).handle(data);
        }
    }

    public void notify(EventType type)
    {
        notify(type, null);
    }
}
