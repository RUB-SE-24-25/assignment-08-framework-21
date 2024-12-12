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

package structra.assignment.framework.model.gen.questions;

import lombok.NonNull;
import structra.assignment.framework.model.answer.AnswerData;
import structra.assignment.framework.model.answer.concrete.BooleanAnswer;
import structra.assignment.framework.model.gen.QuizzMaker;
import structra.assignment.framework.model.question.QuestionData;
import structra.assignment.framework.model.question.concrete.MultiCheckboxQuestion;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a new MultiCheckboxQuestion instance from a given QuestionData instance.
 *
 * @author Artur Rausch
 * @author Lennart Köhler
 * @author Riko Torun
 * @see QuestionData
 */
public class MultiCheckBoxQuestionFactory implements QuestionFactory {
    /**
     * {@inheritDoc}
     *
     * @param data
     * @return A {@link MultiCheckboxQuestion} instance
     */
    @Override
    @NonNull
    public MultiCheckboxQuestion create(@NonNull QuestionData data) {
        List<BooleanAnswer> answers = new ArrayList<>();

        for (AnswerData answer : data.getAnswers()) {
            answers.add((BooleanAnswer) QuizzMaker.createAnswer(answer));
        }
        return new MultiCheckboxQuestion(
                (float) data.getDifficulty(),
                data.getText(),
                data.getPointsPossible(),
                data.getExplanation(),
                data.getImageLink(),
                data.isShuffledAnswers(),
                answers.toArray(new BooleanAnswer[0]));
    }
}
