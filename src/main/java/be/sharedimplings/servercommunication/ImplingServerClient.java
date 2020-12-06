package be.sharedimplings.servercommunication;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.function.Consumer;

public class ImplingServerClient extends WebSocketClient {

	private static final Logger logger = LoggerFactory.getLogger(ImplingServerClient.class);

	private Consumer<String> messageConsumer;

	private ConnectionStateHolder stateHolder;

	public ImplingServerClient(URI serverURI, Consumer<String> messageConsumer, ConnectionStateHolder stateHolder) {
		super(serverURI);
		this.messageConsumer = messageConsumer;
		this.stateHolder = stateHolder;
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		stateHolder.setState(ConnectionState.CONNECTED);
		logger.debug("new connection opened");
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		stateHolder.setState(ConnectionState.DISCONNECTED);
		logger.debug("connection closed");
	}

	@Override
	public void onMessage(String message) {
		logger.debug("received message: " + message);
		messageConsumer.accept(message);
	}

	@Override
	public void onError(Exception ex) {
		logger.error("an error occurred:" ,ex);
	}


	@Override
	public void send(String text) {
		if(isOpen()){
			super.send(text);
		}else{
			logger.info("ignoring send because not connected yet");
		}
	}
}