package me.leoko.advancedban.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the color processing utility class
 */
public class ColorUtilsTest {

    @BeforeEach
    void setUp() {
        // Setup before tests
    }

    @Test
    void testTranslateHexColorCodes_ValidHex() {
        // Test valid hex color code conversion
        String input = "&#FF5555Hello World";
        String result = ColorUtils.translateHexColorCodes(input);
        
        // Verify result contains correct Minecraft color format
        assertTrue(result.contains("§x§F§F§5§5§5§5"));
        assertTrue(result.contains("Hello World"));
    }

    @Test
    void testTranslateHexColorCodes_MultipleHex() {
        // Test multiple hex color codes
        String input = "&#FF5555Red &#00FF00Green &#0000FFBlue";
        String result = ColorUtils.translateHexColorCodes(input);
        
        // Verify all colors are correctly converted
        assertTrue(result.contains("§x§F§F§5§5§5§5"));
        assertTrue(result.contains("§x§0§0§F§F§0§0"));
        assertTrue(result.contains("§x§0§0§0§0§F§F"));
    }

    @Test
    void testTranslateHexColorCodes_TraditionalColors() {
        // Test traditional color code conversion
        String input = "&cRed &aGreen &9Blue";
        String result = ColorUtils.translateHexColorCodes(input);
        
        assertEquals("§cRed §aGreen §9Blue", result);
    }

    @Test
    void testTranslateHexColorCodes_MixedColors() {
        // Test mixed color codes
        String input = "&c&#FF5555Mixed &#00FF00Colors&a";
        String result = ColorUtils.translateHexColorCodes(input);
        
        assertTrue(result.contains("§c"));
        assertTrue(result.contains("§x§F§F§5§5§5§5"));
        assertTrue(result.contains("§x§0§0§F§F§0§0"));
        assertTrue(result.contains("§a"));
    }

    @Test
    void testTranslateGradientColors_ValidGradient() {
        // Test valid gradient
        String input = "{#FF0000>Hello>#00FF00}";
        String result = ColorUtils.translateGradientColors(input);
        
        // Verify result contains gradient color format
        assertFalse(result.contains("{#"));
        assertFalse(result.contains("}"));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("§x"));
    }

    @Test
    void testTranslateGradientColors_MultipleGradients() {
        // Test multiple gradients
        String input = "{#FF0000>Red>#FFFF00} and {#00FF00>Green>#0000FF}";
        String result = ColorUtils.translateGradientColors(input);
        
        assertTrue(result.contains("Red"));
        assertTrue(result.contains("Green"));
        assertTrue(result.contains("and"));
        assertFalse(result.contains("{#"));
    }

    @Test
    void testStripColors_AllColorTypes() {
        // Test removing all types of color codes
        String input = "§cTraditional &#FF5555Hex {#FF0000>Gradient>#00FF00} Normal";
        String result = ColorUtils.stripColors(input);
        
        assertEquals("Traditional Hex Gradient Normal", result);
    }

    @Test
    void testStripColors_OnlyText() {
        // Test plain text (no color codes)
        String input = "Just normal text";
        String result = ColorUtils.stripColors(input);
        
        assertEquals(input, result);
    }

    @Test
    void testHasColors_WithColors() {
        // Test text with color codes
        assertTrue(ColorUtils.hasColors("§cRed text"));
        assertTrue(ColorUtils.hasColors("&#FF5555Hex text"));
        assertTrue(ColorUtils.hasColors("{#FF0000>Gradient>#00FF00}"));
    }

    @Test
    void testHasColors_WithoutColors() {
        // Test text without color codes
        assertFalse(ColorUtils.hasColors("Normal text"));
        assertFalse(ColorUtils.hasColors(""));
        assertFalse(ColorUtils.hasColors(null));
    }

    @Test
    void testIsValidHex_ValidCodes() {
        // Test valid hex codes
        assertTrue(ColorUtils.isValidHex("FF5555"));
        assertTrue(ColorUtils.isValidHex("#FF5555"));
        assertTrue(ColorUtils.isValidHex("00ff00"));
        assertTrue(ColorUtils.isValidHex("#00ff00"));
        assertTrue(ColorUtils.isValidHex("ABCDEF"));
    }

    @Test
    void testIsValidHex_InvalidCodes() {
        // Test invalid hex codes
        assertFalse(ColorUtils.isValidHex("GG5555")); // Invalid characters
        assertFalse(ColorUtils.isValidHex("FF55")); // Too short
        assertFalse(ColorUtils.isValidHex("FF55555")); // Too long
        assertFalse(ColorUtils.isValidHex("")); // Empty string
        assertFalse(ColorUtils.isValidHex(null)); // null
    }

    @Test
    void testTranslateColors_CompleteProcessing() {
        // Test complete color processing flow
        String input = "&c{#FF0000>Gradient>#FFFF00} &#00FF00Normal &aText";
        String result = ColorUtils.translateColors(input);
        
        // Verify all types of colors are processed
        assertTrue(result.contains("§c"));
        assertTrue(result.contains("§x"));
        assertTrue(result.contains("§a"));
        assertTrue(result.contains("Normal"));
        assertTrue(result.contains("Text"));
        assertFalse(result.contains("{#"));
        assertFalse(result.contains("&#"));
    }

    @Test
    void testTranslateColors_NullAndEmpty() {
        // Test null and empty strings
        assertNull(ColorUtils.translateColors(null));
        assertEquals("", ColorUtils.translateColors(""));
    }

    @Test
    void testTranslateColors_NoColors() {
        // Test text without color codes
        String input = "Just normal text without colors";
        String result = ColorUtils.translateColors(input);
        
        assertEquals(input, result);
    }

    @Test
    void testGradientCreation_SingleCharacter() {
        // Test single character gradient
        String input = "{#FF0000>A>#00FF00}";
        String result = ColorUtils.translateGradientColors(input);
        
        assertTrue(result.contains("A"));
        assertTrue(result.contains("§x"));
    }

    @Test
    void testGradientCreation_EmptyContent() {
        // Test empty content gradient
        String input = "{#FF0000>>#00FF00}";
        String result = ColorUtils.translateGradientColors(input);
        
        // Should remove gradient format, keep only empty content
        assertEquals("", result);
    }

    @Test
    void testHexConversion_LowerAndUpperCase() {
        // Test upper and lower case hex codes
        String inputLower = "&#ff5555lower";
        String inputUpper = "&#FF5555UPPER";
        
        String resultLower = ColorUtils.translateHexColorCodes(inputLower);
        String resultUpper = ColorUtils.translateHexColorCodes(inputUpper);
        
        // Both should produce same color format (case insensitive)
        assertTrue(resultLower.contains("§x§f§f§5§5§5§5") || resultLower.contains("§x§F§F§5§5§5§5"));
        assertTrue(resultUpper.contains("§x§F§F§5§5§5§5"));
    }

    @Test
    void testComplexMessage_RealWorldExample() {
        // Test real-world complex message
        String input = "&c&lAdvancedBan &8&l» &#FF5555Player &#FFAA00banned &afor {#FF0000>cheating>#FFFF00}!";
        String result = ColorUtils.translateColors(input);
        
        // Verify all elements exist and format is correct
        assertTrue(result.contains("§c§l"));
        assertTrue(result.contains("§8§l"));
        assertTrue(result.contains("§x"));
        assertTrue(result.contains("§a"));
        assertTrue(result.contains("Player"));
        assertTrue(result.contains("banned"));
        assertTrue(result.contains("for"));
        assertTrue(result.contains("cheating"));
        assertFalse(result.contains("&#"));
        assertFalse(result.contains("{#"));
    }
}
