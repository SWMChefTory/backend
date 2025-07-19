package com.cheftory.api.utils;

import com.cheftory.api.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.response.ValidatableMockMvcResponse;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsTest {

    protected MockMvcRequestSpecification mockMvc;
    private RestDocumentationContextProvider restDocumentation;
    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.restDocumentation = restDocumentation;
    }

    protected MockMvcRequestSpecification given() {
        return mockMvc;
    }

    protected MockMvcBuilder mockMvcBuilder(Object controller) {
        return new MockMvcBuilder(controller, restDocumentation);
    }

    protected MockMvcRequestSpecification setupAuthenticatedJsonRequest() {
        return mockMvc
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .attribute("userId", "testUserId")
                .header("Authorization", "Bearer accessToken");
    }

    protected void assertSuccessResponse(ValidatableMockMvcResponse response) {
        response
            .statusCode(200)
            .body("message", CoreMatchers.equalTo("success"));
    }

    protected void assertErrorResponse(ValidatableMockMvcResponse response, ErrorCode errorCode) {
        response
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("error", CoreMatchers.equalTo(errorCode.name()))
            .body("message", CoreMatchers.equalTo(errorCode.getMessage()));
    }

    protected String jsonBody(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.findAndRegisterModules();
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS);
        return om;
    }

    public class MockMvcBuilder {
        private final Object controller;
        private final RestDocumentationContextProvider restDocumentation;
        private Object advice;
        private Converter<String, ?> customConverter;
        private HandlerMethodArgumentResolver argumentResolver;
        private Validator validator;

        public MockMvcBuilder(Object controller, RestDocumentationContextProvider restDocumentation) {
            this.controller = controller;
            this.restDocumentation = restDocumentation;
        }

        public MockMvcBuilder withAdvice(Object advice) {
            this.advice = advice;
            return this;
        }

        public MockMvcBuilder withCustomConverter(Converter<String, ?> customConverter) {
            this.customConverter = customConverter;
            return this;
        }

        public MockMvcBuilder withArgumentResolver(HandlerMethodArgumentResolver argumentResolver) {
            this.argumentResolver = argumentResolver;
            return this;
        }

        public <T> MockMvcBuilder withValidator(Class<T> serviceClass, T serviceInstance) {
            GenericApplicationContext ctx = new GenericApplicationContext();
            ctx.registerBean(serviceClass, () -> serviceInstance);
            ctx.refresh();

            LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
            validatorFactoryBean.setConstraintValidatorFactory(
                new SpringConstraintValidatorFactory(ctx.getAutowireCapableBeanFactory())
            );
            validatorFactoryBean.afterPropertiesSet();

            this.validator = validatorFactoryBean;
            return this;
        }
        public MockMvcRequestSpecification build() {
            MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(OBJECT_MAPPER);

            StandaloneMockMvcBuilder builder = MockMvcBuilders.standaloneSetup(controller)
                    .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                    .setMessageConverters(converter);

            if (advice != null) {
                builder.setControllerAdvice(advice);
            }

            if (validator != null) {
                builder.setValidator(validator);
            }

            if (customConverter != null) {
                DefaultFormattingConversionService service = new DefaultFormattingConversionService();
                service.addConverter(customConverter);
                builder.setConversionService(service);
            }

            if (argumentResolver != null) {
                builder.setCustomArgumentResolvers(argumentResolver);
            }

            MockMvc mockMvc = builder.build();
            RestDocsTest.this.mockMvc = RestAssuredMockMvc.given().mockMvc(mockMvc);
            return RestDocsTest.this.mockMvc;
        }
    }
}