package mx.kenzie.treasury.command;

import mx.kenzie.centurion.Arguments;
import mx.kenzie.centurion.CompoundArgument;
import mx.kenzie.centurion.TypedArgument;
import mx.kenzie.treasury.Price;

public class MoneyArgument extends TypedArgument<Price> {

    public static final String MONEY_SIGN = "$";

    private final boolean decimal;

    public MoneyArgument(boolean decimal) {
        super(Price.class);
        this.decimal = decimal;
        this.label = "amount";
        this.description = "An amount of money.";
    }

    @Override
    public boolean matches(String s) {
        if (!decimal && s.contains(".")) return false;
        if (!s.startsWith(MONEY_SIGN)) return false;
        if (s.contains("-")) return false;
        return Arguments.DOUBLE.matches(s.substring(MONEY_SIGN.length()));
    }

    @Override
    public Price parse(String s) {
        return Price.valueOf(Math.abs(Arguments.DOUBLE.parse(s.substring(MONEY_SIGN.length()))));
    }

    @Override
    public String[] possibilities() {
        return new String[] {"$"};
    }

}
