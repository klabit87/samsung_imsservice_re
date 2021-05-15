package com.sec.internal.ims.servicemodules.im;

import android.text.TextUtils;
import android.util.Pair;
import android.util.SparseArray;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageMap {
    private final Map<String, Map<Integer, MessageBase>> mChatIdMap = new HashMap();
    private final Map<String, Map<Pair<String, ImDirection>, MessageBase>> mChatImdnIdMap = new HashMap();
    private final SparseArray<MessageBase> mIdMap = new SparseArray<>();
    private final Map<Pair<String, ImDirection>, MessageBase> mImdnIdMap = new HashMap();

    public boolean containsKey(int id) {
        boolean z;
        synchronized (this.mIdMap) {
            z = this.mIdMap.indexOfKey(id) >= 0;
        }
        return z;
    }

    public MessageBase get(int id) {
        MessageBase message;
        synchronized (this.mIdMap) {
            message = null;
            if (id >= 0) {
                message = this.mIdMap.get(id);
            }
        }
        return message;
    }

    public MessageBase get(String imdnId, ImDirection direction) {
        MessageBase message;
        synchronized (this.mIdMap) {
            message = null;
            if (!TextUtils.isEmpty(imdnId) && direction != null) {
                message = this.mImdnIdMap.get(new Pair<>(imdnId, direction));
            }
        }
        return message;
    }

    public MessageBase get(String imdnId, ImDirection direction, String chatId) {
        MessageBase message;
        Map<Pair<String, ImDirection>, MessageBase> map;
        synchronized (this.mIdMap) {
            message = null;
            if (!TextUtils.isEmpty(imdnId) && direction != null && !TextUtils.isEmpty(chatId) && (map = this.mChatImdnIdMap.get(chatId)) != null) {
                message = map.get(new Pair<>(imdnId, direction));
            }
        }
        return message;
    }

    public List<MessageBase> getAll() {
        List<MessageBase> list;
        synchronized (this.mIdMap) {
            list = new ArrayList<>(this.mIdMap.size());
            for (int keyId = 0; keyId < this.mIdMap.size(); keyId++) {
                list.add(this.mIdMap.valueAt(keyId));
            }
        }
        return list;
    }

    public List<MessageBase> getAll(String chatId) {
        List<MessageBase> messages;
        Map<Integer, MessageBase> map;
        synchronized (this.mIdMap) {
            messages = new ArrayList<>();
            if (!TextUtils.isEmpty(chatId) && (map = this.mChatIdMap.get(chatId)) != null) {
                messages.addAll(map.values());
            }
        }
        return messages;
    }

    public void put(MessageBase m) {
        synchronized (this.mIdMap) {
            if (m != null) {
                if (m.getId() > 0) {
                    this.mIdMap.put(m.getId(), m);
                }
                Pair<String, ImDirection> pair = null;
                if (!TextUtils.isEmpty(m.getImdnId()) && m.getDirection() != null) {
                    pair = new Pair<>(m.getImdnId(), m.getDirection());
                }
                if (pair != null) {
                    this.mImdnIdMap.put(pair, m);
                }
                if (!TextUtils.isEmpty(m.getChatId())) {
                    if (m.getId() > 0) {
                        this.mChatIdMap.computeIfAbsent(m.getChatId(), $$Lambda$MessageMap$k72fc8brQE4haDrUOnQwj373ie4.INSTANCE).put(Integer.valueOf(m.getId()), m);
                    }
                    if (pair != null) {
                        this.mChatImdnIdMap.computeIfAbsent(m.getChatId(), $$Lambda$MessageMap$IfOhQABfjohSuy8Veht_DdWbtlA.INSTANCE).put(pair, m);
                    }
                }
            }
        }
    }

    static /* synthetic */ Map lambda$put$0(String k) {
        return new HashMap();
    }

    static /* synthetic */ Map lambda$put$1(String k) {
        return new HashMap();
    }

    public void remove(int id) {
        synchronized (this.mIdMap) {
            if (id > 0) {
                MessageBase m = this.mIdMap.get(id);
                this.mIdMap.delete(id);
                if (m != null) {
                    Pair<String, ImDirection> pair = null;
                    if (!TextUtils.isEmpty(m.getImdnId()) && m.getDirection() != null) {
                        pair = new Pair<>(m.getImdnId(), m.getDirection());
                    }
                    if (pair != null) {
                        this.mImdnIdMap.remove(pair);
                    }
                    if (!TextUtils.isEmpty(m.getChatId())) {
                        Map<Integer, MessageBase> idMap = this.mChatIdMap.get(m.getChatId());
                        if (idMap != null) {
                            idMap.remove(Integer.valueOf(id));
                        }
                        Map<Pair<String, ImDirection>, MessageBase> imdnIdMap = this.mChatImdnIdMap.get(m.getChatId());
                        if (imdnIdMap != null) {
                            imdnIdMap.remove(pair);
                        }
                    }
                }
            }
        }
    }
}
