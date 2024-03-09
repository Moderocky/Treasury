package mx.kenzie.treasury.manager;

import mx.kenzie.treasury.Money;
import mx.kenzie.treasury.Price;

import java.io.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class SimpleBankManager implements Money, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Function<UUID, Price> valuePlacer = or -> Price.ZERO;
    protected final File file;
    protected final Map<UUID, Price> balanceCache;

    public SimpleBankManager(File file) {
        this.file = file;
        this.balanceCache = Collections.synchronizedMap(new LinkedHashMap<>());
    }

    @Override
    public Price get(UUID uuid) {
        return balanceCache.computeIfAbsent(uuid, valuePlacer);
    }

    @Override
    public void set(UUID uuid, double amount) {
        this.balanceCache.put(uuid, Price.valueOf(amount));
    }

    @Override
    public void ensureSaved() throws Exception {
        if (!file.exists()) file.createNewFile();
        try (final OutputStream stream = new FileOutputStream(file);
             final DataOutputStream output = new DataOutputStream(stream)) {
            this.write(output);
        }
    }

    @Override
    public void ensureLoaded() throws Exception {
        if (!file.exists()) return;
        try (final InputStream stream = new FileInputStream(file);
             final DataInputStream input = new DataInputStream(stream)) {
            this.read(input);
        }
    }

    @Serial
    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        this.write(new DataOutputStream(out));
    }

    @Serial
    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        this.read(new DataInputStream(in));
    }

    @Serial
    private void readObjectNoData()
        throws ObjectStreamException {
    }

    public void read(DataInputStream stream) throws IOException {
        synchronized (balanceCache) {
            this.balanceCache.clear();
            final int total = stream.readInt();
            for (int i = 0; i < total; i++) {
                final UUID key = new UUID(stream.readLong(), stream.readLong());
                final Price value = Price.valueOf(stream.readDouble());
                this.balanceCache.put(key, value);
            }
        }
    }

    public void write(DataOutputStream stream) throws IOException {
        synchronized (balanceCache) {
            stream.writeInt(balanceCache.size());
            for (Map.Entry<UUID, Price> entry : balanceCache.entrySet()) {
                final UUID key = entry.getKey();
                stream.writeLong(key.getMostSignificantBits());
                stream.writeLong(key.getLeastSignificantBits());
                stream.writeDouble(entry.getValue().doubleValue());
            }
        }
    }

}
