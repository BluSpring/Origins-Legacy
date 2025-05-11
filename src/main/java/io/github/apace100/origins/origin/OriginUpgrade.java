package io.github.apace100.origins.origin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class OriginUpgrade {

    private final ResourceLocation advancementCondition;
    private final ResourceLocation upgradeToOrigin;
    private final String announcement;

    public OriginUpgrade(ResourceLocation advancementCondition, ResourceLocation upgradeToOrigin, String announcement) {
        this.advancementCondition = advancementCondition;
        this.upgradeToOrigin = upgradeToOrigin;
        this.announcement = announcement;
    }

    public ResourceLocation getAdvancementCondition() {
        return advancementCondition;
    }

    public ResourceLocation getUpgradeToOrigin() {
        return upgradeToOrigin;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(advancementCondition);
        buffer.writeResourceLocation(upgradeToOrigin);
        buffer.writeUtf(announcement);
    }

    public static OriginUpgrade read(FriendlyByteBuf buffer) {
        ResourceLocation condition = buffer.readResourceLocation();
        ResourceLocation origin = buffer.readResourceLocation();
        String announcement = buffer.readUtf(32767);
        return new OriginUpgrade(condition, origin, announcement);
    }

    public static OriginUpgrade fromJson(JsonElement jsonElement) {
        if(!jsonElement.isJsonObject()) {
            throw new JsonParseException("Origin upgrade needs to be a JSON object.");
        }
        JsonObject json = jsonElement.getAsJsonObject();
        JsonElement condition;
        JsonElement origin;
        if(json.has("condition") && (condition = json.get("condition")).isJsonPrimitive()
            && json.has("origin") && (origin = json.get("origin")).isJsonPrimitive()) {
            ResourceLocation conditionId = ResourceLocation.tryParse(condition.getAsString());
            ResourceLocation originId = ResourceLocation.tryParse(origin.getAsString());
            String announcement = "";
            if(json.has("announcement")) {
                JsonElement anno = json.get("announcement");
                if(anno.isJsonPrimitive()) {
                    announcement = anno.getAsString();
                }
            }
            return new OriginUpgrade(conditionId, originId, announcement);
        } else {
            throw new JsonParseException("Origin upgrade JSON requires \"condition\" string and \"origin\" string.");
        }
    }
}
