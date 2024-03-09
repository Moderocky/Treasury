package mx.kenzie.treasury;

import mx.kenzie.centurion.ColorProfile;
import mx.kenzie.centurion.MinecraftCommand;
import mx.kenzie.treasury.command.MoneyArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.constant.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import static java.lang.constant.ConstantDescs.DEFAULT_NAME;

/**
 * A special number kind to represent a price,
 * that can both be used in maths and directly printed in a message.
 */
public class Price extends Number implements Serializable, Constable, ComponentLike, Comparable<Number> {

    protected static final DecimalFormat PRICE_FORMAT = new DecimalFormat("0.00");
    protected static final Map<Number, WeakReference<Price>> cache = new WeakHashMap<>();
    private static final double EPSILON = Double.longBitsToDouble(971L << 52);
    private static final ClassDesc CLASS_DESC = ClassDesc.of("mx.kenzie.treasury.Price");
    private static final DirectMethodHandleDesc BSM_INIT = ConstantDescs.ofConstantBootstrap(CLASS_DESC, "valueOf", CLASS_DESC, ClassDesc.ofDescriptor("D"));
    public static final Price ZERO = new Price();

    private final double value;

    public Price(double value) {
        this.value = value;
        cache.put(this, new WeakReference<>(this));
    }

    public Price() {
        this(0);
    }

    public static Price valueOf(String value) {
        if (value == null || value.isEmpty()) return ZERO;
        if (value.charAt(0) == '$') return valueOf(Double.parseDouble(value.substring(1)));
        return valueOf(Double.parseDouble(value));
    }

    public static Price valueOf(MethodHandles.Lookup lookup, String name, Class<?> type, double value)
        throws Throwable {
        if (type != Price.class && Price.class.isAssignableFrom(type)) {
            MethodHandle handle;
            try {
                handle = lookup.findStatic(type, "valueOf", MethodType.methodType(type, double.class));
            } catch (NoSuchMethodException e) {
                handle = lookup.findConstructor(type, MethodType.methodType(void.class, double.class));
            }
            return (Price) handle.invoke(value);
        }
        return Price.valueOf(value);
    }

    public static Price valueOf(double value) {
        return cache.computeIfAbsent(value, v -> new WeakReference<>(new Price(v.doubleValue()))).get();
    }

    public static Price valueOf(long value) {
        return valueOf((double) value);
    }

    public static Price valueOf(int value) {
        return valueOf((double) value);
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public long longValue() {
        return (long) value;
    }

    @Override
    public float floatValue() {
        return (float) value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public Optional<? extends ConstantDesc> describeConstable() {
        return Optional.of(DynamicConstantDesc.ofNamed(BSM_INIT, DEFAULT_NAME, CLASS_DESC, this.doubleValue()));
    }

    @Override
    public @NotNull Component asComponent() {
        return this.format(MoneyArgument.MONEY_SIGN, MinecraftCommand.DEFAULT_PROFILE);
    }

    public Component format(String currency, ColorProfile profile) {
        final Component format;
        if (value == Math.rint(value)) format = Component.text((int) value, profile.highlight());
        else format = Component.text(PRICE_FORMAT.format(value), profile.highlight());
        return Component.translatable("money.format", currency + "%s", format).color(profile.pop());
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Number number)) return false;
        return Double.compare(number.doubleValue(), this.doubleValue()) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }

    @Override
    public String toString() {
        return '$' + PRICE_FORMAT.format(value);
    }

    public boolean isNegative() {
        return value < 0;
    }

    public boolean isPositive() {
        return value > 0;
    }

    public boolean isZero() {
        return value == 0 || Math.abs(value) < EPSILON;
    }

    public boolean equals(Number amount) {
        return this.compareTo(amount) == 0;
    }

    public boolean gt(Number amount) {
        return this.compareTo(amount) > 0;
    }

    public boolean lt(Number amount) {
        return this.compareTo(amount) < 0;
    }

    public boolean ge(Number amount) {
        return value >= amount.doubleValue();
    }

    public boolean le(Number amount) {
        return value <= amount.doubleValue();
    }

    @Override
    public int compareTo(@NotNull Number o) {
        return Double.compare(value, o.doubleValue());
    }

}
