/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.tillvaxtverket.tsltrust.common.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author stefan
 */
public class DateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
    
    private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//    private final DateFormat format = DateFormat.getInstance();

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        String dateFormatAsString = format.format(src);
        return new JsonPrimitive(dateFormatAsString);
    }
    
    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (!(json instanceof JsonPrimitive)) {
            throw new JsonParseException("The date should be a string value");
        }
        
        try {
            return format.parse(json.getAsString());
        } catch (ParseException e) {
            throw new JsonParseException(e);
        }
    }
}
