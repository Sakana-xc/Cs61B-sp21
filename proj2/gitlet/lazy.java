package gitlet;

import java.util.function.Supplier;

public class lazy<T> {
    private T value;
    private volatile Supplier<T> supplier;
    private volatile boolean initiated;

    public lazy(Supplier<T> supplier){
        this.supplier = supplier;
    }

    public T get() {
        if (!initiated) {
            synchronized (this) {
                if (!initiated) {
                    T t = supplier.get();
                    value = t;
                    supplier = null;
                    initiated = true;
                    return t;

                }
            }
        }
        return value;

    }
}
