package net.deanly.solana.sdk.program.spl.token.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TokenUtils {

    /**
     * Converts a UI representation of a token amount to the raw amount.
     *
     * @param uiAmount The amount in UI representation (e.g., 10.123).
     * @param decimals Number of decimals for the token.
     * @return Raw amount as long.
     */
    public static long uiAmountToAmount(double uiAmount, int decimals) {
        return (long) (uiAmount * Math.pow(10, decimals));
    }

    /**
     * Converts a raw amount to its UI representation.
     *
     * @param amount   The raw amount.
     * @param decimals Number of decimals for the token.
     * @return The UI representation as a double.
     */
    public static double amountToUiAmount(long amount, int decimals) {
        return (double) amount / Math.pow(10, decimals);
    }

    /**
     * Converts a raw token amount to a formatted UI string.
     *
     * @param amount   The raw amount.
     * @param decimals Number of decimals for the token.
     * @return The UI representation as a String.
     */
    public static String amountToUiAmountString(long amount, int decimals) {
        if (decimals > 0) {
            BigDecimal raw = BigDecimal.valueOf(amount).divide(BigDecimal.TEN.pow(decimals), decimals, RoundingMode.UNNECESSARY);
            return raw.toPlainString();
        }
        return String.valueOf(amount);
    }

    /**
     * Converts a raw token amount to its UI representation and trims trailing zeros.
     *
     * @param amount   The raw amount.
     * @param decimals Number of decimals for the token.
     * @return The trimmed UI representation as a String.
     */
    public static String amountToUiAmountStringTrimmed(long amount, int decimals) {
        String formatted = amountToUiAmountString(amount, decimals);
        if (decimals > 0) {
            return formatted.replaceAll("\\.?0+$", ""); // Remove trailing zeros and a possible decimal point.
        }
        return formatted;
    }

    /**
     * Converts a UI representation of a token amount to a raw amount.
     *
     * @param uiAmount The UI representation of the token amount as a string.
     * @param decimals Number of decimals for the token.
     * @return The raw amount as long.
     * @throws IllegalArgumentException If the input is invalid.
     */
    public static long tryUiAmountIntoAmount(String uiAmount, int decimals) throws IllegalArgumentException {
        try {
            String[] parts = uiAmount.split("\\."); // Split into integer and fractional parts.
            String beforeDecimal = parts[0];
            String afterDecimal = (parts.length > 1) ? parts[1] : "";

            // Trim trailing zeros from the fractional part
            afterDecimal = afterDecimal.replaceAll("0+$", "");

            // Validate fractional part length
            if (afterDecimal.length() > decimals) {
                throw new IllegalArgumentException("Invalid UI amount: exceeds allowed decimal places.");
            }

            // Normalize: Concatenate and pad fractional part to match decimals
            String normalized = beforeDecimal + afterDecimal + "0".repeat(decimals - afterDecimal.length());

            return Long.parseLong(normalized);

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid UI amount format.", e);
        }
    }

}
