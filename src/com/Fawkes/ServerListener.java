package com.Fawkes;

import com.Fawkes.network.*;

public interface ServerListener {

    void playerConnected(Connection playerClient);

    void messageReceived(String message, Connection playerClient);

}
