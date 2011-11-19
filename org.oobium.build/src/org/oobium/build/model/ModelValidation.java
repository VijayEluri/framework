package org.oobium.build.model;

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.util.HashMap;
import java.util.Map;

import org.oobium.persist.Validate;


public class ModelValidation {

	private ModelDefinition model;
	
	private String field;
	private boolean isBlank;
	private String isIn;
	private boolean isNotBlank;
	private String isNotIn;
	private boolean isNotNull;
	private boolean isNull;
	private boolean isUnique;
	private int lengthIs;
	private String matches;
	private String max;
	private int maxLength;
	private String message;
	private String min;
	private int minLength;
	private int on;
	private String tokenizer;
	private String unless;
	private boolean unlessBlank;
	private boolean unlessNull;
	private String when;
	private Class<?> with;
	private String withMethod;

	
	private ModelValidation(ModelValidation original) {
		this.model = original.model;
		this.field = original.field;
		this.isBlank = original.isBlank;
		this.isIn = original.isIn;
		this.isNotBlank = original.isNotBlank;
		this.isNotIn = original.isNotIn;
		this.isNotNull = original.isNotNull;
		this.isNull = original.isNull;
		this.isUnique = original.isUnique;
		this.lengthIs = original.lengthIs;
		this.matches = original.matches;
		this.max = original.max;
		this.maxLength = original.maxLength;
		this.message = original.message;
		this.min = original.min;
		this.minLength = original.minLength;
		this.on = original.on;
		this.tokenizer = original.tokenizer;
		this.unless = original.unless;
		this.unlessBlank = original.unlessBlank;
		this.unlessNull = original.unlessNull;
		this.when = original.when;
		this.with = original.with;
		this.withMethod = original.withMethod;
	}

	ModelValidation(ModelDefinition model, String field, Map<String, ?> entries) {
		this.model = model;
		field(field);
		isBlank(coerce(entries.get("isBlank"), false));
		isIn(ModelUtils.getString(entries.get("isIn")));
		isNotBlank(coerce(entries.get("isNotBlank"), false));
		isNotIn(ModelUtils.getString(entries.get("isNotIn")));
		isNotNull(coerce(entries.get("isNotNull"), false));
		isNull(coerce(entries.get("isNull"), false));
		isUnique(coerce(entries.get("isUnique"), false));
		lengthIs(coerce(entries.get("lengthIs"), -1));
		matches(ModelUtils.getString(entries.get("matches")));
		max(ModelUtils.getString(entries.get("max")));
		maxLength(coerce(entries.get("maxLength"), -1));
		message(ModelUtils.getString(entries.get("message")));
		min(ModelUtils.getString(entries.get("min")));
		minLength(coerce(entries.get("minLength"), -1));
		on(coerce(entries.get("on"), -1));
		tokenizer(ModelUtils.getString(entries.get("tokenizer")));
		unless(ModelUtils.getString(entries.get("unless")));
		unlessBlank(coerce(entries.get("unlessBlank"), false));
		unlessNull(coerce(entries.get("unlessNull"), false));
		when(ModelUtils.getString(entries.get("when")));
		// TODO with(coerce(entries.get("with"), Class.class));
		with(Object.class);
		withMethod(ModelUtils.getString(entries.get("withMethod")));
	}
	
	public ModelValidation getCopy() {
		return new ModelValidation(this);
	}

	public Map<String, Object> getCustomProperties() {
		Map<String, Object> props = new HashMap<String, Object>();
		if(isBlank) {
			props.put("isBlank", true);
		}
		if(isIn != null && isIn.length() > 0) {
			props.put("isIn", isIn);
		}
		if(isNotBlank) {
			props.put("isNotBlank", true);
		}
		if(isNotIn != null && isNotIn.length() > 0) {
			props.put("isNotIn", isNotIn);
		}
		if(isNotNull) {
			props.put("isNotNull", true);
		}
		if(isNull) {
			props.put("isNull", true);
		}
		if(isUnique) {
			props.put("isUnique", true);
		}
		if(lengthIs != -1) {
			props.put("lengthIs", lengthIs);
		}
		if(matches != null && matches.length() > 0) {
			props.put("matches", matches);
		}
		if(max != null && max.length() > 0) {
			props.put("max", max);
		}
		if(maxLength != -1) {
			props.put("maxLength", maxLength);
		}
		if(message != null && message.length() > 0) {
			props.put("message", message);
		}
		if(min != null && min.length() > 0) {
			props.put("min", min);
		}
		if(minLength != -1) {
			props.put("minLength", minLength);
		}
		if(on != -1) {
			props.put("on", on);
		}
		if(tokenizer != null && tokenizer.length() > 0) {
			props.put("tokenizer", tokenizer);
		}
		if(unless != null && unless.length() > 0) {
			props.put("unless", unless);
		}
		if(unlessBlank) {
			props.put("unlessBlank", true);
		}
		if(unlessNull) {
			props.put("unlessNull", true);
		}
		if(when != null && when.length() > 0) {
			props.put("when", when);
		}
		if(with != Object.class) {
			props.put("with", with);
		}
		if(withMethod != null && withMethod.length() > 0) {
			props.put("withMethod", withMethod);
		}
		return props;
	}
	
