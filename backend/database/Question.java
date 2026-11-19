package database;

import java.util.List;

public final class Question {
    public final long questionID;
    public final long quizID;
    public final String questionText;
    public final String questionType;
    public final int questionOrder;
    public final List<AnswerOption> options;

    public Question(long questionID, long quizID, String questionText, String questionType, int questionOrder, List<AnswerOption> options) {
        this.questionID = questionID;
        this.quizID = quizID;
        this.questionText = questionText;
        this.questionType = questionType;
        this.questionOrder = questionOrder;
        this.options = options;
    }
}
