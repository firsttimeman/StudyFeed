package FeedStudy.StudyFeed.exception;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

// TODO enum validator에 대해서 알아보기

public class EnumValidator implements ConstraintValidator<ValidEnum, Object> {

    private Class<? extends Enum<?>> enumClass;


    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return false; // null 값 허용 안 함
        }

        if (value instanceof String) { // ✅ String 타입인 경우
            return Arrays.stream(enumClass.getEnumConstants())
                    .map(Enum::name)
                    .anyMatch(enumValue -> enumValue.equalsIgnoreCase((String) value));
        }

        if (value instanceof Enum<?>) { // ✅ Enum 타입인 경우
            return Arrays.asList(enumClass.getEnumConstants()).contains(value);
        }

        return false;
    }
}
