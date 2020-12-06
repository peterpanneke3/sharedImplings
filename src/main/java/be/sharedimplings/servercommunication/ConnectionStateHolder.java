package be.sharedimplings.servercommunication;

import lombok.Getter;

import javax.inject.Singleton;

@Singleton
public class ConnectionStateHolder {


    @Getter
    private ConnectionState state = ConnectionState.DISCONNECTED;

    public void setState(ConnectionState state){
        this.state = state;
    }

}
