package server;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

class SharedMemory {
    private final HashMap<String, byte[]> map;
    private final ReentrantLock lock = new ReentrantLock();
    public UserAuthentication userAuthentication = new UserAuthentication();

    public SharedMemory() {
        map = new HashMap<>();
    }

    public void put(String key, byte[] value) {
        lock.lock();
        try {
            map.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    public byte[] get(String key) {
        lock.lock();
        try {
            return map.get(key);
        } finally {
            lock.unlock();
        }
    }

    //TODO considerar melhorar esta implementação de acordo com o Guiao 5 das práticas
    // Caso um cliente faça um multiPut muito grande, o lock vai bloquear todos os outros clientes durante muito tempo (É greedy)
    public void multiPut(Map<String, byte[]> pairs) {
        lock.lock();
        try {
            map.putAll(pairs);
        } finally {
            lock.unlock();
        }
    }

    //TODO considerar melhorar esta implementação de acordo com o Guiao 5 das práticas
    // É greedy
    public Map<String, byte[]> multiGet(Set<String> keys) {
        Map<String, byte[]> result = new HashMap<>();
        lock.lock();
        try {
            for (String key : keys) {
                result.put(key, this.get(key));
            }
        } finally {
            lock.unlock();
        }
        return result;
    }


}