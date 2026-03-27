package org.yuemi.environmentmanager.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for files that should be hijacked by the Environment Manager Java Agent.
 */
public final class HijackManager {

    private static final HijackManager INSTANCE = new HijackManager();

    private boolean enabled = false;
    private final Map<String, byte[]> hijackedFiles = new ConcurrentHashMap<>();
    private final Map<Object, InputStream> surrogates = new WeakHashMap<>();

    private HijackManager() {}

    public static HijackManager getInstance() {
        return INSTANCE;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Registers a file to be hijacked with its virtual content.
     *
     * @param path    the absolute path to the file
     * @param content the virtual content of the file
     */
    public void register(Path path, byte[] content) {
        hijackedFiles.put(path.toAbsolutePath().toString(), content);
    }

    /**
     * Unregisters a file from hijacking.
     *
     * @param path the absolute path to the file
     */
    public void unregister(Path path) {
        hijackedFiles.remove(path.toAbsolutePath().toString());
    }

    /**
     * Checks if a file path is currently hijacked.
     *
     * @param path the absolute path string
     * @return true if hijacked
     */
    public boolean isHijacked(String path) {
        return enabled && hijackedFiles.containsKey(path);
    }

    /**
     * Retrieves the virtual content for a hijacked file.
     *
     * @param path the absolute path string
     * @return the virtual content, or null if not hijacked
     */
    public byte[] getContent(String path) {
        return hijackedFiles.get(path);
    }

    /**
     * Associates a specific object (e.g., FileInputStream) with a surrogate stream.
     *
     * @param target the source file input stream instance
     * @param path   the file path it was opened with
     */
    public void associate(Object target, String path) {
        if (enabled) {
            byte[] content = hijackedFiles.get(path);
            if (content != null) {
                surrogates.put(target, new ByteArrayInputStream(content));
            }
        }
    }

    /**
     * Reads a byte from the surrogate stream associated with the target.
     *
     * @param target the source file input stream instance
     * @return the byte read, or -2 if no surrogate exists (special value to avoid conflict with -1 EOF)
     */
    public int read(Object target) {
        InputStream surrogate = surrogates.get(target);
        if (surrogate != null) {
            try {
                return surrogate.read();
            } catch (Exception e) {
                return -1; // EOF on error
            }
        }
        return -2; // No surrogate
    }

    /**
     * Reads bytes into a buffer from the surrogate stream associated with the target.
     *
     * @param target the source file input stream instance
     * @param b      the buffer
     * @param off    offset
     * @param len    length
     * @return bytes read, or -2 if no surrogate exists
     */
    public int read(Object target, byte[] b, int off, int len) {
        InputStream surrogate = surrogates.get(target);
        if (surrogate != null) {
            try {
                return surrogate.read(b, off, len);
            } catch (Exception e) {
                return -1;
            }
        }
        return -2;
    }

    /**
     * Clears all hijacked files and surrogates.
     */
    public void clear() {
        hijackedFiles.clear();
        surrogates.clear();
    }
}
