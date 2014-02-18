package net.karmats.weatherful.exception;

/**
 * Exception for any errors that will occur in the application
 * 
 * @author mats
 * 
 */
public class WeatherfulException extends Exception {
    private static final long serialVersionUID = 6665847922763695145L;

    private final ErrorCode errorCode;

    public enum ErrorCode {
        NOT_CONNECTED(100), PARSE_ERROR(101), LOCATION_UNKNOWN(102), IO_ERROR(103), MALFORMED_URL(104);

        private int code;

        private ErrorCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public WeatherfulException(ErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public WeatherfulException(ErrorCode errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    public WeatherfulException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

}
