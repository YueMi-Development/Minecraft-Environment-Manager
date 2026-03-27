package org.yuemi.environmentmanager.agent;

import org.yuemi.environmentmanager.api.HijackManager;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ManualTester {
    public static void main(String[] args) throws IOException {
        File tempFile = File.createTempFile("envmanager-test", ".txt");
        Files.writeString(tempFile.toPath(), "ORIGINAL CONTENT");
        String path = tempFile.getAbsolutePath();

        System.out.println("Testing path: " + path);
        
        // Setup hijacking
        HijackManager.getInstance().setEnabled(true);
        HijackManager.getInstance().register(tempFile.toPath(), "HIJACKED CONTENT".getBytes(StandardCharsets.UTF_8));

        // Attempt to read
        try (FileInputStream fis = new FileInputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int n = fis.read(buffer);
            String result = new String(buffer, 0, n, StandardCharsets.UTF_8);
            
            System.out.println("Read result: " + result);
            
            if ("HIJACKED CONTENT".equals(result)) {
                System.out.println("SUCCESS: Content was hijacked!");
            } else {
                System.out.println("FAILURE: Content was NOT hijacked. (Is the agent attached?)");
            }
        } finally {
            tempFile.delete();
        }
    }
}
