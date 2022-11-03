package cn.ridup.fun.lv.exception;

/**
 * Theme up to date exception.
 *
 * @author ridup
 * @version 0.1.0
 * @since 2022/3/29 20:41
 */
public class ThemeUpToDateException extends BadRequestException {

    public ThemeUpToDateException(String message) {
        super(message);
    }
}
