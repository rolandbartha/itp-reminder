package net.rolibrt.itp_notifier;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class Utils {

    public static DateTimeFormatter formatter;

    static {
        formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault());
    }
}
