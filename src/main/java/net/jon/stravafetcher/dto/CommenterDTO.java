package net.jon.stravafetcher.dto;

public class CommenterDTO {
    private String firstName;
    private String lastName;
    private long numComments;

    public CommenterDTO(String firstName, String lastName, long numComments) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.numComments = numComments;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public long getNumComments() {
        return numComments;
    }

    public void setNumComments(long numComments) {
        this.numComments = numComments;
    }

    // toString method for debugging purposes
    @Override
    public String toString() {
        return "CommenterDTO{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", numComments=" + numComments +
                '}';
    }
}
