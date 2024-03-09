package mx.kenzie.treasury.manager;

import mx.kenzie.treasury.Price;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

public class SimpleBankManagerTest {

    @Test
    public void read() throws Throwable {
        final UUID uuid1 = UUID.randomUUID(), uuid2 = UUID.randomUUID();
        final SimpleBankManager manager = new SimpleBankManager(null);
        assert manager.balanceCache.isEmpty();
        manager.read(new DataInputStream(new ByteArrayInputStream(ByteBuffer.allocate(4 + 8 + 8 + 8 + 8 + 8 + 8)
            .putInt(2)
            .putLong(uuid1.getMostSignificantBits()).putLong(uuid1.getLeastSignificantBits())
            .putDouble(5.5)
            .putLong(uuid2.getMostSignificantBits()).putLong(uuid2.getLeastSignificantBits())
            .putDouble(117.35).flip()
            .array())));
        assert !manager.balanceCache.isEmpty();
        assert manager.balanceCache.size() == 2;
        assert manager.balanceCache.containsKey(uuid1);
        assert manager.balanceCache.containsKey(uuid2);
        assert manager.balanceCache.get(uuid1).equals(5.5);
        assert manager.balanceCache.get(uuid2).equals(117.35);
    }

    @Test
    public void write() throws Throwable {
        final UUID uuid1 = UUID.randomUUID(), uuid2 = UUID.randomUUID();
        final SimpleBankManager manager = new SimpleBankManager(null);
        assert manager.balanceCache.isEmpty();
        manager.balanceCache.put(uuid1, Price.valueOf(5.5));
        manager.balanceCache.put(uuid2, Price.valueOf(117.35));
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        manager.write(new DataOutputStream(stream));
        final ByteBuffer buffer = ByteBuffer.wrap(stream.toByteArray());
        assert buffer.getInt() == 2;
        assert buffer.getLong() == uuid1.getMostSignificantBits();
        assert buffer.getLong() == uuid1.getLeastSignificantBits();
        assert buffer.getDouble() == 5.5;
        assert buffer.getLong() == uuid2.getMostSignificantBits();
        assert buffer.getLong() == uuid2.getLeastSignificantBits();
        assert buffer.getDouble() == 117.35;
    }

}