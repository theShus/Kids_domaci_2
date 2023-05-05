package servent.message.av;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

import java.io.Serial;
import java.util.Map;

public class AvAskTokenMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -7283310091422165288L;

    public AvAskTokenMessage(ServentInfo sender, ServentInfo receiver, ServentInfo neighbor, Map<Integer, Integer> senderVectorClock) {
        super(MessageType.AV_ASK, sender, receiver, neighbor, senderVectorClock);
    }


}
