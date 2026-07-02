package io.github.jeongdonghee.excelkit.grid;

/**
 * 시트가 분할될 때 시트 이름에 번호를 붙이는 방식.
 */
public enum SheetNumbering {

    /** {@code 세션목록_1}, {@code 세션목록_2} … */
    SUFFIX_UNDERSCORE {
        @Override
        public String apply(String base, int index) {
            return base + "_" + index;
        }
    },

    /** {@code 세션목록 (1)}, {@code 세션목록 (2)} … */
    SUFFIX_PAREN {
        @Override
        public String apply(String base, int index) {
            return base + " (" + index + ")";
        }
    };

    /** 베이스 이름과 1-based 시트 번호로 시트명을 만든다. */
    public abstract String apply(String base, int index);
}
