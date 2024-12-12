package structra.assignment.task.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.NonNull;
import structra.assignment.framework.llm.context.SystemContextBuilder;
import structra.assignment.framework.llm.gen.questions.QuestionGenerationTarget;
import structra.assignment.framework.model.StringConstants;
import structra.assignment.framework.model.answer.AnswerData;
import structra.assignment.framework.model.answer.concrete.TextAnswer;
import structra.assignment.framework.model.gen.QuizzMaker;
import structra.assignment.framework.model.question.QuestionData;
import structra.assignment.framework.model.question.QuestionType;
import structra.assignment.framework.model.question.concrete.MultiCheckboxQuestion;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MultipleChoiceTarget implements QuestionGenerationTarget {
    private final String prompt;

    public MultipleChoiceTarget(String prompt) {
        Objects.requireNonNull(prompt, "Prompt can not be null");
        this.prompt = prompt;
    }

    @Override
    public String getBasePrompt() {
        return prompt;
    }

    @Override
    public MultiCheckboxQuestion parse(String input) {
        Objects.requireNonNull(input, "Input string cannot be null");
        try {
            JsonObject object = JsonParser.parseString(input).getAsJsonObject();
            AnswerData[] answerData = constructAnswerData(object);
            QuestionData questionData = parseQuestionData(object, answerData);
            System.out.println(questionData);
            return (MultiCheckboxQuestion) QuizzMaker.createQuestion(questionData);
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            return createErrorQuestion(); // Return error question on parsing failure
        }
    }

    private MultiCheckboxQuestion createErrorQuestion() {
        AnswerData errorAnswer =
                new AnswerData(
                        TextAnswer.class.getName(),
                        "ok", // Adjust error message as needed
                        "true",
                        "");

        QuestionData errorQuestion =
                new QuestionData(
                        QuestionType.MULTIPLE_CHOICE.toString(),
                        "An error occurred while generating the question. Please try again.",
                        0.0,
                        0,
                        "Error in question generation",
                        "",
                        Collections.singletonList(errorAnswer),
                        false);

        return (MultiCheckboxQuestion) QuizzMaker.createQuestion(errorQuestion);
    }

    private QuestionData parseQuestionData(JsonObject questionObject, AnswerData[] answerData)
           throws JsonIOException {
            JsonObject question =
                    questionObject.getAsJsonObject(StringConstants.Questions.QUESTIONS_NAME);
            return new QuestionData(
                    QuestionType.MULTIPLE_CHOICE.toString(),
                    question.get(StringConstants.Questions.QUESTION_TEXT).toString(),
                    question.get(StringConstants.Questions.QUESTION_DIFFICULTY).getAsDouble(),
                    question.get(StringConstants.Overall.POINTS_POSSIBLE).getAsInt(),
                    question.get(StringConstants.Questions.QUESTION_EXPLANATION).getAsString(),
                    "",
                    List.of(answerData),
                    false);
    }

    private AnswerData[] constructAnswerData(JsonObject answerObject) throws JsonIOException {
        JsonArray answer = answerObject.getAsJsonArray(StringConstants.Answers.ANSWERS_NAME);
        //System.out.println("Test answer class:" + TextAnswer.class.getName() + ", answer[0]: "
                // + answer.get(0).getAsJsonObject().get("Text") + ", " + answer.get(0).getAsJsonObject().get("Expected").getAsString());
        AnswerData answer0 = new AnswerData(
                "structra.assignment.framework.model.answer.concrete.BooleanAnswer",
                answer.get(0).getAsJsonObject().get(StringConstants.Answers.ANSWER_TEXT).getAsString(),
                answer.get(0).getAsJsonObject().get(StringConstants.Answers.EXPECTED_ANSWER).getAsString(),
                "");
        AnswerData answer1 = new AnswerData(
                "structra.assignment.framework.model.answer.concrete.BooleanAnswer",
                answer.get(1).getAsJsonObject().get(StringConstants.Answers.ANSWER_TEXT).getAsString(),
                answer.get(1).getAsJsonObject().get(StringConstants.Answers.EXPECTED_ANSWER).getAsString(),
                "");
        AnswerData answer2 = new AnswerData(
                "structra.assignment.framework.model.answer.concrete.BooleanAnswer",
                answer.get(2).getAsJsonObject().get(StringConstants.Answers.ANSWER_TEXT).getAsString(),
                answer.get(2).getAsJsonObject().get(StringConstants.Answers.EXPECTED_ANSWER).getAsString(),
                "");
        AnswerData answer3 = new AnswerData(
                "structra.assignment.framework.model.answer.concrete.BooleanAnswer",
                answer.get(3).getAsJsonObject().get(StringConstants.Answers.ANSWER_TEXT).getAsString(),
                answer.get(3).getAsJsonObject().get(StringConstants.Answers.EXPECTED_ANSWER).getAsString(),
                "");
        return new AnswerData[] {answer0, answer1, answer2, answer3};
    }

    @Override
    public @NonNull String getTargetContext() {
        return new SystemContextBuilder()
                .addContext(MultipleChoiceContext.FORMAT)
                .addContext(MultipleChoiceContext.PROPER_EXPLANATION)
                .build();
    }
}
