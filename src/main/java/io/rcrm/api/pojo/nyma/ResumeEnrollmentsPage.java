package io.rcrm.api.pojo.nyma;

import java.util.ArrayList;

public class ResumeEnrollmentsPage {
    private int choice;
    private ArrayList<Integer> linked_email_type;

    public ResumeEnrollmentsPage() {
    }

    public ResumeEnrollmentsPage(int choice, ArrayList<Integer> linked_email_type) {
        this.choice = choice;
        this.linked_email_type = linked_email_type;
    }

    public int getChoice() {
        return choice;
    }

    public void setChoice(int choice) {
        this.choice = choice;
    }

    public ArrayList<Integer> getLinked_email_type() {
        return linked_email_type;
    }

    public void setLinked_email_type(ArrayList<Integer> linked_email_type) {
        this.linked_email_type = linked_email_type;
    }
}
