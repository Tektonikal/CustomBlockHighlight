package tektonikal.customblockhighlight.legacy;

//? if =1.8.9 {
/*import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.awt.Color;
import java.lang.reflect.Type;

public final class GsonConfigInstance {
	private GsonConfigInstance() {
	}

	public static class ColorTypeAdapter implements JsonSerializer<Color>, JsonDeserializer<Color> {
		@Override
		public Color deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			return new Color(jsonElement.getAsInt(), true);
		}

		@Override
		public JsonElement serialize(Color color, Type type, JsonSerializationContext jsonSerializationContext) {
			return new JsonPrimitive(color.getRGB());
		}
	}
}
*///?}
