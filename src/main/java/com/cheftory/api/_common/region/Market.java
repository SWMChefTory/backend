package com.cheftory.api._common.region;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.GlobalErrorCode;

public enum Market {
    GLOBAL,
    KOREA;

    public static Market fromCountryCode(String countryCode) throws CheftoryException {
        if (countryCode == null || countryCode.isBlank()) throw new CheftoryException(GlobalErrorCode.UNKNOWN_REGION);
        return "KR".equalsIgnoreCase(countryCode.trim()) ? KOREA : GLOBAL;
    }
}
