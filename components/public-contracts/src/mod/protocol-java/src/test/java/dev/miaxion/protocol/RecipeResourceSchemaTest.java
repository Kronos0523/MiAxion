package dev.miaxion.protocol;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecipeResourceSchemaTest {
    private static final String RESOURCE = """
        {"kind":"fluid","resourceId":"demo:water","ingredientTypeUid":"native.fluid",
         "numerator":"270","denominator":"1","unit":"mB"}
        """;
    private static String response(String alternative) {
        return """
            {"recipes":[{"recipeId":"demo:machine","type":"demo:machine","sourceModId":"demo",
            "identity":{"kind":"registered"},"inputs":[{"alternatives":[%s]}],
            "catalysts":[],"outputs":[{"alternatives":[{"itemId":"demo:result","count":1}]}]}],
            "truncated":false,"resourceWarnings":["RESOURCE_QUANTITY_UNKNOWN"]}
            """.formatted(alternative);
    }
    @Test void actualResponseSchemaAcceptsMixedAndUnknownAmountsButRejectsBothEncodingsAndZeroDenominator() {
        assertTrue(ProtocolCorpusTest.validationMessages("recipe.schema.json", response("{\"resource\":"+RESOURCE+"}")).isEmpty());
        assertTrue(ProtocolCorpusTest.validationMessages("recipe.schema.json", response("{\"resource\":{\"kind\":\"custom\",\"resourceId\":\"demo:mana\",\"ingredientTypeUid\":\"mana.api\"}}")).isEmpty());
        assertFalse(ProtocolCorpusTest.validationMessages("recipe.schema.json", response("{\"resource\":"+RESOURCE.replace("\"denominator\":\"1\"", "\"denominator\":\"0\"")+"}")).isEmpty());
        assertFalse(ProtocolCorpusTest.validationMessages("recipe.schema.json", response("{\"itemId\":\"demo:water\",\"count\":1,\"resource\":"+RESOURCE+"}")).isEmpty());
    }
    @Test void productionRequestSchemaAcceptsExactTypeWithoutChangingOldIdentityField() {
        var registry = SchemaRegistry.load();
        String request = new String(ProtocolValidatorTestFixture.validRequest(), java.nio.charset.StandardCharsets.UTF_8)
            .replace("health.ping", "recipe.query")
            .replace("\"nonce\":\"fixed-nonce\"", "\"itemId\":\"demo:water\",\"resourceKind\":\"fluid\",\"ingredientTypeUid\":\"native.fluid\"");
        assertTrue(registry.validateKnown(ProtocolJson.validatorTree(request.getBytes(java.nio.charset.StandardCharsets.UTF_8)), KnownProtocolAction.RECIPE_QUERY).isEmpty());
    }
}
