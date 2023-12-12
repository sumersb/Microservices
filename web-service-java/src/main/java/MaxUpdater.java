import java.util.concurrent.atomic.AtomicReference;

public class MaxUpdater<T extends Comparable<T>> {
    private AtomicReference<T> max = new AtomicReference<>();

    public void updateMax(T value) {
        max.updateAndGet(currentMax -> (currentMax == null || value.compareTo(currentMax) > 0) ? value : currentMax);
    }

    public T getMax() {
        return max.get();
    }
}