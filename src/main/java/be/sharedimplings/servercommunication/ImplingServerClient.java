package be.sharedimplings.servercommunication;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import net.runelite.client.eventbus.EventBus;
import net.runelite.http.api.ws.WebsocketGsonFactory;
import net.runelite.http.api.ws.WebsocketMessage;
import net.runelite.http.api.ws.messages.Handshake;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class ImplingServerClient extends WebSocketListener {

	private static final Logger log = LoggerFactory.getLogger(ImplingServerClient.class);

	private final OkHttpClient client;
	private final String serverURI;
	private final ConnectionStateHolder stateHolder;
	private final UUID sessionId;
	private final Gson gson;
	private final EventBus eventBus;

	private WebSocket webSocket;

	public ImplingServerClient(OkHttpClient client, String serverURI, EventBus eventBus, ConnectionStateHolder stateHolder) {
		this.client = client;
		this.serverURI = serverURI;
		this.eventBus = eventBus;
		this.stateHolder = stateHolder;
		this.sessionId = UUID.randomUUID();
		Collection<Class<? extends WebsocketMessage>> messages = new HashSet<>();
		messages.add(ReportImplingSighting.class);
		messages.add(ReportImplingDespawn.class);
		gson = WebsocketGsonFactory.build(WebsocketGsonFactory.factory(messages));
	}

	public void connect()
	{
		stateHolder.setState(ConnectionState.CONNECTING);
		Request request = new Request.Builder()
				.url(serverURI)
				.build();


		webSocket = client.newWebSocket(request, this);

		Handshake handshake = new Handshake();
		handshake.setSession(sessionId);
		send(handshake);
	}



	public void send(WebsocketMessage message)
	{
		if (webSocket == null)
		{
			log.debug("Reconnecting to server");
			connect();
			send(message);
		}else {
			final String json = gson.toJson(message, WebsocketMessage.class);
			webSocket.send(json);
			log.debug("Sent: {}", json);
		}
	}

	public void close()
	{
		if (webSocket != null)
		{
			webSocket.close(1000, null);
		}
	}

	@Override
	public void onOpen(WebSocket webSocket, Response response)
	{
		stateHolder.setState(ConnectionState.CONNECTED);
	}

	@Override
	public void onMessage(WebSocket webSocket, String text)
	{
		final WebsocketMessage message;

		try
		{
			message = gson.fromJson(text, WebsocketMessage.class);
		}
		catch (JsonParseException e)
		{
			log.debug("Failed to deserialize message", e);
			return;
		}

		log.debug("Got: {}", text);
		eventBus.post(message);
	}

	@Override
	public void onClosed(WebSocket webSocket, int code, String reason)
	{
		log.info("Websocket {} closed: {}/{}", webSocket, code, reason);
		this.webSocket = null;
		stateHolder.setState(ConnectionState.DISCONNECTED);
	}

	@Override
	public void onFailure(WebSocket webSocket, Throwable t, Response response)
	{
		log.warn("Error in websocket {}:{}", response, t);
		this.webSocket = null;
		stateHolder.setState(ConnectionState.DISCONNECTED);
	}


}