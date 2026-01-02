package me.paulf.fairylights.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class FLClientConfig {
    private FLClientConfig() {}

    public static final class Tutorial {
        public final ModConfigSpec.ConfigValue<String> progress;

        private Tutorial(final ModConfigSpec.Builder builder) {
            builder.push("tutorial");
            this.progress = builder
                .comment(
                    "The hanging lights tutorial progress, once any light item enters the inventory a",
                    " toast appears prompting to craft hanging lights. A finished tutorial progress",
                    " value is 'complete' and an unstarted tutorial is 'none'."
                )
                .define("progress", "none");
            builder.pop();
        }
    }

    public static final Tutorial TUTORIAL;

    public static final ModConfigSpec SPEC;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        TUTORIAL = new Tutorial(builder);
        SPEC = builder.build();
    }
}
