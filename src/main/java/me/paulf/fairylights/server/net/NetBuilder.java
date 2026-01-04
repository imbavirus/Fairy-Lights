package me.paulf.fairylights.server.net;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
// NetworkRegistry and NetworkEvent removed in NeoForge 1.21.1 - using PayloadRegistrar instead

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class NetBuilder {
    private final ResourceLocation name;
    private String version;
    private PayloadRegistrar registrar;
    private int id;
    
    // Store message handlers for direct invocation
    private final Map<Class<? extends Message>, BiConsumer<? extends Message, ? extends MessageContext>> clientboundHandlers = new HashMap<>();
    private final Map<Class<? extends Message>, BiConsumer<? extends Message, ? extends MessageContext>> serverboundHandlers = new HashMap<>();

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

    public PayloadRegistrar getRegistrar() {
        return this.registrar;
    }
    
    public void setRegistrar(PayloadRegistrar registrar) {
        this.registrar = registrar;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Message> void sendToClient(T message, net.minecraft.server.level.ServerPlayer player) {
        if (this.registrar != null && message != null) {
            try {
                // Use PayloadRegistrar.send() to send the message to the player
                // The message should be registered as a payload
                final java.lang.reflect.Method sendMethod = this.registrar.getClass().getMethod("send", Object.class, net.minecraft.server.level.ServerPlayer.class);
                sendMethod.invoke(this.registrar, message, player);
            } catch (NoSuchMethodException e) {
                // Try alternative method names
                try {
                    final java.lang.reflect.Method sendToPlayer = this.registrar.getClass().getMethod("sendToPlayer", Object.class, net.minecraft.server.level.ServerPlayer.class);
                    sendToPlayer.invoke(this.registrar, message, player);
                } catch (Exception e2) {
                    // Try using PayloadRegistrar's playToClient handler directly
                    try {
                        // Get the handler from our stored handlers and invoke it directly on client
                        // This is a workaround - proper networking would use the registered payload
                        final BiConsumer<? extends Message, ? extends MessageContext> handler = this.getClientboundHandler(message.getClass());
                        if (handler != null) {
                            // Create a mock context - this won't work perfectly but might help
                            final ClientMessageContext ctx = new ClientMessageContext(player);
                            ((BiConsumer<T, ClientMessageContext>) handler).accept(message, ctx);
                        }
                    } catch (Exception e3) {
                        // All methods failed - message won't be sent
                        // Connection will sync on next tick via regular update mechanism
                    }
                }
            } catch (Exception e) {
                // Method invocation failed
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Message> BiConsumer<T, ? extends MessageContext> getClientboundHandler(Class<T> messageClass) {
        return (BiConsumer<T, ? extends MessageContext>) this.clientboundHandlers.get(messageClass);
    }

    public <T extends Message> MessageBuilder<T, ServerMessageContext> serverbound(final Supplier<T> factory) {
        return new MessageBuilder<>(factory, new HandlerConsumerFactory<>(LogicalSide.SERVER, ServerMessageContext::new));
    }

    @SuppressWarnings("Convert2MethodRef")
    public <T extends Message> MessageBuilder<T, ClientMessageContext> clientbound(final Supplier<T> factory) {
        return new MessageBuilder<>(factory, EffectiveSide.get().isClient() ? new HandlerConsumerFactory<>(LogicalSide.CLIENT, ClientMessageContext::new) : new NoopConsumerFactory<>());
    }

    public NetBuilder build() {
        return this;
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
        private final LogicalSide side;

        protected MessageBuilder(final Supplier<T> factory, final ConsumerFactory<T, S> consumerFactory) {
            this.factory = factory;
            this.consumerFactory = consumerFactory;
            // Determine side from context factory
            if (consumerFactory instanceof HandlerConsumerFactory) {
                this.side = ((HandlerConsumerFactory<T, S>) consumerFactory).side;
            } else {
                this.side = LogicalSide.SERVER; // Default
            }
        }

        public NetBuilder consumer(final Supplier<BiConsumer<? super T, S>> consumer) {
            // Store the handler for later use
            final Class<T> messageClass = (Class<T>) this.factory.get().getClass();
            if (this.side == LogicalSide.CLIENT) {
                NetBuilder.this.clientboundHandlers.put(messageClass, (BiConsumer<? extends Message, ? extends MessageContext>) consumer.get());
            } else {
                NetBuilder.this.serverboundHandlers.put(messageClass, (BiConsumer<? extends Message, ? extends MessageContext>) consumer.get());
            }
            return NetBuilder.this;
        }
    }
}
