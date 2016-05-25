package com.buterfleoge.rabbit;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import org.springframework.format.Printer;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.datetime.DateTimeFormatAnnotationFormatterFactory;
import org.springframework.format.number.NumberFormatAnnotationFormatterFactory;

import com.buterfleoge.whale.type.formatter.ImagePathAnnotationFormatterFactory;
import com.buterfleoge.whale.type.formatter.ImagePathFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
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

        NumberFormat numberFormat = this._findAnnotation(a, NumberFormat.class);
        if (numberFormat != null) {
            NumberFormatAnnotationFormatterFactory factory = new NumberFormatAnnotationFormatterFactory();
            Printer<Number> printer = factory.getPrinter(numberFormat, BigDecimal.class);
            return new RabbitSerializer<Number>(printer);
        }

        ImagePathFormat imagePathFormat = this._findAnnotation(a, ImagePathFormat.class);
        if (imagePathFormat != null) {
            ImagePathAnnotationFormatterFactory factory = new ImagePathAnnotationFormatterFactory(imgHostUrl);
            Printer<String> printer = factory.getPrinter(imagePathFormat, String.class);
            return new RabbitSerializer<String>(printer);
        }

        return super.findSerializer(a);
    }

    private class RabbitSerializer<T> extends JsonSerializer<T> {

        private Printer<T> printer;

        private RabbitSerializer(Printer<T> printer) {
            this.printer = printer;
        }

        public void serialize(T value, JsonGenerator gen, SerializerProvider provider)
                throws IOException, JsonProcessingException {
            String valueStr = printer.print(value, provider.getLocale());
            gen.writeString(valueStr);
        }
    }

}
