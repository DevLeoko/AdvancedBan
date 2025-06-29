package me.leoko.advancedban.utils;

import me.leoko.advancedban.Universal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Color processing utility class that provides conversion and processing functions for hex colors and traditional color codes
 * Supports Minecraft 1.16+ hex color format
 */
public class ColorUtils {
    
    // Hex color matching pattern: &#RRGGBB or &#rrggbb
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    // Gradient color matching pattern: {#RRGGBB>text>#RRGGBB}
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("\\{#([A-Fa-f0-9]{6})>([^>]+)>#([A-Fa-f0-9]{6})\\}");

    // Traditional color codes
    private static final char COLOR_CHAR = '§';
    private static final char ALT_COLOR_CHAR = '&';

    /**
     * Check if hex color support is enabled
     * @return true if enabled
     */
    private static boolean isHexColorsEnabled() {
        try {
            return Universal.get().getMethods().getBoolean(
                Universal.get().getMethods().getConfig(),
                "HexColors.Enabled",
                true
            );
        } catch (Exception e) {
            // Default to enabled if config is not available
            return true;
        }
    }

    /**
     * Check if gradient color support is enabled
     * @return true if enabled
     */
    private static boolean isGradientsEnabled() {
        try {
            return Universal.get().getMethods().getBoolean(
                Universal.get().getMethods().getConfig(),
                "HexColors.Gradients",
                true
            );
        } catch (Exception e) {
            // Default to enabled if config is not available
            return true;
        }
    }
    
    /**
     * Convert text containing hex color codes to Minecraft color format
     * Supported format: &#RRGGBB
     *
     * @param text text containing color codes
     * @return converted text
     */
    public static String translateHexColorCodes(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // Process traditional color codes
        text = text.replace(ALT_COLOR_CHAR, COLOR_CHAR);

        // Check if hex color support is enabled
        if (!isHexColorsEnabled()) {
            // If disabled, remove hex color codes
            return HEX_PATTERN.matcher(text).replaceAll("");
        }

        // Process hex color codes
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexColor = matcher.group(1);
            String replacement = convertHexToMinecraft(hexColor);
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }
    
    /**
     * Convert hex color code to Minecraft format
     *
     * @param hex 6-digit hex color code (without #)
     * @return Minecraft color format string
     */
    private static String convertHexToMinecraft(String hex) {
        StringBuilder result = new StringBuilder();
        result.append(COLOR_CHAR).append('x');
        
        for (char c : hex.toCharArray()) {
            result.append(COLOR_CHAR).append(c);
        }
        
        return result.toString();
    }
    
    /**
     * Process gradient color text
     * Supported format: {#RRGGBB>text>#RRGGBB}
     *
     * @param text text containing gradient color codes
     * @return processed text
     */
    public static String translateGradientColors(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // Check if gradient color support is enabled
        if (!isGradientsEnabled() || !isHexColorsEnabled()) {
            // If disabled, keep only text content and remove gradient format
            return GRADIENT_PATTERN.matcher(text).replaceAll("$2");
        }

        Matcher matcher = GRADIENT_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String startHex = matcher.group(1);
            String content = matcher.group(2);
            String endHex = matcher.group(3);

            String gradientText = createGradient(startHex, endHex, content);
            matcher.appendReplacement(buffer, gradientText);
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }
    
    /**
     * Create gradient color text
     *
     * @param startHex starting color
     * @param endHex ending color
     * @param text text to apply gradient to
     * @return gradient color text
     */
    private static String createGradient(String startHex, String endHex, String text) {
        if (text.length() <= 1) {
            return convertHexToMinecraft(startHex) + text;
        }
        
        int[] startRgb = hexToRgb(startHex);
        int[] endRgb = hexToRgb(endHex);
        
        StringBuilder result = new StringBuilder();
        int length = text.length();
        
        for (int i = 0; i < length; i++) {
            double ratio = (double) i / (length - 1);
            
            int r = (int) (startRgb[0] + (endRgb[0] - startRgb[0]) * ratio);
            int g = (int) (startRgb[1] + (endRgb[1] - startRgb[1]) * ratio);
            int b = (int) (startRgb[2] + (endRgb[2] - startRgb[2]) * ratio);
            
            String hex = String.format("%02x%02x%02x", r, g, b);
            result.append(convertHexToMinecraft(hex)).append(text.charAt(i));
        }
        
        return result.toString();
    }
    
    /**
     * Convert hex color to RGB array
     *
     * @param hex 6-digit hex color code
     * @return RGB array [r, g, b]
     */
    private static int[] hexToRgb(String hex) {
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new int[]{r, g, b};
    }
    
    /**
     * Remove all color codes from text
     *
     * @param text text containing color codes
     * @return plain text with color codes removed
     */
    public static String stripColors(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Remove traditional color codes
        text = text.replaceAll("§[0-9a-fk-or]", "");

        // Remove hex color codes
        text = text.replaceAll("§x(§[0-9a-f]){6}", "");

        // Remove original hex format
        text = HEX_PATTERN.matcher(text).replaceAll("");

        // Remove gradient color format
        text = GRADIENT_PATTERN.matcher(text).replaceAll("$2");
        
        return text;
    }
    
    /**
     * Check if text contains color codes
     *
     * @param text text to check
     * @return true if contains color codes
     */
    public static boolean hasColors(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        return text.contains(String.valueOf(COLOR_CHAR)) || 
               HEX_PATTERN.matcher(text).find() ||
               GRADIENT_PATTERN.matcher(text).find();
    }
    
    /**
     * Process all types of color codes
     * This is the main public method that handles traditional color codes, hex colors, and gradients
     *
     * @param text text containing color codes
     * @return processed text
     */
    public static String translateColors(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // First process gradient colors
        text = translateGradientColors(text);

        // Then process hex colors and traditional color codes
        text = translateHexColorCodes(text);
        
        return text;
    }
    
    /**
     * Validate if hex color code is valid
     *
     * @param hex hex color code (may or may not include #)
     * @return true if valid
     */
    public static boolean isValidHex(String hex) {
        if (hex == null) {
            return false;
        }
        
        // Remove possible # prefix
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        
        return hex.matches("[A-Fa-f0-9]{6}");
    }
}
