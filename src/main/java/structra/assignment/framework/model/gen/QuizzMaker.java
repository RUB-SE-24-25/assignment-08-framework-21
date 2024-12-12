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

package structra.assignment.framework.model.gen;

import lombok.NonNull;
import structra.assignment.framework.model.answer.*;
import structra.assignment.framework.model.answer.base.Answer;
import structra.assignment.framework.model.answer.concrete.BooleanAnswer;
import structra.assignment.framework.model.answer.concrete.TextAnswer;
import structra.assignment.framework.model.gen.answers.*;
import structra.assignment.framework.model.gen.questions.*;
import structra.assignment.framework.model.question.QuestionData;
import structra.assignment.framework.model.question.QuestionType;
import structra.assignment.framework.model.question.base.Question;
import structra.assignment.framework.model.question.concrete.MultiCheckboxQuestion;
import structra.assignment.framework.model.question.concrete.OpenAnswerQuestion;
import structra.assignment.framework.utils.StringUtils;

import java.util.Objects;

/**
 * A static class used to avoid directly using the Factories. <br>
 * {@link QuestionFactory};<br>
 * {@link AnswerFactory}; <br>
 * <br>
 * It will always choose the right Factory for the user assuming the data provides a proper Type
 * String via it's getType method. <br>
 * <br>
 * <font color=#00cf8a><i>Uses {@link QuestionType} and {@link AnswerTypes} for Type
 * strings</i></font>
 *
 * @author Artur Rausch
 * @see BooleanAnswerFactory
 * @see TextAnswerFactory
 * @see OpenAnswerQuestionFactory
 * @see MultiCheckBoxQuestionFactory
 */
public abstract class QuizzMaker {

    private static final BooleanAnswerFactory BOOLEAN_ANSWER_FACTORY = new BooleanAnswerFactory();

    private static final TextAnswerFactory TEXT_ANSWER_FACTORY = new TextAnswerFactory();

    private static final OpenAnswerQuestionFactory OPEN_ANSWER_QUESTION_FACTORY =
            new OpenAnswerQuestionFactory();

    private static final MultiCheckBoxQuestionFactory MULTI_CHECK_BOX_QUESTION_FACTORY =
            new MultiCheckBoxQuestionFactory();

    /**
     * <font color=#f9f970>It is quite unlikely that you will have to use this method directly as
     * Question normally contain a Collection of AnswerData as part of themselves. <br>
     * </font> So use </font color> {@link #createQuestion(QuestionData)} unless there is an
     * explicit reason not to.<br>
     * <br>
     * Takes an arbitrary AnswerData object to create an Answer using the corresponding format. <br>
     * In case of the type not being supported a NULL should be performed. You will also be notified
     * of this Failure via a Log message using the TAG "QuizzMaker".
     *
     * @param data The AnswerData (most likely loaded from a database following a predefined format)
     * @return null if the data's type is not supported else new Answer following one of the Current
     *     Answer formats.
     * @see AnswerData
     * @see TextAnswer
     * @see BooleanAnswer
     */
    public static Answer<?> createAnswer(@NonNull AnswerData data) {
        Objects.requireNonNull(data);
        Answer<?> answer = null;
        String type = data.getType();
        type =
                StringUtils.validateAnswerTypeString(type)
                        .orElseGet(AnswerTypes.BOOLEAN_ANSWER::toString);
        if (type.equals(AnswerTypes.BOOLEAN_ANSWER.toString())) {
            answer = BOOLEAN_ANSWER_FACTORY.create(data);
        } else if (type.equals(AnswerTypes.TEXT_ANSWER.toString())) {
            answer = TEXT_ANSWER_FACTORY.create(data);
        }

        return answer;
    }

    /**
     * Takes an arbitrary QuestionData object to create a Question using the corresponding format.
     * <br>
     * <br>
     * In case of the type not being supported a NULL should be performed. You will also be notified
     * of this Failure via a Log message using the TAG "QuizzMaker".
     *
     * @param data The QuestionData (most likely loaded from a database following a predefined
     *     format)
     * @return null if the data's type is not supported else new Question following one of the
     *     Current Question formats.
     * @see QuestionData
     * @see OpenAnswerQuestion
     * @see MultiCheckboxQuestion
     */
    public static Question<?> createQuestion(@NonNull QuestionData data) {
        Objects.requireNonNull(data);
        Question<?> question = null;
        String type = data.getType().toUpperCase();

        if (type.equals(QuestionType.MULTIPLE_CHOICE.toString())) {
            question = MULTI_CHECK_BOX_QUESTION_FACTORY.create(data);
        } else if (type.equals(QuestionType.OPEN_ANSWER.toString())) {
            question = OPEN_ANSWER_QUESTION_FACTORY.create(data);
        }

        return question;
    }
}
