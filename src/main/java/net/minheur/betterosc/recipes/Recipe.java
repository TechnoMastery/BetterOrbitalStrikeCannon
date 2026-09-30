package net.minheur.betterosc.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record Recipe(String recipeId, ResultContainer result, List<IngredientMatcher> ingredients) {

    public Recipe {
        ingredients = List.copyOf(ingredients);
        Objects.requireNonNull(recipeId, "recipeId");
        Objects.requireNonNull(result, "result");
    }

    public Recipe(String recipeId, ResultContainer result, IngredientMatcher... ingredients) {
        this(recipeId, result, List.of(ingredients));
    }

    /**
     * Checks if the current recipe can be completed with given resources
     * @param availableStacks resources available, likely stored in tanks
     * @return weather the recipe can be completed
     */
    public boolean matches(List<ItemStack> availableStacks) {
        List<ItemStack> remaining = new ArrayList<>(availableStacks);

        for (IngredientMatcher ingredient : ingredients) {
            boolean found = false;
            for (int i = 0; i < remaining.size(); i++) {
                ItemStack candidate = remaining.get(i);
                if (ingredient.matches(candidate)) {
                    found = true;
                    remaining.remove(i);
                    break;
                }
            }
            if (!found) return false;
        }

        return true;
    }

    /**
     * Gets the recipe from a JSON file got in the config folder
     * @param json the object to parse the recipe from
     * @return extracted recipe
     * @throws IllegalArgumentException if null JSON
     * @throws IllegalStateException if invalid JSON
     */
    @Contract("null -> fail")
    public static @NonNull Recipe fromJson(JsonObject json) {
        if (json == null || json.isJsonNull())
            throw new IllegalArgumentException("Recipe JSON cannot be null");

        if (!json.has("id")) throw new IllegalStateException("Recipe JSON has no id!");

        String id = json.get("id").getAsString();
        ResultContainer result = decodeResult(json.getAsJsonObject("result"));

        JsonArray ingredientArray = json.has("ingredients") ? json.getAsJsonArray("ingredients") : new JsonArray();
        List<IngredientMatcher> ingredients = new ArrayList<>();
        for (JsonElement element : ingredientArray)
            ingredients.add(IngredientMatcher.fromJson(element.getAsJsonObject()));

        return new Recipe(id, result, ingredients);
    }

    @Contract("_ -> new")
    private static @NonNull ResultContainer decodeResult(@NonNull JsonObject object) {
        // decode as 'osc' item, if applicable
        String id = object.getAsJsonObject().get("id").getAsString();
        if (id.startsWith("betterosc:")) {
            JsonObject extra = object.has("extra") ?
                    object.getAsJsonObject("extra") :
                    new JsonObject();
            return new ResultContainer(id.substring(id.indexOf(':') +1), extra);
        }

        return new ResultContainer(decodeStack(object));
    }

    /**
     * Decodes a stack with Minecraft's CODEC. Used for identical stacks need, like the recipe result.
     * @param element JSON to extract the stack from
     * @return extracted stack
     * @throws IllegalArgumentException if the JSON is null or invalid
     */
    @Contract("null -> fail")
    private static @NonNull ItemStack decodeStack(JsonElement element) {
        if (element == null || element.isJsonNull())
            throw new IllegalArgumentException("Recipe result can't be null!");

        var dataResult = ItemStack.CODEC.parse(JsonOps.INSTANCE, element);
        if (dataResult.error().isPresent())
            throw new IllegalArgumentException("Invalid item stack JSON: " + dataResult.error().get().message());

        return dataResult.result().orElseThrow(() -> new IllegalArgumentException("Item stack JSON returned no value"));
    }

    public record IngredientMatcher(ItemStack itemStack, int count, MatchRules rules) {

        public IngredientMatcher(ItemStack itemStack, int count, MatchRules rules) {
            this.itemStack = Objects.requireNonNull(itemStack, "itemStack");
            this.count = Math.max(1, count);
            this.rules = rules == null ? MatchRules.EMPTY : rules;
        }

        public IngredientMatcher(ItemStack itemStack, MatchRules rules) {
            this(itemStack, itemStack.getCount(), rules);
        }

        public boolean matches(ItemStack candidate) {
            if (candidate == null || candidate.isEmpty()) return false;
            if (!candidate.isOf(itemStack.getItem())) return false;
            if (candidate.getCount() < count) return false;

            return rules.matches(candidate);
        }

        @Contract("null -> fail")
        public static @NonNull IngredientMatcher fromJson(JsonObject json) {
            if (json == null || json.isJsonNull())
                throw new IllegalArgumentException("Ingredient JSON cannot be null");

            ItemStack itemStack = decodeStack(json);
            int count = json.has("count") ? json.get("count").getAsInt() : Math.max(1, itemStack.getCount());
            MatchRules rules = MatchRules.fromJson(json.has("match") ? json.getAsJsonObject("match") : new JsonObject());
            return new IngredientMatcher(itemStack.copyWithCount(count), count, rules);
        }
    }

    public static final class MatchRules {
        public static final MatchRules EMPTY = new MatchRules(Collections.emptyMap(), Collections.emptyMap(), Collections.emptySet());

        private final Map<String, JsonElement> required;
        private final Map<String, JsonElement> forbidden;
        private final Set<String> ignored;

        public MatchRules(Map<String, JsonElement> required, Map<String, JsonElement> forbidden, Set<String> ignored) {
            this.required = required == null ? Collections.emptyMap() : Collections.unmodifiableMap(required);
            this.forbidden = forbidden == null ? Collections.emptyMap() : Collections.unmodifiableMap(forbidden);
            this.ignored = ignored == null ? Collections.emptySet() : Set.copyOf(ignored);
        }

        public boolean matches(ItemStack stack) {
            JsonElement serialized = encodeStack(stack);

            for (Map.Entry<String, JsonElement> entry : required.entrySet()) {
                if (shouldIgnore(entry.getKey())) continue;
                if (!jsonContains(serialized, entry.getValue())) return false;
            }

            for (Map.Entry<String, JsonElement> entry : forbidden.entrySet()) {
                if (shouldIgnore(entry.getKey())) continue;
                boolean contains = entry.getValue().isJsonNull()
                        ? jsonContainsPath(serialized, entry.getKey())
                        : jsonContains(serialized, entry.getValue());
                if (contains) return false;
            }

            return true;
        }

        private boolean shouldIgnore(String key) {
            for (String ignoredKey : ignored)
                if (ignoredKey.equals(key) || ignoredKey.startsWith(key + ".") || key.startsWith(ignoredKey + ".")) return true;
            return false;
        }

        public static MatchRules fromJson(JsonObject json) {
            if (json == null || json.isJsonNull()) return EMPTY;

            Map<String, JsonElement> required = new java.util.LinkedHashMap<>();
            Map<String, JsonElement> forbidden = new java.util.LinkedHashMap<>();
            Set<String> ignored = new java.util.LinkedHashSet<>();

            if (json.has("required") && !json.get("required").isJsonNull()) {
                JsonObject requiredJson = json.getAsJsonObject("required");
                for (Map.Entry<String, JsonElement> entry : requiredJson.entrySet())
                    required.put(entry.getKey(), entry.getValue());
            }

            if (json.has("forbidden") && !json.get("forbidden").isJsonNull()) {
                JsonElement forbiddenValue = json.get("forbidden");

                if (forbiddenValue.isJsonArray()) {
                    for (JsonElement element : forbiddenValue.getAsJsonArray())
                        forbidden.put(element.getAsString(), JsonNull.INSTANCE);

                } else if (forbiddenValue.isJsonObject()) {
                    JsonObject forbiddenJson = forbiddenValue.getAsJsonObject();

                    for (Map.Entry<String, JsonElement> entry : forbiddenJson.entrySet())
                        forbidden.put(entry.getKey(), entry.getValue());
                }
            }

            if (json.has("ignored") && !json.get("ignored").isJsonNull()) {
                JsonElement ignoredValue = json.get("ignored");
                if (ignoredValue.isJsonArray()) {
                    for (JsonElement element : ignoredValue.getAsJsonArray())
                        ignored.add(element.getAsString());
                } else if (ignoredValue.isJsonObject()) {
                    for (Map.Entry<String, JsonElement> entry : ignoredValue.getAsJsonObject().entrySet())
                        ignored.add(entry.getKey());
                }
            }

            return new MatchRules(required, forbidden, ignored);
        }
    }

    public static final class ResultContainer {
        private final ItemStack resultAsStack;

        private final String id;
        private final JsonObject extra;

        @Contract("null -> fail")
        public ResultContainer(ItemStack resultAsStack) {
            if (resultAsStack == null) throw new IllegalArgumentException("Can't make recipe result with null stack!");
            this.resultAsStack = resultAsStack;

            id = null;
            extra = null;
        }
        @Contract("null, _ -> fail")
        public ResultContainer(String id, JsonObject extra) {
            if (id == null) throw new IllegalArgumentException("Can't have recipe result with null id!");
            this.id = id;
            this.extra = extra == null || extra.isJsonNull() ? new JsonObject() : extra;

            resultAsStack = null;
        }

        public boolean getIsByStack() {
            return id == null;
        }

        public ItemStack getAsStack() {
            return resultAsStack;
        }

        public String getId() {
            return id;
        }
        public JsonObject getExtra() {
            return extra;
        }
    }

    private static JsonElement encodeStack(ItemStack stack) {
        return ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, stack).result().orElseThrow(() -> new IllegalArgumentException("Failed to serialize stack as JSON"));
    }

    private static boolean jsonContains(JsonElement root, JsonElement expected) {
        if (root == null || expected == null) return false;

        if (expected.isJsonObject() && root.isJsonObject()) {
            JsonObject expectedObject = expected.getAsJsonObject();
            JsonObject rootObject = root.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : expectedObject.entrySet()) {
                if (!rootObject.has(entry.getKey())) return false;
                if (!jsonContains(rootObject.get(entry.getKey()), entry.getValue())) return false;
            }
            return true;
        }

        if (expected.isJsonArray() && root.isJsonArray()) {
            JsonArray expectedArray = expected.getAsJsonArray();
            JsonArray rootArray = root.getAsJsonArray();
            if (expectedArray.size() > rootArray.size()) return false;
            for (int i = 0; i < expectedArray.size(); i++)
                if (!jsonContains(rootArray.get(i), expectedArray.get(i))) return false;

            return true;
        }

        return root.equals(expected);
    }

    private static boolean jsonContainsPath(JsonElement root, String path) {
        JsonElement current = root;
        for (String key : path.split("\\.")) {
            if (!current.isJsonObject() || !current.getAsJsonObject().has(key)) return false;
            current = current.getAsJsonObject().get(key);
        }
        return true;
    }
}
