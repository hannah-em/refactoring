package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    public static final int BASE_VOLUME_CREDIT_THRESHOLD = 30;
    public static final int COMEDY_EXTRA_VOLUME_FACTOR = 5;
    // comedy amount constants
    public static final int COMEDY_AMOUNT_PER_AUDIENCE = 300;
    public static final int COMEDY_AUDIENCE_THRESHOLD = 20;
    public static final int COMEDY_BASE_AMOUNT = 30000;
    public static final int COMEDY_OVER_BASE_CAPACITY_AMOUNT = 10000;
    public static final int COMEDY_OVER_BASE_CAPACITY_PER_PERSON = 500;
    // tragedy amount constants
    public static final int TRAGEDY_AUDIENCE_THRESHOLD = 30;
    public static final int TRAGEDY_BASE_AMOUNT = 40000;
    public static final int TRAGEDY_OVER_BASE = 1000;
    // formatting constants
    public static final int PERCENT_FACTOR = 100;
    // history constants
    public static final int HISTORY_BASE_AMOUNT = 20000;
    public static final int HISTORY_OVER_BASE_CAPACITY_PER_PERSON = 1000;
    public static final int HISTORY_AUDIENCE_THRESHOLD = 20;
    public static final int HISTORY_VOLUME_CREDIT_THRESHOLD = 20;
    // pastoral constants
    public static final int PASTORAL_BASE_AMOUNT = 40000;
    public static final int PASTORAL_OVER_BASE_CAPACITY_PER_PERSON = 2500;
    public static final int PASTORAL_AUDIENCE_THRESHOLD = 20;
    public static final int PASTORAL_VOLUME_CREDIT_THRESHOLD = 20;
    private final Invoice invoice;
    private final Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        final StringBuilder rlt = new StringBuilder("Statement for " + invoice.getCustomer() + System.lineSeparator());

        for (Performance performance : invoice.getPerformances()) {
            final String playName = getPlay(performance).getName();
            final int audience = performance.getAudience();
            final String frmt2 = usd(getAmount(performance));
            rlt.append(String.format("  %s: %s (%s seats)%n", playName, frmt2, audience));
        }

        rlt.append(String.format("Amount owed is %s%n", usd(getTotalAmount())));
        rlt.append(String.format("You earned %s credits%n", getTotalVolumeCredits()));
        return rlt.toString();
    }

    private int getTotalAmount() {
        int totalAmount = 0;
        for (Performance performance : invoice.getPerformances()) {
            totalAmount += getAmount(performance);
        }
        return totalAmount;
    }

    private int getTotalVolumeCredits() {
        return getVolumeCredits();
    }

    private int getVolumeCredits() {
        int result = 0;
        for (Performance performance : invoice.getPerformances()) {

            // add volume credits

            result += Math.max(performance.getAudience() - BASE_VOLUME_CREDIT_THRESHOLD, 0);
            // add extra credit for every five comedy attendees
            if ("comedy".equals(getPlay(performance).getType())) {
                result += performance.getAudience() / COMEDY_EXTRA_VOLUME_FACTOR;
            }
        }
        return result;
    }

    private static String usd(int totalAmount) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(totalAmount / PERCENT_FACTOR);
    }

    private Play getPlay(Performance performance) {
        return plays.get(performance.getPlayID());
    }

    private int getAmount(Performance performance) {
        final Play play = getPlay(performance);
        int thisAmount = 0;
        switch (play.getType()) {
            case "tragedy":
                thisAmount = TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > TRAGEDY_AUDIENCE_THRESHOLD) {
                    thisAmount += TRAGEDY_OVER_BASE * (performance.getAudience() - TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;
            case "comedy":
                thisAmount = COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    thisAmount += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                thisAmount += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", play.getType()));
        }
        return thisAmount;
    }
}
