package com.sec.internal.ims.servicemodules.im.listener;

import com.sec.internal.constants.ims.servicemodules.im.ImCacheAction;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import java.util.Collection;

public interface IImCacheActionListener {
    void updateMessage(MessageBase messageBase, ImCacheAction imCacheAction);

    void updateMessage(Collection<MessageBase> collection, ImCacheAction imCacheAction);

    void updateParticipant(ImParticipant imParticipant, ImCacheAction imCacheAction);

    void updateParticipant(Collection<ImParticipant> collection, ImCacheAction imCacheAction);
}
