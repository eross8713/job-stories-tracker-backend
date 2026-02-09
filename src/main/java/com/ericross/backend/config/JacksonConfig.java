package com.ericross.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import jakarta.annotation.PostConstruct;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Configuration
public class JacksonConfig {

    private static final Logger log = LoggerFactory.getLogger(JacksonConfig.class);

    @Autowired
    private ApplicationContext applicationContext;

    // The Spring-provided Jackson2ObjectMapperBuilder type is currently deprecated in this
    // Spring Boot version but still required in some auto-configuration paths. Rather than
    // refactor the entire bootstrap logic now, suppress the deprecation warning for the
    // fallback bean so the compiler/IDE does not spam warnings while we keep compatibility.
    @Bean
    @ConditionalOnMissingBean
    @SuppressWarnings("deprecation")
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        log.info("Registering fallback Jackson2ObjectMapperBuilder bean");
        return new Jackson2ObjectMapperBuilder();
    }

    @PostConstruct
    public void configureMapperSafely() {
        Object objectMapper = null;

        // 1) Try to find an existing ObjectMapper bean of the concrete Jackson type(s)
        String[] mapperClassNames = new String[] {"tools.jackson.databind.ObjectMapper", "com.fasterxml.jackson.databind.ObjectMapper"};
        for (String mapperClassName : mapperClassNames) {
            try {
                Class<?> mapperClass = Class.forName(mapperClassName);
                try {
                    objectMapper = applicationContext.getBean(mapperClass);
                    if (objectMapper != null) {
                        log.info("Using ObjectMapper bean of type {}", mapperClassName);
                        break;
                    }
                } catch (Exception e) {
                    // bean not found, continue to next
                }
            } catch (ClassNotFoundException e) {
                // class not on classpath
            }
        }

        // 2) Fallback: try to instantiate a mapper directly (no builder required)
        if (objectMapper == null) {
            for (String mapperClassName : mapperClassNames) {
                try {
                    Class<?> mapperClass = Class.forName(mapperClassName);
                    try {
                        objectMapper = mapperClass.getDeclaredConstructor().newInstance();
                        log.info("Instantiated ObjectMapper of type {} via no-arg constructor", mapperClassName);
                        break;
                    } catch (NoSuchMethodException nsme) {
                        // no default constructor; continue
                    }
                } catch (ClassNotFoundException e) {
                    // class not on classpath; try next
                } catch (Exception ex) {
                    // ignore instantiation errors and try next
                }
            }
        }

        if (objectMapper == null) {
            log.warn("No ObjectMapper bean found and unable to instantiate one; skipping Jackson configuration");
            return;
        }

        // 3) try to call findAndRegisterModules() if present
        try {
            java.lang.reflect.Method findAndRegister = objectMapper.getClass().getMethod("findAndRegisterModules");
            findAndRegister.invoke(objectMapper);
            log.info("Invoked findAndRegisterModules() on ObjectMapper");
        } catch (NoSuchMethodException e) {
            // not available; ignore
            log.debug("findAndRegisterModules() not present on ObjectMapper");
        } catch (Exception e) {
            // ignore
            log.debug("findAndRegisterModules() invocation failed: {}", e.toString());
        }

        // 4) Attempt to disable candidate SerializationFeature enums if present
        String[] serializationFeatureClasses = new String[] {
            "tools.jackson.databind.SerializationFeature",
            "com.fasterxml.jackson.databind.SerializationFeature"
        };
        String[] candidateFeatureNames = new String[] {
            "WRITE_DATES_AS_TIMESTAMPS",
            "WRITE_DATES_WITH_ZONE_ID"
        };

        boolean disabled = false;
        for (String featClassName : serializationFeatureClasses) {
            try {
                Class<?> featClass = Class.forName(featClassName);
                for (String fname : candidateFeatureNames) {
                    try {
                        @SuppressWarnings({"rawtypes", "unchecked"})
                        Enum<?> feat = Enum.valueOf((Class) featClass, fname);
                        try {
                            java.lang.reflect.Method configure = objectMapper.getClass().getMethod("configure", featClass, boolean.class);
                            configure.invoke(objectMapper, feat, Boolean.FALSE);
                            log.info("Disabled serialization feature {} via configure()", fname);
                            disabled = true;
                            break;
                        } catch (NoSuchMethodException nsme) {
                            try {
                                java.lang.reflect.Method disable = objectMapper.getClass().getMethod("disable", featClass);
                                disable.invoke(objectMapper, feat);
                                log.info("Disabled serialization feature {} via disable()", fname);
                                disabled = true;
                                break;
                            } catch (NoSuchMethodException nsme2) {
                                // ignore
                            }
                        }
                    } catch (IllegalArgumentException iae) {
                        // enum constant not present; continue
                    }
                }
                if (disabled) break;
            } catch (ClassNotFoundException cnfe) {
                // class not found; try next
            } catch (Exception ex) {
                // ignore
            }
        }

        // 5) fallback: set ISO date format for java.util.Date
        if (!disabled) {
            try {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                java.lang.reflect.Method setDateFormat = objectMapper.getClass().getMethod("setDateFormat", DateFormat.class);
                setDateFormat.invoke(objectMapper, df);
                log.info("Set fallback DateFormat on ObjectMapper");
            } catch (NoSuchMethodException e) {
                // ignore
                log.debug("setDateFormat() not available on ObjectMapper");
            } catch (Exception e) {
                // ignore
                log.debug("Failed to set DateFormat on ObjectMapper: {}", e.toString());
            }
        }
    }
}
