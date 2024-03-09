package mx.kenzie.treasury;

import org.junit.Test;

import java.lang.constant.ConstantDesc;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

public class PriceTest {

    @Test
    public void simple() throws Throwable {
        final Price price = Price.valueOf(10);
        assert price.equals(10);
        assert price.equals((Object) 10);
        assert price.equals(Price.valueOf(10));
        assert price == Price.valueOf(10);
        assert price.isPositive();
        assert !price.isNegative();
        assert !price.isZero();
        assert price.doubleValue() > 5;
        final Optional<? extends ConstantDesc> optional = price.describeConstable();
        assert optional.isPresent();
        final ConstantDesc constantDesc = optional.get();
        final Object o = constantDesc.resolveConstantDesc(MethodHandles.lookup());
        assert o != null;
        assert o instanceof Number;
        assert o instanceof Price price1 && price1.intValue() == 10;
        assert price.equals(o);
        assert o.equals(price);
    }

}