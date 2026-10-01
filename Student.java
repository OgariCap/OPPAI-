public class Student extends User {
    private String currentQueueNumber;

    public Student(String studentId, String name) {
        super(studentId, name);
        this.currentQueueNumber = null;
    }

    public String getCurrentQueueNumber() {
        return currentQueueNumber;
    }

    public void setCurrentQueueNumber(String queueNumber) {
        this.currentQueueNumber = queueNumber;
    }

    @Override
    public void displayDashboard() {
        System.out.println("\n--- STUDENT DASHBOARD ---");
        System.out.println("Welcome, " + getName() + " (ID: " + getId() + ")");
        if (currentQueueNumber != null) {
            System.out.println("Your Queue Ticket: " + currentQueueNumber);
        } else {
            System.out.println("Status: No active queue ticket.");
        }
    }
}