	public Map<String, Object> getProperties() {
		Map<String, Object> props = getCustomProperties();
		props.put("field", field);
		return props;
	}
	
	public String field() {
		return field;
	}
	
	public boolean isBlank() {
		return isBlank;
	}

	public String isIn() {
		return isIn;
	}

	public boolean isNotBlank() {
		return isNotBlank;
	}

	public String isNotIn() {
		return isNotIn;
	}

	public boolean isNotNull() {
		return isNotNull;
	}

	public boolean isNull() {
		return isNull;
	}

	public boolean isUnique() {
		return isUnique;
	}

	public int lengthIs() {
		return lengthIs;
	}

	public String matches() {
		return matches;
	}

	public String max() {
		return max;
	}

	public int maxLength() {
		return maxLength;
	}

	public String message() {
		return message;
	}

	public String min() {
		return min;
	}

	public int minLength() {
		return minLength;
	}

	public int on() {
		return on;
	}

	public String tokenizer() {
		return tokenizer;
	}

	public String unless() {
		return unless;
	}

	public boolean unlessBlank() {
		return unlessBlank;
	}

	public boolean unlessNull() {
		return unlessNull;
	}

	public String when() {
		return when;
	}

	public Class<?> with() {
		return with;
	}
	
	public String withMethod() {
		return withMethod;
	}


	public ModelValidation field(String field) {
		this.field = field;
		return this;
	}
	
	public ModelValidation isBlank(boolean isBlank) {
		this.isBlank = isBlank;
		return this;
	}

	public ModelValidation isIn(String isIn) {
		this.isIn = isIn;
		return this;
	}

	public ModelValidation isNotBlank(boolean isNotBlank) {
		this.isNotBlank = isNotBlank;
		return this;
	}

	public ModelValidation isNotIn(String isNotIn) {
		this.isNotIn = isNotIn;
		return this;
	}

	public ModelValidation isNotNull(boolean isNotNull) {
		this.isNotNull = isNotNull;
		return this;
	}

	public ModelValidation isNull(boolean isNull) {
		this.isNull = isNull;
		return this;
	}

	public ModelValidation isUnique(boolean isUnique) {
		this.isUnique = isUnique;
		return this;
	}

	public ModelValidation lengthIs(int lengthIs) {
		this.lengthIs = lengthIs;
		return this;
	}

	public ModelValidation matches(String matches) {
		this.matches = matches;
		return this;
	}

	public ModelValidation max(String max) {
		this.max = max;
		return this;
	}

	public ModelValidation maxLength(int maxLength) {
		this.maxLength = maxLength;
		return this;
	}

	public ModelValidation message(String message) {
		this.message = message;
		return this;
	}

	public ModelValidation min(String min) {
		this.min = min;
		return this;
	}

	public ModelValidation minLength(int minLength) {
		this.minLength = minLength;
		return this;
	}

	public ModelValidation on(int on) {
		this.on = on;
		return this;
	}

	public ModelValidation tokenizer(String tokenizer) {
		this.tokenizer = tokenizer;
		return this;
	}

	public ModelValidation unless(String unless) {
		this.unless = unless;
		return this;
	}

	public ModelValidation unlessBlank(boolean unlessBlank) {
		this.unlessBlank = unlessBlank;
		return this;
	}

	public ModelValidation unlessNull(boolean unlessNull) {
		this.unlessNull = unlessNull;
		return this;
	}

	public ModelValidation when(String when) {
		this.when = when;
		return this;
	}

	public ModelValidation with(Class<?> with) {
		this.with = with;
		return this;
	}
	
	public ModelValidation withMethod(String withMethod) {
		this.withMethod = withMethod;
		return this;
	}

