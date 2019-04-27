package ronan_tommey.reg_logger.reg_logging;

/**
 * Interface for classes which can log log car pass details in
 * some way (eg. to a database or a file system)
 */
public interface CarPassLogger {
    void logPass(CarPassDetails carPassDetails);
}
