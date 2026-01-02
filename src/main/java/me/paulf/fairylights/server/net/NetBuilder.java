package me.paulf.fairylights.server.net;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.fml.LogicalSide;
// NetworkRegistry and NetworkEvent removed in NeoForge 1.21.1 - using PayloadRegistrar instead
// This is a temporary stub implementation - needs full rewrite to PayloadRegistrar API

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class NetBuilder {
    // TODO: Rewrite to use PayloadRegistrar API for NeoForge 1.21.1
    private final ResourceLocation name;
    private String version;
    private Object channel; // Placeholder - will be PayloadRegistrar
    private int id;

    public NetBuilder(final ResourceLocation name) {
        this.name = name;
    }

    public NetBuilder version(final int version) {
        return this.version(String.valueOf(version));
    }

    public NetBuilder version(final String version) {
        if (this.version == null) {
            this.version = Objects.requireNonNull(version);
            return this;
        }
        throw new IllegalArgumentException("version already assigned");
    }

    public NetBuilder optionalServer() {
        // TODO: Implement with PayloadRegistrar
        return this;
    }

    public NetBuilder requiredServer() {
        // TODO: Implement with PayloadRegistrar
        return this;
    }

    public NetBuilder optionalClient() {
        // TODO: Implement with PayloadRegistrar
        return this;
    }

    public NetBuilder requiredClient() {
        // TODO: Implement with PayloadRegistrar
        return this;
    }

    private Object channel() {
        if (this.channel == null) {
            // TODO: Create PayloadRegistrar here
            this.channel = new Object(); // Placeholder
        }
        return this.channel;
    }

    public <T extends Message> MessageBuilder<T, ServerMessageContext> serverbound(final Supplier<T> factory) {
        return new MessageBuilder<>(factory, new HandlerConsumerFactory<>(LogicalSide.SERVER, ServerMessageContext::new));
    }

    @SuppressWarnings("Convert2MethodRef")
    public <T extends Message> MessageBuilder<T, ClientMessageContext> clientbound(final Supplier<T> factory) {
        return new MessageBuilder<>(factory, EffectiveSide.get().isClient() ? new HandlerConsumerFactory<>(LogicalSide.CLIENT, ClientMessageContext::new) : new NoopConsumerFactory<>());
    }

    public Object build() {
        return this.channel();
    }

    // TODO: Rewrite to use PayloadRegistrar API
    interface ConsumerFactory<T extends Message, S extends MessageContext> {
        BiConsumer<T, Supplier<Object>> create(final Supplier<BiConsumer<? super T, S>> handlerFactory);
    }

    private static class NoopConsumerFactory<T extends Message, S extends MessageContext> implements ConsumerFactory<T, S> {
        @Override
        public BiConsumer<T, Supplier<Object>> create(final Supplier<BiConsumer<? super T, S>> handlerFactory) {
            return (msg, ctx) -> {
                // TODO: Implement with PayloadRegistrar
            };
        }
    }

    private static class HandlerConsumerFactory<T extends Message, S extends MessageContext> implements ConsumerFactory<T, S> {
        private final LogicalSide side;
        private final Function<Object, S> contextFactory;

        HandlerConsumerFactory(final LogicalSide side, final Function<Object, S> contextFactory) {
            this.side = side;
            this.contextFactory = contextFactory;
        }

        @Override
        public BiConsumer<T, Supplier<Object>> create(final Supplier<BiConsumer<? super T, S>> handlerFactory) {
            final BiConsumer<? super T, S> handler = handlerFactory.get();
            return (msg, ctx) -> {
                // TODO: Implement with PayloadRegistrar
                // This needs to be rewritten to use the new PayloadRegistrar API
            };
        }
    }

    public class MessageBuilder<T extends Message, S extends MessageContext> {
        private final Supplier<T> factory;
        private final ConsumerFactory<T, S> consumerFactory;

        protected MessageBuilder(final Supplier<T> factory, final ConsumerFactory<T, S> consumerFactory) {
            this.factory = factory;
            this.consumerFactory = consumerFactory;
        }

        public NetBuilder consumer(final Supplier<BiConsumer<? super T, S>> consumer) {
            // TODO: Register payload with PayloadRegistrar
            // This needs to be completely rewritten for NeoForge 1.21.1
            return NetBuilder.this;
        }
    }
}
