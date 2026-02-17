package za.co.infernos.fairylights;

import za.co.infernos.fairylights.server.ServerProxy;
import za.co.infernos.fairylights.server.block.FLBlocks;
import za.co.infernos.fairylights.server.block.entity.FLBlockEntities;
import za.co.infernos.fairylights.server.connection.ConnectionType;
import za.co.infernos.fairylights.server.connection.ConnectionTypes;
import za.co.infernos.fairylights.server.creativetabs.FairyLightsItemGroup;
import za.co.infernos.fairylights.server.entity.FLEntities;
import za.co.infernos.fairylights.server.item.FLItems;
import za.co.infernos.fairylights.server.item.crafting.FLCraftingRecipes;
import za.co.infernos.fairylights.server.net.NetBuilder;
import za.co.infernos.fairylights.server.net.clientbound.JingleMessage;
import za.co.infernos.fairylights.server.net.clientbound.OpenEditLetteredConnectionScreenMessage;
import za.co.infernos.fairylights.server.net.clientbound.UpdateEntityFastenerMessage;
import za.co.infernos.fairylights.server.net.serverbound.EditLetteredConnectionMessage;
import za.co.infernos.fairylights.server.net.serverbound.InteractionConnectionMessage;
import za.co.infernos.fairylights.server.sound.FLSounds;
import za.co.infernos.fairylights.server.string.StringType;
import za.co.infernos.fairylights.server.string.StringTypes;
import za.co.infernos.fairylights.util.CalendarEvent;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.time.Month;
import java.util.function.Supplier;

import za.co.infernos.fairylights.server.ServerEventHandler;

@Mod(FairyLights.ID)
public final class FairyLights {
    public static final String ID = "fairylights";

    public static final ResourceLocation STRING_TYPE = ResourceLocation.fromNamespaceAndPath(ID, "string_type");

    public static final ResourceLocation CONNECTION_TYPE = ResourceLocation.fromNamespaceAndPath(ID, "connection_type");

