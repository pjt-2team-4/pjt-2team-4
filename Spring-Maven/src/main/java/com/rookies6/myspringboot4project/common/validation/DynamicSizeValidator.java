package com.rookies6.myspringboot4project.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class DynamicSizeValidator
        implements ConstraintValidator<DynamicSize, String> {

    private final Environment environment;

    private String maxProperty;

    public DynamicSizeValidator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void initialize(DynamicSize annotation) {
        this.maxProperty = annotation.maxProperty();
    }

    @Override
    public boolean isValid(
            String value,
            ConstraintValidatorContext context
    ) {
        // @NotBlank 등이 별도로 처리하므로 null은 여기서는 통과
        if (value == null) {
            return true;
        }

        Integer maxLength = environment.getProperty(
                maxProperty,
                Integer.class
        );

        if (maxLength == null) {
            return true;
        }

        return value.length() <= maxLength;
    }
}
