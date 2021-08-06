package bigdata.cdc.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class ExceptionUtil {
    /**
     * @param e
     * @return stack log
     */
    public static String getStackLog(Exception e) {
        return ExceptionUtils.getStackTrace(e);
    }

}
