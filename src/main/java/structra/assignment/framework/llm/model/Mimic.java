package structra.assignment.framework.llm.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import structra.assignment.framework.llm.KeyProvider;
import structra.assignment.framework.llm.MachineLearningModel;
import structra.assignment.framework.llm.context.specification.SystemContext;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides a "large language model" instance, which returns pre-defined answers based on
 * primitive criteria. If you don't want to use an API-key use this instance instead of {@link
 * ChatGPTModel}
 *
 * @author Artur Rausch
 */
public class Mimic implements MachineLearningModel {

    public static final String NONSENSE = "nonsense";
    public static final String MULTIPLE_CHOICE = "multiple_choice";
    public static final String OPEN_ANSWER = "open_answer";

    private static final String PSEUDO_REQUIRED_API_KEY =
            "structra-1343abnc-dGhpcyBpcyBub3Qgb3VyIGFwaSBrZXksIG5pY2UgdHJ5IHRobyA6KQ==";

    private static final double FAILURE_PROBABILITY = 0.1;

    private final Random random = new Random();

    private final KeyProvider keyProvider;

    public Mimic(KeyProvider keyProvider) {
        if (keyProvider == null) {
            throw new NullPointerException("key provider must not be null");
        }
        this.keyProvider = keyProvider;
    }

    /**
     * Sets the context. Ignored since this model does not generate different outputs based on the
     * provided context.
     */
    @Override
    public void setContext(String systemContext) {
        // Context is ignored in this simpler model
    }

    /**
     * {@inheritDoc} The prompt can be set to invoke the following behaviour:
     *
     * <ul>
     *   <li>Case: non-empty (checked via {@link #loadFromJSON(String)}
     *       <ul>
     *         <li>"multiple_choice" -> always returns a multiple choice question
     *         <li>"open_answer" -> always returns an open answer question
     *         <li>"nonsense" -> always returns nonsense json
     *       </ul>
     *   <li>Cases checked via {@link #contextDependentAction(JsonObject, String)}
     *       <ul>
     *         <li>Case: matches pattern [int(0,2)][int(0,49)] -> returns respective element from
     *             the underlying JSON
     *         <li>Case: empty -> returns multiple_choice with 40% probability, open_answer with 40%
     *             probability, fails otherwise
     *         <li>Case: null -> always fails
     *       </ul>
     * </ul>
     *
     * @see #loadFromJSON(String)
     * @see #contextDependentAction(JsonObject, String)
     */
    @Override
    public CompletableFuture<String> execute(String prompt) {
        String key = keyProvider.getApiKey();
        if (!key.equals(PSEUDO_REQUIRED_API_KEY)) {
            return CompletableFuture.failedFuture(new AccessDeniedException("Invalid api key"));
        }

        String res;

        try {
            if (random.nextFloat() < FAILURE_PROBABILITY) {
                throw new RuntimeException("Failed to generate question");
            }
            res = loadFromJSON(prompt);
        } catch (RuntimeException exception) {
            return CompletableFuture.failedFuture(exception);
        }

        return CompletableFuture.completedFuture(String.format("$%s$", res));
    }

