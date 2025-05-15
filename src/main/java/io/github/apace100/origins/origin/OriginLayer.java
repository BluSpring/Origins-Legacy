package io.github.apace100.origins.origin;

import com.google.common.collect.Lists;
import com.google.gson.*;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class OriginLayer implements Comparable<OriginLayer> {
    public static final StreamCodec<RegistryFriendlyByteBuf, OriginLayer> STREAM_CODEC = StreamCodec.of((buf, value) -> value.write(buf), OriginLayer::read);

    private int order;
    private ResourceLocation identifier;
    private List<ConditionedOrigin> conditionedOrigins;
    private boolean enabled = false;

    private String nameTranslationKey;
    private String titleViewOriginTranslationKey;
    private String titleChooseOriginTranslationKey;
    private String missingOriginNameTranslationKey;
    private String missingOriginDescriptionTranslationKey;

    private boolean isRandomAllowed = false;
    private boolean doesRandomAllowUnchoosable = false;
    private List<ResourceLocation> originsExcludedFromRandom = null;

    private ResourceLocation defaultOrigin = null;
    private boolean autoChooseIfNoChoice = false;

    private boolean hidden = false;
    private boolean overrideViewOriginTitle = false;
    private boolean overrideChooseOriginTitle = false;

    public String getOrCreateTranslationKey() {
        if(nameTranslationKey == null || nameTranslationKey.isEmpty()) {
            this.nameTranslationKey = "layer." + identifier.getNamespace() + "." + identifier.getPath() + ".name";
        }
        return nameTranslationKey;
    }

    public String getTranslationKey() {
        return getOrCreateTranslationKey();
    }

    public String getMissingOriginNameTranslationKey() {
        if(missingOriginNameTranslationKey == null || missingOriginNameTranslationKey.isEmpty()) {
            this.missingOriginNameTranslationKey = "layer." + identifier.getNamespace() + "." + identifier.getPath() + ".missing_origin.name";
        }
        return missingOriginNameTranslationKey;
    }

    public String getTitleViewOriginTranslationKey() {
        if(titleViewOriginTranslationKey == null || titleViewOriginTranslationKey.isEmpty()) {
            this.titleViewOriginTranslationKey = "layer." + identifier.getNamespace() + "." + identifier.getPath() + ".view_origin.name";
        }
        return titleViewOriginTranslationKey;
    }

    public boolean shouldOverrideViewOriginTitle() {
        return overrideViewOriginTitle;
    }

    public String getTitleChooseOriginTranslationKey() {
        if(titleChooseOriginTranslationKey == null || titleChooseOriginTranslationKey.isEmpty()) {
            this.titleChooseOriginTranslationKey = "layer." + identifier.getNamespace() + "." + identifier.getPath() + ".choose_origin.name";
        }
        return titleChooseOriginTranslationKey;
    }

    public boolean shouldOverrideChooseOriginTitle() {
        return overrideChooseOriginTitle;
    }

    public String getMissingOriginDescriptionTranslationKey() {
        if(missingOriginDescriptionTranslationKey == null || missingOriginDescriptionTranslationKey.isEmpty()) {
            this.missingOriginDescriptionTranslationKey = "layer." + identifier.getNamespace() + "." + identifier.getPath() + ".missing_origin.description";
        }
        return missingOriginDescriptionTranslationKey;
    }

    public ResourceLocation getIdentifier() {
        return identifier;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean hasDefaultOrigin() {
        return defaultOrigin != null;
    }

    public ResourceLocation getDefaultOrigin() {
        return defaultOrigin;
    }

    public boolean shouldAutoChoose() {
        return autoChooseIfNoChoice;
    }

    public List<ResourceLocation> getOrigins() {
        return conditionedOrigins.stream().flatMap(co -> co.getOrigins().stream()).filter(OriginRegistry::contains).collect(Collectors.toList());
    }

    public List<ResourceLocation> getOrigins(Player playerEntity) {
        return conditionedOrigins.stream().filter(co -> co.isConditionFulfilled(playerEntity)).flatMap(co -> co.getOrigins().stream()).filter(OriginRegistry::contains).collect(Collectors.toList());
    }

    public int getOriginOptionCount(Player playerEntity) {
        long choosableOrigins = getOrigins(playerEntity).stream().map(OriginRegistry::get).filter(Origin::isChoosable).count();
        if(isRandomAllowed && getRandomOrigins(playerEntity).size() > 0) {
            choosableOrigins++;
        }
        return (int)choosableOrigins;
    }

    public boolean contains(Origin origin) {
        return conditionedOrigins.stream().anyMatch(co -> co.getOrigins().stream().anyMatch(o -> o.equals(origin.getIdentifier())));
    }

    public boolean contains(Origin origin, Player playerEntity) {
        return conditionedOrigins.stream().filter(co -> co.isConditionFulfilled(playerEntity)).anyMatch(co -> co.getOrigins().stream().anyMatch(o -> o.equals(origin.getIdentifier())));
    }

    public boolean isRandomAllowed() {
        return isRandomAllowed;
    }

    public boolean isHidden() {
        return hidden;
    }

    public List<ResourceLocation> getRandomOrigins(Player playerEntity) {
        return conditionedOrigins.stream().filter(co -> co.isConditionFulfilled(playerEntity)).flatMap(co -> co.getOrigins().stream()).filter(OriginRegistry::contains).filter(o -> !originsExcludedFromRandom.contains(o)).filter(id -> doesRandomAllowUnchoosable || OriginRegistry.get(id).isChoosable()).collect(Collectors.toList());
    }

    public void merge(JsonObject json) {
        if(json.has("order")) {
            this.order = json.get("order").getAsInt();
        }
        if(json.has("enabled")) {
            this.enabled = json.get("enabled").getAsBoolean();
        }
        if(json.has("layers")) {
            JsonArray originArray = json.getAsJsonArray("layers");
            originArray.forEach(je -> this.conditionedOrigins.add(ConditionedOrigin.read(je)));
        }
        if(json.has("name")) {
            this.nameTranslationKey = GsonHelper.getAsString(json, "name", "");
        }
        if(json.has("gui_title")) {
            JsonObject guiTitleObj = json.getAsJsonObject("gui_title");
            if(guiTitleObj.has("view_origin")) {
                this.titleViewOriginTranslationKey = GsonHelper.getAsString(guiTitleObj, "view_origin", "");
                this.overrideViewOriginTitle = true;
            }
            if(guiTitleObj.has("choose_origin")) {
                this.titleChooseOriginTranslationKey = GsonHelper.getAsString(guiTitleObj, "choose_origin", "");
                this.overrideChooseOriginTitle = true;
            }
        }
        if(json.has("missing_name")) {
            this.missingOriginNameTranslationKey = GsonHelper.getAsString(json, "missing_name", "");
        }
        if(json.has("missing_description")) {
            this.missingOriginDescriptionTranslationKey = GsonHelper.getAsString(json, "missing_description", "");
        }
        if(json.has("allow_random")) {
            this.isRandomAllowed = GsonHelper.getAsBoolean(json, "allow_random");
        }
        if(json.has("allow_random_unchoosable")) {
            this.doesRandomAllowUnchoosable = GsonHelper.getAsBoolean(json, "allow_random_unchoosable");
        }
        if(json.has("exclude_random") && json.get("exclude_random").isJsonArray()) {
            boolean replaceExclude = GsonHelper.getAsBoolean(json, "replace_exclude_random", false);
            if(replaceExclude) {
                originsExcludedFromRandom.clear();
            }
            JsonArray excludeRandomArray = json.getAsJsonArray("exclude_random");
            excludeRandomArray.forEach(je -> originsExcludedFromRandom.add(ResourceLocation.tryParse(je.getAsString())));
        }
        if(json.has("default_origin")) {
            this.defaultOrigin = ResourceLocation.parse(GsonHelper.getAsString(json, "default_origin"));
        }
        if(json.has("auto_choose")) {
            this.autoChooseIfNoChoice = GsonHelper.getAsBoolean(json, "auto_choose");
        }
        if(json.has("hidden")) {
            this.hidden = GsonHelper.getAsBoolean(json, "hidden");
        }
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        } else if(!(obj instanceof OriginLayer)) {
            return false;
        } else {
            return identifier.equals(((OriginLayer)obj).identifier);
        }
    }

    @Override
    public int compareTo(OriginLayer o) {
        return Integer.compare(order, o.order);
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(identifier.toString());
        buffer.writeInt(order);
        buffer.writeBoolean(enabled);
        buffer.writeInt(conditionedOrigins.size());
        conditionedOrigins.forEach(co -> co.write(buffer));
        buffer.writeUtf(getOrCreateTranslationKey());
        buffer.writeUtf(getTitleViewOriginTranslationKey());
        buffer.writeUtf(getTitleChooseOriginTranslationKey());
        buffer.writeUtf(getMissingOriginNameTranslationKey());
        buffer.writeUtf(getMissingOriginDescriptionTranslationKey());
        buffer.writeBoolean(isRandomAllowed());
        if(isRandomAllowed()) {
            buffer.writeBoolean(doesRandomAllowUnchoosable);
            buffer.writeInt(originsExcludedFromRandom.size());
            originsExcludedFromRandom.forEach(buffer::writeResourceLocation);
        }
        buffer.writeBoolean(hasDefaultOrigin());
        if(hasDefaultOrigin()) {
            buffer.writeResourceLocation(defaultOrigin);
        }
        buffer.writeBoolean(autoChooseIfNoChoice);
        buffer.writeBoolean(hidden);
        buffer.writeBoolean(overrideViewOriginTitle);
        buffer.writeBoolean(overrideChooseOriginTitle);
    }

    @Environment(EnvType.CLIENT)
    public static OriginLayer read(RegistryFriendlyByteBuf buffer) {
        OriginLayer layer = new OriginLayer();
        layer.identifier = ResourceLocation.tryParse(buffer.readUtf());
        layer.order = buffer.readInt();
        layer.enabled = buffer.readBoolean();
        int conditionedOriginCount = buffer.readInt();
        layer.conditionedOrigins = new ArrayList<>(conditionedOriginCount);
        for(int i = 0; i < conditionedOriginCount; i++) {
            layer.conditionedOrigins.add(ConditionedOrigin.read(buffer));
        }
        layer.nameTranslationKey = buffer.readUtf();
        layer.titleViewOriginTranslationKey = buffer.readUtf();
        layer.titleChooseOriginTranslationKey = buffer.readUtf();
        layer.missingOriginNameTranslationKey = buffer.readUtf();
        layer.missingOriginDescriptionTranslationKey = buffer.readUtf();
        layer.isRandomAllowed = buffer.readBoolean();
        if(layer.isRandomAllowed) {
            layer.doesRandomAllowUnchoosable = buffer.readBoolean();
            int excludedSize = buffer.readInt();
            layer.originsExcludedFromRandom = new LinkedList<>();
            for(int i = 0; i < excludedSize; i++) {
                layer.originsExcludedFromRandom.add(buffer.readResourceLocation());
            }
        }
        if(buffer.readBoolean()) {
            layer.defaultOrigin = buffer.readResourceLocation();
        }
        layer.autoChooseIfNoChoice = buffer.readBoolean();
        layer.hidden = buffer.readBoolean();
        layer.overrideViewOriginTitle = buffer.readBoolean();
        layer.overrideChooseOriginTitle = buffer.readBoolean();
        return layer;
    }

    public static OriginLayer fromJson(ResourceLocation id, JsonObject json) {
        int order = GsonHelper.getAsInt(json, "order", OriginLayers.size());
        if(!json.has("layers") || !json.get("layers").isJsonArray()) {
            throw new JsonParseException("Origin layer JSON requires \"layers\" array of origin IDs to include in the layer.");
        }
        JsonArray originArray = json.getAsJsonArray("layers");
        List<ConditionedOrigin> list = new ArrayList<>(originArray.size());
        originArray.forEach(je -> list.add(ConditionedOrigin.read(je)));
        boolean enabled = GsonHelper.getAsBoolean(json, "enabled", true);
        OriginLayer layer = new OriginLayer();
        layer.order = order;
        layer.conditionedOrigins = list;
        layer.enabled = enabled;
        layer.identifier = id;
        layer.nameTranslationKey = GsonHelper.getAsString(json, "name", "");
        if(json.has("gui_title") && json.get("gui_title").isJsonObject()) {
            JsonObject guiTitleObj = json.getAsJsonObject("gui_title");
            if(guiTitleObj.has("view_origin")) {
                layer.titleViewOriginTranslationKey = GsonHelper.getAsString(guiTitleObj, "view_origin", "");
                layer.overrideViewOriginTitle = true;
            }
            if(guiTitleObj.has("choose_origin")) {
                layer.titleChooseOriginTranslationKey = GsonHelper.getAsString(guiTitleObj, "choose_origin", "");
                layer.overrideChooseOriginTitle = true;
            }
        }
        layer.missingOriginNameTranslationKey = GsonHelper.getAsString(json, "missing_name", "");
        layer.missingOriginDescriptionTranslationKey = GsonHelper.getAsString(json, "missing_description", "");
        layer.isRandomAllowed = GsonHelper.getAsBoolean(json, "allow_random", false);
        layer.doesRandomAllowUnchoosable = GsonHelper.getAsBoolean(json, "allow_random_unchoosable", false);
        layer.originsExcludedFromRandom = new LinkedList<>();
        if(json.has("exclude_random") && json.get("exclude_random").isJsonArray()) {
            JsonArray excludeRandomArray = json.getAsJsonArray("exclude_random");
            excludeRandomArray.forEach(je -> layer.originsExcludedFromRandom.add(ResourceLocation.tryParse(je.getAsString())));
        }
        if(json.has("default_origin")) {
            layer.defaultOrigin = ResourceLocation.parse(GsonHelper.getAsString(json, "default_origin"));
        }
        layer.autoChooseIfNoChoice = GsonHelper.getAsBoolean(json, "auto_choose", false);
        layer.hidden = GsonHelper.getAsBoolean(json, "hidden", false);
        return layer;
    }

    public static class ConditionedOrigin {
        private final ConditionFactory<Entity>.Instance condition;
        private final List<ResourceLocation> origins;

        public ConditionedOrigin(ConditionFactory<Entity>.Instance condition, List<ResourceLocation> origins) {
            this.condition = condition;
            this.origins = origins;
        }

        public boolean isConditionFulfilled(Player playerEntity) {
            return condition == null || condition.test(playerEntity);
        }

        public List<ResourceLocation> getOrigins() {
            return origins;
        }
        private static final SerializableData conditionedOriginObjectData = new SerializableData()
            .add("condition", ApoliDataTypes.ENTITY_CONDITION)
            .add("layers", SerializableDataTypes.IDENTIFIERS);

        public void write(RegistryFriendlyByteBuf buffer) {
            buffer.writeBoolean(condition != null);
            if(condition != null)
                condition.write(buffer);
            buffer.writeInt(origins.size());
            origins.forEach(buffer::writeResourceLocation);
        }

        @Environment(EnvType.CLIENT)
        public static ConditionedOrigin read(RegistryFriendlyByteBuf buffer) {
            ConditionFactory<Entity>.Instance condition = null;
            if(buffer.readBoolean()) {
                condition = ConditionTypes.ENTITY.read(buffer);
            }
            int originCount = buffer.readInt();
            List<ResourceLocation> originList = new ArrayList<>(originCount);
            for(int i = 0; i < originCount; i++) {
                originList.add(buffer.readResourceLocation());
            }
            return new ConditionedOrigin(condition, originList);
        }

        @SuppressWarnings("unchecked")
        public static ConditionedOrigin read(JsonElement element) {
            if(element.isJsonPrimitive()) {
                JsonPrimitive elemPrimitive = element.getAsJsonPrimitive();
                if(elemPrimitive.isString()) {
                    return new ConditionedOrigin(null, Lists.newArrayList(ResourceLocation.tryParse(elemPrimitive.getAsString())));
                }
                throw new JsonParseException("Expected origin in layer to be either a string or an object.");
            } else if(element.isJsonObject()) {
                SerializableData.Instance data = conditionedOriginObjectData.read(element.getAsJsonObject());
                return new ConditionedOrigin((ConditionFactory<Entity>.Instance)data.get("condition"), (List<ResourceLocation>)data.get("layers"));
            }
            throw new JsonParseException("Expected origin in layer to be either a string or an object.");
        }
    }
}
