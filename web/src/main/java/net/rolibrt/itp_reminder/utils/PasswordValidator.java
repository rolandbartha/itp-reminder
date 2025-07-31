package net.rolibrt.itp_reminder.utils;

public class PasswordValidator {
    public static boolean isWeakPassword(String password) {
        if (password == null) return true;

        return password.length() < 8 ||
                !password.matches(".*[A-Z].*") ||     // At least one uppercase
                !password.matches(".*[a-z].*") ||     // At least one lowercase
                !password.matches(".*\\d.*");       // At least one digit
                //password.matches(".*[^A-Za-z0-9].*"); At least one special char
    }
}