	ModelValidation putAll(Map<String, ?> entries) {
		if(entries.containsKey("isBlank"))     isBlank(coerce(entries.get("isBlank"), false));
		if(entries.containsKey("isIn"))        isIn(ModelUtils.getString(entries.get("isIn")));
		if(entries.containsKey("isNotBlank"))  isNotBlank(coerce(entries.get("isNotBlank"), false));
		if(entries.containsKey("isNotIn"))     isNotIn(ModelUtils.getString(entries.get("isNotIn")));
		if(entries.containsKey("isNotNull"))   isNotNull(coerce(entries.get("isNotNull"), false));
		if(entries.containsKey("isNull"))      isNull(coerce(entries.get("isNull"), false));
		if(entries.containsKey("isUnique"))    isUnique(coerce(entries.get("isUnique"), false));
		if(entries.containsKey("lengthIs"))    lengthIs(coerce(entries.get("lengthIs"), -1));
		if(entries.containsKey("matches"))     matches(ModelUtils.getString(entries.get("matches")));
		if(entries.containsKey("max"))         max(ModelUtils.getString(entries.get("max")));
		if(entries.containsKey("maxLength"))   maxLength(coerce(entries.get("maxLength"), -1));
		if(entries.containsKey("message"))     message(ModelUtils.getString(entries.get("message")));
		if(entries.containsKey("min"))         min(ModelUtils.getString(entries.get("min")));
		if(entries.containsKey("minLength"))   minLength(coerce(entries.get("minLength"), -1));
		if(entries.containsKey("on"))          on(coerce(entries.get("on"), -1));
		if(entries.containsKey("tokenizer"))   tokenizer(ModelUtils.getString(entries.get("tokenizer")));
		if(entries.containsKey("unless"))      unless(ModelUtils.getString(entries.get("unless")));
		if(entries.containsKey("unlessBlank")) unlessBlank(coerce(entries.get("unlessBlank"), false));
		if(entries.containsKey("unlessNull"))  unlessNull(coerce(entries.get("unlessNull"), false));
		if(entries.containsKey("when"))        when(ModelUtils.getString(entries.get("when")));
		// TODO with(coerce(entries.get("with"), Class.class));
		if(entries.containsKey("with"))        with(Object.class);
		if(entries.containsKey("withMethod"))  withMethod(ModelUtils.getString(entries.get("withMethod")));
		return this;
	}
	
	@Override
	public String toString() {
		return toString(field);
	}
	
	public String toString(String field) {
		StringBuilder sb = new StringBuilder();
		sb.append('@').append(Validate.class.getSimpleName()).append('(');
		sb.append("field=\"").append(field).append('"');
		if(isBlank) {
			sb.append(", isBlank=true");
		}
		if(isIn != null && isIn.length() > 0) {
			sb.append(", isIn=").append(isIn);
		}
		if(isNotBlank) {
			sb.append(", isNotBlank=true");
		}
		if(isNotIn != null && isNotIn.length() > 0) {
			sb.append(", isNotIn\"").append(isNotIn).append('"');
		}
		if(isNotNull) {
			sb.append(", isNotNull=true");
		}
		if(isNull) {
			sb.append(", isNull=true");
		}
		if(isUnique) {
			sb.append(", isUnique=true");
		}
		if(lengthIs != -1) {
			sb.append(", lengthIs=").append(lengthIs);
		}
		if(matches != null && matches.length() > 0) {
			sb.append(", matches\"").append(matches).append('"');
		}
		if(max != null && max.length() > 0) {
			sb.append(", max\"").append(max).append('"');
		}
		if(maxLength != -1) {
			sb.append(", maxLength=").append(maxLength);
		}
		if(message != null && message.length() > 0) {
			sb.append(", message\"").append(message).append('"');
		}
		if(min != null && min.length() > 0) {
			sb.append(", min\"").append(min).append('"');
		}
		if(minLength != -1) {
			sb.append(", minLength=").append(minLength);
		}
		if(on != -1) {
			sb.append(", on=").append(on);
		}
		if(tokenizer != null && tokenizer.length() > 0) {
			sb.append(", tokenizer\"").append(tokenizer).append('"');
		}
		if(unless != null && unless.length() > 0) {
			sb.append(", unless\"").append(unless).append('"');
		}
		if(unlessBlank) {
			sb.append(", unlessBlank=true");
		}
		if(unlessNull) {
			sb.append(", unlessNull=true");
		}
		if(when != null && when.length() > 0) {
			sb.append(", when\"").append(when).append('"');
		}
		if(with != Object.class) {
			sb.append(", with=").append(with.getSimpleName()).append(".class");
		}
		if(withMethod != null && withMethod.length() > 0) {
			sb.append(", withMethod\"").append(withMethod).append('"');
		}
		
		sb.append(')');
		return sb.toString();
	}
	
}
