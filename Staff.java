public class Staff extends User {
    private String assignedDepartment;

    public Staff(String staffId, String name, String assignedDepartment) {
        super(staffId, name);
        this.assignedDepartment = assignedDepartment;
    }

    public String getAssignedDepartment() {
        return assignedDepartment;
    }

    @Override
    public void displayDashboard() {
        System.out.println("\n--- STAFF DASHBOARD ---");
        System.out.println("Staff: " + getName() + " | Department: " + assignedDepartment);
    }
}