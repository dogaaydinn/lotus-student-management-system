package com.lotus.lotusSPM.versioning;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

/**
 * Custom annotation for API versioning.
 *
 * Usage:
 * @ApiVersion("1")
 * @GetMapping("/students")
 * public List<Student> getStudentsV1() { ... }
 *
 * @ApiVersion("2")
 * @GetMapping("/students")
 * public List<StudentDTO> getStudentsV2() { ... }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface ApiVersion {
    String value() default "1";
}
