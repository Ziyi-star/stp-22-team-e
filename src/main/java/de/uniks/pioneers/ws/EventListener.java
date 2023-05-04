package de.uniks.pioneers.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.Event;
import de.uniks.pioneers.service.LoginResultStorage;
import io.reactivex.rxjava3.core.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@Singleton
public class EventListener {

    private final LoginResultStorage loginResultStorage;
    private final ObjectMapper mapper;
    private ClientEndpoint endpoint;

    @Inject
    public EventListener(LoginResultStorage loginResultStorage, ObjectMapper mapper) {
        this.loginResultStorage = loginResultStorage;
        this.mapper = mapper;
    }

    private void ensureOpen() {
        if (endpoint != null) {
            return;
        }

        try {
            endpoint = new ClientEndpoint(new URI(Constants.WS_URL + loginResultStorage.getLoginResult().accessToken()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public <T> Observable<Event<T>> listen(String pattern, Class<T> type) {
        return Observable.create(emitter -> {
            this.ensureOpen();
            send(Map.of("event", "subscribe", "data", pattern));

            final Pattern regex = Pattern.compile(pattern
                    .replace(".", "\\.")
                    .replace("*", "[^.]*"));
            final Consumer<String> handler = eventStr -> {
                try {
                    final JsonNode node = mapper.readTree(eventStr);
                    // Example: event = "games.12345.created"
                    final String event = node.get("event").asText();
                    if (!regex.matcher(event).matches()) {
                        return;
                    }
                    final T data = mapper.treeToValue(node.get("data"), type);
                    emitter.onNext(new Event<>(event, data));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            };
            endpoint.addMessageHandler(handler);
            emitter.setCancellable(() -> removeEventHandler(pattern, handler));
        });
    }

    private void removeEventHandler(String pattern, Consumer<String> handler) {
        if (endpoint == null) {
            return;
        }

        send(Map.of("event", "unsubscribe", "data", pattern));
        endpoint.removeMessageHandler(handler);
        if (!endpoint.hasMessageHandlers()) {
            close();
        }
    }

    public void send(Object message) {
        ensureOpen();

        try {
            final String msg = mapper.writeValueAsString(message);
            endpoint.sendMessage(msg);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void close() {
        if (endpoint != null) {
            endpoint.close();
            endpoint = null;
        }
    }
}
