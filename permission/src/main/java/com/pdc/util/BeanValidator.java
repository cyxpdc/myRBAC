package com.pdc.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pdc.exception.ParamException;
import org.apache.commons.collections.MapUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 基于validator的校验类
 * @author pdc
 */
public class BeanValidator {

    private static final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

    /**
     * 校验单个对象，返回错误信息map
     * @param t
     * @param groups
     * @param <T>
     * @return
     */
    public static <T> Map<String, String> validate(T t, Class... groups) {
        Validator validator = validatorFactory.getValidator();
        Set validateResult = validator.validate(t, groups);
        if (validateResult.isEmpty()) {
            return Collections.emptyMap();
        }
        LinkedHashMap<String,String> errors = Maps.newLinkedHashMap();
        Iterator iterator = validateResult.iterator();
        while (iterator.hasNext()) {
            ConstraintViolation violation = (ConstraintViolation)iterator.next();
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        return errors;

    }

    /**
     * 校验多个对象，返回错误信息map
     * @param collection
     * @return
     */
    public static Map<String, String> validateList(Collection<?> collection) {
        Preconditions.checkNotNull(collection);
        Iterator iterator = collection.iterator();
        Map errors;
        do {
            if (!iterator.hasNext()) {
                return Collections.emptyMap();
            }
            Object object = iterator.next();
            errors = validate(object, new Class[0]);
        } while (errors.isEmpty());

        return errors;
    }

    /**
     * 统一API
     * @param first
     * @param objects
     * @return
     */
    public static Map<String, String> validateObject(Object first, Object... objects) {
        if (objects != null && objects.length > 0) {
            return validateList(Lists.asList(first, objects));
        } else {
            return validate(first, new Class[0]);
        }
    }

    /**
     * 最终的对外API
     * @param param
     * @throws ParamException
     */
    public static void check(Object param) throws ParamException {
        Map<String, String> map = BeanValidator.validateObject(param);
        if (MapUtils.isNotEmpty(map)) {
            throw new ParamException(map.toString());
        }
    }
}
