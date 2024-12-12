/*
 * MIT License
 *
 * Copyright (c) 2024 Riko Torun, Artur Rausch, Lennart Köhler, Moritz Wiedemann, Tim Stöcker, Souren Ishkhanian
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package structra.assignment.framework.llm.gen.questions;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.NonNull;
import structra.assignment.framework.llm.MachineLearningModel;
import structra.assignment.framework.llm.context.SystemContextBuilder;
import structra.assignment.framework.llm.context.specification.OpenQuestionContext;
import structra.assignment.framework.model.StringConstants;
import structra.assignment.framework.model.answer.AnswerData;
import structra.assignment.framework.model.answer.concrete.TextAnswer;
import structra.assignment.framework.model.gen.QuizzMaker;
import structra.assignment.framework.model.question.QuestionData;
import structra.assignment.framework.model.question.QuestionType;
import structra.assignment.framework.model.question.concrete.OpenAnswerQuestion;

import java.util.Collections;
import java.util.Objects;

/**
 * Used to format a JSON-like String from a {@link MachineLearningModel} response to a native
 * format; in this case an {@link OpenAnswerQuestion} and {@link TextAnswer}.
 *
 * @author Riko Torun
 * @author Moritz Wiedemann
 */
public class OpenQuestionTarget implements QuestionGenerationTarget<OpenAnswerQuestion> {

    private final String prompt;

    /**
     * Creates a new {@code OpenQuestionTarget} instance, with the given prompt.
     *
     * @param prompt the prompt used by the implementation of {@link MachineLearningModel}
     * @see OpenAnswerQuestion
     */
    public OpenQuestionTarget(String prompt) {
        Objects.requireNonNull(prompt, "Prompt can not be null");
        this.prompt = prompt;
    }

    /**
     * {@inheritDoc}
     *
     * @return the prompt
     */
    @Override
    public String getBasePrompt() {
        return prompt;
    }

    /**
     * Formats the input JSON object to produce an OpenAnswerQuestion.
     *
     * @param input The input JSON object containing question and answer data
     * @return The formatted OpenAnswerQuestion, or an error question if parsing fails
     * @throws IllegalArgumentException if the input is in an invalid format
     * @throws NullPointerException if the input is null
     */
    public OpenAnswerQuestion parse(String input) {
        Objects.requireNonNull(input, "Input string cannot be null");

        try {
            JsonObject object = JsonParser.parseString(input).getAsJsonObject();
            AnswerData answerData = constructAnswerData(object);
            QuestionData questionData = parseQuestionData(object, answerData);
            return (OpenAnswerQuestion) QuizzMaker.createQuestion(questionData);
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            return createErrorQuestion(); // Return error question on parsing failure
        }
    }

    /**
     * Parses the answer data from a JSON object.
     *
     * @param answerObject The JSON object containing the answer data
     * @return An AnswerData object constructed from the parsed data
     * @throws JsonIOException If there's an error parsing the JSON object
     */
    private AnswerData constructAnswerData(JsonObject answerObject) throws JsonIOException {
        JsonObject answer = answerObject.getAsJsonObject(StringConstants.Answers.ANSWERS_NAME);
        return new AnswerData(
                TextAnswer.class.getName(),
                answer.get(StringConstants.Answers.ANSWER_TEXT).getAsString(),
                answer.get(StringConstants.Answers.EXPECTED_ANSWER).getAsString(),
                "");
    }

    /**
     * Parses the question data from a JSON object and combines it with the given answer data.
     *
     * @param questionObject The JSON object containing the question data
     * @param answerData The AnswerData object to be associated with this question
     * @return A QuestionData object constructed from the parsed data and the given answer
     * @throws JsonIOException If there's an error parsing the JSON object
     */
    private QuestionData parseQuestionData(JsonObject questionObject, AnswerData answerData)
            throws JsonIOException {
        JsonObject question =
                questionObject.getAsJsonObject(StringConstants.Questions.QUESTIONS_NAME);
        return new QuestionData(
                QuestionType.OPEN_ANSWER.toString(),
                question.get(StringConstants.Questions.QUESTION_TEXT).toString(),
                question.get(StringConstants.Questions.QUESTION_DIFFICULTY).getAsDouble(),
                question.get(StringConstants.Overall.POINTS_POSSIBLE).getAsInt(),
                question.get(StringConstants.Questions.QUESTION_EXPLANATION).getAsString(),
                "",
                Collections.singletonList(answerData),
                false);
    }

    /**
     * Creates a default error question; to be used when parsing fails.
     *
     * @return An OpenAnswerQuestion object representing an error state
     */
    private OpenAnswerQuestion createErrorQuestion() {
        AnswerData errorAnswer =
                new AnswerData(
                        TextAnswer.class.getName(),
                        "ok", // Adjust error message as needed
                        "",
                        "");

        QuestionData errorQuestion =
                new QuestionData(
                        QuestionType.OPEN_ANSWER.toString(),
                        "An error occurred while generating the question. Please try again.",
                        0.0,
                        0,
                        "Error in question generation",
                        "",
                        Collections.singletonList(errorAnswer),
                        false);

        return (OpenAnswerQuestion) QuizzMaker.createQuestion(errorQuestion);
    }

    @Override
    public @NonNull String getTargetContext() {
        return new SystemContextBuilder()
                .addContext(OpenQuestionContext.FORMAT)
                .addContext(OpenQuestionContext.PROPER_EXPLANATION)
                .build();
    }
}
