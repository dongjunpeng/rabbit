package com.buterfleoge.rabbit;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.datetime.DateTimeFormatAnnotationFormatterFactory;
import org.springframework.format.number.NumberFormatAnnotationFormatterFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.buterfleoge.whale.type.formatter.ImagePathAnnotationFormatterFactory;
import com.buterfleoge.whale.type.formatter.ImagePathFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

/**
 *
 * @author xiezhenzong
 *
 */
public class RabbitJacksonAnnotationIntrospector extends JacksonAnnotationIntrospector {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitJacksonAnnotationIntrospector.class);

    /**
     * serial version uid
     */
    private static final long serialVersionUID = 3532125948415135352L;

    private String imgHostUrl;

    /**
     * @param imgHostUrl
     */
    public RabbitJacksonAnnotationIntrospector(String imgHostUrl) {
        this.imgHostUrl = imgHostUrl;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object findSerializer(Annotated a) {
        DateTimeFormat dateTimeFormat = _findAnnotation(a, DateTimeFormat.class);
        if (dateTimeFormat != null) {
            DateTimeFormatAnnotationFormatterFactory factory = new DateTimeFormatAnnotationFormatterFactory();
            Printer<Date> printer = (Printer<Date>) factory.getPrinter(dateTimeFormat, Date.class);
            return new RabbitSerializer<Date>(printer);
        }

        NumberFormat numberFormat = _findAnnotation(a, NumberFormat.class);
        if (numberFormat != null) {
            NumberFormatAnnotationFormatterFactory factory = new NumberFormatAnnotationFormatterFactory();
            Printer<Number> printer = factory.getPrinter(numberFormat, BigDecimal.class);
            return new RabbitSerializer<Number>(printer);
        }

        ImagePathFormat imagePathFormat = _findAnnotation(a, ImagePathFormat.class);
        if (imagePathFormat != null) {
            ImagePathAnnotationFormatterFactory factory = new ImagePathAnnotationFormatterFactory(imgHostUrl);
            Printer<String> printer = factory.getPrinter(imagePathFormat, String.class);
            return imagePathFormat.isComposite() ? new RabbitListSerializer<String>(printer)
                    : new RabbitSerializer<String>(printer);
        }
        return super.findSerializer(a);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object findDeserializer(Annotated a) {
        DateTimeFormat dateTimeFormat = _findAnnotation(a, DateTimeFormat.class);
        if (dateTimeFormat != null) {
            DateTimeFormatAnnotationFormatterFactory factory = new DateTimeFormatAnnotationFormatterFactory();
            Parser<Date> parser = (Parser<Date>) factory.getParser(dateTimeFormat, Date.class);
            return new RabbitDerializer<Date>(parser);
        }

        NumberFormat numberFormat = _findAnnotation(a, NumberFormat.class);
        if (numberFormat != null) {
            NumberFormatAnnotationFormatterFactory factory = new NumberFormatAnnotationFormatterFactory();
            Parser<Number> parser = (Parser<Number>) factory.getParser(numberFormat, Number.class);
            return new RabbitDerializer<Number>(parser);
        }
        return super.findDeserializer(a);
    }

    private class RabbitSerializer<T> extends JsonSerializer<T> {

        private Printer<T> printer;

        private RabbitSerializer(Printer<T> printer) {
            this.printer = printer;
        }

        public void serialize(T value, JsonGenerator gen, SerializerProvider provider)
                throws IOException, JsonProcessingException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            String valueStr = printer.print(value, provider.getLocale());
            gen.writeString(valueStr);
        }
    }

    private class RabbitListSerializer<T> extends JsonSerializer<List<T>> {

        private Printer<T> printer;

        private RabbitListSerializer(Printer<T> printer) {
            this.printer = printer;
        }

        @Override
        public void serialize(List<T> value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException, JsonProcessingException {
            if (CollectionUtils.isEmpty(value)) {
                return;
            }
            Locale locale = serializers.getLocale();
            gen.writeStartArray(value.size());
            for (T t : value) {
                gen.writeString(printer.print(t, locale));
            }
            gen.writeEndArray();
        }
    }

    private class RabbitDerializer<T> extends JsonDeserializer<T> {

        private Parser<T> parser;

        public RabbitDerializer(Parser<T> parser) {
            this.parser = parser;
        }

        @Override
        public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String token = p.getText();
            try {
                return StringUtils.hasText(token) ? parser.parse(token, ctxt.getLocale()) : null;
            } catch (ParseException e) {
                LOG.error("parse failed", e);
                return null;
            }
        }

    }

}
