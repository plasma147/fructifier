package uk.co.optimisticpanda.config.serializing.typeadaptors;

import java.io.IOException;
import java.util.Set;

import uk.co.optimisticpanda.conf.ConfigurationException;
import uk.co.optimisticpanda.conf.Phase;
import uk.co.optimisticpanda.conf.TypeAdaptorRegistration;
import uk.co.optimisticpanda.config.serializing.ReflectionUtil;
import uk.co.optimisticpanda.config.serializing.ReflectionUtil.GetterCallBack;
import uk.co.optimisticpanda.runner.RegisteredExtensions;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class PhaseTypeAdaptor extends TypeAdaptorRegistration<Phase> {

	private final Gson gson;
	private final TypeAdapterFactory parent;
	private final RegisteredExtensions registeredExtensions;

	public PhaseTypeAdaptor(Gson gson, TypeAdapterFactory parent, RegisteredExtensions registeredExtensions) {
		this.gson = gson;
		this.parent = parent;
		this.registeredExtensions = registeredExtensions;
	}

	/**
	 * Write the phase out, using it's accessor's rather than fields to provide
	 * the attributes and their values
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void write(final JsonWriter out, Phase phase) throws IOException {
		phase.setPhaseType(registeredExtensions.getByPhaseType(phase.getClass()));
		out.beginObject();
		ReflectionUtil.invokeGetters(phase.getClass(), phase, new GetterCallBack() {
			public void visit(String name, Object value) throws Exception {
				if (value != null) {
					TypeToken<?> token = TypeToken.get(value.getClass());
					@SuppressWarnings("rawtypes")
					TypeAdapter adapter = gson.getAdapter(token);
					out.name(name);
					adapter.write(out, value);
				}
			}
		});
		out.endObject();
	}

	@Override
	public Phase read(JsonReader in) throws IOException {
		JsonObject object = (JsonObject) gson.fromJson(in, JsonElement.class);
		if (!object.has("name") || !object.get("name").isJsonPrimitive()) {
			throw new ConfigurationException("A phase does not have a name or is not a primitive type");
		}
		String phaseName = object.get("name").getAsString();

		if (!object.has("phaseType")) {
			throw new ConfigurationException(phaseName + " does not have a specified 'phaseType'");
		}
		String phaseType = object.get("phaseType").getAsString();
		Phase phase = create(registeredExtensions.getPhaseNames(), phaseName, phaseType, registeredExtensions.getPhaseTypeForName(phaseType), object);

		return phase;
	}

	// Create a phase of the type referred to by the "phaseType" attribute.
	private Phase create(Set<String> availablePhases, String phaseName, String phaseType, Class<? extends Phase> clazz, JsonElement element) {
		if (clazz == null) {
			throw new ConfigurationException("Do not know what type of phase: " + phaseType + " is, for phase:" + phaseName + ". Possible phase types are: " + availablePhases);
		}
		return gson.getDelegateAdapter(parent, TypeToken.get(clazz)).fromJsonTree(element);
	}

	@Override
	public <T> boolean supplies(TypeToken<T> type) {
		Class<? super T> t = type.getRawType();
		return Phase.class.isAssignableFrom(t);
	}

}