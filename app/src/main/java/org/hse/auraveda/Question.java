package org.hse.auraveda;

public class Question {
    private final int id;
    private final String name;
    private final String correctAnswer;
    private final String option1;
    private final String option2;

    public Question(int id, String name, String correctAnswer, String option1, String option2) {
        this.id = id;
        this.name = name;
        this.correctAnswer = correctAnswer;
        this.option1 = option1;
        this.option2 = option2;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getOption1() { return option1; }
    public String getOption2() { return option2; }
}