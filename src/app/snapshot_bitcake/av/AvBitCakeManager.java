package app.snapshot_bitcake.av;

import app.snapshot_bitcake.BitcakeManager;

import java.util.concurrent.atomic.AtomicInteger;

public class AvBitCakeManager implements BitcakeManager {

    private final AtomicInteger currentAmount = new AtomicInteger(1000);


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

}
