package com.nng.unit;

import com.nng.exception.TreesesException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 数字工具类.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class NumberUtil implements Util{
    
    /**
     * 将数字类型对象四舍五入并转换为整形.
     *
     * @param obj 待转换的对象
     * @return 四舍五入后的整形值
     */
    public static int roundHalfUp(final Object obj) {
        if (obj instanceof Integer) {
            return (int) obj;
        }
        if (obj instanceof Double) {
            return new BigDecimal((double) obj).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        }
        if (obj instanceof Float) {
            return new BigDecimal((float) obj).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        }
        if (obj instanceof String) {
            return new BigDecimal((String) obj).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        }
        throw new TreesesException("Invalid value to transfer: %s", obj);
    }
    
    /**
     * 获取准确的数字以及类型.
     * 
     * @param value 数字字符串
     * @param radix 进制
     * @return 准确的数字以及类型
     */
    public static Number getExactlyNumber(final String value, final int radix) {
        BigInteger result = new BigInteger(value, radix);
        if (result.compareTo(new BigInteger(String.valueOf(Integer.MIN_VALUE))) >= 0 && result.compareTo(new BigInteger(String.valueOf(Integer.MAX_VALUE))) <= 0) {
            return result.intValue();
        }
        if (result.compareTo(new BigInteger(String.valueOf(Long.MIN_VALUE))) >= 0 && result.compareTo(new BigInteger(String.valueOf(Long.MAX_VALUE))) <= 0) {
            return result.longValue();
        }
        return result;
    }
}
