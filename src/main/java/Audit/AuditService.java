package Audit;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditService {

    private List<String> auditLog;
    private int currentPage;
    private int itemsPerPage;
    private boolean isAuditMode;

    public AuditService() {
        auditLog = new ArrayList<>();
        currentPage = 0;
        itemsPerPage = 0;
        isAuditMode = false;
    }

    public void logCommand(String username, String command, boolean success) {
        if (!isAuditMode
                && username
                        != null) { // Log commands only if not in audit mode and user is logged in
            String logEntry =
                    LocalDateTime.now()
                            + " - ["
                            + username
                            + "] -> "
                            + command
                            + " (Success: "
                            + success
                            + ")";
            auditLog.add(logEntry);
            saveLogToFile("audit.log");
        }
    }

    private void saveLogToFile(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(auditLog.get(auditLog.size() - 1));
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startAuditLogPagination(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
        this.currentPage = 1;
        this.isAuditMode = true;
        showAuditLogPage(currentPage);
    }

    public void showAuditLogPage(int page) {
        if (itemsPerPage <= 0) {
            System.out.println("Invalid items per page value.");
            return;
        }
        int totalPages = (int) Math.ceil((double) auditLog.size() / itemsPerPage);
        if (page > totalPages || page < 1) {
            System.out.println("Invalid page number.");
            return;
        }
        currentPage = page;
        System.out.println(
                "Page " + page + " of " + totalPages + " (" + itemsPerPage + " items per page):");
        for (int i = 0; i < itemsPerPage; i++) {
            int index = (page - 1) * itemsPerPage + i;
            if (index < auditLog.size()) {
                System.out.println((index + 1) + ". " + auditLog.get(index));
            }
        }
        if (page < totalPages) {
            System.out.println("To view the next page, enter 'nextpage'.");
        }
    }

    public void showNextPage() {
        int totalPages = (int) Math.ceil((double) auditLog.size() / itemsPerPage);
        if (currentPage < totalPages) {
            showAuditLogPage(currentPage + 1);
        } else {
            System.out.println("You are already on the last page.");
        }
    }

    public void closeAuditLog() {
        this.isAuditMode = false;
        System.out.println("Audit log viewing closed.");
        // Remove the last entry if it is the close_auditlog command
        if (!auditLog.isEmpty() && auditLog.get(auditLog.size() - 1).contains("close_auditlog")) {
            auditLog.removeLast();
        }
    }

    public boolean isAuditMode() {
        return isAuditMode;
    }
}