    /**
     * Loads questions from a json of form
     *
     * <blockquote>
     *
     * <pre>
     *  {
     *     "nonsense": [
     *         // 50 json objects
     *         // does not have any consistent structure
     *     ],
     *     "open_answer": [
     *         // 50 json objects, with the following 'schema'
     *         {
     *            "Questions": {
     *              "Text": String,
     *              "Difficulty": float,
     *              "PointsPossible": int,
     *              "Explanation": String
     *           },
     *           "Answers": {
     *             "Text": String,
     *             "Expected": String
     *           }
     *         }
     *     ],
     *     "multiple_choice": [
     *         // 50 json objects, with the following 'schema'
     *         {
     *            "Questions": {
     *              "Text": String,
     *              "Difficulty": float,
     *              "PointsPossible": int,
     *              "Explanation": String
     *           },
     *           "Answers": [
     *             {
     *              "Text": String,
     *              "Expected": String (true/false)
     *             },
     *             ...,
     *             {
     *              "Text": String,
     *              "Expected": String (true/false)
     *             }
     *           ]
     *         }
     *     ]
     *  }
     * </pre>
     *
     * </blockquote>
     *
     * @return Returns different things based on the systemContext
     *     <ul>
     *       <li>Case: non-empty
     *           <ul>
     *             <li>"multiple_choice" -> always returns a multiple choice question
     *             <li>"open_answer" -> always returns an open answer question
     *             <li>"nonsense" -> always returns nonsense json
     *           </ul>
     *       <li>Cases checked via {@link #contextDependentAction(JsonObject, String)}
     *           <ul>
     *             <li>Case: matches pattern [int(0,2)][int(0,49)] -> returns respective element
     *                 from the underlying JSON
     *             <li>Case: empty -> returns multiple_choice with 40% probability, open_answer with
     *                 40% probability, fails otherwise
     *             <li>Case: null -> always fails
     *           </ul>
     *     </ul>
     */
    private String loadFromJSON(String prompt) throws RuntimeException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject jsonObject;

        try {
            FileReader reader = new FileReader("src/main/resources/questions.json");
            jsonObject = gson.fromJson(reader, JsonObject.class);
            reader.close();
        } catch (IOException e) {
            return "Error loading questions.json";
        }

        return switch (prompt) {
            case NONSENSE -> {
                final JsonArray a = jsonObject.get(NONSENSE).getAsJsonArray();
                yield a.get(random.nextInt(a.size())).toString();
            }
            case MULTIPLE_CHOICE -> {
                final JsonArray a = jsonObject.get(MULTIPLE_CHOICE).getAsJsonArray();
                yield a.get(random.nextInt(a.size())).toString();
            }
            case OPEN_ANSWER -> {
                final JsonArray a = jsonObject.get(OPEN_ANSWER).getAsJsonArray();
                yield a.get(random.nextInt(a.size())).toString();
            }
            default -> contextDependentAction(jsonObject, prompt);
        };
    }

    /**
     * Checks special cases of working with the JSON depending on the {@link SystemContext}
     *
     * @param json A json object of the previously specified form
     * @return a string representation version of the json object
     * @throws RuntimeException when a null value was provided for the context, or it contained a
     *     malformed value
     */
    private String contextDependentAction(final JsonObject json, String prompt)
            throws RuntimeException {
        final String[] types = new String[] {NONSENSE, OPEN_ANSWER, MULTIPLE_CHOICE};

        // check for malformed json
        if ((json.get(types[0]) == null)
                || (json.get(types[1]) == null)
                || (json.get(types[2]) == null)) {
            throw new RuntimeException("Failed to generate question");
        }

        // should match [0-2][0-45], so e.g. [0][45]
        final Pattern indexNotation = Pattern.compile("\\[([0-2])]\\[(\\d|[1-4]\\d)]");
        final Matcher matcher = indexNotation.matcher(prompt);

        int type = -1;
        int index = -1;

        while (matcher.find()) {
            type = Integer.parseInt(matcher.group(1));
            index = Integer.parseInt(matcher.group(2));
        }

        matcher.reset();

        // 2 pattern groups should exist in the match (0-1), (0-49)
        if ((type != -1) && (index != -1)) {

            return json.get(types[type]).getAsJsonArray().get(index).toString();

        } else if (prompt.isBlank()) {
            final float rng = random.nextFloat();
            final int maxSize = 50;

            if (rng < 0.4f) {
                return json.get(OPEN_ANSWER)
                        .getAsJsonArray()
                        .get(random.nextInt(maxSize))
                        .toString();
            } else if (rng < 0.8f) {
                return json.get(MULTIPLE_CHOICE)
                        .getAsJsonArray()
                        .get(random.nextInt(maxSize))
                        .toString();
            } else {
                return json.get(NONSENSE)
                        .getAsJsonArray()
                        .get(random.nextInt(maxSize))
                        .toString();
            }
        } else {
            throw new RuntimeException("Failed to generate question");
        }
    }
}
