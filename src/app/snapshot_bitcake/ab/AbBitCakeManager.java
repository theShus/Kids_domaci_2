package app.snapshot_bitcake.ab;

import app.AppConfig;
import app.snapshot_bitcake.BitcakeManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public class AbBitCakeManager implements BitcakeManager {

    private final AtomicInteger currentAmount = new AtomicInteger(1000);
    private final Map<Integer, Integer> giveHistory = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> getHistory = new ConcurrentHashMap<>();
    public int recordedAmount = 0;

    public AbBitCakeManager() {//inizijalizujemo mapu
        for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
            giveHistory.put(neighbor, 0);
            getHistory.put(neighbor, 0);
        }
    }

    @Override
    public void takeSomeBitcakes(int amount) {
        currentAmount.getAndAdd(-amount);
    }

    @Override
    public void addSomeBitcakes(int amount) {
        currentAmount.getAndAdd(amount);
    }

    @Override
    public int getCurrentBitcakeAmount() {
        return currentAmount.get();
    }


    //iz v8
    private class MapValueUpdater implements BiFunction<Integer, Integer, Integer> {
        private final int valueToAdd;

        public MapValueUpdater(int valueToAdd) {
            this.valueToAdd = valueToAdd;
        }

        @Override
        public Integer apply(Integer key, Integer oldValue) {
            return oldValue + valueToAdd;
        }
    }

    public void recordGiveTransaction(int neighbor, int amount) {//todo
        giveHistory.compute(neighbor, new MapValueUpdater(amount));
    }

    public void recordGetTransaction(int neighbor, int amount) {
        getHistory.compute(neighbor, new MapValueUpdater(amount));
    }

}
