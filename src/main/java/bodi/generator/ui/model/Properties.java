package bodi.generator.ui.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * This class stores configuration properties of the Bodi Generator and the generated bot. It is filled from the Bodi
 * Generator user interface.
 */
public class Properties implements Serializable {

    /**
     * The Bodi Generator properties.
     */
    @Getter
    @Setter
    private Map<String, Object> bodiGeneratorProperties;

    /**
     * The properties of the generated bot.
     */
    @Getter
    @Setter
    private Map<String, Object> botProperties;

    /**
     * The properties of the generated bot.
     * <p>
     * These properties are language-dependant. So, for each language (the keys), there is a {@link Map} containing
     * its properties.
     */
    @Getter
    @Setter
    private Map<String, Map<String, Object>> botPropertiesLang;
}
