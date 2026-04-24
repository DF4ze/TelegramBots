package fr.ses10doigts.telegrambots.configuration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public class TelegramConflict409LogConfigurer implements AutoCloseable {

    private static final String BOT_SESSION_LOGGER = "org.telegram.telegrambots.longpolling.BotSession";
    private static final String CONFLICT_MESSAGE_PREFIX = "Error received from Telegram GetUpdates Request, retrying in";
    private static final String CONFLICT_409_PREFIX = "409:";
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TelegramConflict409LogConfigurer.class);

    private final TelegramPollingLoggingProperties.Conflict409LogMode mode;
    private TurboFilter installedFilter;

    public TelegramConflict409LogConfigurer(TelegramPollingLoggingProperties.Conflict409LogMode mode) {
        this.mode = mode;
        installFilterIfNeeded();
    }

    private void installFilterIfNeeded() {
        if (mode == null || mode == TelegramPollingLoggingProperties.Conflict409LogMode.ERROR) {
            return;
        }

        ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
        if (!(iLoggerFactory instanceof LoggerContext loggerContext)) {
            LOG.warn("telegram.polling-logging.conflict-409-log-mode={} ignored because Logback is not active", mode);
            return;
        }

        Conflict409TurboFilter filter = new Conflict409TurboFilter(mode);
        filter.setContext(loggerContext);
        filter.start();
        loggerContext.addTurboFilter(filter);
        installedFilter = filter;

        LOG.info("Configured Telegram 409 polling log mode: {}", mode);
    }

    @Override
    public void close() {
        if (installedFilter == null) {
            return;
        }

        ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
        if (iLoggerFactory instanceof LoggerContext loggerContext) {
            loggerContext.getTurboFilterList().remove(installedFilter);
            installedFilter.stop();
        }
        installedFilter = null;
    }

    private static class Conflict409TurboFilter extends TurboFilter {

        private static final ThreadLocal<Boolean> RELAY_GUARD = ThreadLocal.withInitial(() -> false);

        private final TelegramPollingLoggingProperties.Conflict409LogMode mode;
        private final org.slf4j.Logger relayLogger = LoggerFactory.getLogger(TelegramConflict409LogConfigurer.class);

        private Conflict409TurboFilter(TelegramPollingLoggingProperties.Conflict409LogMode mode) {
            this.mode = mode;
        }

        @Override
        public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable throwable) {
            if (!isTargetConflict409(logger, level, format, params, throwable)) {
                return FilterReply.NEUTRAL;
            }

            if (mode == TelegramPollingLoggingProperties.Conflict409LogMode.OFF) {
                return FilterReply.DENY;
            }

            if (mode == TelegramPollingLoggingProperties.Conflict409LogMode.WARN) {
                relayAsWarn(format, params, throwable);
                return FilterReply.DENY;
            }

            return FilterReply.NEUTRAL;
        }

        private boolean isTargetConflict409(Logger logger, Level level, String format, Object[] params, Throwable throwable) {
            if (logger == null || level != Level.ERROR) {
                return false;
            }
            if (!BOT_SESSION_LOGGER.equals(logger.getName())) {
                return false;
            }
            if (format == null || !format.startsWith(CONFLICT_MESSAGE_PREFIX)) {
                return false;
            }
            Throwable candidate = throwable != null ? throwable : findThrowableInParams(params);
            return containsConflict409(candidate);
        }

        private Throwable findThrowableInParams(Object[] params) {
            if (params == null || params.length == 0) {
                return null;
            }
            Object last = params[params.length - 1];
            return last instanceof Throwable t ? t : null;
        }

        private boolean containsConflict409(Throwable throwable) {
            Throwable current = throwable;
            while (current != null) {
                String asString = current.toString();
                if (asString != null && asString.startsWith(CONFLICT_409_PREFIX)) {
                    return true;
                }
                current = current.getCause();
            }
            return false;
        }

        private void relayAsWarn(String format, Object[] params, Throwable throwable) {
            if (RELAY_GUARD.get()) {
                return;
            }

            RELAY_GUARD.set(true);
            try {
                relayLogger.warn(format, withoutTrailingThrowable(params));
            } finally {
                RELAY_GUARD.set(false);
            }
        }

        private Object[] withoutTrailingThrowable(Object[] params) {
            if (params == null || params.length == 0) {
                return new Object[0];
            }
            Object last = params[params.length - 1];
            if (last instanceof Throwable) {
                Object[] trimmed = new Object[params.length - 1];
                System.arraycopy(params, 0, trimmed, 0, params.length - 1);
                return trimmed;
            }
            return params;
        }
    }
}
