package me.leoko.advancedban;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;


/**
 * @author SupremeMortal
 */
@RequiredArgsConstructor
public class AdvancedBanLogger {
    private static final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.gz");
    private final AdvancedBan advancedBan;
    private Path logDirPath;
    private Path latestLogPath;

    private static void gzipFile(Path from, Path to) throws IOException {
        try (InputStream in = Files.newInputStream(from)) {
            try (GZIPOutputStream out = new GZIPOutputStream(Files.newOutputStream(to))) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
                out.finish();
            }
        }
    }

    private static String surroundMessage(String msg, char color) {
        StringBuilder builder = new StringBuilder("\u00A7" + color);

        int maxLength = 0;
        for (String line : msg.split("\n")) {
            if (line.length() > maxLength) {
                maxLength = line.length();
            }
        }
        for (int i = 0; i < maxLength; i++) {
            builder.append('-');
        }

        String separator = builder.toString();
        return '\n' + separator + "\n\u00A7" + color + msg + '\n' + separator;
    }

    public void onEnable() {
        this.logDirPath = advancedBan.getDataFolderPath().resolve("logs");
        this.latestLogPath = logDirPath.resolve("latest.log");

        try {
            if (Files.notExists(logDirPath) || !Files.isDirectory(logDirPath)) {
                Files.deleteIfExists(logDirPath);
                Files.createDirectories(logDirPath);
            }
        } catch (IOException e) {
            warn("Unable to load logger");
            return;
        }

        checkLastLog(true);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(logDirPath,
                path -> Files.isRegularFile(path) && matcher.matches(path) &&
                        Files.getLastModifiedTime(path).to(TimeUnit.DAYS) >= advancedBan.getConfiguration().getPurgeLogDays())) {
            for (Path path : stream) {
                Files.delete(path);
            }
        } catch (IOException e) {
            warn("Unable to load logger");
        }
    }

    public void info(String msg) {
        log(Level.INFO, msg);
    }

    public void debug(String msg) {
        log(Level.FINER, msg);
    }

    public void warn(String msg) {
        log(Level.WARNING, surroundMessage(msg, 'c'));
    }

    public void severe(String msg) {
        log(Level.SEVERE, surroundMessage(msg, '4'));
    }

    public final void logException(Throwable t) {
        StringWriter string = new StringWriter();
        try (PrintWriter writer = new PrintWriter(string)) {
            t.printStackTrace(writer);
            writer.flush();
        }
        log(Level.FINER, string.toString());
    }

    public void log(Level level, String msg) {
        if (level.intValue() > Level.FINE.intValue() || advancedBan.getConfiguration().isDebug()) {
            advancedBan.log(level, msg);
        }
        if (level.intValue() > Level.FINE.intValue()) {
            return;
        }

        checkLastLog(false);

        try {
            Files.write(latestLogPath, Collections.singletonList(msg), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            severe("Unable to write to the debug log");
        }
    }

    /**
     * Checks the last log and gzip it if is older.
     *
     * @param force If we should force the compression of the file.
     */
    private void checkLastLog(boolean force) {
        long time = System.currentTimeMillis();
        long day = TimeUnit.MILLISECONDS.toDays(time);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (Files.exists(latestLogPath) && Files.isRegularFile(latestLogPath)) {
            try {
                FileTime lastModified = Files.getLastModifiedTime(latestLogPath);
                if (force || day == lastModified.to(TimeUnit.DAYS)) {
                    long lineCount;
                    try (Stream<String> stream = Files.lines(latestLogPath)) {
                        lineCount = stream.count();
                    }
                    if (lineCount <= 0) {
                        return;
                    }
                    int logCount = 0;
                    Path compressedLogPath;
                    do {
                        compressedLogPath = logDirPath.resolve(sdf.format(lastModified.toMillis()) + '-' + ++logCount + ".log.gz");
                    } while (Files.exists(compressedLogPath));

                    gzipFile(latestLogPath, compressedLogPath);
                    Files.delete(latestLogPath);
                    Files.createFile(latestLogPath);
                }
            } catch (IOException ex) {
                Logger.getLogger(AdvancedBanLogger.class.getName()).log(Level.WARNING, "An unexpected error has ocurred while trying to compress the latest log file. {0}", ex.getMessage());
            }
        }
    }
}