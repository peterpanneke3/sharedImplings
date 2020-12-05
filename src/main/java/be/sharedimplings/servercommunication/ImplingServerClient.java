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

	public ImplingServerClient(URI serverURI, Consumer<String> messageConsumer) {
		super(serverURI);
		this.messageConsumer = messageConsumer;
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		logger.debug("new connection opened");
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
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