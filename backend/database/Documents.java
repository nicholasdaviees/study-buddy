package database;

public final class Documents {
    public final long docID;
    public final long userID;
    public final String fileName;
    public final byte[] pdfData;
    public final String extractedText;
    public final long fileSizeBytes;

    public Documents(long docID, long userID, String fileName, byte[] pdfData, long fileSizeBytes) {
        this(docID, userID, fileName, pdfData, null, fileSizeBytes);
    }

    public Documents(long docID, long userID, String fileName, byte[] pdfData, String extractedText, long fileSizeBytes) {
        this.docID = docID;
        this.userID = userID;
        this.fileName = fileName;
        this.pdfData = pdfData;
        this.extractedText = extractedText;
        this.fileSizeBytes = fileSizeBytes;
    }
}
