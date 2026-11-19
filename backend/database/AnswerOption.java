package database;

public final class AnswerOption {
    public final long optionID;
    public final long questionID;
    public final String optionText;
    public final int optionOrder;
    public final boolean isCorrect;

    public AnswerOption(long optionID, long questionID, String optionText,
                        int optionOrder, boolean isCorrect) {
        this.optionID = optionID;
        this.questionID = questionID;
        this.optionText = optionText;
        this.optionOrder = optionOrder;
        this.isCorrect = isCorrect;
    }
}