    @SuppressWarnings("Convert2MethodRef")
    public static final Object NETWORK = new NetBuilder(ResourceLocation.fromNamespaceAndPath(ID, "net"))
            .version(1).optionalServer().requiredClient()
            .clientbound(JingleMessage::new)
            .consumer(() -> (msg, ctx) -> {
                if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
                    za.co.infernos.fairylights.client.net.ClientMessageHandlers.handleJingle((JingleMessage) msg, ctx);
                }
            })
            .clientbound(() -> new UpdateEntityFastenerMessage(0, new net.minecraft.nbt.CompoundTag()))
            .consumer(() -> (msg, ctx) -> {
                if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
                    za.co.infernos.fairylights.client.net.ClientMessageHandlers.handleUpdateEntityFastener((UpdateEntityFastenerMessage) msg, ctx);
                }
            })
            .clientbound(OpenEditLetteredConnectionScreenMessage::new)
            .consumer(() -> (msg, ctx) -> {
                if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
                    za.co.infernos.fairylights.client.net.ClientMessageHandlers.handleOpenScreen((OpenEditLetteredConnectionScreenMessage) msg, ctx);
                }
            })
            .serverbound(InteractionConnectionMessage::new)
            .consumer(() -> new InteractionConnectionMessage.Handler())
            .serverbound(EditLetteredConnectionMessage::new)
            .consumer(() -> new EditLetteredConnectionMessage.Handler())
            .build();

    public static final CalendarEvent CHRISTMAS = new CalendarEvent(Month.DECEMBER, 24, 26);

    public static final CalendarEvent HALLOWEEN = new CalendarEvent(Month.OCTOBER, 31, 31);

    public static Supplier<Registry<ConnectionType<?>>> CONNECTION_TYPES;
    public static Supplier<Registry<StringType>> STRING_TYPES;

    public FairyLights(final IEventBus modEventBus) {
        final IEventBus bus = modEventBus;

        FLSounds.REG.register(bus);
        FLBlocks.REG.register(bus);
        FLEntities.REG.register(bus);
        FLItems.REG.register(bus);
        FLBlockEntities.REG.register(bus);
        FLCraftingRecipes.REG.register(bus);
        ConnectionTypes.REG.register(bus);
        StringTypes.REG.register(bus);
        za.co.infernos.fairylights.server.item.FLDataComponents.REG.register(bus);

        // Create custom registries - ConnectionTypes and StringTypes already create the
        // DeferredRegisters
        // Set up registry access - will be available after registration
        // DeferredRegister may not have getRegistry() - try accessing through registry
        // manager
        // For now, use a supplier that accesses the registry when needed
        // registryOrThrow() needs a RegistryKey, not ResourceLocation
        CONNECTION_TYPES = () -> {
            try {
                return net.minecraft.core.RegistryAccess
                    .fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY)
                    .registryOrThrow(net.minecraft.resources.ResourceKey
                            .createRegistryKey(FairyLights.CONNECTION_TYPE));
            } catch (Exception e) {
                com.mojang.logging.LogUtils.getLogger().error("FL_DEBUG: Failed to get CONNECTION_TYPES registry", e);
                throw e;
            }
        };
        STRING_TYPES = () -> {
            try {
                return net.minecraft.core.RegistryAccess
                    .fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY)
                    .registryOrThrow(net.minecraft.resources.ResourceKey
                            .createRegistryKey(FairyLights.STRING_TYPE));
            } catch (Exception e) {
                com.mojang.logging.LogUtils.getLogger().error("FL_DEBUG: Failed to get STRING_TYPES registry", e);
                throw e;
            }
        };
        
        ServerProxy proxy;
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            try {
                proxy = (ServerProxy) Class.forName("za.co.infernos.fairylights.client.ClientProxy").getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate ClientProxy", e);
            }
        } else {
            proxy = new ServerProxy();
        }
        
        proxy.init(bus);
        FairyLightsItemGroup.TAB_REG.register(modEventBus);

        // Register networking payloads
        modEventBus.addListener((RegisterPayloadHandlersEvent event) -> {
            final PayloadRegistrar registrar = event.registrar(FairyLights.ID);
            final NetBuilder network = (NetBuilder) NETWORK;
            network.setRegistrar(registrar);

            // Register UpdateEntityFastenerMessage as clientbound payload
            registrar.playToClient(
                    UpdateEntityFastenerMessage.TYPE,
                    UpdateEntityFastenerMessage.STREAM_CODEC,
                    // Use the handler from network configuration (which delegates safely)
                    // Or replicate the safe delegation here if needed
                    (msg, ctx) -> {
                        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
                            za.co.infernos.fairylights.client.net.ClientMessageHandlers.handleUpdateEntityFastener(msg, new za.co.infernos.fairylights.server.net.ClientMessageContext(ctx));
                        }
                    });

            // Register OpenEditLetteredConnectionScreenMessage as clientbound payload
            registrar.playToClient(
                    OpenEditLetteredConnectionScreenMessage.TYPE,
                    OpenEditLetteredConnectionScreenMessage.STREAM_CODEC,
                    (msg, ctx) -> {
                         if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
                             za.co.infernos.fairylights.client.net.ClientMessageHandlers.handleOpenScreen(msg, new za.co.infernos.fairylights.server.net.ClientMessageContext(ctx));
                         }
                    });

            // Register EditLetteredConnectionMessage as serverbound payload
            registrar.playToServer(
                    EditLetteredConnectionMessage.TYPE,
                    EditLetteredConnectionMessage.STREAM_CODEC,
                    EditLetteredConnectionMessage::handle);

            // Register InteractionConnectionMessage as serverbound payload
            registrar.playToServer(
                    InteractionConnectionMessage.TYPE,
                    InteractionConnectionMessage.STREAM_CODEC,
                    InteractionConnectionMessage::handle);
        });

        // Register ServerEventHandler
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(ServerEventHandler.class);
    }
}
