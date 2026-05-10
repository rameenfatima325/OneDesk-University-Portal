package business.shared;

public abstract class DocumentRequest extends Request {
    protected int copies;
    protected String purpose;

    public DocumentRequest(int studentId) {
        super(studentId);
    }

    public int getCopies() { return copies; }
    public void setCopies(int copies) { this.copies = copies; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
}