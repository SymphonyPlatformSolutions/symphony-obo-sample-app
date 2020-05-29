package com.symphony.platformsolutions.obo.bot;

import authentication.SymOBOUserRSAAuth;
import clients.SymBotClient;
import clients.SymOBOClient;
import com.symphony.platformsolutions.obo.OboBot;
import listeners.IMListener;
import model.InboundMessage;
import model.OutboundMessage;
import model.Stream;
import model.StreamListItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class IMListenerImpl implements IMListener {
    private SymBotClient botClient;
    private static Logger LOGGER = LoggerFactory.getLogger(OboBot.class);

    public IMListenerImpl(SymBotClient botClient){
        this.botClient = botClient;
    }

    public void onIMMessage(InboundMessage inboundMessage) {
        LOGGER.info(inboundMessage.getMessageText());
        OutboundMessage message;
        //List<String> userStreams = new ArrayList<String>();
        HashMap<String, String> streamInfo = new HashMap<String, String>();

        if (inboundMessage.getMessageText().equalsIgnoreCase("/streams")) {
            SymOBOUserRSAAuth userAuth = OboBot.getOboAuth().getUserAuth(inboundMessage.getUser().getUserId());
            SymOBOClient oboClient = SymOBOClient.initOBOClient(OboBot.getConfig(), userAuth);

            List<StreamListItem> streamsMap = oboClient.getStreamsClient().getUserStreams(Arrays.asList("IM", "ROOM"), false);
            for (StreamListItem item : streamsMap){
                if (item.getType().equals("ROOM")) {
                    streamInfo.put(item.getRoomAttributes().getName(), item.getId());
                }
            }
            LOGGER.info(streamInfo.toString());

        }
    }


    public void onIMCreated(Stream stream) {
        return;
    }
}