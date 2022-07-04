package bodi.generator.ui.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * This class stores the necessary information to merge two or more {@link bodi.generator.dataSchema.SchemaField}
 * into one.
 */
public class MergeFields {

    @Getter
    @Setter
    private String newName;

    @Getter
    @Setter
    private String fieldSeparator;

    @Getter
    @Setter
    private boolean categorical;

    @Getter
    @Setter
    private boolean removeOriginalFields;

    @Getter
    @Setter
    private List<String> fieldsToMerge;

    public MergeFields() {
        newName = null;
        fieldSeparator = " ";
        categorical = false;
        fieldsToMerge = new ArrayList<>();
    }

}
