package dev.miaxion.protocol;

public final class ProtocolConstants {
    private ProtocolConstants() {}

    public static final String VERSION = "1.0";
    public static final String RECIPE_CAPABILITY_VERSION = "1.9";
    public static final String RECIPE_SOURCE_CAPABILITY_VERSION = "1.3";
    public static final int RECIPE_SOURCE_PAGE_DEFAULT = 4096;
    public static final int RECIPE_SOURCE_PAGE_MAX = 4096;
    public static final int RECIPE_SOURCE_CURSOR_MAX = 128;
    public static final String QUEST_CAPABILITY_VERSION = "1.1";
    public static final String ENTITY_RESOLVE_CAPABILITY_VERSION = "1.1";
    public static final String WORLD_NEARBY_CAPABILITY_VERSION = "1.2";
    public static final String WORLD_STRUCTURE_TARGETS_CAPABILITY_VERSION = "1.1";
    public static final String ENTITY_DESCRIBE_CAPABILITY_VERSION = "1.0";
    public static final String MOD_VERSIONS_RESOLVE_CAPABILITY_VERSION = "1.0";
    public static final int QUEST_QUERY_RESULT_MAX = 32;
    public static final int QUEST_DETAIL_COLLECTION_MAX = 128;
    public static final int QUEST_DESCRIPTION_LINE_SCALARS_MAX = 1024;
    public static final int QUEST_TITLE_SCALARS_MAX = 256;
    public static final long QUEST_PROGRESS_INTEGER_MAX = 9007199254740991L;
    public static final int RECIPE_SLOT_ALTERNATIVES_MAX = 16384;
    public static final int RECIPE_INPUT_SLOTS_MAX = 4096;
    public static final int RECIPE_CATALYST_SLOTS_MAX = 4096;
    public static final int RECIPE_OUTPUT_SLOTS_MAX = 4096;
    public static final int WORLD_QUERY_RESULT_MAX = 128;
    public static final int WORLD_QUERY_MACHINE_FEATURE_MAX = 32;
    public static final int WORLD_QUERY_CONTAINER_STACK_MAX = 1024;
    public static final int WORLD_QUERY_NAME_CANDIDATE_MAX = 8;
    public static final int WORLD_QUERY_WARNING_MAX = 16;
    public static final int WORLD_QUERY_STRUCTURE_PIECE_MAX = 256;
}
