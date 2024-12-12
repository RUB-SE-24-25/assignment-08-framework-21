package structra.assignment.task.impl;

import structra.assignment.framework.llm.KeyProvider;
import structra.assignment.framework.llm.MachineLearningModel;
import structra.assignment.framework.llm.gen.questions.OpenQuestionTarget;
import structra.assignment.framework.llm.gen.questions.QuestionGenerationTarget;
import structra.assignment.framework.llm.gen.questions.RandomTargetProvider;
import structra.assignment.framework.llm.gen.questions.TargetProvider;
import structra.assignment.framework.llm.model.Mimic;
import structra.assignment.framework.model.answer.base.Answer;
import structra.assignment.framework.model.question.QuestionType;
import structra.assignment.framework.model.question.base.Question;
import structra.assignment.framework.provide.ModelQuestionProvider;
import structra.assignment.framework.provide.QuestionProvider;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import javax.swing.*;

public class Example {


    /**
     * Create the GUI and show it. For thread safety, this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        // Create and set up the window
        JFrame frame = new JFrame("Quiz");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add the "Hello World" label to the center of the window
        JButton button = new JButton(" Get Next Question");
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));

        JLabel label1 = new JLabel("");
        panel.add(label1);
        JPanel buttonpanel = new JPanel();
        buttonpanel.setBackground(Color.GRAY);
        buttonpanel.add(button);
        JPanel whitepanel = new JPanel();
        whitepanel.setBackground(Color.WHITE);
        
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Create a new KeyProvider to provide the API key
                KeyProvider keyProvider = new ApiKeyProvider();

                // Pass your Plugin to the Model you want to use
                MachineLearningModel mimic = new Mimic(keyProvider);

                // If you wish to use a real model, just set the prompt to the default prompt for generating new questions (or provide your own)
                TargetProvider provider = new RandomTargetProvider(new OpenQuestionTarget(Mimic.OPEN_ANSWER));
                TargetProvider provider2 = new RandomTargetProvider(new MultipleChoiceTarget(Mimic.MULTIPLE_CHOICE));

                // Creates a new QuestionProvider with the model, provider and an empty list of questions, since
                // we do not want to pass any additional questions as context to the model.
                QuestionProvider questionProvider = new ModelQuestionProvider(mimic, provider2, new ArrayList<>());

                // Create a new CompletableFuture object, holding the question, once generation has finished
                CompletableFuture<Question<?>> future = questionProvider.next();
                // Handle an error without blocking
                future.exceptionally(throwable -> {
                    System.out.println("Task from future1 failed (non-blocking) failed with: " + throwable.getMessage());
                    return null;
                });
                try {
                // Use get() to block and get the result (throws exceptions that must be handled)
                    Question<?> result2 = future.get();
                    if (result2.getType().compareTo(QuestionType.MULTIPLE_CHOICE) == 0) {
                        label1.setText("<html>Question text: " + result2.getText()
                                + "<br>Difficulty: " + result2.getDifficulty()
                                + "<br>Points possible: " + result2.getPointsPossible()
                                + "<br>Explanation: " + result2.getExplanation()
                                + "<br>Answers: <br>" + result2.getAnswers()[0].getText()
                                + "<br>" + result2.getAnswers()[1].getText()
                                + "<br>" + result2.getAnswers()[2].getText()
                                + "<br>" + result2.getAnswers()[3].getText()
                                + "</html>"
                        );
                        frame.pack();
                        int currentFrameWidth = frame.getSize().width;
                        int currentFrameHeight = frame.getSize().height;
                        frame.setLocation((int) (width - currentFrameWidth) / 2, (int) (height - currentFrameHeight) / 2);
                    }
                    else {label1.setText("<html>Question text: " + result2.getText()
                            + "<br>Difficulty: " + result2.getDifficulty()
                            + "<br>Points possible: " + result2.getPointsPossible()
                            + "<br>Explanation: " + result2.getExplanation()
                            + "<br>Answers: <br>" + result2.getAnswers()[0].getText()
                            + "</html>");
                        frame.pack();
                        int currentFrameWidth = frame.getSize().width;
                        int currentFrameHeight = frame.getSize().height;
                        frame.setLocation((int) (width - currentFrameWidth) / 2, (int) (height - currentFrameHeight) / 2);
                    }
                } catch (Exception er) {
                    // Handle the exception
                    System.err.println("Error occurred: " + er.getMessage());
                }
            }
        });



        // Adjust position of the window
        int frameWidth = (int) (width / 2);
        int frameHeight = (int) (height / 4);
        frame.setLocation((int) (width - frameWidth) / 2, (int) (height - frameHeight) / 2);
        panel.setLayout(new GridLayout(0, 1));
        button.setBounds(0,frameHeight-50, 0, 30);
        frame.add(panel, BorderLayout.NORTH);
        frame.add(whitepanel, BorderLayout.CENTER);
        frame.add(buttonpanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setSize(frameWidth, frameHeight);


        // Display the window
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(Example::createAndShowGUI);
    }
}
