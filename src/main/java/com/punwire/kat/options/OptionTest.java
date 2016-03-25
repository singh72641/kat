package com.punwire.kat.options;

/**
 * Created by Kanwal on 01/02/16.
 */
public class  OptionTest {

    public static void main(String[] args) {

        // TEST DATA -- trade 106005
        double strike = 1060.00;
        double price = 978.70;
        double timeToExp = (17.00/365.00);
        //double timeToExp = .383562;
        double stDev = 0.3172;
        double interest = 0.073521;
        char call = 'c';
        Boolean callOption = true;

        BlackScholes_abbreviated bsShort = new BlackScholes_abbreviated();
        Double price1 = bsShort.blackScholesCall(price, strike, stDev, timeToExp, interest);

        System.out.println("  price3 = [" + price1.toString() + "]");

        OptionDetails od = new OptionDetails(true,price,strike,interest,timeToExp,stDev);
        OptionGreeks og =  BlackScholesGreeks.calculate(od);

        System.out.println(od.toString());
        System.out.println(og.toString());
//        System.out.println("Delta: " + og.delta + "Gamma: " + og.gamma);
//
//
//        Black_76 bs76 = new Black_76();
//        Double price2 = bs76.Black76(call, price, strike, timeToExp, interest, stDev);
//        System.out.println("  price2 = [" + price2.toString() + "]");
//
//        BlackScholesFormula bsFormula = new BlackScholesFormula();
//        Double price3 = bsFormula.calculate(callOption, price, strike, interest, timeToExp, stDev);
//        System.out.println("  price3 = [" + price3.toString() + "]");
//
//        System.out.println();
//
//        // TEST DATA -- trade 104335
//        strike = 18.25;
//        price = 13.10;
//        //timeToExp = 2.13035;
//        timeToExp = 2.134247;
//        stDev = 0.21;
//        interest = 0.002541;
//        call = 'c';
//        callOption = true;
//
//        price1 = bsShort.blackScholesCall(price, strike, stDev, timeToExp, interest);
//        System.out.println("  price1 = [" + price1.toString() + "]");
//
//        price2 = bs76.Black76(call, price, strike, timeToExp, interest, stDev);
//        System.out.println("  price2 = [" + price2.toString() + "]");
//
//        price3 = bsFormula.calculate(callOption, price, strike, interest, timeToExp, stDev);
//        System.out.println("  price3 = [" + price3.toString() + "]");
    }
}
