package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuizDAO {

    
    public void saveQuiz(long userID, long materialID, String title, List<Question> questions)
            throws SQLException {

        validateQuiz(title, questions);

        if (userID <= 0 || materialID <= 0) {
    throw new IllegalArgumentException(
        "Valid user and material IDs are required.");
}

        String quizSQL =
            "INSERT INTO quizzes (userID, materialID, title) "
            + "VALUES (?, ?, ?)";

        String questionSQL =
            "INSERT INTO quiz_questions "
            + "(quizID, questionText, questionType, questionOrder) "
            + "VALUES (?, ?, ?, ?)";

        String optionSQL =
            "INSERT INTO question_options "
            + "(questionID, optionText, optionOrder, isCorrect) "
            + "VALUES (?, ?, ?, ?)";

        try (Connection connection = Database.open()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement quizStatement =
                         connection.prepareStatement(
                             quizSQL, Statement.RETURN_GENERATED_KEYS);

                     PreparedStatement questionStatement =
                         connection.prepareStatement(
                             questionSQL, Statement.RETURN_GENERATED_KEYS);

                     PreparedStatement optionStatement =
                         connection.prepareStatement(optionSQL)) {

                    quizStatement.setLong(1, userID);
                    quizStatement.setLong(2, materialID);
                    quizStatement.setString(3, title);
                    long quizID = insertAndGetID(quizStatement);

                    for (Question question : questions) {
                        questionStatement.setLong(1, quizID);
                        questionStatement.setString(2, question.questionText);
                        questionStatement.setString(3, question.questionType);
                        questionStatement.setInt(4, question.questionOrder);

                        long questionID = insertAndGetID(questionStatement);

                        for (AnswerOption option : question.options) {
                            optionStatement.setLong(1, questionID);
                            optionStatement.setString(2, option.optionText);
                            optionStatement.setInt(3, option.optionOrder);
                            optionStatement.setBoolean(4, option.isCorrect);

                            if (optionStatement.executeUpdate() != 1) {
                                throw new SQLException(
                                    "Failed to save an answer option."
                                );
                            }
                        }
                    }
                }

                connection.commit();

            } catch (SQLException | RuntimeException | Error failure) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                }

                throw failure;
            }
        }
    }

    public Quiz getSavedQuiz(long quizID, long userID) throws SQLException {

        String sql =
            "SELECT q.quizID, q.userID, q.materialID, q.title, "
            + "qq.questionID, qq.questionText, qq.questionType, "
            + "qq.questionOrder, "
            + "o.optionID, o.optionText, o.optionOrder, o.isCorrect "
            + "FROM quizzes q "
            + "LEFT JOIN quiz_questions qq ON qq.quizID = q.quizID "
            + "LEFT JOIN question_options o "
            + "ON o.questionID = qq.questionID "
            + "WHERE q.quizID = ? AND q.userID = ? "
            + "ORDER BY qq.questionOrder, o.optionOrder";

        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, quizID);
            statement.setLong(2, userID);

            try (ResultSet results = statement.executeQuery()) {
                Quiz quiz = null;
                Question currentQuestion = null;

                while (results.next()) {
                    if (quiz == null) {
                        quiz = new Quiz(
                            results.getLong("quizID"),
                            results.getLong("userID"),
                            results.getLong("materialID"),
                            results.getString("title"),
                            new ArrayList<Question>()
                        );
                    }

                    long questionID = results.getLong("questionID");

                    if (results.wasNull()) {
                        continue;
                    }

                    if (currentQuestion == null
                            || currentQuestion.questionID != questionID) {

                        currentQuestion = new Question(
                            questionID,
                            quiz.quizID,
                            results.getString("questionText"),
                            results.getString("questionType"),
                            results.getInt("questionOrder"),
                            new ArrayList<AnswerOption>()
                        );

                        quiz.questions.add(currentQuestion);
                    }

                    long optionID = results.getLong("optionID");

                    if (!results.wasNull()) {
                        currentQuestion.options.add(new AnswerOption(
                            optionID,
                            questionID,
                            results.getString("optionText"),
                            results.getInt("optionOrder"),
                            results.getBoolean("isCorrect")
                        ));
                    }
                }

                return quiz;
            }
        }
    }
    //untested
    public List<Long> getQuizIDsByUserID(long userID)
            throws SQLException {

        String sql =
            "SELECT quizID FROM quizzes WHERE userID = ? ORDER BY quizID";

        List<Long> quizIDs = new ArrayList<Long>();

        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userID);

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    quizIDs.add(results.getLong("quizID"));
                }
            }
        }

        return quizIDs;
    }

    public List<Long> getQuizIDsByUserIDAndMaterialID(
        long userID,
        long materialID)
        throws SQLException {

    String sql =
        "SELECT quizID FROM quizzes "
        + "WHERE userID = ? AND materialID = ? "
        + "ORDER BY quizID";

    List<Long> quizIDs = new ArrayList<>();

    try (Connection connection = Database.open();
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setLong(1, userID);
        statement.setLong(2, materialID);

        try (ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                quizIDs.add(results.getLong("quizID"));
            }
        }
    }

    return quizIDs;
}
    
    public boolean deleteQuiz(long quizID, long userID)
            throws SQLException {

        String sql =
            "DELETE FROM quizzes WHERE quizID = ? AND userID = ?";

        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, quizID);
            statement.setLong(2, userID);

            return statement.executeUpdate() == 1;
        }
    }
    //untested
    private long insertAndGetID(PreparedStatement statement)
            throws SQLException {

        if (statement.executeUpdate() != 1) {
            throw new SQLException("Failed to insert a quiz or question.");
        }

        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getLong(1);
            }

            throw new SQLException("Could not retrieve the generated ID.");
        }
    }
    //untested
    private void validateQuiz(String title, List<Question> questions) {
        if (!hasText(title) || title.length() > 255) {
            throw new IllegalArgumentException(
                "Quiz title must contain 1 to 255 characters."
            );
        }

        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException(
                "A quiz must have at least one question."
            );
        }

        Set<Integer> questionOrders = new HashSet<Integer>();

        for (Question question : questions) {
            if (question == null || !hasText(question.questionText)) {
                throw new IllegalArgumentException(
                    "Each question needs text."
                );
            }

            boolean trueFalse =
                "TRUE_FALSE".equals(question.questionType);

            if (!trueFalse
                    && !"MULTIPLE_CHOICE".equals(question.questionType)) {
                throw new IllegalArgumentException(
                    "Use MULTIPLE_CHOICE or TRUE_FALSE."
                );
            }

            if (question.questionOrder < 1
                    || !questionOrders.add(question.questionOrder)) {
                throw new IllegalArgumentException(
                    "Question positions must be positive and unique."
                );
            }

            if (question.options == null || question.options.size() < 2) {
                throw new IllegalArgumentException(
                    "Each question needs at least two options."
                );
            }

            Set<Integer> optionOrders = new HashSet<Integer>();
            int correctCount = 0;
            boolean hasTrue = false;
            boolean hasFalse = false;

            for (AnswerOption option : question.options) {
                if (option == null || !hasText(option.optionText)) {
                    throw new IllegalArgumentException(
                        "Each option needs text."
                    );
                }

                if (option.optionOrder < 1
                        || !optionOrders.add(option.optionOrder)) {
                    throw new IllegalArgumentException(
                        "Option positions must be positive and unique."
                    );
                }

                if (option.isCorrect) {
                    correctCount++;
                }

                hasTrue |= "True".equalsIgnoreCase(option.optionText);
                hasFalse |= "False".equalsIgnoreCase(option.optionText);
            }

            if (correctCount != 1) {
                throw new IllegalArgumentException(
                    "Each question needs exactly one correct option."
                );
            }

            if (trueFalse
                    && (question.options.size() != 2
                        || !hasTrue || !hasFalse)) {
                throw new IllegalArgumentException(
                    "A true/false question needs True and False options."
                );
            }
        }
    }

    private boolean hasText(String text) {
        return text != null && !text.trim().isEmpty();
    }
}

