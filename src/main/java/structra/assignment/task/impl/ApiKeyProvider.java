package structra.assignment.task.impl;

import structra.assignment.framework.llm.KeyProvider;

public class ApiKeyProvider implements KeyProvider {
    String apiKey = "structra-1343abnc-dGhpcyBpcyBub3Qgb3VyIGFwaSBrZXksIG5pY2UgdHJ5IHRobyA6KQ==";
    public String getApiKey() {
        return apiKey;
    }
}
