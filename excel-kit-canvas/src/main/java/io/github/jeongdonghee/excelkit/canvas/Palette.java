package io.github.jeongdonghee.excelkit.canvas;

/** 기본 시리즈 색 팔레트(Tableau10 계열). 색을 지정하지 않은 시리즈에 순서대로 배정한다. */
final class Palette {

    private static final String[] HEX = {
            "4E79A7", "F28E2B", "E15759", "76B7B2", "59A14F",
            "EDC948", "B07AA1", "FF9DA7", "9C755F", "BAB0AC"
    };

    private Palette() {
    }

    /** index번째 기본 색 hex(# 없이). */
    static String at(int index) {
        return HEX[Math.floorMod(index, HEX.length)];
    }

    /** {@code "#RRGGBB"} 또는 {@code "RRGGBB"} → RGB 바이트 3개. */
    static byte[] rgb(String hex) {
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        int v = Integer.parseInt(h, 16);
        return new byte[] {(byte) (v >> 16 & 0xFF), (byte) (v >> 8 & 0xFF), (byte) (v & 0xFF)};
    }
}
